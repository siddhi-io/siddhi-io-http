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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListener;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for multiple event sink synchronously.
 */
public class HttpMultipleEventSinkTestCase {
    private static final Logger logger = Logger.getLogger(HttpMultipleEventSinkTestCase.class);

    /**
     * Test cases for multiple event sink synchronously.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPMultipleEvents() throws Exception {
        logger.info("Creating test for multiple event sink synchronously.");
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getLogger(HttpServerListener.class);
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
        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(logEvent.getMessage().toString());
        }
        Assert.assertEquals(logMessages.contains("Event Arrived: " + event1 + "\n"), true);
        Assert.assertEquals(logMessages.contains("Event Arrived: " + event2 + "\n"), true);
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }

    private static class TestAppender extends AppenderSkeleton {

        private final List<LoggingEvent> log = new ArrayList<>();

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(final LoggingEvent loggingEvent) {
            log.add(loggingEvent);
        }

        @Override
        public void close() {
        }

        List<LoggingEvent> getLog() {
            return new ArrayList<>(log);
        }
    }
}
