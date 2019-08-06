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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active connector listeners for
 * http-response source.
 */
class HttpCallResponseSourceConnectorRegistry {
    private static HttpCallResponseSourceConnectorRegistry instance = new HttpCallResponseSourceConnectorRegistry();
    private Map<String, HttpCallResponseConnectorListener> sourceListenersMap = new ConcurrentHashMap<>();

    private HttpCallResponseSourceConnectorRegistry() {

    }

    /**
     * Get HttpCallResponseSourceConnectorRegistry instance.
     *
     * @return HttpCallResponseSourceConnectorRegistry instance
     */
    static HttpCallResponseSourceConnectorRegistry getInstance() {
        return instance;
    }


    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    Map<String, HttpCallResponseConnectorListener> getSourceListenersMap() {
        return this.sourceListenersMap;
    }


    /**
     * Register new source listener.
     *
     * @param sinkId the sink id for the source
     */
    void registerSourceListener(HttpCallResponseConnectorListener httpResponseSourceListener, String sinkId,
                                String statusCode) {
        HttpCallResponseConnectorListener sourceListener =
                this.sourceListenersMap.putIfAbsent((sinkId + statusCode), httpResponseSourceListener);
        if (sourceListener != null) {
            throw new SiddhiAppCreationException("There is a connection already established for the source with " +
                    "sink.id : '" + sinkId + "' and http.status.code : '" + statusCode + "'.");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param sinkId        the sink id of the source
     * @param siddhiAppName name of the siddhi app
     */
    void unregisterSourceListener(String sinkId, String statusCode, String siddhiAppName) {
        HttpCallResponseConnectorListener httpSourceListener =
                this.sourceListenersMap.get(sinkId + statusCode);
        if (httpSourceListener != null && httpSourceListener.getSiddhiAppName().equals(siddhiAppName)) {
            sourceListenersMap.remove(sinkId + statusCode);
            httpSourceListener.disconnect();
        }
    }
}
