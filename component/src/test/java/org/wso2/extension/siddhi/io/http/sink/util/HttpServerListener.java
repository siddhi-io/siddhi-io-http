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
package org.wso2.extension.siddhi.io.http.sink.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Server Listener Manger.
 */
public class HttpServerListener implements HttpHandler {
    private AtomicBoolean isEventArrived = new AtomicBoolean(false);
    private StringBuilder stringBuilder;
    private Headers headers;
    private static final Logger logger = Logger.getLogger(HttpServerListener.class);

    HttpServerListener() {
    }

    @Override
    public void handle(HttpExchange event) throws IOException {
        // Get the paramString form the request
        String line;
        headers = event.getRequestHeaders();
        InputStream is = event.getRequestBody();
        // initiating
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        stringBuilder = new StringBuilder();
        while ((line = in.readLine()) != null) {
            stringBuilder = stringBuilder.append(line).append("\n");
        }
        logger.info("Event Arrived: " + stringBuilder.toString());
        isEventArrived.set(true);
    }

    public String getData() {
        String data = stringBuilder.toString();
        isEventArrived = new AtomicBoolean(false);
        return data;
    }

    public Headers getHeaders() {
        return headers;
    }

    public boolean isMessageArrive() {
        return isEventArrived.get();
    }

}
