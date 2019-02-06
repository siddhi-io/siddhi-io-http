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

import io.netty.handler.codec.http.HttpHeaderValues;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.sink.util.HttpSinkUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.transport.http.netty.common.Constants.HTTPS_SCHEME;


/**
 * {@code HttpsClient} Handle the HTTP client.
 */
public class HttpsClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsClient.class);
    private AccessTokenCache accessTokenCache = AccessTokenCache.getInstance();
    private Map<String, String> tokenURLProperties;

    public void getPasswordGrantAccessToken(String tokenUrl, String trustStorePath, String trustStorePassword,
                                            String username, String password, String encodedAuth) {
        tokenURLProperties = HttpSinkUtil.getURLProperties(tokenUrl);
        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        HttpClientConnector httpClientConnector = factory
                .createHttpClientConnector(new HashMap<>(), getSenderConfigurationForHttp(trustStorePath,
                        trustStorePassword));
        final Map<String, String> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_PASSWORD);
        refreshTokenBody.put(HttpConstants.USERNAME, username);
        refreshTokenBody.put(HttpConstants.PASSWORD, password);
        String payload = getPayload(refreshTokenBody);
        Map<String, String> headers = setHeaders(encodedAuth);
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector,
                tokenURLProperties.get(HttpConstants.PROTOCOL), tokenURLProperties.get(Constants.HTTP_HOST),
                Integer.parseInt(tokenURLProperties.get(Constants.HTTP_PORT)),
                tokenURLProperties.get(HttpConstants.PATH), payload,
                headers);
        JSONObject jsonObject = new JSONObject(response.get(1));
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = jsonObject.getString(HttpConstants.ACCESS_TOKEN);
            String newRefreshToken = jsonObject.getString(HttpConstants.REFRESH_TOKEN);
            accessTokenCache.setAccessToken(encodedAuth,  HttpConstants.BEARER  + accessToken);
            accessTokenCache.setRefreshtoken(encodedAuth, newRefreshToken);
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        } else {
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        }
    }

    public void getRefreshGrantAccessToken(String url, String trustStorePath, String trustStorePassword,
                                           String encodedAuth, String refreshToken) {
        tokenURLProperties = HttpSinkUtil.getURLProperties(url);
        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        HttpClientConnector httpClientConnector = factory
                .createHttpClientConnector(new HashMap<>(), getSenderConfigurationForHttp(trustStorePath,
                        trustStorePassword));
        final Map<String, String> refreshTokenBody = new HashMap<>();
        Map<String, String> headers = setHeaders(encodedAuth);
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_REFRESHTOKEN);
        refreshTokenBody.put(HttpConstants.GRANT_REFRESHTOKEN, refreshToken);
        String payload = getPayload(refreshTokenBody);
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector,
                tokenURLProperties.get(HttpConstants.PROTOCOL), tokenURLProperties.get(Constants.HTTP_HOST),
                Integer.parseInt(tokenURLProperties.get(Constants.HTTP_PORT)),
                tokenURLProperties.get(HttpConstants.PATH), payload, headers);
        int statusCode = Integer.parseInt(response.get(0));
        JSONObject jsonObject = new JSONObject(response.get(1));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = jsonObject.getString(HttpConstants.ACCESS_TOKEN);
            String newRefreshToken = jsonObject.getString(HttpConstants.REFRESH_TOKEN);
            accessTokenCache.setAccessToken(encodedAuth,  HttpConstants.BEARER  + accessToken);
            accessTokenCache.setRefreshtoken(encodedAuth, newRefreshToken);
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        } else if (statusCode == HttpConstants.AUTHENTICATION_FAIL_CODE
                || statusCode == HttpConstants.PERSISTENT_ACCESS_FAIL_CODE) {
            getClientGrantAccessToken(url, trustStorePath, trustStorePassword, encodedAuth);
        } else {
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        }
    }

    public void getClientGrantAccessToken(String url, String trustStorePath, String trustStorePassword,
                                          String encodedAuth) {
        tokenURLProperties = HttpSinkUtil.getURLProperties(url);
        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        HttpClientConnector httpClientConnector = factory
                .createHttpClientConnector(new HashMap<>(), getSenderConfigurationForHttp(trustStorePath,
                        trustStorePassword));
        final Map<String, String> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_CLIENTTOKEN);
        String payload = getPayload(refreshTokenBody);
        Map<String, String> headers = setHeaders(encodedAuth);
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector,
                tokenURLProperties.get(HttpConstants.PROTOCOL), tokenURLProperties.get(Constants.HTTP_HOST),
                Integer.parseInt(tokenURLProperties.get(Constants.HTTP_PORT)),
                tokenURLProperties.get(HttpConstants.PATH), payload, headers);
        JSONObject jsonObject = new JSONObject(response.get(1));
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = jsonObject.getString(HttpConstants.ACCESS_TOKEN);
            accessTokenCache.setAccessToken(encodedAuth, HttpConstants.BEARER + accessToken);
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        } else {
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        }
    }

    private static SenderConfiguration getSenderConfigurationForHttp(String trustStorePath, String trustStorePassword) {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setTrustStoreFile(trustStorePath);
        senderConfiguration.setTrustStorePass(trustStorePassword);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        senderConfiguration.setHostNameVerificationEnabled(false);
        return senderConfiguration;
    }

    private static String encodeMessage(Object s) {
        try {
            return URLEncoder.encode((String) s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to encode the message while generating new access token: " + e);
            return HttpConstants.EMPTY_STRING;
        }
    }

    private static String getPayload(Map<String, String> refreshTokenBody) {
        return refreshTokenBody.entrySet().stream()
                .map(p -> encodeMessage(p.getKey()) + "=" + encodeMessage(p.getValue()))
                .reduce("", (p1, p2) -> p1 + "&" + p2);
    }

    private static HashMap<String, String> setHeaders(String encodedAuth) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.AUTHORIZATION_HEADER, encodedAuth);
        headers.put(HttpConstants.HTTP_CONTENT_TYPE,
                String.valueOf(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED));
        return headers;
    }
}
