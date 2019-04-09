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
package org.wso2.extension.siddhi.io.http.sink.util;

import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Http test sever listener for file downloading feature.
 */
public class HttpFileServerListenerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpFileServerListenerHandler.class);
    private HttpFileServerListener fileServerListener;
    private HttpServer server;
    private int port;

    public HttpFileServerListenerHandler(int port) {
        this.fileServerListener = new HttpFileServerListener();
        this.port = port;
    }

    public HttpFileServerListenerHandler(int port, int expectedStatusCode) {
        this.fileServerListener = new HttpFileServerListener(expectedStatusCode / 100);
        this.port = port;
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 5);
            server.createContext("/files", fileServerListener);
            server.start();
        } catch (IOException e) {
            logger.error("Error in creating test server.", e);
        }
    }

    public void shutdown() {
        if (server != null) {
            logger.info("Shutting down");
            server.stop(1);
        }
    }

    public HttpFileServerListener getFileServerListener() {
        return fileServerListener;
    }
}
