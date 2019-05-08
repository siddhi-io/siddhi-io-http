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
package io.siddhi.extension.io.http.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.extension.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import io.siddhi.extension.io.http.sink.util.HttpServerListenerHandler;
import io.siddhi.extension.map.xml.sinkmapper.XMLSinkMapper;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test cases for basic authentication.
 */
public class HttpAuthTestCase {
    private static final Logger log = Logger.getLogger(HttpAuthTestCase.class);

    /**
     * Creating test for publishing events wth basic authentication false.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextAuthFalse() throws Exception {
        log.info(" Creating test for publishing events wth basic authentication false.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition
                + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events with basic authentication true.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPTextAuthFalse")
    public void testHTTPTextMappingBasicAuthTrue() throws Exception {
        log.info("Creating test for publishing events with basic authentication true.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}', basic.auth.username='admin',basic.auth.password='admin',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
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
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events with username but not password.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPTextMappingBasicAuthTrue",
            expectedExceptions = {HttpSinkAdaptorRuntimeException.class, SiddhiAppCreationException.class})
    public void testHTTPTextMappingBasicAuthTrueIncorrectCredential() throws Exception {
        log.info("Creating test for publishing events with basic authentication true.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',headers=" +
                "'{{headers}}',basic.auth.password='admin',"
                + "@map(type='xml',"
                + "@payload" + "('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "Name:John,Age:23"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events without URL.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPTextMappingBasicAuthTrueIncorrectCredential",
            expectedExceptions = {SiddhiAppValidationException.class})
    public void testHTTPWithoutURL() throws Exception {
        log.info("Creating test for publishing events without URL.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',method='{{method}}',headers='{{headers}}',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition
                + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

}
