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
package org.wso2.extension.siddhi.io.http.source;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.source.util.HttpTestUtil;
import org.wso2.extension.siddhi.map.xml.sourcemapper.XmlSourceMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test case for HTTPS protocol.
 */
public class HttpsSSLSourceTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(HttpsSSLSourceTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Creating test for publishing events with https protocol.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransport() throws Exception {
        logger.info("Creating test for publishing events with https protocol.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");
        masterConfigs.put("source.http.keyStorePassword", "wso2carbon");
        masterConfigs.put("source.http.certPassword", "wso2carbon");
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs , null);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml'), receiver.url='https://localhost:8005"
                + "/endpoints/RecPro')"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;"
                );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        String event1 = "<events>"
                            + "<event>"
                                + "<name>John</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event2 = "<events>"
                            + "<event>"
                                + "<name>Mike</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpsPublishEvent(event1);
        HttpTestUtil.httpsPublishEvent(event2);
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with https protocol with invalid keystore.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidKeyStore() throws Exception {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        logger.info("Creating test for publishing events with https protocol with invalid keystore.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/store.jks");
        masterConfigs.put("source.http.keyStorePassword", "wso2carbon");
        masterConfigs.put("source.http.certPassword", "wso2carbon");

        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs , null);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml'), receiver.url='https://localhost:8005/"
                + "endpoints/RecPro')"
                + "define stream inputStream (name string, age int, country string);";
        String query = (
                "@info(name = 'query') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;"
                );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        String event1 = "<events>"
                            + "<event>"
                                + "<name>John</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event2 = "<events>"
                            + "<event>"
                                + "<name>Mike</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpsPublishEvent(event1);
        HttpTestUtil.httpsPublishEvent(event2);
        final List<LoggingEvent> log = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(logEvent.getLevel());
        }
        Assert.assertEquals(logMessages.contains(Level.ERROR), true);
        Assert.assertEquals(Collections.frequency(logMessages, Level.ERROR), 2);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with https protocol with invalid keystore pass.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidKeyStorePass() throws Exception {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        logger.info("Creating test for publishing events with https protocol with invalid keystore pass.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");
        masterConfigs.put("source.http.keyStorePassword", "wso2carbon123");
        masterConfigs.put("source.http.certPassword", "wso2carbon");
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs , null);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml'), receiver.url='https://localhost:8005/" +
                "endpoints/RecPro')"
                + "define stream inputStream (name string, age int, country string);";
        String query = (
                "@info(name = 'query') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        String event1 = "<events>"
                            + "<event>"
                                + "<name>John</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event2 = "<events>"
                            + "<event>"
                                + "<name>Mike</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpsPublishEvent(event1);
        HttpTestUtil.httpsPublishEvent(event2);
        final List<LoggingEvent> log = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(logEvent.getLevel());
        }
        Assert.assertEquals(logMessages.contains(Level.ERROR), true);
        Assert.assertEquals(Collections.frequency(logMessages, Level.ERROR), 2);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with https protocol with invalid cert pass.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidCertPassword() throws Exception {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        logger.info(" Creating test for publishing events with https protocol with invalid cert pass.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");
        masterConfigs.put("source.http.https.keystore,password", "wso2carbon");
        masterConfigs.put("source.http.certPassword", "wso2carbon123");
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs , null);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), receiver.url='https://localhost:" +
                "8005/endpoints/RecPro' )"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;"
                );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        String event1 = "<events>"
                            + "<event>"
                                + "<name>John</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event2 = "<events>"
                            + "<event>"
                                + "<name>Mike</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpsPublishEvent(event1);
        HttpTestUtil.httpsPublishEvent(event2);
        final List<LoggingEvent> log = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(logEvent.getLevel());
        }
        Assert.assertEquals(logMessages.contains(Level.ERROR), true);
        Assert.assertEquals(Collections.frequency(logMessages, Level.ERROR), 2);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    private class TestAppender extends AppenderSkeleton {
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
            return new ArrayList<LoggingEvent>(log);
        }
    }
}
