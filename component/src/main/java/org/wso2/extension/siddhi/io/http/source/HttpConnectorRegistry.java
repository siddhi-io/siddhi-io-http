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
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
class HttpConnectorRegistry {
    private static HttpConnectorRegistry instance = new HttpConnectorRegistry();
    private final Logger log = Logger.getLogger(HttpConnectorRegistry.class);
    private final Map<String, HTTPServerConnector> serverConnectorMap = new ConcurrentHashMap<>();
    private final Map<String, HttpSourceListener> sourceListenersMap = new ConcurrentHashMap<>();
    private HttpMessageProcessor httpMessageProcessor = new HttpMessageProcessor();
    private ServerConnectorController serverConnectorController;

    private HttpConnectorRegistry() {}

    /**
     * Get HttpConnectorRegistry instance.
     *
     * @return HttpConnectorRegistry instance
     */
    static HttpConnectorRegistry getInstance() {
        return instance;
    }

    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    Map<String, HttpSourceListener> getSourceListenersMap() {
        return this.sourceListenersMap;
    }

    /**
     * Get the Server Connector Map.
     *
     * @return ServerConnectorMap
     */
    Map<String, HTTPServerConnector> getServerConnectorMap() {
        return serverConnectorMap;
    }

    /**
     * Register new source listener.
     *
     * @param sourceEventListener the source event listener.
     * @param listenerUrl the listener url.
     * @param workerThread the worker thread count of siddhi level thread pool executor.
     * @param isAuth the authentication is required for source listener.
     */
    void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl, int
            workerThread, Boolean isAuth , String[] requestedTransportPropertyNames) {
        synchronized (this) {
            String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl);
            HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                    new HttpSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener
                            , requestedTransportPropertyNames));
            if (httpSourceListener != null) {
                throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected.");
            }
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param listenerUrl the listener url
     */
    void unregisterSourceListener(String listenerUrl) {
        String key = HttpSourceUtil.getSourceListenerKey(listenerUrl);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.remove(key);
        if (httpSourceListener != null) {
            httpSourceListener.disconnect();
        }
    }

    /**
     * Initialize and start the server connector controller.
     *
     * @param sourceConfigReader the siddhi source config reader.
     */
    void initHttpServerConnector(ConfigReader sourceConfigReader) {
        if (this.serverConnectorController == null) {
            TransportsConfiguration configuration = new TransportsConfiguration();
            configuration.setTransportProperties(new HttpSourceUtil().getTransportConfigurations
                    (sourceConfigReader));
            this.serverConnectorController = new ServerConnectorController(configuration);
            this.serverConnectorController.start();
        }
    }

    /**
     * Stop server connector controller.
     */
    void stopHttpServerConnectorController() {
        synchronized (this) {
            if ((this.sourceListenersMap.isEmpty()) && (serverConnectorController != null)) {
                this.serverConnectorController.stop();
                this.serverConnectorController = null;
            }
        }
    }

    /**
     * return the boolean value to indicate creation of new server connector is successful or not.
     *
     * @param listenerUrl                      the listener full url.
     * @param sourceId                         source unique Id .
     * @param listenerConfig                   set of listeners configurations.
     */
    void registerServerConnector(String listenerUrl, String sourceId,
                                 ListenerConfiguration listenerConfig) {
        String port = HttpSourceUtil.getPort(listenerUrl);
        synchronized (this) {
            if (!this.serverConnectorMap.containsKey(port)) {
                HTTPServerConnector httpServerConnector = new HTTPServerConnector(port);
                httpServerConnector.setMessageProcessor(this.httpMessageProcessor);
                httpServerConnector.setServerConnectorController(this.serverConnectorController);
                httpServerConnector.setListenerConfiguration(listenerConfig);
                try {
                    httpServerConnector.init();
                    httpServerConnector.start();
                    this.serverConnectorMap.put(port, httpServerConnector);
                    log.info("Http server connector is started on port '" + port + "'");
                } catch (ServerConnectorException e) {
                    throw new HttpSourceAdaptorRuntimeException("Failed to initialized server for URL " + listenerUrl +
                            " in " + sourceId + "connection refuse in " + sourceId, e);
                }
            }
        }
    }

    /**
     * Register the new server connector.
     *
     * @param listenerUrl the listener url
     */
    void unregisterServerConnector(String listenerUrl) {
        Boolean isContainedAnotherDependentListener = false;
        String port = HttpSourceUtil.getPort(listenerUrl);
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl);
        synchronized (this) {
            if (this.serverConnectorMap.containsKey(port)) {
                for (String url : this.sourceListenersMap.keySet()) {
                    if ((url.contains(port) && !(url.contains(listenerKey)))) {
                        isContainedAnotherDependentListener = true;
                    }
                }
                if (!isContainedAnotherDependentListener) {
                    ServerConnector serverConnector = this.serverConnectorMap.remove(port);
                    if (serverConnector != null) {
                        try {
                            serverConnector.stop();
                            log.info("Server connector for port '" + port + "' has successfully shutdown.");
                        } catch (ServerConnectorException e) {
                            log.error("Failed to shutdown server connector for port " + port);
                        }
                    }
                }
            }
        }
    }
}
