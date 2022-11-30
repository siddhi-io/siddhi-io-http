/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
public class HttpSyncConnectorRegistry extends HttpConnectorRegistry {

    private static HttpSyncConnectorRegistry instance = new HttpSyncConnectorRegistry();
    private Map<String, HttpSyncSourceListener> sourceListenersMap = new ConcurrentHashMap<>();

    private HttpSyncConnectorRegistry() {

    }

    /**
     * Get HttpConnectorRegistry instance.
     *
     * @return HttpConnectorRegistry instance
     */
    public static HttpSyncConnectorRegistry getInstance() {

        return instance;
    }

    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    protected Map<String, HttpSyncSourceListener> getSyncSourceListenersMap() {

        return this.sourceListenersMap;
    }

    /**
     * Register new source listener.
     *
     * @param sourceEventListener             the source event listener.
     * @param listenerUrl                     the listener url.
     * @param workerThread                    the worker thread count of siddhi level thread pool executor.
     * @param isAuth                          the authentication is required for source listener.
     * @param requestedTransportPropertyNames requested transport property names.
     * @param sourceId                        source Id.
     * @param siddhiAppName                   siddhi app name
     * @param metrics                         source metrics
     * @param isWebSub                        is web sub
     */
    protected void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl,
                                          int workerThread, Boolean isAuth, String[] requestedTransportPropertyNames,
                                          String sourceId, String siddhiAppName, SourceMetrics metrics,
                                          boolean isWebSub) {
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSyncSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener,
                        requestedTransportPropertyNames, sourceId, siddhiAppName, metrics, isWebSub));
        if (httpSourceListener != null) {
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param listenerUrl   the listener url
     * @param siddhiAppName siddhi app name
     */
    protected void unregisterSourceListener(String listenerUrl, String siddhiAppName, SourceMetrics metrics) {
        String key = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.get(key);
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

        connectorFuture.setHttpConnectorListener(new HTTPSyncConnectorListener());
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId, metrics));
    }
}
