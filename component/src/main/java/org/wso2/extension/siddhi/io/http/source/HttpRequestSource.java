/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.extension.siddhi.io.http.source;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HTTPSourceRegistry;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.messaging.Constants.DIRECTION;
import static org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_STATUS_CODE;

/**
 * Http source for receive the http and https request.
 */
@Extension(name = "http-request", namespace = "source", description = "The HTTP request is correlated with the " +
        "HTTP response sink, through a unique `source.id`, and for each POST requests it receives via " +
        "HTTP or HTTPS in format such as `text`, `XML` and `JSON` it sends the response via the HTTP response sink. " +
        "The individual request and response messages are correlated at the sink using the `message.id` of " +
        "the events. " +
        "If required, you can enable basic authentication at the source " +
        "to ensure that events are received only from users who are authorized to access the service.",
        parameters = {
                @Parameter(name = "receiver.url",
                        description = "The URL to which the events should be received. " +
                                "User can provide any valid url and if the url is not provided the system will use" +
                                " the " +
                                "following format `http://0.0.0.0:9763/<appNAme>/<streamName>`" +
                                "If the user want to use SSL the url should be given in following format " +
                                "`https://localhost:8080/<streamName>`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "http://0.0.0.0:9763/<appNAme>/<streamName>"),
                @Parameter(name = "source.id",
                        description = "Identifier need to map the source to sink.",
                        type = {DataType.STRING}),
                @Parameter(name = "connection.timeout",
                        description = "Connection timeout in milliseconds. If the mapped http-response sink does not "
                                + "get a correlated message, after this timeout value, a timeout response is sent",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(name = "basic.auth.enabled",
                        description = "If this is set to `true`, " +
                                "basic authentication is enabled for incoming events, and the credentials with " +
                                "which each " +
                                "event is sent are verified to ensure that the user is authorized to access the " +
                                "service. " +
                                "If basic authentication fails, the event is not authenticated and an " +
                                "authentication error is logged in the CLI. By default this values 'false' ",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "worker.count",
                        description = "The number of active worker threads to serve the " +
                                "incoming events. The value is 1 by default. This will ensure that the events are " +
                                "directed " +
                                "to the event stream in the same order in which they arrive. By increasing this " +
                                "value " +
                                "the performance might increase at the cost of loosing event ordering.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "Idle timeout for HTTP connection.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(
                        name = "ssl.verify.client",
                        description = "The type of client certificate verification.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "ssl/tls related options",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TLS"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TLS store type.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "JKS"),
                @Parameter(
                        name = "parameters",
                        description = "Parameters other than basics such as ciphers,sslEnabledProtocols,client.enable" +
                                ".session.creation. Expected format of these parameters is as follows: " +
                                "\"'ciphers:xxx','sslEnabledProtocols,client.enable:xxx'\"",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "ciphers",
                        description = "List of ciphers to be used. This parameter should include under parameters Ex:" +
                                " 'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "ssl.enabled.protocols",
                        description = "SSL/TLS protocols to be enabled. This parameter should be in camel case format" +
                                "(sslEnabledProtocols) under parameters. Ex 'sslEnabledProtocols:true'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "server.enable.session.creation",
                        description = "Enable HTTP session creation.This parameter should include under parameters " +
                                "Ex:" +
                                " 'client.enable.session.creation:true'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "server.supported.snimatchers",
                        description = "Http SNIMatcher to be added. This parameter should include under parameters" +
                                " Ex:" +
                                " 'server.supported.snimatchers:SNIMatcher'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "server.suported.server.names",
                        description = "Http supported servers. This parameter should include under parameters Ex:" +
                                " 'server.suported.server.names:server'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),

                //header validation parameters
                @Parameter(
                        name = "request.size.validation.configuration",
                        description = "Parameters that responsible for validating the http request and request " +
                                "headers. Expected format of these parameters is as follows:" +
                                " \"'request.size.validation:xxx','request.size.validation.maximum.value:xxx'\"",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "request.size.validation",
                        description = "To enable the request size validation.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "request.size.validation.maximum.value",
                        description = "If request size is validated then maximum size.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Integer.MAX_VALUE"),
                @Parameter(
                        name = "request.size.validation.reject.status.code",
                        description = "If request is exceed maximum size and request.size.validation is enabled then " +
                                "status code to be send as response.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "401"),
                @Parameter(
                        name = "request.size.validation.reject.message",
                        description = "If request is exceed maximum size and request.size.validation is enabled then " +
                                "status message to be send as response.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Message is bigger than the valid size"),
                @Parameter(
                        name = "request.size.validation.reject.message.content.type",
                        description = "If request is exceed maximum size and request.size.validation is enabled then " +
                                "content type to be send as response.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "plain/text"),
                @Parameter(
                        name = "header.size.validation",
                        description = "To enable the header size validation.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "header.validation.maximum.request.line",
                        description = "If header header validation is enabled then the maximum request line.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "4096"),
                @Parameter(
                        name = "header.validation.maximum.size",
                        description = "If header header validation is enabled then the maximum expected header size.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(
                        name = "header.validation.maximum.chunk.size",
                        description = "If header header validation is enabled then the maximum expected chunk size.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(
                        name = "header.validation.reject.status.code",
                        description = "401",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "If header is exceed maximum size and header.size.validation is enabled then " +
                                "status code to be send as response."),
                @Parameter(
                        name = "header.validation.reject.message",
                        description = "If header is exceed maximum size and header.size.validation is enabled then " +
                                "message to be send as response.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Message header is bigger than the valid size"),
                @Parameter(
                        name = "header.validation.reject.message.content.type",
                        description = "If header is exceed maximum size and header.size.validation is enabled then " +
                                "content type to be send as response.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "plain/text"),

                //bootstrap configuration
                @Parameter(
                        name = "server.bootstrap.configuration",
                        description = "Parameters that for bootstrap configurations of the server. Expected format of" +
                                " these parameters is as follows:" +
                                " \"'ciphers:xxx','sslEnabledProtocols,client.enable:xxx'\"",
                        type = {DataType.OBJECT},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "server.bootstrap.nodelay",
                        description = "Http server no delay.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "server.bootstrap.keepalive",
                        description = "Http server keep alive.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "server.bootstrap.sendbuffersize",
                        description = "Http server send buffer size.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "server.bootstrap.recievebuffersize",
                        description = "Http server receive buffer size.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "server.bootstrap.connect.timeout",
                        description = "Http server connection timeout.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "15000"),
                @Parameter(
                        name = "server.bootstrap.socket.reuse",
                        description = "To enable http socket reuse.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "Http server socket timeout.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "15"),
                @Parameter(
                        name = "server.bootstrap.socket.backlog",
                        description = "THttp server socket backlog.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "100"),
                @Parameter(
                        name = "trace.log.enabled",
                        description = "Http traffic monitoring.",
                        defaultValue = "false",
                        optional = true,
                        type = {DataType.BOOL}

                )
        },
        examples = {
                @Example(syntax = "@source(type='http-request', source.id='sampleSourceId, " +
                        "receiver.url='http://localhost:9055/endpoints/RecPro', " +
                        "connection.timeout='150000', parameters=\"'ciphers : TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'," +
                        " 'sslEnabledProtocols:TLSv1.1,TLSv1.2'\", request.size.validation.configuration=\"request" +
                        ".size.validation:true\", server.bootstrap.configuration=\"server.bootstrap.socket" +
                        ".timeout:25\", " +
                        "@map(type='json, @attributes(messageId='trp:messageId', symbol='$.events.event.symbol', " +
                        "price='$.events.event.price', volume='$.events.event.volume')))\n"
                        + "define stream FooStream (messageId string, symbol string, price float, volume long);\n",
                        description = "The expected input is as follows:\n"
                                + "{\"events\":\n"
                                + "    {\"event\":\n"
                                + "        \"symbol\":WSO2,\n"
                                + "        \"price\":55.6,\n"
                                + "        \"volume\":100,\n"
                                + "    }\n"
                                + "}\n"
                                + "If basic authentication is enabled via the `basic.auth.enabled='true` setting, " +
                                "each input event is also expected to contain the " +
                                "`Authorization:'Basic encodeBase64(username:Password)'` header.")},
        systemParameter = {
                @SystemParameter(
                        name = "serverBootstrapBossGroupSize",
                        description = "property to configure number of boss threads, which accepts incoming " +
                                "connections until the ports are unbound. Once connection accepts successfully, " +
                                "boss thread passes the accepted channel to one of the worker threads.",
                        defaultValue = "Number of available processors",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapWorkerGroupSize",
                        description = "property to configure number of worker threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "(Number of available processors)*2",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapClientGroupSize",
                        description = "property to configure number of client threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "(Number of available processors)*2",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "defaultHost",
                        description = "The default host of the transport.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "Any valid host"
                ),
                @SystemParameter(
                        name = "defaultHttpPort",
                        description = "The default port if the default scheme is 'http'.",
                        defaultValue = "8280",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "defaultHttpsPort",
                        description = "The default port if the default scheme is 'https'.",
                        defaultValue = "8243",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "defaultScheme",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = {"http", "https"}
                ),
                @SystemParameter(
                        name = "keyStoreLocation",
                        description = "The default keystore file path.",
                        defaultValue = "${carbon.home}/resources/security/wso2carbon.jks",
                        possibleParameters = "Path to wso2carbon.jks file"
                ),
                @SystemParameter(
                        name = "keyStorePassword",
                        description = "The default keystore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "String of keystore password"
                ),
                @SystemParameter(
                        name = "certPassword",
                        description = "The default cert password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "String of cert password"
                )
        }
)
public class HttpRequestSource extends HttpSource {

    private static final Logger log = Logger.getLogger(HttpRequestSource.class);
    private HttpSyncConnectorRegistry httpConnectorRegistry;
    private String sourceId;
    private long connectionTimeout;

    private Map<String, HttpCarbonMessage> requestContainerMap = new ConcurrentHashMap<>();

    private HashedWheelTimer timer;
    private WeakHashMap<String, Timeout> schedularMap = new WeakHashMap<>();
    private String siddhiAppName;

    /**
     * The initialization method for {@link io.siddhi.core.stream.input.source.Source}, which will be called
     * before other methods and validate the all listenerConfiguration and getting the intial values.
     *
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static listenerConfiguration related to the
     *                            {@link io.siddhi.core.stream.input.source.Source}
     * @param configReader        to read the {@link io.siddhi.core.stream.input.source.Source} related system
     *                            listenerConfiguration.
     * @param siddhiAppContext    the context of the {@link io.siddhi.query.api.SiddhiApp} used to get siddhi
     *                            related utilty functions.
     */
    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {

        initSource(sourceEventListener, optionHolder, requestedTransportPropertyNames, configReader, siddhiAppContext);
        initConnectorRegistry(optionHolder, configReader);
        timer = new HashedWheelTimer();
        return null;
    }

    protected void initSource(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                              String[] requestedTransportPropertyNames, ConfigReader configReader,
                              SiddhiAppContext siddhiAppContext) {

        super.initSource(sourceEventListener, optionHolder, requestedTransportPropertyNames, configReader,
                siddhiAppContext);
        this.sourceId = optionHolder.validateAndGetStaticValue(HttpConstants.SOURCE_ID);
        this.connectionTimeout = Long.parseLong(
                optionHolder.validateAndGetStaticValue(HttpConstants.CONNECTION_TIMEOUT, "120000"));
        siddhiAppName = siddhiAppContext.getName();
    }

    protected void initConnectorRegistry(OptionHolder optionHolder, ConfigReader configReader) {

        String requestSizeValidationConfigList = optionHolder
                .validateAndGetStaticValue(HttpConstants.REQUEST_SIZE_VALIDATION_CONFIG, HttpConstants.EMPTY_STRING);
        String serverBootstrapPropertiesList = optionHolder
                .validateAndGetStaticValue(HttpConstants.SERVER_BOOTSTRAP_CONFIGURATION, HttpConstants.EMPTY_STRING);

        this.httpConnectorRegistry = HttpSyncConnectorRegistry.getInstance();
        this.httpConnectorRegistry.initBootstrapConfigIfFirst(configReader);
        this.httpConnectorRegistry.setTransportConfig(serverBootstrapPropertiesList, requestSizeValidationConfigList);
    }

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class};
    }

    /**
     * Called to connect to the source backend for receiving events
     *
     * @param connectionCallback Callback to pass the ConnectionUnavailableException for connection failure after
     *                           initial successful connection
     * @param state              current state of the source
     * @throws ConnectionUnavailableException if it cannot connect to the source backend
     */
    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        this.httpConnectorRegistry.createHttpServerConnector(listenerConfiguration);
        this.httpConnectorRegistry.registerSourceListener(sourceEventListener, listenerUrl,
                workerThread, isAuth, requestedTransportPropertyNames, sourceId, siddhiAppName);

        HTTPSourceRegistry.registerRequestSource(sourceId, this);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(this.listenerUrl, siddhiAppName);
        this.httpConnectorRegistry.unregisterServerConnector(this.listenerUrl);

        HTTPSourceRegistry.removeRequestSource(sourceId);
        for (Map.Entry<String, HttpCarbonMessage> entry : requestContainerMap.entrySet()) {
            cancelRequest(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Called at the end to clean all the resources consumed by the
     * {@link io.siddhi.core.stream.input.source.Source}
     */
    @Override
    public void destroy() {
        this.httpConnectorRegistry.clearBootstrapConfigIfLast();
        HTTPSourceRegistry.removeRequestSource(sourceId);
        timer.stop();
    }

    @Override
    public void pause() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSyncSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isRunning())) {
            httpSourceListener.pause();
        }
    }

    /**
     * Called to resume event consumption
     */
    @Override
    public void resume() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSyncSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isPaused())) {
            httpSourceListener.resume();
        }
    }

    public void registerCallback(HttpCarbonMessage carbonMessage, String messageId) {

        // Add timeout handler to the timer.
        addTimeout(messageId);

        requestContainerMap.put(messageId, carbonMessage);
    }

    public void handleCallback(String messageId, String payload, List<Header> headersList, String contentType) {

        HttpCarbonMessage carbonMessage = requestContainerMap.get(messageId);
        if (carbonMessage != null) {
            // Remove the message from the map as we are going to reply to the message.
            requestContainerMap.remove(messageId);
            // Remove the timeout task as are replying to the message.
            removeTimeout(messageId);
            // Send the response to the correlating message.
            handleResponse(carbonMessage, 200, payload, headersList, contentType);
        } else {
            log.warn("No source message found for source: " + sourceId + " and message: " + messageId);
        }
    }

    private void addTimeout(String messageId) {

        Timeout timeout = timer.newTimeout(new HttpRequestSource.TimerTaskImpl(messageId), connectionTimeout,
                TimeUnit.MILLISECONDS);
        schedularMap.put(messageId, timeout);
    }

    private void removeTimeout(String messageId) {

        schedularMap.get(messageId).cancel();
    }

    private void handleResponse(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg) {

        try {
            requestMsg.respond(responseMsg);
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

    private void cancelRequest(String messageId, HttpCarbonMessage carbonMessage) {

        requestContainerMap.remove(messageId);
        schedularMap.remove(messageId);
        handleResponse(carbonMessage, 504, null, null, null);
    }

    private HttpCarbonMessage createResponseMessage(String payload, int statusCode, List<Header> headers,
                                                    String contentType) {

        HttpCarbonMessage response = new HttpCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload
                .getBytes(Charset.defaultCharset()))));

        HttpHeaders httpHeaders = response.getHeaders();

        response.setProperty(HTTP_STATUS_CODE, statusCode);
        response.setProperty(DIRECTION, DIRECTION_RESPONSE);

        // Set the Content-Type header as the system generated value. If the user has defined a specific Content-Type
        // header this will be overridden.
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

    class TimerTaskImpl implements TimerTask {

        String messageId;

        TimerTaskImpl(String messageId) {

            this.messageId = messageId;
        }

        @Override
        public void run(Timeout timeout) {

            HttpCarbonMessage carbonMessage = requestContainerMap.get(messageId);
            cancelRequest(messageId, carbonMessage);
        }
    }
}
