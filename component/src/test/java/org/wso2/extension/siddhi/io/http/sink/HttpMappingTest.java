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
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.output.mapper.json.JsonSinkMapper;
import org.wso2.siddhi.extension.output.mapper.text.TextSinkMapper;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;

/**
 * Test case for mapping type.
 */
public class HttpMappingTest {
    private static final Logger log = Logger.getLogger(HttpMappingTest.class);

    /**
     * Creating test for publishing events with XML mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingXML() throws Exception {
        log.info("Creating test for publishing events with XML mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
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
        fooStream.send(new Object[]{payload, "GET", "Name:John#Age:23"});
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
     * Creating test for publishing events with JSON mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingJson() throws Exception {

        log.info("Creating test for publishing events with JSON mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", JsonSinkMapper.class);

        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{
                "{\"event\""
                        + ":{\"symbol\":\"WSO2\","
                        + "\"price\":55.6,"
                        + "\"volume\":100"
                        + "}}"
                , "GET", "Name:John#Age:23"});
        while (!lst.getServerListener().iaMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData,
                "{\"event\""
                + ":{\"symbol\":\"WSO2\","
                + "\"price\":55.6,"
                + "\"volume\":100"
                + "}}\n");
        lst.shutdown();
        executionPlanRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with TEXT mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingText() throws Exception {

        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("text-output-mapper", TextSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{"WSO2,55.6,100", "GET", "Name:John#Age:23"});
        while (!lst.getServerListener().iaMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, "WSO2,55.6,100\n");
        lst.shutdown();
        executionPlanRuntime.shutdown();
    }

}
