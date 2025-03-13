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
package io.siddhi.extension.io.http.sink.updatetoken;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.siddhi.extension.io.http.util.HttpConstants;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.wso2.transport.http.netty.contractimpl.common.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * {@code HttpsClient} Handle the HTTP client.
 */
public class HttpsClient {
    private static final Logger LOG = LogManager.getLogger(HttpsClient.class);
    private AccessTokenCache accessTokenCache = AccessTokenCache.getInstance();

    private static String encodeMessage(Object s) {
        try {
            return URLEncoder.encode((String) s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to encode the message while generating new access token: {}", String.valueOf(e));
            return HttpConstants.EMPTY_STRING;
        }
    }

    private static String getPayload(Map<String, String> refreshTokenBody) {
        return refreshTokenBody.entrySet().stream()
                .map(p -> encodeMessage(p.getKey()) + "=" + encodeMessage(p.getValue()))
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");
    }

    private static HashMap<String, String> setHeaders(String encodedAuth) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.AUTHORIZATION_HEADER, encodedAuth);
        headers.put(HttpConstants.HTTP_CONTENT_TYPE,
                String.valueOf(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED));
        return headers;
    }

    private static OkHttpClient getOkHttpClient(String keyStorePath, String keyStorePassword, String keyPassword,
                                                String trustStorePath, String trustStorePassword,
                                                boolean hostnameVerificationEnabled) {
        KeyStore keyStore;
        KeyStore trustStore;
        try {
            keyStore = readKeyStore(keyStorePath, keyStorePassword);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());

            trustStore = readKeyStore(trustStorePath, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());

            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory(),
                            (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
            if (!hostnameVerificationEnabled) {
                builder.hostnameVerifier((hostname, session) -> true);
            }
            return builder.build();
        } catch (IOException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException
                 | KeyManagementException e) {
            LOG.error("Error occurred while initializing the http client, Returning normal client", e);
            return new OkHttpClient();
        }
    }

    private static KeyStore readKeyStore(String trustStorePath, String trustStorePassword)
            throws IOException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // Get user password and file input stream
        char[] password = trustStorePassword.toCharArray();
        FileInputStream fis = null;
        try {
            File file = new File(Util.substituteVariables(trustStorePath));
            fis = new FileInputStream(file);
            ks.load(fis, password);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            LOG.error("{}", e.getMessage(), e);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }

    public void getPasswordGrantAccessToken(String tokenUrl, String keyStorePath, String keyStorePassword,
                                            String keyPassword, String trustStorePath, String trustStorePassword,
                                            String username, String password, String encodedAuth, String consumerKey,
                                            String consumerSecret, String oAuth2Scope,
                                            boolean hostnameVerificationEnabled) {
        final Map<String, String> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_PASSWORD);
        refreshTokenBody.put(HttpConstants.USERNAME, username);
        refreshTokenBody.put(HttpConstants.PASSWORD, password);
        if (!HttpConstants.EMPTY_STRING.equals(oAuth2Scope)) {
            refreshTokenBody.put(HttpConstants.OAUTH2_SCOPE_PARAMETER_KEY, oAuth2Scope);
        }
        if (!HttpConstants.EMPTY_STRING.equals(consumerKey)) {
            refreshTokenBody.put(HttpConstants.OAUTH_CLIENT_ID, consumerKey);
        }
        if (!HttpConstants.EMPTY_STRING.equals(consumerSecret)) {
            refreshTokenBody.put(HttpConstants.OAUTH_CLIENT_SECRET, consumerSecret);
        }

        OkHttpClient client =
                getOkHttpClient(keyStorePath, keyStorePassword, keyPassword, trustStorePath, trustStorePassword,
                        hostnameVerificationEnabled);
        Map<String, String> headers = setHeaders(encodedAuth);
        List<String> response = HttpRequest.getResponse(tokenUrl, encodedAuth, getPayload(refreshTokenBody), client,
                headers);
        client.dispatcher().executorService().shutdown();
        JSONObject jsonObject = new JSONObject(response.get(1));
        int statusCode = Integer.parseInt(response.get(0));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = jsonObject.getString(HttpConstants.ACCESS_TOKEN);
            accessTokenCache.setAccessToken(encodedAuth, HttpConstants.BEARER + accessToken);
            String newRefreshToken = jsonObject.getString(HttpConstants.REFRESH_TOKEN);
            if (newRefreshToken != null) {
                accessTokenCache.setRefreshtoken(encodedAuth, newRefreshToken);
            }
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        } else {
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        }
    }

    public void getRefreshGrantAccessToken(String url, String keyStorePath, String keyStorePassword, String keyPassword,
                                           String trustStorePath, String trustStorePassword, String encodedAuth,
                                           String refreshToken, String oauthUsername, String oauthUserPassword,
                                           String bodyConsumerKey, String bodyConsumerSecret, String oauth2Scope,
                                           boolean hostnameVerificationEnabled) {
        final Map<String, String> refreshTokenBody = new HashMap<>();
        Map<String, String> headers = setHeaders(encodedAuth);
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_REFRESHTOKEN);
        refreshTokenBody.put(HttpConstants.GRANT_REFRESHTOKEN, refreshToken);
        OkHttpClient client =
                getOkHttpClient(keyStorePath, keyStorePassword, keyPassword, trustStorePath, trustStorePassword,
                        hostnameVerificationEnabled);
        List<String> response = HttpRequest.getResponse(url, encodedAuth, getPayload(refreshTokenBody), client,
                headers);
        client.dispatcher().executorService().shutdown();
        int statusCode = Integer.parseInt(response.get(0));
        JSONObject jsonObject = new JSONObject(response.get(1));
        if (statusCode == HttpConstants.SUCCESS_CODE) {
            String accessToken = jsonObject.getString(HttpConstants.ACCESS_TOKEN);
            String newRefreshToken = jsonObject.getString(HttpConstants.REFRESH_TOKEN);
            accessTokenCache.setAccessToken(encodedAuth, HttpConstants.BEARER + accessToken);
            accessTokenCache.setRefreshtoken(encodedAuth, newRefreshToken);
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        } else if ((statusCode == HttpConstants.AUTHENTICATION_FAIL_CODE
                || statusCode == HttpConstants.PERSISTENT_ACCESS_FAIL_CODE) &&
                (!HttpConstants.EMPTY_STRING.equals(oauthUsername) &&
                        !HttpConstants.EMPTY_STRING.equals(oauthUserPassword))) {
            getPasswordGrantAccessToken(url, keyStorePath, keyStorePassword, keyPassword, trustStorePath,
                    trustStorePassword, oauthUsername, oauthUserPassword, encodedAuth, bodyConsumerKey,
                    bodyConsumerSecret, oauth2Scope, hostnameVerificationEnabled);
        } else if (statusCode == HttpConstants.AUTHENTICATION_FAIL_CODE
                || statusCode == HttpConstants.PERSISTENT_ACCESS_FAIL_CODE) {
            getClientGrantAccessToken(url, keyStorePath, keyStorePassword, keyPassword, trustStorePath,
                    trustStorePassword, encodedAuth, hostnameVerificationEnabled);
        } else {
            accessTokenCache.setResponseCode(encodedAuth, statusCode);
        }
    }

    public void getClientGrantAccessToken(String url, String keyStorePath, String keyStorePassword, String keyPassword,
                                          String trustStorePath, String trustStorePassword, String encodedAuth,
                                          boolean hostnameVerificationEnabled) {
        final Map<String, String> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put(HttpConstants.GRANT_TYPE, HttpConstants.GRANT_CLIENTTOKEN);
        OkHttpClient client =
                getOkHttpClient(keyStorePath, keyStorePassword, keyPassword, trustStorePath, trustStorePassword,
                        hostnameVerificationEnabled);
        Map<String, String> headers = setHeaders(encodedAuth);
        List<String> response = HttpRequest.getResponse(url, encodedAuth, getPayload(refreshTokenBody), client,
                headers);
        client.dispatcher().executorService().shutdown();
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
}
