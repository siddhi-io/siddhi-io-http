/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ArrayList<String> getPasswordGrandAccessToken(String url, String trustStorePath, String trustStorePassword,
                                                      String username, String password, String encodedAuth) {
        ArrayList<String> serverInfo = getServerInfo(url);
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
        ArrayList<String> tokenResponse = new ArrayList<>();
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector, Constants.HTTPS_SCHEME,
                serverInfo.get(1), Integer.parseInt(serverInfo.get(0)), HttpConstants.SUFFIX_REFRESH_TOKEN_URL, payload,
                headers);
        String[] responseArray = response.get(1).split(HttpConstants.COMMA_SEPARATOR);
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = responseArray[0].replace(HttpConstants.ACCESS_TOKEN_SPLIT, HttpConstants.EMPTY_STRING)
                    .replace(HttpConstants.INVERTED_COMMA_SEPARATOR, HttpConstants.EMPTY_STRING);
            String refreshToken = responseArray[1].replace(HttpConstants.REFRESH_TOKEN_SPLIT,
                    HttpConstants.EMPTY_STRING).replace(HttpConstants.INVERTED_COMMA_SEPARATOR,
                    HttpConstants.EMPTY_STRING);
            tokenResponse.add(response.get(0));
            tokenResponse.add(accessToken);
            tokenResponse.add(refreshToken);
        } else {
            tokenResponse.add(response.get(0));
        }
        return tokenResponse;

    }

    public ArrayList<String> getRefreshGrandAccessToken(String url, String trustStorePath, String trustStorePassword,
                                                     String encodedAuth, String refreshToken) {
        ArrayList<String> serverInfo = getServerInfo(url);
        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        HttpClientConnector httpClientConnector = factory
                .createHttpClientConnector(new HashMap<>(), getSenderConfigurationForHttp(trustStorePath,
                        trustStorePassword));
        ArrayList<String> tokenResponse = new ArrayList<>();
        final Map<String, String> refreshTokenBody = new HashMap<>();
        Map<String, String> headers = setHeaders(encodedAuth);
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_REFRESHTOKEN);
        refreshTokenBody.put(HttpConstants.GRANT_REFRESHTOKEN, refreshToken);
        String payload = getPayload(refreshTokenBody);
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector, Constants.HTTP_SCHEME,
                serverInfo.get(1), Integer.parseInt(serverInfo.get(0)), HttpConstants.SUFFIX_NEW_ACCESS_TOKEN_URL,
                payload, headers);
        String[] responseArray = response.get(1).split(HttpConstants.COMMA_SEPARATOR);
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String token = responseArray[0].replace(HttpConstants.ACCESS_TOKEN_SPLIT, HttpConstants.EMPTY_STRING)
                    .replace(HttpConstants.INVERTED_COMMA_SEPARATOR, HttpConstants.EMPTY_STRING);
            String newRefreshToken = responseArray[1].replace(HttpConstants.REFRESH_TOKEN_SPLIT,
                    HttpConstants.EMPTY_STRING).replace(HttpConstants.INVERTED_COMMA_SEPARATOR,
                    HttpConstants.EMPTY_STRING);
            tokenResponse.add(response.get(0));
            tokenResponse.add(token);
            tokenResponse.add(newRefreshToken);
            return tokenResponse;
        } else if (statusCode == HttpConstants.AUTHENTICATION_FAIL_CODE
                || statusCode == HttpConstants.PERSISTENT_ACCESS_FAIL_CODE) {
            return getClientGrandAccessToken(url, trustStorePath, trustStorePassword, encodedAuth);
        } else {
            tokenResponse.add(response.get(0));
            return tokenResponse;
        }
    }

    public ArrayList<String> getClientGrandAccessToken(String url, String trustStorePath, String trustStorePassword,
                                                    String encodedAuth) {
        String token;
        ArrayList<String> serverInfo = getServerInfo(url);
        HttpWsConnectorFactory factory = new DefaultHttpWsConnectorFactory();
        HttpClientConnector httpClientConnector = factory
                .createHttpClientConnector(new HashMap<>(), getSenderConfigurationForHttp(trustStorePath,
                        trustStorePassword));
        ArrayList<String> tokenResponse = new ArrayList<>();
        final Map<String, String> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_CLIENTTOKEN);
        String payload = getPayload(refreshTokenBody);
        Map<String, String> headers = setHeaders(encodedAuth);
        ArrayList<String> response = HttpRequest.sendPostRequest(httpClientConnector, Constants.HTTP_SCHEME,
                serverInfo.get(1), Integer.parseInt(serverInfo.get(0)), HttpConstants.SUFFIX_NEW_ACCESS_TOKEN_URL,
                payload, headers);
        String[] responseArray = response.get(1).split(HttpConstants.COMMA_SEPARATOR);
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            token = responseArray[0].replace(HttpConstants.ACCESS_TOKEN_SPLIT, HttpConstants.EMPTY_STRING)
                    .replace(HttpConstants.INVERTED_COMMA_SEPARATOR, HttpConstants.EMPTY_STRING);
            tokenResponse.add(response.get(0));
            tokenResponse.add(token);
            tokenResponse.add(HttpConstants.EMPTY_STRING);
        } else {
            tokenResponse.add(response.get(0));
        }
        return tokenResponse;
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
            LOG.error("Unable to encode the message: " + e);
            return "Unable to encode the message: " + e;
        }
    }

    private static String getPayload(Map<String, String> refreshTokenBody) {
        return refreshTokenBody.entrySet().stream()
                .map(p -> encodeMessage(p.getKey()) + "=" + encodeMessage(p.getValue()))
                .reduce("", (p1, p2) -> p1 + "&" + p2);
    }

    private static ArrayList<String> getServerInfo(String url) {
        ArrayList<String> serverInfo = new ArrayList<>();
        String splitUrl[] = url.split(HttpConstants.PROTOCOL_HOST_SEPARATOR);
        String splitByColon[] = splitUrl[1].split(HttpConstants.PORT_HOST_SEPARATOR);
        String serverPorts[] = splitByColon[1].split(HttpConstants.PORT_CONTEXT_SEPARATOR);
        serverInfo.add(Integer.valueOf(serverPorts[0]).toString());
        serverInfo.add(splitByColon[0]);
        return serverInfo;
    }

    private static HashMap<String, String> setHeaders(String encodedAuth) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.AUTHORIZATION_HEADER, encodedAuth);
        headers.put(HttpConstants.HTTP_CONTENT_TYPE,
                String.valueOf(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED));
        return headers;
    }
}
