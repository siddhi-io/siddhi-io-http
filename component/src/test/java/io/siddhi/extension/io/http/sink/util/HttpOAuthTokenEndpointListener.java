/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.sink.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Server Listener Manger for TokenEndPoint.
 */
public class HttpOAuthTokenEndpointListener implements HttpHandler {
    private AtomicBoolean isEventArrived = new AtomicBoolean(false);

    public HttpOAuthTokenEndpointListener() {
    }

    @Override
    public void handle(HttpExchange event) throws IOException {
        int responseCode;
        responseCode = HttpURLConnection.HTTP_OK;
        StringBuilder gettingGrantType = new StringBuilder();
        String outputStream;
        String line;
        InputStream grantInputStream = event.getRequestBody();
        BufferedReader grantBufferedReader = new BufferedReader(new InputStreamReader(grantInputStream));
        while ((line = grantBufferedReader.readLine()) != null) {
            gettingGrantType = gettingGrantType.append(line).append("\n");
        }
        if (gettingGrantType.toString().contains("password")) {
            outputStream = "{\"scope\":\"default\",\"token_type\":\"Bearer\",\"expires_in\":3600," +
                    "\"access_token\":\"yyyyy\",\"refresh_token\":\"ppppp\"}";
        } else if (gettingGrantType.toString().contains("refresh_token")) {
            if ("ppppp".contains(gettingGrantType.toString())) {
                outputStream = "{\"scope\":\"default\",\"token_type\":\"Bearer\",\"expires_in\":3600," +
                        "\"access_token\":\"yyyyy\",\"refresh_token\":\"ppppp\"}";
            } else {
                responseCode = HttpURLConnection.HTTP_UNAUTHORIZED;
                outputStream = "{\"access_token\":\"\",\"refresh_token\":\"\"}";
            }
        } else {
            outputStream = "{\"token_type\":\"Bearer\",\"expires_in\":3600,\"access_token\":\"yyyyy\"}";
        }

        InputStream responseInputStream = new ByteArrayInputStream(outputStream.getBytes());
        BufferedReader responseBufferedReader = new BufferedReader(new InputStreamReader(responseInputStream));
        StringBuilder responseString = new StringBuilder();
        while ((line = responseBufferedReader.readLine()) != null) {
            responseString = responseString.append(line);
        }
        byte[] response = responseString.toString().getBytes();
        event.sendResponseHeaders(responseCode, response.length);
        event.getResponseBody().write(response);
        event.close();
        isEventArrived.set(true);
    }

    public boolean isMessageArrive() {
        return isEventArrived.get();
    }
}
