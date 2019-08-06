/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.io.http.util.HttpIoUtil;
import org.apache.log4j.Logger;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import java.util.Locale;

import static io.siddhi.extension.io.http.util.HttpConstants.DEFAULT_WORKER_COUNT;
import static io.siddhi.extension.io.http.util.HttpConstants.SOCKET_IDEAL_TIMEOUT_VALUE;


/**
 * Http source for receive the http and https request.
 */
@Extension(name = "http", namespace = "source",
        description = "HTTP source receives POST requests via HTTP and " +
                "HTTPS protocols in format such as `text`, `XML` and `JSON`. It also supports basic " +
                "authentication to ensure events are received from authorized users/systems.\n" +
                "The request headers and properties are exposed via transport properties and they can be retrieved " +
                "in the mapper in the format `trp:<header>`." ,
        parameters = {
                @Parameter(name = "receiver.url",
                        description = "The URL on which events should be received. " +
                                "To enable SSL use `https` protocol in the url.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "`http://0.0.0.0:9763/<appNAme>/<streamName>`"),
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
                        "@app.name('StockProcessor')\n\n" +
                        "@source(type='http', @map(type = 'json'))\n" +
                        "define stream StockStream (symbol string, price float, volume long);\n",
                        description = "Above HTTP source listeners on url " +
                                "`http://0.0.0.0:9763/StockProcessor/StockStream` " +
                                "for JSON messages on the format:\n"
                                + "```{\n" +
                                "  \"event\": {\n" +
                                "    \"symbol\": \"FB\",\n" +
                                "    \"price\": 24.5,\n" +
                                "    \"volume\": 5000\n" +
                                "  }\n" +
                                "}```" +
                                "It maps the incoming messages and sends them to `StockStream` for processing."),
                @Example(syntax = "" +
                        "@source(type='http', receiver.url='http://localhost:5005/stocks',\n" +
                        "        @map(type = 'xml'))\n" +
                        "define stream StockStream (symbol string, price float, volume long);\n",
                        description = "Above HTTP source listeners on url `http://localhost:5005/stocks` for " +
                                "JSON messages on the format:\n"
                                + "```<events>\n"
                                + "    <event>\n"
                                + "        <symbol>Fb</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>```\n" +
                                "It maps the incoming messages and sends them to `StockStream` for processing."),
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
public class HttpSource extends Source {
    private static final Logger log = Logger.getLogger(HttpSource.class);
    protected String listenerUrl;
    protected Boolean isAuth;
    protected int workerThread;
    protected SourceEventListener sourceEventListener;
    protected String[] requestedTransportPropertyNames;
    protected ListenerConfiguration listenerConfiguration;
    private HttpConnectorRegistry httpConnectorRegistry;
    private String siddhiAppName;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    private boolean isSecured;

    /**
     * The initialization method for {@link Source}, which will be called before other methods and validate
     * the all listenerConfiguration and getting the intial values.
     *
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static listenerConfiguration related to the {@link Source}
     * @param configReader        to read the {@link Source} related system listenerConfiguration.
     * @param siddhiAppContext    the context of the {@link io.siddhi.query.api.SiddhiApp} used to get siddhi
     *                            related utilty functions.
     */
    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {

        initSource(sourceEventListener, optionHolder, requestedTransportPropertyNames, configReader, siddhiAppContext);
        initConnectorRegistry(optionHolder, configReader);
        return null;
    }

    protected void initConnectorRegistry(OptionHolder optionHolder, ConfigReader configReader) {

        String requestSizeValidationConfigList = optionHolder
                .validateAndGetStaticValue(HttpConstants.REQUEST_SIZE_VALIDATION_CONFIGS, HttpConstants.EMPTY_STRING);
        String serverBootstrapPropertiesList = optionHolder
                .validateAndGetStaticValue(HttpConstants.SERVER_BOOTSTRAP_CONFIGS, HttpConstants.EMPTY_STRING);

        this.httpConnectorRegistry = HttpConnectorRegistry.getInstance();
        this.httpConnectorRegistry.initBootstrapConfigIfFirst(configReader);
        this.httpConnectorRegistry.setTransportConfig(serverBootstrapPropertiesList, requestSizeValidationConfigList);
    }

    protected void initSource(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                              String[] requestedTransportPropertyNames, ConfigReader configReader,
                              SiddhiAppContext siddhiAppContext) {

        siddhiAppName = siddhiAppContext.getName();
        String scheme = configReader.readConfig(HttpConstants.DEFAULT_SOURCE_SCHEME, HttpConstants
                .DEFAULT_SOURCE_SCHEME_VALUE);
        //generate default URL
        String defaultURL;
        int port;
        if (HttpConstants.SCHEME_HTTPS.equals(scheme)) {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTPS_PORT, HttpConstants.HTTPS_PORT_VALUE));
            defaultURL = HttpConstants.SCHEME_HTTPS + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + port + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + sourceEventListener.getStreamDefinition().getId();
        } else {
            port = Integer.parseInt(configReader.readConfig(HttpConstants.HTTP_PORT, HttpConstants.HTTP_PORT_VALUE));
            defaultURL = HttpConstants.SCHEME_HTTP + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + port + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + sourceEventListener.getStreamDefinition().getId();
        }
        //read configuration
        this.listenerUrl = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_URL, defaultURL);
        this.isAuth = Boolean.parseBoolean(optionHolder
                .validateAndGetStaticValue(HttpConstants.IS_AUTH, HttpConstants.EMPTY_IS_AUTH)
                .toLowerCase(Locale.ENGLISH));
        this.workerThread = Integer.parseInt(optionHolder
                .validateAndGetStaticValue(HttpConstants.WORKER_COUNT, DEFAULT_WORKER_COUNT));
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        int socketIdleTimeout = Integer.parseInt(optionHolder
                .validateAndGetStaticValue(HttpConstants.SOCKET_IDEAL_TIMEOUT, SOCKET_IDEAL_TIMEOUT_VALUE));
        String verifyClient = optionHolder
                .validateAndGetStaticValue(HttpConstants.SSL_VERIFY_CLIENT, HttpConstants.EMPTY_STRING);
        String sslProtocol = optionHolder
                .validateAndGetStaticValue(HttpConstants.SSL_PROTOCOL, HttpConstants.EMPTY_STRING);
        String tlsStoreType = optionHolder
                .validateAndGetStaticValue(HttpConstants.TLS_STORE_TYPE, HttpConstants.EMPTY_STRING);
        String requestSizeValidationConfigList = optionHolder
                .validateAndGetStaticValue(HttpConstants.REQUEST_SIZE_VALIDATION_CONFIGS, HttpConstants.EMPTY_STRING);

        String sslConfigs = optionHolder
                .validateAndGetStaticValue(HttpConstants.SSS_CONFIGS, HttpConstants.EMPTY_STRING);
        if (sslConfigs.equalsIgnoreCase(HttpConstants.EMPTY_STRING)) {
            sslConfigs = optionHolder
                    .validateAndGetStaticValue(HttpConstants.SOURCE_PARAMETERS, HttpConstants.EMPTY_STRING);
        }
        String traceLog = optionHolder.validateAndGetStaticValue(HttpConstants.TRACE_LOG_ENABLED, configReader
                .readConfig(HttpConstants.DEFAULT_TRACE_LOG_ENABLED, HttpConstants.EMPTY_STRING));
        this.listenerConfiguration = HttpSourceUtil.getListenerConfiguration(this.listenerUrl, configReader);
        if (socketIdleTimeout != -1) {
            this.listenerConfiguration.setSocketIdleTimeout(socketIdleTimeout);
        }
        if (!HttpConstants.EMPTY_STRING.equals(verifyClient)) {
            this.listenerConfiguration.setVerifyClient(verifyClient);
        }
        if (!HttpConstants.EMPTY_STRING.equals(sslProtocol)) {
            this.listenerConfiguration.setSSLProtocol(sslProtocol);
        }
        if (!HttpConstants.EMPTY_STRING.equals(tlsStoreType)) {
            this.listenerConfiguration.setTLSStoreType(tlsStoreType);
        }
        if (!HttpConstants.EMPTY_STRING.equals(traceLog)) {
            this.listenerConfiguration.setHttpTraceLogEnabled(Boolean.parseBoolean(traceLog));
        }
        if (!HttpConstants.EMPTY_STRING.equals(requestSizeValidationConfigList)) {
            this.listenerConfiguration.setRequestSizeValidationConfig(HttpConnectorRegistry.getInstance()
                    .populateRequestSizeValidationConfiguration());
        }
        isSecured = (listenerConfiguration.getScheme().equalsIgnoreCase(HttpConstants.SCHEME_HTTPS));
        port = listenerConfiguration.getPort();
        listenerConfiguration.setParameters(HttpIoUtil.populateParameters(sslConfigs));
        serviceDeploymentInfo = new ServiceDeploymentInfo(port, isSecured);
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return serviceDeploymentInfo;
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
        this.httpConnectorRegistry.registerSourceListener(sourceEventListener, this.listenerUrl,
                workerThread, isAuth, requestedTransportPropertyNames, siddhiAppName);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(this.listenerUrl, siddhiAppName);
        this.httpConnectorRegistry.unregisterServerConnector(this.listenerUrl);
    }

    /**
     * Called at the end to clean all the resources consumed by the {@link Source}
     */
    @Override
    public void destroy() {
        this.httpConnectorRegistry.clearBootstrapConfigIfLast();
    }

    /**
     * Called to pause event consumption
     */
    @Override
    public void pause() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSourceListenersMap().get(HttpSourceUtil
                .getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isRunning())) {
            httpSourceListener.pause();
        }
    }

    /**
     * Called to resume event consumption
     */
    @Override
    public void resume() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isPaused())) {
            httpSourceListener.resume();
        }
    }

}
