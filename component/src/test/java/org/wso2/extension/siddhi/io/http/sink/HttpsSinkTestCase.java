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
package org.wso2.extension.siddhi.io.http.sink;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.util.HttpsServerListenerHandler;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Https protocol function tests.
 */
public class HttpsSinkTestCase {
    private static final Logger logger = Logger.getLogger(HttpsSinkTestCase.class);
    public static final String CARBON_HOME = "carbon.home";
    private String payload;
    private String expected;

    @BeforeTest
    public void init() {
        payload = "<events>"
                    + "<event>"
                        + "<symbol>WSO2</symbol>"
                        + "<price>55.645</price>"
                        + "<volume>100</volume>"
                    + "</event>"
                + "</events>";
        expected = "<events>"
                    + "<event>"
                        + "<symbol>WSO2</symbol>"
                        + "<price>55.645</price>"
                        + "<volume>100</volume>"
                    + "</event>"
                + "</events>\n";
    }

    /**
     * Test case for HTTP output publisher.
     */
    private void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty(CARBON_HOME, carbonHome.toString());
        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

    }

    /**
     * Test case for HTTPS output publisher.
     */
    @Test
    public void testHTTPSPublisher() throws Exception {
        setCarbonHome();
        logger.info("Test case for HTTPS output publisher.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='https://localhost:8009/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpsServerListenerHandler lst = new HttpsServerListenerHandler(8009);
        lst.run();
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Test case for HTTPS output publisher.
     */
    @Test (dependsOnMethods = "testHTTPSPublisher")
    public void testHTTPSPublisherCustomClientTrustStorePath() throws Exception {
        setCarbonHome();
        logger.info("Test case for HTTPS output publisher with custom trustore.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='https://localhost:8009/abc',method='{{method}}',"
                + "headers='{{headers}}',client.truststore.path='${carbon.home}/resources/security/client-"
                + "truststore.jks', client.truststore.password='wso2carbon',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
                    );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpsServerListenerHandler lst = new HttpsServerListenerHandler(8009);
        lst.run();
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

}

