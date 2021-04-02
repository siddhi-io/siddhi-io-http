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

package io.siddhi.extension.io.http.sink;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HTTPSinkRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.io.http.util.HttpIoUtil;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.siddhi.extension.io.http.util.HttpConstants.DEFAULT_WORKER_COUNT;
import static io.siddhi.extension.io.http.util.HttpConstants.HTTP_STATUS_CODE;
import static org.wso2.carbon.messaging.Constants.DIRECTION;
import static org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE;

/**
 * Http sink to publish sse events.
 */
@Extension(name = "sse", namespace = "sink",
        description = "HTTP SSE sink sends events to all subscribers.",
        parameters = {
                @Parameter(
                        name = "event.sink.url",
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
public class HttpSSESink extends Sink {
    private static final Logger logger = Logger.getLogger(HttpSSESink.class);

    private String siddhiAppName;
    private String streamId;
    private String listenerUrl;
    private int workerThread;
    private boolean isAuth;
    private boolean isSecured;
    private SourceMetrics metrics;
    private String[] requestedTransportPropertyNames;
    private ListenerConfiguration listenerConfiguration;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    private SSESyncConnectorRegistry httpConnectorRegistry;
    private Option httpHeaderOption;
    private String mapType;
    private final List<HttpCarbonMessage> requestContainerList = new ArrayList<HttpCarbonMessage>();

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class, Map.class};
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }

    @Override
    protected StateFactory init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                                ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        String[] requestedTransportPropertyNames = new String[10];
        initSource(streamDefinition, optionHolder, requestedTransportPropertyNames, configReader, siddhiAppContext);
        initConnectorRegistry(optionHolder, configReader);
        //TODO: Init metrics
        return null;
    }

    private void initSource(StreamDefinition streamDefinition, OptionHolder optionHolder,
                            String[] requestedTransportPropertyNames, ConfigReader configReader,
                            SiddhiAppContext siddhiAppContext) {
        this.siddhiAppName = siddhiAppContext.getName();
        this.streamId = streamDefinition.getId();
        String scheme = configReader.readConfig(HttpConstants.DEFAULT_SOURCE_SCHEME, HttpConstants
                .DEFAULT_SOURCE_SCHEME_VALUE);
        String defaultURL;
        int port;
        if (HttpConstants.SCHEME_HTTPS.equals(scheme)) {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTPS_PORT, HttpConstants.HTTPS_PORT_VALUE));
            defaultURL = HttpConstants.SCHEME_HTTPS + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + port + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + streamId;
        } else {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTP_PORT, HttpConstants.HTTP_PORT_VALUE));
            defaultURL = HttpConstants.SCHEME_HTTP + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + port + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + streamId;
        }
        this.listenerUrl = optionHolder.validateAndGetStaticValue(HttpConstants.EVENT_SINK_URL, defaultURL);
        this.isAuth = Boolean.parseBoolean(optionHolder
                .validateAndGetStaticValue(HttpConstants.IS_AUTH, HttpConstants.EMPTY_IS_AUTH)
                .toLowerCase(Locale.ENGLISH));
        this.workerThread = Integer.parseInt(optionHolder
                .validateAndGetStaticValue(HttpConstants.WORKER_COUNT, DEFAULT_WORKER_COUNT));
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.mapType = streamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                .getValue();
        String sslConfigs = optionHolder
                .validateAndGetStaticValue(HttpConstants.SSS_CONFIGS, HttpConstants.EMPTY_STRING);
        if (sslConfigs.equalsIgnoreCase(HttpConstants.EMPTY_STRING)) {
            sslConfigs = optionHolder
                    .validateAndGetStaticValue(HttpConstants.SOURCE_PARAMETERS, HttpConstants.EMPTY_STRING);
        }
        this.listenerConfiguration = HttpSourceUtil.getListenerConfiguration(this.listenerUrl, configReader);
        this.listenerConfiguration.setSocketIdleTimeout(-1);
