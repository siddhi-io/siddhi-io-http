/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.siddhi.extension.io.http.source;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.sink.ClientConnector;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.si.metrics.core.internal.MetricsDataHolder;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.http.sink.util.HttpSinkUtil.createConnectorFactory;
import static io.siddhi.extension.io.http.sink.util.HttpSinkUtil.createPoolConfigurations;
import static io.siddhi.extension.io.http.util.HttpConstants.DEFAULT_WORKER_COUNT;
import static io.siddhi.extension.io.http.util.HttpConstants.EMPTY_STRING;
import static org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants.REQUEST_URL;

/**
 * Http source to receive sse events.
 */
@Extension(name = "sse", namespace = "source",
        description = "HTTP SSE source send a request to a given url and listen to the response stream.",
        parameters = {
                @Parameter(
                        name = "event.source.url",
                        description = "The sse endpoint url which should be listened.",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "method",
                        description = "The HTTP method used for calling the endpoint.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "GET"),
                @Parameter(
                        name = "basic.auth.username",
                        description = "The username to be included in the authentication header when calling " +
                                "endpoints protected by basic authentication. `basic.auth.password` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "basic.auth.password",
                        description = "The password to be included in the authentication header when calling " +
                                "endpoints protected by basic authentication. `basic.auth.username` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "worker.count",
                        description = "The number of active worker threads to serve the " +
                                "incoming events. By default the value is set to `1` to ensure events are processed " +
                                "in the same order they arrived. By increasing this value, " +
                                "higher performance can be achieved in the expense of loosing event ordering.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1"
                ),
                @Parameter(
                        name = "headers",
                        description = "HTTP request headers in format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "When the `Content-Type` header is not provided the system decides the " +
                                "Content-Type based on the provided sink mapper as following: \n" +
                                " - `@map(type='xml')`: `application/xml`\n" +
                                " - `@map(type='json')`: `application/json`\n" +
                                " - `@map(type='text')`: `plain/text`\n" +
                                " - `@map(type='keyvalue')`: `application/x-www-form-urlencoded`\n" +
                                " - For all other cases system defaults to `plain/text`\n" +
                                "Also the `Content-Length` header need not to be provided, as the system " +
                                "automatically defines it by calculating the size of the payload.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Content-Type and Content-Length headers"),
                @Parameter(
                        name = "https.truststore.file",
                        description = "The file path of the client truststore when sending messages through `https`" +
                                " protocol.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "`${carbon.home}/resources/security/client-truststore.jks`"
                ),
                @Parameter(
                        name = "https.truststore.password",
                        description = "The password for the client-truststore.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "wso2carbon"
                ),
                @Parameter(
                        name = "client.bootstrap.configurations",
                        description = "Client bootstrap configurations in format " +
                                "`\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Client connect timeout in millis: `'client.bootstrap.connect.timeout:15000'`\n" +
                                " - Client socket timeout in seconds: `'client.bootstrap.socket.timeout:15'`\n" +
                                " - Client socket reuse: `'client.bootstrap.socket.reuse:true'`\n" +
                                " - Enable TCP no delay: `'client.bootstrap.nodelay:true'`\n" +
                                " - Enable client keep alive: `'client.bootstrap.keepalive:true'`\n" +
                                " - Send buffer size: `'client.bootstrap.sendbuffersize:1048576'`\n" +
                                " - Receive buffer size: `'client.bootstrap.recievebuffersize:1048576'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
        },
        systemParameter = {
                @SystemParameter(
                        name = "defaultScheme",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = {"http", "https"}
                ),
                @SystemParameter(
                        name = "defaultHttpPort",
                        description = "The default HTTP port when default scheme is `http`.",
                        defaultValue = "8280",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "defaultHost",
                        description = "The default host of the transport.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "Any valid host"
                ),
        },
        examples = {
                @Example(
                        syntax = "" +
                                "@Source(type='sse', event.source.url='http://localhost:8080/sse', " +
                                "@map(type='json')) " +
                                "define stream IncomingStream (param1 string);",
                        description = "This subscribes to the events which gets published by the " +
                                "SSE server at event.source.url"
                )
        }
)
public class SSESource extends Source {
    private static final Logger log = Logger.getLogger(SSESource.class);
    private int workerThread;
    private String siddhiAppName;
    private String streamID;
    private String eventSourceUrl;
    private String mapType;
    private String clientStoreFile;
    private String clientStorePass;
    private String httpMethod;
    private String clientBootstrapConfiguration;
    private String userName;
    private String userPassword;
    private String authType;
    private String authorizationHeader;
    private String[] requestedTransportPropertyNames;
    private Option httpHeaderOption;
    private ClientConnector clientConnector;
    private DefaultHttpWsConnectorFactory httpConnectorFactory;
    private ConfigReader configReader;
    private PoolConfiguration connectionPoolConfiguration;
    private SSESourceConnectorRegistry httpConnectorRegistry;
    private SSEResponseConnectorListener httpSSEResponseConnectorListener;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    private SourceEventListener sourceEventListener;
    private SourceMetrics metrics;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return serviceDeploymentInfo;
    }

    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {
        this.siddhiAppName = siddhiAppContext.getName();
        this.streamID = sourceEventListener.getStreamDefinition().getId();
        this.sourceEventListener = sourceEventListener;
        this.configReader = configReader;
        this.mapType = HttpConstants.MAP_JSON;
        this.workerThread = Integer.parseInt(optionHolder
                .validateAndGetStaticValue(HttpConstants.WORKER_COUNT, DEFAULT_WORKER_COUNT));
        this.httpMethod = optionHolder.validateAndGetStaticValue(HttpConstants.METHOD, HttpConstants.HTTP_METHOD_GET);
        this.clientStoreFile = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PATH_PARAM,
                HttpSinkUtil.trustStorePath(configReader));
        this.clientStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_PARAM,
                HttpSinkUtil.trustStorePassword(configReader));
        this.clientBootstrapConfiguration = optionHolder
                .validateAndGetStaticValue(HttpConstants.CLIENT_BOOTSTRAP_CONFIGURATION, EMPTY_STRING);
        this.userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME, EMPTY_STRING);
        this.userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD, EMPTY_STRING);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.authType = validateAndGetAuthType();
        String scheme = configReader.readConfig(HttpConstants.DEFAULT_SOURCE_SCHEME, HttpConstants
                .DEFAULT_SOURCE_SCHEME_VALUE);
        int port;
        boolean isSecured;
        if (HttpConstants.SCHEME_HTTPS.equals(scheme)) {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTPS_PORT, HttpConstants.HTTPS_PORT_VALUE));
            isSecured = true;
        } else {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTP_PORT, HttpConstants.HTTP_PORT_VALUE));
            isSecured = false;
        }

        this.serviceDeploymentInfo = new ServiceDeploymentInfo(port, isSecured);
        this.eventSourceUrl = optionHolder.validateAndGetOption(HttpConstants.EVENT_SOURCE_URL).getValue();
        this.httpConnectorRegistry = SSESourceConnectorRegistry.getInstance();
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.httpConnectorFactory = createConnectorFactory(configReader);
        this.connectionPoolConfiguration = createPoolConfigurations(optionHolder);
        this.clientConnector = createClientConnector();
        initMetrics();
        return null;
    }


    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        this.httpSSEResponseConnectorListener =
                new SSEResponseConnectorListener(workerThread, sourceEventListener, streamID,
                        requestedTransportPropertyNames, siddhiAppName);
        this.httpConnectorRegistry.registerSourceListener(httpSSEResponseConnectorListener, streamID);
        HTTPSourceRegistry.registerSSESource(streamID, this);

        // Send initial request
        String headers = httpHeaderOption.getValue();
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        HttpMethod httpReqMethod = new HttpMethod(httpMethod);
        HttpCarbonMessage cMessage = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpReqMethod, EMPTY_STRING));
        cMessage = generateCarbonMessage(headersList, contentType, httpMethod, cMessage,
                clientConnector.getHttpURLProperties());
        cMessage.completeMessage();
        HttpResponseFuture httpResponseFuture = clientConnector.send(cMessage);
        CountDownLatch latch = null;
        if (HttpConstants.OAUTH.equals(authType)) {
            latch = new CountDownLatch(1);
        }

        SSEResponseListener httpListener = new SSEResponseListener(this, streamID, latch, metrics);
        httpResponseFuture.setHttpConnectorListener(httpListener);
        if (latch != null) {
            try {
                boolean latchCount = latch.await(30, TimeUnit.SECONDS);
                if (!latchCount) {
                    log.debug("Timeout due to getting response from " + clientConnector.getPublisherURL() +
                            ". Message dropped.");
                    throw new ConnectionUnavailableException("Time out due to getting response from " +
                            clientConnector.getPublisherURL() + ". Message dropped.");

                }
            } catch (InterruptedException e) {
                log.debug("Failed to get a response from " + clientConnector.getPublisherURL() + "," + e +
                        ". Message dropped.");
                throw new ConnectionUnavailableException("Failed to get a response from " +
                        clientConnector.getPublisherURL() + ", " + e + ". Message dropped.");
            }
        }
    }

    @Override
    public void disconnect() {
        if (clientConnector != null) {
            String publisherURL = clientConnector.getPublisherURL();
            clientConnector = null;
            log.debug("Server connector for url " + publisherURL + " disconnected.");
        }

        if (httpConnectorFactory != null) {
            httpConnectorFactory.shutdownNow();
            httpConnectorFactory = null;
        }

        httpConnectorRegistry.unregisterSourceListener(streamID, siddhiAppName);
        HTTPSourceRegistry.removeSSESource(streamID);
    }

    @Override
    public void destroy() {
        if (clientConnector != null) {
            String publisherURL = clientConnector.getPublisherURL();
            clientConnector = null;
            log.debug("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private String validateAndGetAuthType() {
        if ((EMPTY_STRING.equals(userName) ^
                EMPTY_STRING.equals(userPassword))) {
            throw new SiddhiAppCreationException("Please provide user name and password in " +
                    HttpConstants.HTTP_SINK_ID + " with the stream " + streamID + " in Siddhi app " +
                    siddhiAppName);
        } else if (!(EMPTY_STRING.equals(userName))) {
            byte[] val = (userName + HttpConstants.AUTH_USERNAME_PASSWORD_SEPARATOR + userPassword).getBytes(Charset
                    .defaultCharset());
            this.authorizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                    (Unpooled.copiedBuffer(val));
        }

        if (!HttpConstants.EMPTY_STRING.equals(userName) && !HttpConstants.EMPTY_STRING.equals(userPassword)) {
            return HttpConstants.BASIC_AUTH;
        } else {
            return HttpConstants.NO_AUTH;
        }
    }

    public SSEResponseConnectorListener getConnectorListener() {
        return httpSSEResponseConnectorListener;
    }

    public ClientConnector createClientConnector() {
        Map<String, String> httpURLProperties = HttpSinkUtil.getURLProperties(eventSourceUrl);
        SenderConfiguration senderConfig = HttpSinkUtil
                .getSenderConfigurations(httpURLProperties, clientStoreFile, clientStorePass, configReader);
        senderConfig.setKeepAliveConfig(KeepAliveConfig.ALWAYS);
        if (EMPTY_STRING.equals(eventSourceUrl)) {
            throw new SiddhiAppCreationException("Event Source URL found empty but it is Mandatory field " +
                    "in http source " + streamID);
        }

        senderConfig.setPoolConfiguration(connectionPoolConfiguration);
        Map<String, Object> bootStrapProperties = HttpSinkUtil
                .populateTransportConfiguration(clientBootstrapConfiguration);
        return new ClientConnector(eventSourceUrl, httpURLProperties,
                httpConnectorFactory.createHttpClientConnector(bootStrapProperties, senderConfig));
    }

    private HttpCarbonMessage generateCarbonMessage(List<Header> headers, String contentType,
                                                    String httpMethod, HttpCarbonMessage cMessage,
                                                    Map<String, String> httpURLProperties) {
        cMessage.setProperty(Constants.PROTOCOL, httpURLProperties.get(Constants.PROTOCOL));
        cMessage.setProperty(Constants.TO, httpURLProperties.get(Constants.TO));
        cMessage.setProperty(Constants.HTTP_HOST, httpURLProperties.get(Constants.HTTP_HOST));
        cMessage.setProperty(Constants.HTTP_PORT, Integer.valueOf(httpURLProperties.get(Constants.HTTP_PORT)));
        cMessage.setHttpMethod(httpMethod);
        cMessage.setRequestUrl(httpURLProperties.get(REQUEST_URL));
        HttpHeaders httpHeaders = cMessage.getHeaders();
        if (!(userName.equals(EMPTY_STRING)) && !(userPassword.equals
                (EMPTY_STRING))) {
            httpHeaders.set(HttpConstants.AUTHORIZATION_HEADER, authorizationHeader);
        } else if (!(userName.equals(EMPTY_STRING)) || !(userPassword.equals
                (EMPTY_STRING))) {
            log.error("One of the basic authentication username or password missing. Hence basic authentication not " +
                    "supported.");
        }

        httpHeaders.set(Constants.HTTP_HOST, cMessage.getProperty(Constants.HTTP_HOST));
        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }

        if (contentType.contains(mapType)) {
            httpHeaders.set(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }

        cMessage.setHttpMethod(httpMethod);
        return cMessage;
    }

    public boolean matches(String thatSinkId) {
        return (Objects.equals(streamID, thatSinkId));
    }

    private void initMetrics() {
        if (MetricsDataHolder.getInstance().getMetricService() != null
                && MetricsDataHolder.getInstance().getMetricManagementService().isEnabled()) {
            try {
                if (MetricsDataHolder.getInstance().getMetricManagementService()
                        .isReporterRunning(HttpConstants.PROMETHEUS_REPORTER_NAME)) {
                    metrics = new SourceMetrics(siddhiAppName, sourceEventListener.getStreamDefinition().getId(),
                            eventSourceUrl);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Prometheus reporter is not running. Hence http source metrics will not be initialized for "
                        + siddhiAppName);
            }
        }
    }
}
