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
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * test cases for basic authentication.
 */
public class HttpAuthTest {
    private static final Logger log = Logger.getLogger(HttpAuthTest.class);

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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition
                + query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                            + "<event>"
                                + "<symbol>WSO2</symbol>"
                                + "<price>55.645</price>"
                                + "<volume>100</volume>"
                            + "</event>"
                        + "</events>";
        fooStream.send(new Object[]{payload, "GET", "'Name:John','Age:23','Country:USA'"});
        while (!lst.getServerListener().iaMessageArrive()) {
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
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events with basic authentication true.
     *
     * @throws Exception Interrupted exception
     */
    @Test
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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                            + "<event>"
                                + "<symbol>WSO2</symbol>"
                                + "<price>55.645</price>"
                                + "<volume>100</volume>"
                            + "</event>"
                        + "</events>";
        fooStream.send(new Object[]{payload, "GET", "'Name:John','Age:23'"});
        while (!lst.getServerListener().iaMessageArrive()) {
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
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events with username but not password.
     *
     * @throws Exception Interrupted exception
     */
    @Test(expectedExceptions = {HttpSinkAdaptorRuntimeException.class, ExceptionInInitializerError.class})
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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                            + "<event>"
                                + "<symbol>WSO2</symbol>"
                                + "<price>55.645</price>"
                                + "<volume>100</volume>"
                            + "</event>"
                        + "</events>";
        fooStream.send(new Object[]{payload, "GET", "Name:John,Age:23"});
        while (!lst.getServerListener().iaMessageArrive()) {
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
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }

    /**
     * Creating test for publishing events without URL.
     *
     * @throws Exception Interrupted exception
     */
    @Test(expectedExceptions = {ExecutionPlanValidationException.class})
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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition
                + query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                            + "<event>"
                                + "<symbol>WSO2</symbol>"
                                + "<price>55.645</price>"
                                + "<volume>100</volume>"
                            + "</event>"
                        + "</events>";
        fooStream.send(new Object[]{payload, "GET", "'Name:John','Age:23'"});
        while (!lst.getServerListener().iaMessageArrive()) {
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
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }

}
