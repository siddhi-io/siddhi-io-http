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
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorController;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
class HttpConnectorRegistry {

    private Map<String, HTTPServerConnector> registeredServerConnectors = new ConcurrentHashMap<>();
    private Map<String, HttpMessageProcessor> registeredMessageProcessors = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> registeredServerConnectorsConfigs = new ConcurrentHashMap<>();
    private static HttpConnectorRegistry instance = new HttpConnectorRegistry();

    private static final Logger log = Logger.getLogger(HttpConnectorRegistry.class);

    private HttpConnectorRegistry() {
    }

    static HttpConnectorRegistry getInstance() {
        return instance;
    }

    /**
     * Returns the message processor instance associated with the given url.
     *
     * @param baseUrl the identifier of the mssage processor.
     * @return server connector instance.
     */
    HttpMessageProcessor getMessageProcessor(String baseUrl) {
        return registeredMessageProcessors.get(baseUrl);

    }

    /**
     * Returns the server connector instance associated with the given protocol.
     *
     * @param baseURL the identifier of the server connector.
     * @return server connector instance.
     */
    HTTPServerConnector getServerConnector(String baseURL) {
        return registeredServerConnectors.get(baseURL);
    }


    /**
     * return the boolean value to indicate creation of new server connector is successful or not.
     *
     * @param listenerUrl           the listener full url.
     * @param context               the listener url context.
     * @param sourceId              source unique Id .
     * @param listenerConfig        set of listeners configurations.
     * @param registeredListenerURL a static map of all registered listeners.
     * @param workerThreadCount     siddhi level thread pool executor thread count.
     * @param serverBootstrapBossThreadCount netty thread pool executor parameter.
     * @param serverBootstrapWorkerThreadCount netty thread pool executor parameter.
     * @return return the boolean value to indicate creation of new server connector is successful or not.
     */
    boolean createServerConnector(String listenerUrl, String context, String sourceId,
                                  ListenerConfiguration listenerConfig,
                                  Map<String, SourceEventListener> registeredListenerURL, Map<SourceEventListener,
            Boolean> registeredListenerAuthentication, ConfigReader sourceConfigReader, String
                                          workerThreadCount, String serverBootstrapWorkerThreadCount, String
                                          serverBootstrapBossThreadCount) {
        String baseUrl = listenerUrl.replace(context, HttpConstants.EMPTY_STRING);
        if (!registeredServerConnectors.containsKey(baseUrl)) {
            TransportsConfiguration configuration = new TransportsConfiguration();
            HttpMessageProcessor httpMessageProcessor = new HttpMessageProcessor(baseUrl, workerThreadCount);
            configuration.setTransportProperties(new HttpSourceUtil().getTransportConfigurations
                    (sourceConfigReader, serverBootstrapBossThreadCount, serverBootstrapWorkerThreadCount));
            configuration.setListenerConfigurations(new HashSet<>(Collections.singletonList(listenerConfig)));
            ServerConnectorController serverConnectorController = new ServerConnectorController(configuration);
            serverConnectorController.start();
            HTTPServerConnector httpServerConnector = new HTTPServerConnector(listenerUrl.replace(context,
                    HttpConstants.EMPTY_STRING));
            httpServerConnector.setMessageProcessor(httpMessageProcessor);
            httpServerConnector.setListenerConfiguration(listenerConfig);
            httpServerConnector.setServerConnectorController(serverConnectorController);
            try {
                httpServerConnector.init();
                httpServerConnector.start();
                Map<String, String> config = new HashMap<>();
                config.put(HttpConstants.WORKER_COUNT, workerThreadCount);
                config.put(HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, serverBootstrapWorkerThreadCount);
                config.put(HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, serverBootstrapBossThreadCount);
                registeredServerConnectorsConfigs.put(baseUrl, config);
                registeredServerConnectors.put(baseUrl, httpServerConnector);
                registeredMessageProcessors.put(baseUrl, httpMessageProcessor);
            } catch (ServerConnectorException e) {
                registeredListenerAuthentication.remove(registeredListenerURL.get(listenerUrl));
                registeredListenerURL.remove(listenerUrl);
                throw new HttpSourceAdaptorRuntimeException("Failed to initialized server for URL " + listenerUrl +
                        " in " + sourceId + "connection refuse in " + sourceId, e);
            }
            return true;
        } else {
            Map configs = registeredServerConnectorsConfigs.get(baseUrl);
            if ((configs.get(HttpConstants.WORKER_COUNT) != workerThreadCount) || (configs.get(HttpConstants.
                    SERVER_BOOTSTRAP_WORKER_GROUP_SIZE) != serverBootstrapWorkerThreadCount) ||
                    (configs.get(HttpConstants.WORKER_COUNT) != serverBootstrapBossThreadCount)) {
                log.error("Listener already exist for host:port " + listenerUrl.replace
                        (context, HttpConstants.EMPTY_STRING) + " But conflict with already existing serve connector " +
                        "which has following configurations " + configs.toString() + ". So source is connected to " +
                        "the server which has those configurations.");
            } else {
                log.info("Listener already exist for host:port " + listenerUrl.replace
                        (context, HttpConstants.EMPTY_STRING) + " no need to register again in " + sourceId);
            }
            return false;
        }
    }

    /**
     * destroy the already started listener.
     *
     * @param registeredListenerURL set of listeners configurations.
     * @param listenerUrl           the listener full url.
     * @param context               the listener url context.
     * @return return the boolean value to indicate destroying of new server connector is successful or not.
     */
    boolean destroyServerConnector(Map<String, SourceEventListener> registeredListenerURL, String listenerUrl,
                                   String context) {
        HttpSource.getRegisteredListenerAuthentication().remove(registeredListenerURL.get(listenerUrl));
        registeredListenerURL.remove(listenerUrl);
        String baseUrl = listenerUrl.replace(context, HttpConstants.EMPTY_STRING);
        if (registeredServerConnectors.containsKey(baseUrl)) {
            for (String url : registeredListenerURL.keySet()) {
                if (url.contains(baseUrl)) {
                    return false;
                }
            }
            registeredMessageProcessors.get(baseUrl).disconnect();
            registeredServerConnectors.get(baseUrl).stop();
            registeredMessageProcessors.remove(baseUrl);
            registeredServerConnectors.remove(baseUrl);
        }
        return true;
    }
}
