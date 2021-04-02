/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.siddhi.extension.io.http.sink;

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.ConnectorStartupSynchronizer;
import io.siddhi.extension.io.http.source.HttpConnectorPortBindingListener;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.log4j.Logger;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for maintaining the connections.
 */
public class SSESyncConnectorRegistry extends SSEConnectorRegistry {

    private static SSESyncConnectorRegistry instance = new SSESyncConnectorRegistry();
    private final Logger log = Logger.getLogger(SSESyncConnectorRegistry.class);
    private Map<String, HttpSSERequestListener> sourceListenersMap = new ConcurrentHashMap<>();

    private SSESyncConnectorRegistry() {

    }

    /**
     * Get HttpConnectorRegistry instance.
     *
     * @return HttpConnectorRegistry instance
     */
    public static SSESyncConnectorRegistry getInstance() {

        return instance;
    }

    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    protected Map<String, HttpSSERequestListener> getSyncSourceListenersMap() {

        return this.sourceListenersMap;
    }

    /**
     * Register new source listener.
     *
     * @param listenerUrl                     the listener url.
     * @param workerThread                    the worker thread count of siddhi level thread pool executor.
     * @param isAuth                          the authentication is required for source listener.
     * @param requestedTransportPropertyNames requested transport property names.
     * @param sourceId                        source Id.
     * @param siddhiAppName
     */
    protected void registerSourceListener(String listenerUrl,
                                          int workerThread, Boolean isAuth, String[] requestedTransportPropertyNames,
                                          String sourceId, String siddhiAppName, SourceMetrics metrics) {
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSSERequestListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSSERequestListener(workerThread, listenerUrl, isAuth,
                        requestedTransportPropertyNames, sourceId, siddhiAppName, metrics));
        if (httpSourceListener != null) {
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param listenerUrl   the listener url
     * @param siddhiAppName
     */
    protected void unregisterSourceListener(String listenerUrl, String siddhiAppName, SourceMetrics metrics) {
        String key = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSSERequestListener httpSourceListener = this.sourceListenersMap.get(key);
        if (httpSourceListener != null && httpSourceListener.getSiddhiAppName().equals(siddhiAppName)) {
            sourceListenersMap.remove(key);
            httpSourceListener.disconnect();
        }
    }

    /**
     * Initialize and start the server connector factory. This should be created at once for siddhi.
     *
     * @param sourceConfigReader the siddhi source config reader.
     */
    protected synchronized void initBootstrapConfigIfFirst(ConfigReader sourceConfigReader) {
        // to make sure it will create only once
        if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory == null)) {
            String bootstrapWorker = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            String bootstrapBoss = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            String bootstrapClient = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            if (!HttpConstants.EMPTY_STRING.equals(bootstrapBoss) && !HttpConstants.EMPTY_STRING.equals
                    (bootstrapWorker)) {
                if (!HttpConstants.EMPTY_STRING.equals(bootstrapClient)) {
                    httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss), Integer
                            .parseInt(bootstrapWorker), Integer.parseInt(bootstrapClient));
                } else {
                    httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss), Integer
                            .parseInt(bootstrapWorker), Integer.parseInt(bootstrapWorker));
                }
            } else {
                httpConnectorFactory = new DefaultHttpWsConnectorFactory();
            }
        }
    }

    /**
     * Stop server connector controller.
     */
    protected void clearBootstrapConfigIfLast() {

        synchronized (this) {
            if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory != null)) {
                this.httpConnectorFactory.shutdownNow();
                this.httpConnectorFactory = null;
            }
        }
    }

    protected void setConnectorListeners(ServerConnectorFuture connectorFuture, String serverConnectorId,
                                         ConnectorStartupSynchronizer startupSyncer, SourceMetrics metrics) {

        connectorFuture.setHttpConnectorListener(new SSESyncConnectorListener());
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId, metrics));
    }
}
