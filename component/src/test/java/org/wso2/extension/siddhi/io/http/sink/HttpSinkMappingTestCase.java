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
import org.wso2.extension.siddhi.map.json.sinkmapper.JsonSinkMapper;
import org.wso2.extension.siddhi.map.text.sinkmapper.TextSinkMapper;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;


/**
 * Test case for mapping type.
 */
public class HttpSinkMappingTestCase {
    private static final Logger log = Logger.getLogger(HttpSinkMappingTestCase.class);

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
     * Creating test for publishing events with JSON mapping.
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPTextMappingXML")
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
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{
                "{\"event\""
                        + ":{\"symbol\":\"WSO2\","
                        + "\"price\":55.6,"
                        + "\"volume\":100"
                        + "}}"
                , "POST", "Name:John,Age:23"});
        while (!lst.getServerListener().isMessageArrive()) {
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
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with TEXT mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPTextMappingJson")
    public void testHTTPTextMappingText() throws Exception {

        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("text-output-mapper", TextSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='text', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{"WSO2,55.6,100", "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, "WSO2,55.6,100\n");
        Thread.sleep(100);
        lst.shutdown();
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with TEXT mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPTextMappingText")
    public void testHTTPTextMappingText2() throws Exception {
        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("text-output-mapper", TextSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (name String, age int,country" +
                " String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='text')) "
                + "Define stream BarStream (name String, age int,country String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select * insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{"WSO2", 55.6, "USA", "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, "name:\"WSO2\",\n" +
                "age:55.6,\n" +
                "country:\"USA\",\n" +
                "method:\"POST\",\n" +
                "headers:\"'Name:John','Age:23'\"\n");
        lst.shutdown();
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with TEXT mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPTextMappingText2")
    public void testHTTPTextMappingText3() throws Exception {

        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',"
                + "@map(type='text', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{"WSO2,55.6,100", "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, "WSO2,55.6,100\n");
        Thread.sleep(100);
        lst.shutdown();
        siddhiAppRuntime.shutdown();
    }
    /**
     * Creating test for publishing events with TEXT mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test (dependsOnMethods = "testHTTPTextMappingText3")
    public void testHTTPTextMappingText4() throws Exception {

        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='POST',"
                + "headers=\"'Name:John','Age:23'\","
                + "@map(type='text')) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        fooStream.send(new Object[]{"WSO2,55.6,100", "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        Assert.assertEquals(eventData, "message:\"WSO2,55.6,100\",\n" +
                "method:\"POST\",\n" +
                "headers:\"'Name:John','Age:23'\"\n");
        Thread.sleep(100);
        lst.shutdown();
        siddhiAppRuntime.shutdown();
    }
}
