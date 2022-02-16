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
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.extension.io.http.sink.util.HttpServerListener;
import io.siddhi.extension.io.http.sink.util.HttpServerListenerHandler;
import io.siddhi.extension.map.xml.sinkmapper.XMLSinkMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for multiple event sink synchronously.
 */
public class HttpMultipleEventSinkTestCase {
    private static final Logger logger = (Logger) LogManager.getLogger(HttpMultipleEventSinkTestCase.class);

    /**
     * Test cases for multiple event sink synchronously.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPMultipleEvents() throws Exception {
        logger.info("Creating test for multiple event sink synchronously.");
        final TestAppender appender = new TestAppender("TestAppender", null);
        Logger logger = (Logger) LogManager.getLogger(HttpServerListener.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);


        String inStreamDefinition1 = "Define stream FooStreamA (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}'," +
                "headers='{{headers}}',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
                + "Define stream BarStreamA (message String,method String,headers String);";
        String query1 = ("@info(name = 'queryA') " +
                "from FooStreamA "
                + "select message,method,headers "
                + "insert into BarStreamA;"
        );

        String inStreamDefinition2 = "Define stream FooStreamB (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}'," +
                "headers='{{headers}}',"
                + "@map(type='xml', "
                + "@payload('{{message}}'))) "
                + "Define stream BarStreamB (message String,method String,headers String);";
        String query2 = ("@info(name = 'queryB') " +
                "from FooStreamB "
                + "select message,method,headers "
                + "insert into BarStreamB;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition1 +
                inStreamDefinition2 + query1 + query2);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStreamA");
        InputHandler fooStream2 = siddhiAppRuntime.getInputHandler("FooStreamB");
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        siddhiAppRuntime.start();
        String event1 = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        String event2 = "<events>"
                + "<event>"
                + "<symbol>IFS</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{event1, "POST", "'Name:John','Age:23'"});
        fooStream2.send(new Object[]{event2, "POST", "'Name:John','Age:23'"});
        Thread.sleep(1000);
        final List<String> loggedEvents = ((TestAppender) logger.getAppenders().
                get("TestAppender")).getLog();
        List<String> logMessages = new ArrayList<>();
        for (String logEvent : loggedEvents) {
            String message = String.valueOf(logEvent);
            if (message.contains(":")) {
                message = message.split(":")[1].trim();
            }
            logMessages.add(message);
        }
        Assert.assertEquals(logMessages.contains(event1), true);
        Assert.assertEquals(logMessages.contains(event2), true);
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
        logger.removeAppender(appender);
    }

    @Plugin(name = "TestAppender",
            category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
    private static class TestAppender extends AbstractAppender {

        private final List<String> log = new ArrayList<>();

        public TestAppender(String name, Filter filter) {

            super(name, filter, null);
        }

        @PluginFactory
        public static TestAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Filter") Filter filter) {

            return new TestAppender(name, filter);
        }

        @Override
        public void append(LogEvent event) {

            log.add(event.getMessage().getFormattedMessage());

        }

        public List<String> getLog() {
            List<String> clone = new ArrayList<>(log);
            log.clear();
            return clone;
        }
    }
}
