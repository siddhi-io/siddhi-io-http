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
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorController;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
class HttpConnectorRegistry {
    private static final Logger log = Logger.getLogger(HttpConnectorRegistry.class);
    private static HttpConnectorRegistry instance = new HttpConnectorRegistry();
    private final Map<String, HTTPServerConnector> registeredServerConnectors = new ConcurrentHashMap<>();
    private HttpMessageProcessor httpMessageProcessor = new HttpMessageProcessor();

    private HttpConnectorRegistry() {
    }

    static HttpConnectorRegistry getInstance() {
        return instance;
    }

    /**
     * Returns the server connector instance associated with the given protocol.
     *
     * @param baseURL the identifier of the server connector.
     * @return server connector instance.
     */
    HTTPServerConnector getServerConnector(String baseURL) {
        return registeredServerConnectors.get(HttpSourceUtil.getPort(baseURL));
    }

    /**
     * return the boolean value to indicate creation of new server connector is successful or not.
     *
     * @param listenerUrl                      the listener full url.
     * @param sourceId                         source unique Id .
     * @param listenerConfig                   set of listeners configurations.
     */
    void createServerConnector(String listenerUrl, String sourceId,
                               ListenerConfiguration listenerConfig, ConfigReader
                                       sourceConfigReader) {
        String port = HttpSourceUtil.getPort(listenerUrl);
        synchronized (registeredServerConnectors) {
            if (!registeredServerConnectors.containsKey(port)) {
                TransportsConfiguration configuration = new TransportsConfiguration();
                configuration.setTransportProperties(new HttpSourceUtil().getTransportConfigurations
                        (sourceConfigReader));
                configuration.setListenerConfigurations(new HashSet<>(Collections.singletonList(listenerConfig)));
                ServerConnectorController serverConnectorController = new ServerConnectorController(configuration);
                serverConnectorController.start();
                HTTPServerConnector httpServerConnector = new HTTPServerConnector(port);
                httpServerConnector.setMessageProcessor(this.httpMessageProcessor);
                httpServerConnector.setListenerConfiguration(listenerConfig);
                httpServerConnector.setServerConnectorController(serverConnectorController);
                try {
                    httpServerConnector.init();
                    httpServerConnector.start();
                    registeredServerConnectors.put(port, httpServerConnector);
                    log.info("Http server connector is started on port '" + port + "'");
                } catch (ServerConnectorException e) {
                    throw new HttpSourceAdaptorRuntimeException("Failed to initialized server for URL " + listenerUrl +
                            " in " + sourceId + "connection refuse in " + sourceId, e);
                }
            }

        }
    }

    /**
     * destroy the already started listener.
     *
     * @param listenerUrl the listener full url.
     */
    void clearServerConnector(String listenerUrl) {
        Boolean isContainedAnotherDependentListener = false;
        HttpSourceListener httpSourceListener = HttpSource.getRegisteredSourceListenersMap().get
                (HttpSourceUtil.getSourceListenerKey(listenerUrl));
        if (httpSourceListener != null) {
            httpSourceListener.disconnect();
            HttpSource.getRegisteredSourceListenersMap().remove(HttpSourceUtil
                    .getSourceListenerKey(listenerUrl));
            String port = HttpSourceUtil.getPort(listenerUrl);
            if (registeredServerConnectors.containsKey(port)) {
                for (String url : HttpSource.getRegisteredSourceListenersMap().keySet()) {
                    if (url.contains(port)) {
                        isContainedAnotherDependentListener = true;
                    }
                }
                if (!isContainedAnotherDependentListener) {
                    ServerConnector serverConnector = registeredServerConnectors.remove(port);
                    if (serverConnector != null) {
                        try {
                            serverConnector.stop();
                        } catch (ServerConnectorException e) {
                            log.error("Failed to shutdown server connector for port " + port);
                        }
                    }
                }
            }
        }
    }
}
