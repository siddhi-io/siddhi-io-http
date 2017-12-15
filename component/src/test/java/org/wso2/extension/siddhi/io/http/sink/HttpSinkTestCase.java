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

import com.sun.net.httpserver.Headers;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Test cases for content type header.
 */
public class HttpSinkTestCase {
    private static final Logger log = Logger.getLogger(HttpSinkTestCase.class);
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
     * Creating test for publishing events without Content-Type header include.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPContentTypeNotIncluded() throws Exception {
        log.info("Creating test for publishing events without Content-Type header include.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                + "from FooStream "
                + "select message,method,headers "
                + "insert into BarStream;"
                );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        ArrayList<String> headerName = new ArrayList<>();
        headerName.add("John");
        LinkedList<String> headerAge = new LinkedList<>();
        headerAge.add("23");
        ArrayList<String> headerContentType = new ArrayList<>();
        headerContentType.add("application/xml");
        Headers headers = lst.getServerListener().getHeaders();
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(expected, eventData);
        Assert.assertEquals(headers.get("Name").toString(), headerName.toString());
        Assert.assertEquals(headers.get("Age").toString(), headerAge.toString());
        Assert.assertEquals(headers.get("Content-Type").toString(), headerContentType.toString());
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events including Content-Type header at header list.
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPContentTypeNotIncluded")
    public void testHTTPContentTypeAtHeaders() throws Exception {
        log.info("Creating test for publishing events including Content-Type header at header list.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') "
                + "from FooStream "
                + "select message,method,headers "
                + "insert into BarStream;"
                );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Content-Type:text'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        ArrayList<String> headerName = new ArrayList<>();
        headerName.add("John");
        LinkedList<String> headerAge = new LinkedList<>();
        headerAge.add("23");
        ArrayList<String> headerContentType = new ArrayList<>();
        headerContentType.add("text");
        Headers headers = lst.getServerListener().getHeaders();
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(expected, eventData);
        Assert.assertEquals(headers.get("Name").toString(), headerName.toString());
        Assert.assertEquals(headers.get("Age").toString(), headerAge.toString());
        Assert.assertEquals(headers.get("Content-Type").toString(), headerContentType.toString());
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

}

