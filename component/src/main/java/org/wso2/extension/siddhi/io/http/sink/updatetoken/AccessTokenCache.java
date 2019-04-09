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
package org.wso2.extension.siddhi.io.http.sink.updatetoken;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code AccessTokenCache} Handle the access token caching.
 */
public class AccessTokenCache {
    private static Map<String, String> accessToken;
    private static Map<String, String> refreshToken;
    private static Map<String, Integer> responseCode;

    private static AccessTokenCache accessTokenCache;

    private AccessTokenCache() {
    }

    public static synchronized AccessTokenCache getInstance() {
        if (accessTokenCache == null) {
            accessTokenCache = new AccessTokenCache();
            accessToken = new HashMap<>();
            refreshToken = new HashMap<>();
            responseCode = new HashMap<>();
            return accessTokenCache;
        } else {
            return accessTokenCache;
        }
    }

    public void setAccessToken(String key, String value) {
        accessToken.put(key, value);
    }

    public void setRefreshtoken(String key, String value) {
        refreshToken.put(key, value);
    }

    public void setResponseCode(String key, int value) {
        responseCode.put(key, value);
    }

    public int getResponseCode(String key) {
        return responseCode.get(key);
    }

    public String getAccessToken(String key) {
        return accessToken.get(key);
    }

    public String getRefreshtoken(String key) {
        return refreshToken.get(key);
    }

    public boolean checkAvailableKey(String value) {
        return accessToken.containsKey(value);
    }

    public boolean checkRefreshAvailableKey(String value) {
        return refreshToken.containsKey(value);
    }
}
