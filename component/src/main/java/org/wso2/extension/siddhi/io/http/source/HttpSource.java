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

import org.apache.log4j.Logger;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http source for receive the http and https request.
 */
@Extension(name = "http", namespace = "source", description = "The HTTP source receives POST requests via HTTP or " +
        "HTTPS in `text`, `XML` or `JSON` format. If required, you can enable basic authentication to ensure that " +
        "events are received only from users who are authorized to access WSO2 DAS.",
        parameters = {
                @Parameter(name = "receiver.url", description = "The URL to which the events should be received. The " +
                        "default value is the URL to the event stream for which the source is configured. This URL " +
                        "is specified in the following format. " +
                        "`http://localhost:8080/<streamName>`" +
                        "If you want to use SSL authentication for the event flow, you can specify the URL as " +
                        "follows." +
                        "`https://localhost:8080/<streamName>`", type = {DataType.STRING}, optional = true),
                @Parameter(name = "basic.auth.enabled", description = "If this is set to `true`, " +
                        "basic authentication is enabled for incoming events, and the credentials with which each " +
                        "event is sent are verified to ensure that the user is authorized to access the WSO2 DAS " +
                        "server. If basic authentication fails, the event flow is not authenticated and an " +
                        "authentication error is logged in the CLI.",
                        type = {DataType.STRING},
                        optional = true),
                @Parameter(name = "worker.count", description = "The number of active threads in the Siddhi level " +
                        "thread pool. The value is 1 by default. If you want to ensure that the events are directed " +
                        "to the event stream in the same order in which they arrive, the value for this parameter " +
                        "must be 1 so that events are not delivered via multiple threads, distorting the order.",
                        type = {DataType.STRING}, optional = true),
                @Parameter(name = "server.bootstrap.boss.group.size", description = "The Netty level bootstrap boss " +
                        "group size. This uses the configurations in the `netty-transport.yml` file by default.",
                        type = {DataType.STRING}),
                @Parameter(name = "server.bootstrap.worker.group.size", description = "The Netty level bootstrap " +
                        "boss group size. This uses the configurations in the `netty-transport.yml` file by default.",
                        type = {DataType.STRING})},
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
                        description = "Netty transportation property.",
                        defaultValue = "true",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "Netty transportation property.",
                        defaultValue = "15",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "client.bootstrap.socket.timeout",
                        description = "Netty transportation property.",
                        defaultValue = "15",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.host",
                        description = "The default host.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.port",
                        description = "The default port.",
                        defaultValue = "9763",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.protocol",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.keystore.file",
                        description = "The default keystore file path.",
                        defaultValue = "${carbon.home}/conf/security/wso2carbon.jks",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.keystore.pass",
                        description = "The default keystore pass.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.cert.pass",
                        description = "The default cert pass.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "N/A"
                )
        }
)
public class HttpSource extends Source {
    private static final Map<String, SourceEventListener> registeredListenerURL = new ConcurrentHashMap<>();
    private static final Map<SourceEventListener, Boolean> registeredListenerAuthentication = new ConcurrentHashMap<>();
    private static final Logger log = Logger.getLogger(HttpSource.class);
    private String sourceId;
    private String listenerUrl;
    private ListenerConfiguration listenerConfig;
    private String context;
    private ConfigReader configReader;
    private String workerThread;
    private String serverBootstrapWorkerThread;
    private String serverBootstrapBossThread;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ConfigReader configReader, ExecutionPlanContext executionPlanContext) {
        sourceId = sourceEventListener.getStreamDefinition().toString();
        String defaultBaseURL = configReader.readConfig(HttpConstants.DEFAULT_PROTOCOL, HttpConstants
                .DEFAULT_PROTOCOL_VALUE) + HttpConstants.PROTOCOL_HOST_SEPERATOR + configReader.
                readConfig(HttpConstants.DEFAULT_HOST, HttpConstants.DEFAULT_HOST_VALUE) +
                HttpConstants.PORT_HOST_SEPERATOR + configReader.readConfig(HttpConstants.
                DEFAULT_PORT, HttpConstants.DEFAULT_PORT_VALUE) + HttpConstants.
                PORT_CONTEXT_SEPERATOR;
        listenerUrl = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_URL,
                defaultBaseURL + sourceEventListener.getStreamDefinition().getId());
        Boolean isAuth = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(HttpConstants.ISAUTH,
                HttpConstants.EMPTY_ISAUTH).toLowerCase(Locale.ENGLISH));
        workerThread = optionHolder.validateAndGetStaticValue(HttpConstants.WORKER_COUNT, HttpConstants
                .DEFAULT_WORKER_COUNT);
        serverBootstrapWorkerThread = optionHolder.validateAndGetStaticValue(HttpConstants
                .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        serverBootstrapBossThread = optionHolder.validateAndGetStaticValue(HttpConstants
                .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        Object[] configuration = new HttpSourceUtil().setListenerProperty(listenerUrl, configReader);
        listenerConfig = (ListenerConfiguration) configuration[0];
        context = (String) configuration[1];
        this.configReader = configReader;
        if (registeredListenerURL.containsKey(listenerUrl)) {
            throw new ExecutionPlanCreationException("Listener URL " + listenerUrl + " already connected in "
                    + sourceId);
        } else {
            registeredListenerURL.put(HttpSourceUtil.getPortContext(listenerUrl), sourceEventListener);
            registeredListenerAuthentication.put(sourceEventListener, isAuth);
        }
    }

    static Map<SourceEventListener, Boolean> getRegisteredListenerAuthentication() {
        return registeredListenerAuthentication;
    }

    static Map<String, SourceEventListener> getRegisteredListenerURL() {
        return registeredListenerURL;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        if (HttpConnectorRegistry.getInstance().createServerConnector(listenerUrl, context, sourceId, listenerConfig,
                registeredListenerURL, registeredListenerAuthentication, configReader,
                workerThread, serverBootstrapWorkerThread, serverBootstrapBossThread)) {
            log.info("New server connector has started on " + listenerUrl.replace(context, HttpConstants.
                    EMPTY_STRING));
        } else {
            log.info(HttpConnectorRegistry.getInstance().getServerConnector(listenerUrl.replace(context,
                    HttpConstants.EMPTY_STRING)).toString() + " is already exists for url "
                    + listenerUrl.replace(context, HttpConstants.EMPTY_STRING));
        }
    }

    @Override
    public void disconnect() {
        if (registeredListenerURL.containsKey(HttpSourceUtil.getPortContext(listenerUrl))) {
            if (!HttpConnectorRegistry.getInstance().destroyServerConnector(registeredListenerURL, listenerUrl,
                    context)) {
                log.info("Server has already stopped for url " + listenerUrl + " in " + sourceId);
            } else {
                log.info("Server has stop for url " + listenerUrl + " in " + sourceId);
            }
        }
    }

    @Override
    public void destroy() {
        if (registeredListenerURL.containsKey(HttpSourceUtil.getPortContext(listenerUrl))) {
            if (!HttpConnectorRegistry.getInstance().destroyServerConnector(registeredListenerURL, listenerUrl,
                    context)) {
                log.info("Server has already stopped for url " + listenerUrl + " in " + sourceId);
            } else {
                log.info("Server has stop for url " + listenerUrl + " in " + sourceId);
            }
        }
    }

    @Override
    public void pause() {
        if (HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                .EMPTY_STRING)).isRunning()) {
            HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                    .EMPTY_STRING)).pause();
        }
    }

    @Override
    public void resume() {
        if (HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                .EMPTY_STRING)).isPaused()) {
            HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                    .EMPTY_STRING)).resume();
        }
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // no state to restore
    }
}
