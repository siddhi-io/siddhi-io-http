/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *
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
package io.siddhi.extension.io.http.test.osgi.source.util;

import io.netty.handler.codec.http.HttpMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Util class for test cases.
 */
public class TestUtil {
    private static final Logger logger = Logger.getLogger(TestUtil.class);
    private static final String CARBON_HOME = "carbon.home";

    public TestUtil() {
    }

    public void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test", "resources");
        System.setProperty(CARBON_HOME, carbonHome.toString());
        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

    }

    public void httpPublishEventAuthIncorrect(String event, URI baseURI, Boolean auth, String mapping) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = ServerUtil.request(baseURI, "/endpoints/RecPro", HttpMethod.POST.name(), true);
            } catch (IOException e) {
                ServerUtil.handleException("IOException occurred while running the HttpsSSLSourceTestCase", e);
            }
            if (auth) {
                ServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin2")
                                .getBytes()));
            }
            ServerUtil.setHeader(urlConn, "Content-Type", mapping);
            ServerUtil.setHeader(urlConn, "HTTP_METHOD", "POST");
            ServerUtil.writeContent(urlConn, event);
            assert urlConn != null;
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            ServerUtil.handleException("IOException occurred while running the HttpsSSLSourceTestCase", e);
        }
    }

    public void httpPublishEvent(String event, URI baseURI, String path, Boolean auth, String mapping,
                                 String methodType) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = ServerUtil.request(baseURI, path, methodType, true);
            } catch (IOException e) {
                ServerUtil.handleException("IOException occurred while running the HttpsSSLSourceTestCase", e);
            }
            if (auth) {
                byte[] val = ("admin" + ":" + "admin").getBytes("UTF-8");
                ServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes()));
            }
            ServerUtil.setHeader(urlConn, "Content-Type", mapping);
            ServerUtil.setHeader(urlConn, "HTTP_METHOD", methodType);
            ServerUtil.writeContent(urlConn, event);
            assert urlConn != null;
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            ServerUtil.handleException("IOException occurred while running the HttpsSSLSourceTestCase", e);
        }
    }
}
