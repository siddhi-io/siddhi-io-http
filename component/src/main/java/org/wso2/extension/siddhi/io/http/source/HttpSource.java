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
package org.wso2.extension.siddhi.io.http.source;

import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.transport.http.netty.config.ListenerConfiguration;

import java.util.Locale;
import java.util.Map;

/**
 * Http source for receive the http and https request.
 */
@Extension(name = "http", namespace = "source", description = "The HTTP source receives POST requests via HTTP or " +
        "HTTPS in format such as `text`, `XML` and `JSON`. If required, you can enable basic authentication to " +
        "ensure that events are received only from users who are authorized to access the service.",
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
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "1"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "TODO",
                        type = {DataType.INT},
                        optional = true,
                         defaultValue = "120000"),
                @Parameter(
                        name = "verify.client",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TLS"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "JKS"),
                @Parameter(
                        name = "ciphers",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                @Parameter(
                        name = "ssl.enabled.protocols",
                        description = "sslEnabledProtocols->ssl.enabled.protocols",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                @Parameter(
                        name = "server.enable.session.creation",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                @Parameter(
                        name = "server.supported.snimatchers",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                @Parameter(
                        name = "server.suported.server.names",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "null"),
                //header validation parameters
                @Parameter(
                        name = "request.size.validation",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "request.size.validation.maximum.value",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "request.size.validation.reject.status.code",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "request.size.validation.reject.message",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "request.size.validation.reject.message.content.type",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.size.validation",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.maximum.request.line",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.maximum.size",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.maximum.chunk.size",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.reject.status.code",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.reject.message",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                @Parameter(
                        name = "header.validation.reject.message.content.type",
                        description = "TODO",
                        type = {DataType.STRING},
                        optional = true,
                         defaultValue = "TODO"),
                //bootstrap configuration
                @Parameter(
                        name = "server.bootstrap.nodelay",
                        description = "TODO",
                        type = {DataType.BOOL},
                        optional = true,
                         defaultValue = "true"),
                @Parameter(
                        name = "server.bootstrap.keepalive",
                        description = "TODO",
                        type = {DataType.BOOL},
                        optional = true,
                         defaultValue = "true"),
                @Parameter(
                        name = "server.bootstrap.sendbuffersize",
                        description = "TODO",
                        type = {DataType.INT},
                        optional = true,
                         defaultValue = "1048576"),
                @Parameter(
                        name = "server.bootstrap.recievebuffersize",
                        description = "TODO",
                        type = {DataType.INT},
                        optional = true,
                         defaultValue = "1048576"),
                @Parameter(
                        name = "server.bootstrap.connect.timeout",
                        description = "TODO",
                        type = {DataType.INT},
                        optional = true,
                         defaultValue = "15000"),
                @Parameter(
                        name = "server.bootstrap.socket.reuse",
                        description = "TODO",
                        type = {DataType.BOOL},
                        optional = true,
                         defaultValue = "false"),
                @Parameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "TODO",
                        type = {DataType.BOOL},
                        optional = true,
                         defaultValue = "15"),
                @Parameter(
                        name = "server.bootstrap.socket.backlog",
                        description = "TODO",
                        type = {DataType.BOOL},
                        optional = true,
                         defaultValue = "100")
        },
        examples = {
                @Example(syntax = "@source(type='http', receiver.url='http://localhost:9055/endpoints/RecPro', " +
                        "socketIdleTimeout='150000', parameters=\"'ciphers : TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'," +
                        " 'sslEnabledProtocols:TLSv1.1,TLSv1.2'\",request.size.validation.configuration=\"request" +
                        ".size.validation:true\",server.bootstrap.configuration=\"server.bootstrap.socket" +
                        ".timeout:25\"”  " +
                        "@map(type='xml'))\n"
                        + "define stream FooStream (symbol string, price float, volume long);\n",
                        description = "Above source listenerConfiguration performs a default XML input mapping. " +
                                "The expected "
                                + "input is as follows:"
                                + "<events>\n"
                                + "    <event>\n"
                                + "        <symbol>WSO2</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>\n"
                                + "If basic authentication is enabled via the `basic.auth.enabled='true` setting, " +
                                "each input event is also expected to contain the " +
                                "`Authorization:'Basic encodeBase64(username:Password)'` header.")},
        systemParameter = {
                @SystemParameter(
                        name = "serverBootstrapBossGroupSize",
                        description = "property to configure number of boss threads, which accepts incoming " +
                                "connections until the ports are unbound. Once connection accepts successfully, " +
                                "boss thread passes the accepted channel to one of the worker threads.",
                        defaultValue = "4",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapWorkerGroupSize",
                        description = "property to configure number of worker threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "8",
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
                ),
                @SystemParameter(
                        name = "httpTraceLogEnabled",
                        description = "Http traffic monitoring.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "String of cert password"
                )
        }
)
public class HttpSource extends Source {
    //private String sourceId;
    private String listenerUrl;
    private HttpConnectorRegistry httpConnectorRegistry;
    private Boolean isAuth;
    private String workerThread;
    private SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;
    private ListenerConfiguration listenerConfiguration;

    /**
     * The initialization method for {@link Source}, which will be called before other methods and validate
     * the all listenerConfiguration and getting the intial values.
     *
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static listenerConfiguration related to the {@link Source}
     * @param configReader        to read the {@link Source} related system listenerConfiguration.
     * @param siddhiAppContext    the context of the {@link org.wso2.siddhi.query.api.SiddhiApp} used to get siddhi
     *                            related utilty functions.
     */
    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        String scheme = configReader.readConfig(HttpConstants.DEFAULT_SOURCE_SCHEME, HttpConstants
                .DEFAULT_SOURCE_SCHEME_VALUE);
        //generate default URL
        String defaultURL;
        if (HttpConstants.SCHEME_HTTPS.equals(scheme)) {
            defaultURL = HttpConstants.SCHEME_HTTPS + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + configReader.readConfig(HttpConstants.
                    HTTPS_PORT, HttpConstants.HTTPS_PORT_VALUE) + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + sourceEventListener.getStreamDefinition().getId();
        } else {
            defaultURL = HttpConstants.SCHEME_HTTP + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                    readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                    HttpConstants.PORT_HOST_SEPARATOR + configReader.readConfig(HttpConstants.
                    HTTP_PORT, HttpConstants.HTTP_PORT_VALUE) + HttpConstants.
                    PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                    + HttpConstants.PORT_CONTEXT_SEPARATOR + sourceEventListener.getStreamDefinition().getId();
        }
        //read configuration
        this.listenerUrl = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_URL, defaultURL);
        this.isAuth = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(HttpConstants.IS_AUTH,
                HttpConstants.EMPTY_IS_AUTH).toLowerCase(Locale.ENGLISH));
        this.workerThread = optionHolder.validateAndGetStaticValue(HttpConstants.WORKER_COUNT, HttpConstants
                .DEFAULT_WORKER_COUNT);
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        int socketIdleTimeout = Integer.parseInt(optionHolder.validateAndGetStaticValue
                (HttpConstants.SOCKET_IDEAL_TIMEOUT, "-1"));
        String verifyClient = optionHolder.validateAndGetStaticValue(HttpConstants.SSL_VERIFY_CLIENT, HttpConstants
                .EMPTY_STRING);
        String sslProtocol = optionHolder.validateAndGetStaticValue(HttpConstants.SSL_PROTOCOL, HttpConstants
                .EMPTY_STRING);
        String tlsStoreType = optionHolder.validateAndGetStaticValue(HttpConstants.TLS_STORE_TYPE, HttpConstants
                .EMPTY_STRING);
        String requestSizeValidationConfigList = optionHolder.validateAndGetStaticValue(HttpConstants
                .REQUEST_SIZE_VALIDATION_CONFIG, HttpConstants.EMPTY_STRING);
        String serverBootstrapPropertiesList = optionHolder.validateAndGetStaticValue(HttpConstants
                .SERVER_BOOTSTRAP_CONFIGURATION, HttpConstants.EMPTY_STRING);
        String parameterList = optionHolder.validateAndGetStaticValue(HttpConstants.SOURCE_PARAMETERS, HttpConstants
                .EMPTY_STRING);
        this.listenerConfiguration = HttpSourceUtil.getListenerConfiguration(this.listenerUrl, configReader);
        if (socketIdleTimeout != -1) {
            this.listenerConfiguration.setSocketIdleTimeout(socketIdleTimeout);
        }
        if (!HttpConstants.EMPTY_STRING.equals(verifyClient)) {
            this.listenerConfiguration.setVerifyClient(verifyClient);
        }
        if (!HttpConstants.EMPTY_STRING.equals(sslProtocol)) {
            this.listenerConfiguration.setSslProtocol(sslProtocol);
        }
        if (!HttpConstants.EMPTY_STRING.equals(tlsStoreType)) {
            this.listenerConfiguration.setTlsStoreType(tlsStoreType);
        }
        this.httpConnectorRegistry = HttpConnectorRegistry.getInstance();
        this.httpConnectorRegistry.initBootstrapConfigIfFirst(configReader);
        this.httpConnectorRegistry.setLogTraceEnabled(configReader);
        this.httpConnectorRegistry.setTrpConfig(serverBootstrapPropertiesList, requestSizeValidationConfigList);
        if (!HttpConstants.EMPTY_STRING.equals(requestSizeValidationConfigList)) {
            this.listenerConfiguration.setRequestSizeValidationConfig(HttpConnectorRegistry.getInstance()
                    .populateRequestSizeValidationConfiguration());
        }
        listenerConfiguration.setParameters(HttpSourceUtil.getInstance().populateParameters(parameterList));
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
     * Intialy Called to connect to the end point for start  retriving the messages asynchronisly .
     *
     * @param connectionCallback Callback to pass the ConnectionUnavailableException in case of connection failure
     *                           after initial successful connection(can be used when events are receving
     *                           asynchronasily)
     * @throws ConnectionUnavailableException if it cannot connect to the source backend immediately.
     */
    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        this.httpConnectorRegistry.createHttpServerConnector(listenerConfiguration);
        this.httpConnectorRegistry.registerSourceListener(sourceEventListener, this.listenerUrl,
                Integer.parseInt(workerThread), isAuth, requestedTransportPropertyNames);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(this.listenerUrl);
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

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as a map
     */
    @Override
    public Map<String, Object> currentState() {
        //no current state
        return null;
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     *
     * @param map stateful objects of the element as a map.
     *            This is the same map that is created upon calling currentState() method.
     */
    @Override
    public void restoreState(Map<String, Object> map) {
        // no state to restore
    }
}
