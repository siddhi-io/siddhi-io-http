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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test handler for file downloading feature.
 */
public class HttpFileServerListener implements HttpHandler {
    private AtomicBoolean isEventArrived = new AtomicBoolean(false);
    private Headers headers;
    private static final Logger logger = Logger.getLogger(HttpFileServerListener.class);
    private String filePath;

    public HttpFileServerListener() {
        ClassLoader classLoader = getClass().getClassLoader();
        filePath = classLoader.getResource("files").getFile();
    }
    
    @Override
    public void handle(HttpExchange event) throws IOException {
        // Get the paramString form the request
        String line;
        headers = event.getRequestHeaders();
        InputStream inputStream = event.getRequestBody();
        // initiating
        File file = new File(filePath + File.separator + "testFile.txt");
        InputStream fileInputStream = new FileInputStream(file);

        logger.info("Event Arrived");

        byte[] response = IOUtils.toByteArray(fileInputStream);
        event.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
        event.getResponseBody().write(response);
        inputStream.close();
        event.close();
        isEventArrived.set(true);
    }
}
