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
@Extension(name = "http", namespace = "source", description = "HTTP Source", parameters = {
        @Parameter(name = "receiver.url", description = "Used to get the listening url. this is an optional parameter "
                + "and by default it listening to the stream.", type = {DataType.STRING}),
        @Parameter(name = "is.basic.auth.enabled", description = "Used to specify the whether user need to " +
                "authenticated with basic authentication " + "or not", type = {DataType.STRING})},
        examples = {
                @Example(syntax = "@source(type='http', topic='stock', @map(type='xml'))\n"
                        + "define stream FooStream (symbol string, price float, volume long);\n",
                        description = "Above configuration will do a default XML input mapping. Expected "
                                + "input will look like below."
                                + "<events>\n"
                                + "    <event>\n"
                                + "        <symbol>WSO2</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>\n")},
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
                        name = "https.keyStoreFile",
                        description = "The default keystore file path.",
                        defaultValue = "${carbon.home}/conf/security/wso2carbon.jks",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.keyStorePass",
                        description = "The default keystore pass.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.certPass",
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
        Object[] configuration = new HttpSourceUtil().setListenerProperty(listenerUrl, configReader);
        listenerConfig = (ListenerConfiguration) configuration[0];
        context = (String) configuration[1];
        this.configReader = configReader;
        if (registeredListenerURL.containsKey(listenerUrl)) {
            throw new ExecutionPlanCreationException("Listener URL " + listenerUrl + " already connected in "
                    + sourceId);
        } else {
            registeredListenerURL.put(listenerUrl, sourceEventListener);
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
                registeredListenerURL, registeredListenerAuthentication, configReader)) {
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
        if (registeredListenerURL.containsKey(listenerUrl)) {
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
        if (registeredListenerURL.containsKey(listenerUrl)) {
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
        HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                .EMPTY_STRING)).pause();
    }

    @Override
    public void resume() {
        HttpConnectorRegistry.getInstance().getMessageProcessor(listenerUrl.replace(context, HttpConstants
                .EMPTY_STRING)).resume();
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
