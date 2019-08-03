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
import io.siddhi.extension.io.http.source.HttpServiceSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result handler
 */
public class HTTPSourceRegistry {

    private static Map<String, HttpServiceSource> serviceSourceRegistry = new ConcurrentHashMap<>();
    private static Map<ResponseSourceId, HttpCallResponseSource> callResponseSourceRegistry = new ConcurrentHashMap<>();

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

    // handle response sources
    public static HttpCallResponseSource getCallResponseSource(String sinkId, String statusCode) {
        return callResponseSourceRegistry.get(new ResponseSourceId(sinkId, statusCode));
    }

    public static void registerCallResponseSource(String sinkId, String statusCode, HttpCallResponseSource source) {
        callResponseSourceRegistry.put(new ResponseSourceId(sinkId, statusCode), source);
    }

    public static void removeCallResponseSource(String sinkId, String statusCode) {
        callResponseSourceRegistry.remove(new ResponseSourceId(sinkId, statusCode));
    }

    public static Map<ResponseSourceId, HttpCallResponseSource> getCallResponseSourceRegistry() {
        return callResponseSourceRegistry;
    }
}
