/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.extension.io.http.source.util.HttpTestUtil;
import io.siddhi.extension.map.xml.sourcemapper.XmlSourceMapper;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic test cases for http source functions.
 */
public class HttpBasicTestCase {
    private static final Logger logObj = (Logger) LogManager.getLogger(HttpBasicTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Creating test for publishing events without URL.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportWithoutURL() throws Exception {
        logObj.info(" Creating test for publishing events without URL.");
        URI baseURI = URI.create(String.format("http://%s:%d", "0.0.0.0", 8280));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@App:name('TestSiddhiApp')" +
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
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPInputTransportWithoutURL")
    public void testHTTPInputTransportPutMethod() throws Exception {
        logObj.info("Test case for put method");
        final HttpBasicTestCaseTestAppender appender = new
                HttpBasicTestCaseTestAppender("HttpBasicTestCaseTestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();
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
        final List<String> loggedEvents = ((HttpBasicTestCaseTestAppender) logger.getAppenders().
                get("HttpBasicTestCaseTestAppender")).getLog();
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(loggedEvents.contains("Event response code 400"), true);
        Assert.assertEquals(Collections.frequency(loggedEvents, "Event response code 400"), 2);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
        logger.removeAppender(appender);
    }

    /**
     * Creating test for publishing events with XML mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPInputTransportPutMethod")
    public void testMultipleListenersSameURL() throws Exception {
        logObj.info("Creating test for same url in different execution plain.");
        final HttpBasicTestCaseTestAppender appender = new
                HttpBasicTestCaseTestAppender("HttpBasicTestCaseTestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();
        logger.info("ADDED the HttpBasicTestCaseTestAppender appender in testMultipleListenersSameURL");
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
        List<String> expected = new ArrayList<>(2);
        try {
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
            try {
                siddhiAppRuntime2.start();
                // publishing events
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
                final List<String> loggedEvents = ((HttpBasicTestCaseTestAppender) logger.getAppenders().
                        get("HttpBasicTestCaseTestAppender")).getLog();
                SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
                Assert.assertEquals(loggedEvents.contains("Error on '" + siddhiAppRuntime2.getName() +
                                "'. Listener URL http://localhost:8008/endpoints/abc already connected " +
                                "Error while connecting at Source 'http' at 'inputStream2'.")
                        , true);
                Assert.assertEquals(Collections.frequency(loggedEvents, "Error on '" + siddhiAppRuntime2.getName() +
                        "'. Listener URL http://localhost:8008/endpoints/abc already connected " +
                        "Error while connecting at Source 'http' at 'inputStream2'."), 1);
            } catch (InterruptedException e) {
                logger.info("Test case interrupted ");
            } finally {
                siddhiAppRuntime2.shutdown();
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            siddhiAppRuntime.shutdown();
            logger.removeAppender(appender);
        }
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());

    }

    /**
     * Creating test for publishing events without URL multiple events with same url.
     */
    @Test(dependsOnMethods = "testMultipleListenersSameURL")
    public void testMultipleListenersSameURLInSameExecutionPlan() throws InterruptedException {
        logObj.info("Creating test for publishing events same url in same execution plain.");
        final HttpBasicTestCaseTestAppender appender = new
                HttpBasicTestCaseTestAppender("HttpBasicTestCaseTestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();
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
        try {
            siddhiAppRuntime.start();
            //To check weather only one is deployed
            final List<String> loggedEvents = ((HttpBasicTestCaseTestAppender) logger.getAppenders().
                    get("HttpBasicTestCaseTestAppender")).getLog();
            SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
            Assert.assertEquals(loggedEvents.contains("Error on '" + siddhiAppRuntime.getName() + "'. Listener URL " +
                    "http://localhost:8006/endpoints/RecPro already connected Error while " +
                    "connecting at Source 'http' at 'inputStreamA'."), true);
            Assert.assertEquals(Collections.frequency(loggedEvents, "Error on '" + siddhiAppRuntime.getName() +
                    "'. Listener URL http://localhost:8006/endpoints/RecPro already connected Error while " +
                    "connecting at Source 'http' at 'inputStreamA'."), 1);
        } catch (InterruptedException t) {
            logger.error(t.getMessage(), t);
        } finally {
            siddhiAppRuntime.shutdown();
            logger.removeAppender(appender);
        }
    }

    /**
     * Creating test for publishing events with different url with same context.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testMultipleListenersSameURLInSameExecutionPlan")
    public void testHTTPInputTransportDifferentURL() throws Exception {
        logObj.info("Creating test for publishing events with different url with same context.");
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
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPInputTransportDifferentURL")
    public void testHTTPInputTransportEmployPayload() throws Exception {
        logObj.info("Creating test for publishing events with empty payload.");
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

    @Plugin(name = "HttpBasicTestCaseTestAppender",
            category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
    private static class HttpBasicTestCaseTestAppender extends AbstractAppender {

        private final List<String> log = new ArrayList<>();

        public HttpBasicTestCaseTestAppender(String name, Filter filter) {

            super(name, filter, null);
        }

        @PluginFactory
        public static HttpBasicTestCaseTestAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Filter") Filter filter) {

            return new HttpBasicTestCaseTestAppender(name, filter);
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
