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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result handler
 */
public class HTTPSourceRegistry {
    private static HTTPSourceRegistry instance = new HTTPSourceRegistry();
    private Map<String, HttpServiceSource> requestSourceRegistry = new ConcurrentHashMap<>();
    private Map<String, List<HttpCallResponseSource>> responseSourceRegistry = new ConcurrentHashMap<>();

    private HTTPSourceRegistry() {
    }

    public static HTTPSourceRegistry getInstance() {
        return instance;
    }

    // handle service sources
    public HttpServiceSource getServiceSource(String sourceId) {
        return requestSourceRegistry.get(sourceId);
    }

    public void registerServiceSource(String sourceId, HttpServiceSource source) {
        requestSourceRegistry.put(sourceId, source);
    }

    public void removeServiceSource(String sourceId) {
        requestSourceRegistry.remove(sourceId);
    }

    // handle call response sources
    public HttpCallResponseSource getCallResponseSource(String sinkId, String statusCode) {
        List<HttpCallResponseSource> sourcesMatchingSinkID = responseSourceRegistry.get(sinkId);
        if (sourcesMatchingSinkID != null) {
            for (HttpCallResponseSource entry: sourcesMatchingSinkID) {
                if (statusCode.equals(entry.getHttpStatusCode())) {
                    return entry;
                }
            }
        }
        return null;
    }

    public  List<HttpCallResponseSource> getCallResponseSourcesMatchingHttpCodeRegex(String sinkId, String statusCode) {
        List<HttpCallResponseSource> sourcesMatchingSinkID = responseSourceRegistry.get(sinkId);
        List<HttpCallResponseSource> result = new LinkedList<>();
        if (sourcesMatchingSinkID != null) {
            for (HttpCallResponseSource entry: sourcesMatchingSinkID) {
                if (statusCode.matches(entry.getHttpStatusCode())) {
                    result.add(entry);
                }
            }
        }
        return result;
    }


    public void registerCallResponseSource(String sinkId, HttpCallResponseSource source) {
        List<HttpCallResponseSource> sourcesMatchingSinkID = responseSourceRegistry.get(sinkId);
        if (sourcesMatchingSinkID == null) {
            List<HttpCallResponseSource> sourcesMatchingSinkIDNew = new LinkedList<>();
            sourcesMatchingSinkIDNew.add(source);
            responseSourceRegistry.put(sinkId, sourcesMatchingSinkIDNew);
        } else {
            sourcesMatchingSinkID.add(source);
        }
    }

    public void removeCallResponseSource(String sinkId, String statusCode) {
        List<HttpCallResponseSource> sourcesMatchingSinkID = responseSourceRegistry.get(sinkId);
        if (sourcesMatchingSinkID != null) {
            for (HttpCallResponseSource entry: sourcesMatchingSinkID) {
                if (statusCode.equals(entry.getHttpStatusCode())) {
                    sourcesMatchingSinkID.remove(entry);
                    return;
                }
            }
        }
    }
}
