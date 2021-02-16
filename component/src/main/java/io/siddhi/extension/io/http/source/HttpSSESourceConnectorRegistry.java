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

package io.siddhi.extension.io.http.source;

import io.siddhi.core.exception.SiddhiAppCreationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpSSESourceConnectorRegistry} The code is responsible for maintaining the all active connector listeners
 * for http-sse source.
 */
class HttpSSESourceConnectorRegistry {
    private static HttpSSESourceConnectorRegistry instance = new HttpSSESourceConnectorRegistry();
    private Map<String, HttpSSEResponseConnectorListener> sourceListenersMap = new ConcurrentHashMap<>();

    private HttpSSESourceConnectorRegistry() {

    }

    /**
     * Get HttpSSESourceConnectorRegistry instance.
     *
     * @return HttpSSESourceConnectorRegistry instance
     */
    static HttpSSESourceConnectorRegistry getInstance() {
        return instance;
    }


    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    Map<String, HttpSSEResponseConnectorListener> getSourceListenersMap() {
        return this.sourceListenersMap;
    }


    /**
     * Register a new source listener.
     *
     * @param streamId the sink id for the source
     */
    void registerSourceListener(HttpSSEResponseConnectorListener httpSSEResponseConnectorListener, String streamId) {
        HttpSSEResponseConnectorListener sourceListener =
                this.sourceListenersMap.putIfAbsent((streamId), httpSSEResponseConnectorListener);
        if (sourceListener != null) {
            //TODO: update error message
            throw new SiddhiAppCreationException("There is a connection already established for the source with " +
                    "sink.id : '" + streamId + "' and http.status.code : '" + "statusCode" + "'.");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param streamId the stream id of the source
     * @param siddhiAppName name of the siddhi app
     */
    void unregisterSourceListener(String streamId, String siddhiAppName) {
        HttpSSEResponseConnectorListener httpSourceListener =
                this.sourceListenersMap.get(streamId);
        if (httpSourceListener != null && httpSourceListener.getSiddhiAppName().equals(siddhiAppName)) {
            sourceListenersMap.remove(streamId);
            httpSourceListener.disconnect();
        }
    }
}