//        if (!HttpConstants.EMPTY_STRING.equals(requestSizeValidationConfigList)) {
//            this.listenerConfiguration.setMsgSizeValidationConfig(HttpConnectorRegistry.getInstance()
//                    .populateRequestSizeValidationConfiguration());
//        }
        isSecured = (listenerConfiguration.getScheme().equalsIgnoreCase(HttpConstants.SCHEME_HTTPS));
        port = listenerConfiguration.getPort();
        listenerConfiguration.setParameters(HttpIoUtil.populateParameters(sslConfigs));
        serviceDeploymentInfo = new ServiceDeploymentInfo(port, isSecured);
        siddhiAppName = siddhiAppContext.getName();
    }

    private void initConnectorRegistry(OptionHolder optionHolder, ConfigReader configReader) {
        String requestSizeValidationConfigList = optionHolder
                .validateAndGetStaticValue(HttpConstants.REQUEST_SIZE_VALIDATION_CONFIGS, HttpConstants.EMPTY_STRING);
        String serverBootstrapPropertiesList = optionHolder
                .validateAndGetStaticValue(HttpConstants.SERVER_BOOTSTRAP_CONFIGS, HttpConstants.EMPTY_STRING);

        this.httpConnectorRegistry = SSESyncConnectorRegistry.getInstance();
        this.httpConnectorRegistry.initBootstrapConfigIfFirst(configReader);
        this.httpConnectorRegistry.setTransportConfig(serverBootstrapPropertiesList, requestSizeValidationConfigList);
    }

    private void connectConnectorRegistry() {
        listenerConfiguration.setChunkConfig(ChunkConfig.ALWAYS);
        listenerConfiguration.setKeepAliveConfig(KeepAliveConfig.ALWAYS);
        this.httpConnectorRegistry.createHttpServerConnector(listenerConfiguration, metrics);
        this.httpConnectorRegistry.registerSourceListener(listenerUrl,
                workerThread, isAuth, requestedTransportPropertyNames, streamId, siddhiAppName, metrics);

        HTTPSinkRegistry.registerSSESink(streamId, this);
    }

    public boolean matches(String thatSinkId) {
        return (Objects.equals(streamId, thatSinkId));
    }

    public void registerCallback(HttpCarbonMessage carbonMessage) {
        requestContainerList.add(carbonMessage);
    }

    private void removeCallback(HttpCarbonMessage carbonMessage) {
        requestContainerList.remove(carbonMessage);
    }

    public void handleCallback(String payload, List<Header> headersList, String contentType) {
        if (!requestContainerList.isEmpty()) {
            requestContainerList.forEach(carbonMessage -> {
                if (carbonMessage != null) {
                    handleResponse(carbonMessage, 200, payload, headersList, contentType);
                }
            });
        } else {
            logger.warn("No subscription found" + streamId);
        }
    }

    private void handleResponse(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg) {
        try {
            requestMsg.respond(responseMsg).setHttpConnectorListener(new HttpConnectorListener() {
                @Override
                public void onMessage(HttpCarbonMessage httpMessage) {}

                @Override
                public void onError(Throwable throwable) {
                    removeCallback(requestMsg);
                }
            });
        } catch (ServerConnectorException e) {
            throw new HttpSourceAdaptorRuntimeException("Error occurred during response", e);
        }
    }

    private void handleResponse(HttpCarbonMessage requestMessage, Integer code, String payload, List<Header>
            headers, String contentType) {

        int statusCode = (code == null) ? 500 : code;
        String responsePayload = (payload != null) ? payload : "";
        handleResponse(requestMessage, createResponseMessage(responsePayload, statusCode, headers, contentType));
    }

    private HttpCarbonMessage createResponseMessage(String payload, int statusCode, List<Header> headers,
                                                    String contentType) {

        HttpCarbonMessage response = new HttpCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        response.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(payload
                .getBytes(Charset.defaultCharset()))));

        HttpHeaders httpHeaders = response.getHeaders();

        response.setProperty(HTTP_STATUS_CODE, statusCode);
        response.setProperty(DIRECTION, DIRECTION_RESPONSE);
        response.setStreaming(true);

        if (contentType != null) {
            httpHeaders.set(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }

        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }

        return response;
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        String headers = httpHeaderOption.getValue(dynamicOptions);
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        HTTPSinkRegistry.
                findAndGetSSESource(streamId).handleCallback((String) payload, headersList, contentType);
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        connectConnectorRegistry();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }
}
