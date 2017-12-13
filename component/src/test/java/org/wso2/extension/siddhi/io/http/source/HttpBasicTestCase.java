/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.io.http.source;

import org.apache.log4j.AppenderSkeleton;
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
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic test cases for http source functions.
 */
public class HttpBasicTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j
            .Logger.getLogger(HttpBasicTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Creating test for publishing events without URL.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportWithoutURL() throws Exception {
        logger.info(" Creating test for publishing events without URL.");
        URI baseURI = URI.create(String.format("http://%s:%d", "0.0.0.0", 8280));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition =  "@App:name('TestSiddhiApp')"  +
                "@source(type='http', @map(type='xml') )" +
                        "define stream inputStream (name string, age int, country string);";
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
        HttpTestUtil.httpPublishEventDefault(event1, baseURI);
        HttpTestUtil.httpPublishEventDefault(event2, baseURI);
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events from PUT method.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportPutMethod() throws Exception {
        logger.info("Test case for put method");
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        logger.info("Creating test for publishing events from PUT method.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8005" +
                "/endpoints/RecPro') " +
                "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query') " +
                "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
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
        HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                "PUT");
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro", "PUT");
        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(String.valueOf(logEvent.getMessage()));
        }
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(logMessages.contains("Event response code 400"), true);
        Assert.assertEquals(Collections.frequency(logMessages, "Event response code 400"), 2);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with XML mapping.
     * @throws Exception Interrupted exception
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void testMultipleListenersSameURL() throws Exception {
        logger.info("Creating test for same url in different execution plain.");
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8008));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager1 = new SiddhiManager();
        SiddhiManager siddhiManager2 = new SiddhiManager();
        siddhiManager1.setPersistenceStore(persistenceStore);
        siddhiManager1.setExtension("xml-input-mapper", XmlSourceMapper.class);
        siddhiManager2.setPersistenceStore(persistenceStore);
        siddhiManager2.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8008/endpoints/abc', " + "basic.auth.enabled='false'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = (
                "@info(name = 'query') "
                        + "from inputStream "
                        + "select *  "
                        + "insert into outputStream;"
        );
        String inStreamDefinition2 = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8008/endpoints/abc', " + "basic.auth.enabled='false'" + ")"
                + "define stream inputStream2 (name string, age int, country string);";
        String query2 = (
                "@info(name = 'query2') "
                        + "from inputStream2 "
                        + "select *  "
                        + "insert into outputStream2;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager1
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
        SiddhiAppRuntime siddhiAppRuntime2 = siddhiManager2
                .createSiddhiAppRuntime(inStreamDefinition2 + query2);

        siddhiAppRuntime2.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime2.start();
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
        HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/abc",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/abc",
                "POST");
        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(String.valueOf(logEvent.getMessage()));
        }
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(logMessages.contains("Error while connecting at Source 'http' at 'inputStream2'," +
                        " Listener URL http://localhost:8008/endpoints/abc already connected.")
                , true);
        Assert.assertEquals(Collections.frequency(logMessages, "Error while connecting at Source 'http' at " +
                "'inputStream2', Listener URL http://localhost:8008/endpoints/abc already connected."), 1);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events without URL multiple events with same url.
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void testMultipleListenersSameURLInSameExecutionPlan() throws InterruptedException {
        logger.info("Creating test for publishing events same url in same execution plain.");
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
        List<String> receivedEventNameListA = new ArrayList<>(2);
        List<String> receivedEventNameListB = new ArrayList<>(2);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinitionA =
                "" + "@source(type='http', @map(type='xml'),receiver.url='http://localhost:8006/endpoints/" +
                        "RecPro', basic.auth.enabled='false')"
                        + "define stream inputStreamA (name string, age int, country string);";
        String queryA = (
                "@info(name = 'queryA') "
                        + "from inputStreamA "
                        + "select *  "
                        + "insert into outputStreamA;"
                         );
        String inStreamDefinitionB = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8006" +
                "/endpoints/RecPro', basic.auth.enabled='false')"
                + "define stream inputStreamB (name string, age int, country string);";
        String queryB = (
                "@info(name = 'queryB') "
                        + "from inputStreamB "
                        + "select *  "
                        + "insert into outputStreamB;"
                        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinitionA + inStreamDefinitionB + queryA + queryB);
        siddhiAppRuntime.addCallback("queryA", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    for (Event event : inEvents) {
                        eventCount.incrementAndGet();
                        receivedEventNameListA.add(event.getData(0).toString());
                    }
                }
            });
        siddhiAppRuntime.addCallback("queryB", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    for (Event event : inEvents) {
                        eventCount.incrementAndGet();
                        receivedEventNameListB.add(event.getData(0).toString());
                    }
                }
            });
        siddhiAppRuntime.start();
        //To check weather only one is deployed
        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            logMessages.add(String.valueOf(logEvent.getMessage()));
        }
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(logMessages.contains("Error while connecting at Source 'http' at 'inputStreamA', " +
                "Listener URL http://localhost:8006/endpoints/RecPro already connected."), true);
        Assert.assertEquals(Collections.frequency(logMessages, "Error while connecting at Source 'http' at " +
                "'inputStreamA', Listener URL http://localhost:8006/endpoints/RecPro already connected."), 1);
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with different url with same context.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportDifferentURL() throws Exception {
        logger.info("Creating test for publishing events with different url with same context.");
        URI baseURIA = URI.create(String.format("http://%s:%d", "localhost", 8005));
        URI baseURIB = URI.create(String.format("http://%s:%d", "localhost", 8009));
        List<String> receivedEventNameListA = new ArrayList<>(2);
        List<String> receivedEventNameListB = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition1 = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8005" +
                "/endpoints/RecPro', basic.auth.enabled='false' )"
                + "define stream inputStreamA (name string, age int, country string);";
        String query1 = (
                "@info(name = 'queryA') "
                        + "from inputStreamA "
                        + "select *  "
                        + "insert into outputStreamA;"
                        );
        String inStreamDefinition2 = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8009/" +
                "endpoints/RecPro', basic.auth.enabled='false' )"
                + "define stream inputStreamB (name string, age int, country string);";
        String query2 = (
                "@info(name = 'queryB') "
                        + "from inputStreamB "
                        + "select *  "
                        + "insert into outputStreamB;"
                        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition1 + inStreamDefinition2 + query1 + query2);
        siddhiAppRuntime.addCallback("queryA", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameListA.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.addCallback("queryB", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameListB.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expectedA = new ArrayList<>(2);
        expectedA.add("John");
        expectedA.add("Mike");
        List<String> expectedB = new ArrayList<>(2);
        expectedB.add("Donna");
        expectedB.add("Miano");
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
        String event3 = "<events>"
                            + "<event>"
                                + "<name>Donna</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event4 = "<events>"
                            + "<event>"
                                + "<name>Miano</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpPublishEvent(event1, baseURIA, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURIA, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event3, baseURIB, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event4, baseURIB, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 4, eventCount, timeout);
        Assert.assertEquals(receivedEventNameListA.toString(), expectedA.toString());
        Assert.assertEquals(receivedEventNameListB.toString(), expectedB.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with empty payload.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportEmployPayload() throws Exception {
        logger.info("Creating test for publishing events with empty payload.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8005" +
                "/endpoints/RecPro', basic.auth.enabled='false')"
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
        HttpTestUtil.httpPublishEmptyPayload(baseURI);
        HttpTestUtil.httpPublishEmptyPayload(baseURI);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
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
