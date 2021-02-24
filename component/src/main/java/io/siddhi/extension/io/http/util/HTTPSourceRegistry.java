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
package io.siddhi.extension.io.http.util;

import io.siddhi.extension.io.http.source.HttpCallResponseSource;
import io.siddhi.extension.io.http.source.HttpSSESource;
import io.siddhi.extension.io.http.source.HttpServiceSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result handler.
 */
public class HTTPSourceRegistry {

    private static Map<String, HttpServiceSource> serviceSourceRegistry = new ConcurrentHashMap<>();
    private static Map<String, HttpCallResponseSource> callResponseSourceRegistry = new ConcurrentHashMap<>();
    private static Map<String, HttpSSESource> sseSourceRegistry = new ConcurrentHashMap<>();

    // handle service sources
    public static HttpServiceSource getServiceSource(String sourceId) {
        return serviceSourceRegistry.get(sourceId);
    }

    public static void registerServiceSource(String sourceId, HttpServiceSource source) {
        serviceSourceRegistry.put(sourceId, source);
    }

    public static void removeServiceSource(String sourceId) {
        serviceSourceRegistry.remove(sourceId);
    }

    public static void registerCallResponseSource(String sinkId, String statusCode, HttpCallResponseSource source) {
        callResponseSourceRegistry.put(sinkId + statusCode, source);
    }

    public static void removeCallResponseSource(String sinkId, String statusCode) {
        callResponseSourceRegistry.remove(sinkId + statusCode);
    }

    public static HttpCallResponseSource findAndGetResponseSource(String sinkId, String statusCode) {
        for (HttpCallResponseSource responseSource : callResponseSourceRegistry.values()) {
            if (responseSource.matches(sinkId, statusCode)) {
                return responseSource;
            }
        }
        return null;
    }

    public static void registerSSESource(String streamId, HttpSSESource source) {
        sseSourceRegistry.put(streamId, source);
    }

    public static void removeSSESource(String streamId) {
        sseSourceRegistry.remove(streamId);
    }

    public static HttpSSESource findAndGetSSESource(String streamId) {
        for (HttpSSESource sseSource : sseSourceRegistry.values()) {
            if (sseSource.matches(streamId)) {
                return sseSource;
            }
        }
        return null;
    }

}
