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

import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
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
                        "User can provide any valid url and if the url is not provided the system will use the " +
                                "following format `http://0.0.0.0:9763/<appNAme>/<streamName>`" +
                        "If the user want to use SSL the url should be given in following format " +
                        "`https://localhost:8080/<streamName>`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "http://0.0.0.0:9763/<appNAme>/<streamName>"),
                @Parameter(name = "basic.auth.enabled",
                        description = "If this is set to `true`, " +
                        "basic authentication is enabled for incoming events, and the credentials with which each " +
                        "event is sent are verified to ensure that the user is authorized to access the service. " +
                        "If basic authentication fails, the event is not authenticated and an " +
                        "authentication error is logged in the CLI. By default this values 'false' ",
                        type = {DataType.STRING},
                        optional = true ,
                        defaultValue = "false"),
                @Parameter(name = "worker.count",
                        description = "The number of active worker threads to serve the " +
                        "incoming events. The value is 1 by default. This will ensure that the events are directed " +
                        "to the event stream in the same order in which they arrive. By increasing this value " +
                        "the performance might increase at the cost of loosing event ordering.",
                        type = {DataType.STRING},
                        optional = true ,
                        defaultValue = "1")
        },
        examples = {
                @Example(syntax = "@source(type='http', receiver.url='http://localhost:9055/endpoints/RecPro', " +
                        "@map(type='xml'))\n"
                        + "define stream FooStream (symbol string, price float, volume long);\n",
                        description = "Above source configuration performs a default XML input mapping. The expected "
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
                        name = "latency.metrics.enabled",
                        description = "Property to enable metrics logs to monitor transport latency for netty.",
                        defaultValue = "true",
                        possibleParameters = {"true", "false"}
                ),
                @SystemParameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "property to configure specified timeout in milliseconds which server socket " +
                                "will block for this amount of time for http message content to be received.",
                        defaultValue = "15",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "client.bootstrap.socket.timeout",
                        description = "property to configure specified timeout in milliseconds which client " +
                                "socket will block for this amount of time for http message content to be received",
                        defaultValue = "15",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "server.bootstrap.boss.group.size",
                        description = "property to configure number of boss threads, which accepts incoming " +
                                "connections until the ports are unbound. Once connection accepts successfully, " +
                                "boss thread passes the accepted channel to one of the worker threads.",
                        defaultValue = "4",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "server.bootstrap.worker.group.size",
                        description = "property to configure number of worker threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "8",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "default.host",
                        description = "The default host.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "Any valid host"
                ),
                @SystemParameter(
                        name = "default.port",
                        description = "The default port.",
                        defaultValue = "9763",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "default.protocol",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = {"http", "https"}
                ),
                @SystemParameter(
                        name = "https.keystore.file",
                        description = "The default keystore file path.",
                        defaultValue = "${carbon.home}/resources/security/wso2carbon.jks",
                        possibleParameters = "Path to wso2carbon.jks file"
                ),
                @SystemParameter(
                        name = "https.keystore.password",
                        description = "The default keystore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "String of keystore password"
                ),
                @SystemParameter(
                        name = "https.cert.password",
                        description = "The default cert password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "String of cert password"
                )
        }
)
public class HttpSource extends Source {
    private String sourceId;
    private String listenerUrl;
    private ListenerConfiguration listenerConfig;
    private HttpConnectorRegistry httpConnectorRegistry;
    private Boolean isAuth;
    private String workerThread;
    private SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        this.sourceId = sourceEventListener.getStreamDefinition().toString();
        String defaultURL = configReader.readConfig(HttpConstants.DEFAULT_PROTOCOL, HttpConstants
                .DEFAULT_PROTOCOL_VALUE) + HttpConstants.PROTOCOL_HOST_SEPARATOR + configReader.
                readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                HttpConstants.PORT_HOST_SEPARATOR + configReader.readConfig(HttpConstants.
                DEFAULT_PORT, HttpConstants.DEFAULT_PORT_VALUE) + HttpConstants.
                PORT_CONTEXT_SEPARATOR + siddhiAppContext.getName()
                + HttpConstants.PORT_CONTEXT_SEPARATOR + sourceEventListener.getStreamDefinition().getId();
        this.listenerUrl = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_URL, defaultURL);
        this.isAuth = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(HttpConstants.IS_AUTH,
                HttpConstants.EMPTY_IS_AUTH).toLowerCase(Locale.ENGLISH));
        this.workerThread = optionHolder.validateAndGetStaticValue(HttpConstants.WORKER_COUNT, HttpConstants
                .DEFAULT_WORKER_COUNT);
        this.listenerConfig = new HttpSourceUtil().setListenerProperty(this.listenerUrl, configReader);
        this.httpConnectorRegistry = HttpConnectorRegistry.getInstance();
        this.httpConnectorRegistry.initHttpServerConnectorController(configReader);
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        this.httpConnectorRegistry.registerServerConnector(this.listenerUrl, this.sourceId, this.listenerConfig);
        this.httpConnectorRegistry.registerSourceListener(sourceEventListener, this.listenerUrl,
                Integer.valueOf(workerThread), isAuth, requestedTransportPropertyNames);
    }

    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(this.listenerUrl);
        this.httpConnectorRegistry.unregisterServerConnector(this.listenerUrl);
    }

    @Override
    public void destroy() {
        // TODO: 7/26/17 Until fix for multiple worker and boss thread loop group
        //this.httpConnectorRegistry.stopHttpServerConnectorController();
    }

    @Override
    public void pause() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSourceListenersMap().get(HttpSourceUtil
                .getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isRunning())) {
            httpSourceListener.pause();
        }
    }

    @Override
    public void resume() {
        HttpSourceListener httpSourceListener = this.httpConnectorRegistry.getSourceListenersMap()
                .get(HttpSourceUtil.getSourceListenerKey(listenerUrl));
        if ((httpSourceListener != null) && (httpSourceListener.isPaused())) {
            httpSourceListener.resume();
        }
    }

    @Override
    public Map<String, Object> currentState() {
        //no current state
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        // no state to restore
    }
}
