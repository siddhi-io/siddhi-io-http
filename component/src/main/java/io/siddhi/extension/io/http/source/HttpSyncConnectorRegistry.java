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
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

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
     * @param siddhiAppName
     */
    protected void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl,
                                          int workerThread, Boolean isAuth, String[] requestedTransportPropertyNames,
                                          String sourceId, String siddhiAppName) {

        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSyncSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener,
                        requestedTransportPropertyNames, sourceId, siddhiAppName));
        if (httpSourceListener != null) {
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    protected void setConnectorListeners(ServerConnectorFuture connectorFuture, String serverConnectorId,
                                         ConnectorStartupSynchronizer startupSyncer) {

        connectorFuture.setHttpConnectorListener(new HTTPSyncConnectorListener());
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId));
    }
}
