/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.http.source;

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
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.http.util.HttpConstants.HTTP_STATUS_CODE;
import static org.wso2.carbon.messaging.Constants.DIRECTION;
import static org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE;

/**
 * http-service source for receive the http and https request.
 */
@Extension(name = "http-service", namespace = "source",
        description = "" +
                "The http-service source receives POST requests via HTTP and HTTPS protocols " +
                "in format such as `text`, `XML` and `JSON` and sends responses via its corresponding " +
                "http-service-response sink correlated through a unique `source.id`.\n" +
                "For request and response correlation, it generates a `messageId` upon each incoming request " +
                "and expose it via transport properties in the format `trp:messageId` to correlate them with " +
                "the responses at the http-service-response sink.\n" +
                "The request headers and properties can be accessed via transport properties in the format " +
                "`trp:<header>`.\n" +
                "It also supports basic authentication to ensure events are received from authorized users/systems.",
        parameters = {
                @Parameter(name = "receiver.url",
                        description = "The URL on which events should be received. " +
                                "To enable SSL use `https` protocol in the url.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "`http://0.0.0.0:9763/<appNAme>/<streamName>`"),
                @Parameter(name = "source.id",
                        description = "Identifier to correlate the http-service source to its corresponding " +
                                "http-service-response sinks to send responses.",
                        type = {DataType.STRING}),
                @Parameter(name = "connection.timeout",
                        description = "Connection timeout in millis. The system will send a timeout, " +
                                "if a corresponding response is not sent by an associated " +
                                "http-service-response sink within the given time.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(name = "basic.auth.enabled",
                        description = "This only works in VM, Docker and Kubernetes.\nWhere when enabled " +
                                "it authenticates each request using the " +
                                "`Authorization:'Basic encodeBase64(username:Password)'` header.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "worker.count",
                        description = "The number of active worker threads to serve the " +
                                "incoming events. By default the value is set to `1` to ensure events are processed " +
                                "in the same order they arrived. By increasing this value, " +
                                "higher performance can be achieved in the expense of loosing event ordering.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "Idle timeout for HTTP connection in millis.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(
                        name = "ssl.verify.client",
                        description = "The type of client certificate verification. " +
                                "Supported values are `require`, `optional`.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "SSL/TLS protocol.",
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
                        name = "ssl.configurations",
                        description = "SSL/TSL configurations in format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported parameters:\n" +
                                " - SSL/TLS protocols: `'sslEnabledProtocols:TLSv1.1,TLSv1.2'`\n" +
                                " - List of ciphers: `'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'`\n" +
                                " - Enable session creation: `'client.enable.session.creation:true'`\n" +
                                " - Supported server names: `'server.suported.server.names:server'`\n" +
                                " - Add HTTP SNIMatcher: `'server.supported.snimatchers:SNIMatcher'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "request.size.validation.configurations",
                        description = "Configurations to validate the HTTP request size.\n" +
                                "Expected format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Enable request size validation: `'request.size.validation:true'`\n" +
                                " If request size is validated\n" +
                                " - Maximum request size: `'request.size.validation.maximum.value:2048'`\n" +
                                " - Response status code when request size validation fails: " +
                                "`'request.size.validation.reject.status.code:401'`\n" +
                                " - Response message when request size validation fails: " +
                                "`'request.size.validation.reject.message:Message is bigger than the valid size'`\n" +
                                " - Response Content-Type when request size validation fails: " +
                                "`'request.size.validation.reject.message.content.type:plain/text'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "header.validation.configurations",
                        description = "Configurations to validate HTTP headers.\n" +
                                "Expected format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Enable header size validation: `'header.size.validation:true'`\n" +
                                " If header size is validated\n" +
                                " - Maximum length of initial line: `'header.validation.maximum.request.line:4096'`\n" +
                                " - Maximum length of all headers: `'header.validation.maximum.size:8192'`\n" +
                                " - Maximum length of the content or each chunk: " +
                                "`'header.validation.maximum.chunk.size:8192'`\n" +
                                " - Response status code when header validation fails: " +
                                "`'header.validation.reject.status.code:401'`\n" +
                                " - Response message when header validation fails: " +
                                "`'header.validation.reject.message:Message header is bigger than the valid size'`\n" +
                                " - Response Content-Type when header validation fails: " +
                                "`'header.validation.reject.message.content.type:plain/text'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "server.bootstrap.configurations",
                        description = "Server bootstrap configurations in " +
                                "format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Server connect timeout in millis:" +
                                " `'server.bootstrap.connect.timeout:15000'`\n" +
                                " - Server socket timeout in seconds:" +
                                " `'server.bootstrap.socket.timeout:15'`\n" +
                                " - Enable TCP no delay: `'server.bootstrap.nodelay:true'`\n" +
                                " - Enable server keep alive: `'server.bootstrap.keepalive:true'`\n" +
                                " - Send buffer size: `'server.bootstrap.sendbuffersize:1048576'`\n" +
                                " - Receive buffer size: `'server.bootstrap.recievebuffersize:1048576'`\n" +
                                " - Number of connections queued: `'server.bootstrap.socket.backlog:100'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "trace.log.enabled",
                        description = "Enable trace log for traffic monitoring.",
                        defaultValue = "false",
                        optional = true,
                        type = {DataType.BOOL}
                )
        },
        examples = {
                @Example(syntax = "" +
                        "@source(type='http-service', receiver.url='http://localhost:5005/add',\n" +
                        "        source.id='adder',\n" +
                        "        @map(type='json, @attributes(messageId='trp:messageId',\n" +
                        "                                     value1='$.event.value1',\n" +
                        "                                     value2='$.event.value2')))\n" +
                        "define stream AddStream (messageId string, value1 long, value2 long);\n" +
                        "\n" +
                        "@sink(type='http-service-response', source.id='adder',\n" +
                        "      message.id='{{messageId}}', @map(type = 'json'))\n" +
                        "define stream ResultStream (messageId string, results long);\n" +
                        "\n" +
                        "@info(name = 'query1')\n" +
                        "from AddStream \n" +
                        "select messageId, value1 + value2 as results \n" +
                        "insert into ResultStream;",
                        description = "Above sample listens events on `http://localhost:5005/stocks` url for " +
                                "JSON messages on the format:\n" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"value1\": 3,\n" +
                                "    \"value2\": 4\n" +
                                "  }\n" +
                                "}```\n" +
                                "Map the vents into AddStream, process the events through query `query1`, and " +
                                "sends the results produced on ResultStream via http-service-response sink " +
                                "on the message format:" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"results\": 7\n" +
                                "  }\n" +
                                "}```"),
        },
        systemParameter = {
                @SystemParameter(
                        name = "serverBootstrapBossGroupSize",
                        description = "Number of boss threads to accept incoming connections.",
                        defaultValue = "Number of available processors",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapWorkerGroupSize",
                        description = "Number of worker threads to accept the connections from boss threads and " +
                                "perform non-blocking read and write from one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapClientGroupSize",
                        description = "Number of client threads to perform non-blocking read and write to " +
                                "one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "defaultHost",
                        description = "The default host of the transport.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "Any valid host"
                ),
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
                        name = "defaultHttpsPort",
                        description = "The default HTTPS port when default scheme is `https`.",
                        defaultValue = "8243",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "keyStoreLocation",
                        description = "The default keystore file path.",
                        defaultValue = "`${carbon.home}/resources/security/wso2carbon.jks`",
                        possibleParameters = "Path to `.jks` file"
                ),
                @SystemParameter(
                        name = "keyStorePassword",
                        description = "The default keystore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Keystore password as string"
                )
        }
)
public class HttpServiceSource extends HttpSource {

    private static final Logger log = Logger.getLogger(HttpServiceSource.class);
    private HttpSyncConnectorRegistry httpConnectorRegistry;
    private String sourceId;
    private long connectionTimeout;

    private Map<String, HttpCarbonMessage> requestContainerMap = new ConcurrentHashMap<>();

    private HashedWheelTimer timer;
    private WeakHashMap<String, Timeout> schedulerMap = new WeakHashMap<>();
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
        initMetrics(siddhiAppContext.getName());
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
                .validateAndGetStaticValue(HttpConstants.REQUEST_SIZE_VALIDATION_CONFIGS, HttpConstants.EMPTY_STRING);
        String serverBootstrapPropertiesList = optionHolder
                .validateAndGetStaticValue(HttpConstants.SERVER_BOOTSTRAP_CONFIGS, HttpConstants.EMPTY_STRING);

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
     * Called to connect to the source backend for receiving events.
     *
     * @param connectionCallback Callback to pass the ConnectionUnavailableException for connection failure after
     *                           initial successful connection
     * @param state              current state of the source
     * @throws ConnectionUnavailableException if it cannot connect to the source backend
     */
    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        this.httpConnectorRegistry.createHttpServerConnector(listenerConfiguration, metrics);
        this.httpConnectorRegistry.registerSourceListener(sourceEventListener, listenerUrl,
                workerThread, isAuth, requestedTransportPropertyNames, sourceId, siddhiAppName, metrics);

        HTTPSourceRegistry.registerServiceSource(sourceId, this);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(this.listenerUrl, siddhiAppName, metrics);
        this.httpConnectorRegistry.unregisterServerConnector(this.listenerUrl);

        HTTPSourceRegistry.removeServiceSource(sourceId);
        for (Map.Entry<String, HttpCarbonMessage> entry : requestContainerMap.entrySet()) {
            cancelRequest(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Called at the end to clean all the resources consumed by the
     * {@link io.siddhi.core.stream.input.source.Source}.
     */
    @Override
    public void destroy() {
        this.httpConnectorRegistry.clearBootstrapConfigIfLast();
        HTTPSourceRegistry.removeServiceSource(sourceId);
        timer.stop();
    }

    @Override
    public void pause() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSyncSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics));
        if ((httpSourceListener != null) && (httpSourceListener.isRunning())) {
            httpSourceListener.pause();
        }
    }

    /**
     * Called to resume event consumption.
     */
    @Override
    public void resume() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSyncSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics));
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

        Timeout timeout = timer.newTimeout(new HttpServiceSource.TimerTaskImpl(messageId), connectionTimeout,
                TimeUnit.MILLISECONDS);
        schedulerMap.put(messageId, timeout);
    }

    private void removeTimeout(String messageId) {

        schedulerMap.get(messageId).cancel();
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
        schedulerMap.remove(messageId);
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
