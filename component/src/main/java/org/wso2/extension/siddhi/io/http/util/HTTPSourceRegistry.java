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
package org.wso2.extension.siddhi.io.http.util;

import org.wso2.extension.siddhi.io.http.source.HttpRequestSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result handler
 */
public class HTTPSourceRegistry {

    private static Map<String, HttpRequestSource> sourceRegistry = new ConcurrentHashMap<>();

    public static HttpRequestSource getSource(String sourceId) {

        return sourceRegistry.get(sourceId);
    }

    public static void registerSource(String sourceId, HttpRequestSource source) {

        sourceRegistry.put(sourceId, source);
    }

    public static void removeSource(String sourceId) {

        sourceRegistry.remove(sourceId);
    }
}
