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
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.source.util.HttpTestUtil;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.extension.input.mapper.xml.XmlSourceMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Basic test cases for http source functions.
 */
public class HttpBasicTests {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpBasicTests.class);

    /**
     * Creating test for publishing events without URL.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportWithoutURL() throws Exception {
        logger.info(" Creating test for publishing events without URL.");
        URI baseURI = URI.create(String.format("http://%s:%d", "0.0.0.0", 9763));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@source(type='http', @map(type='xml') )" +
                        "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;"
                        );
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
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
        new HttpTestUtil().httpPublishEventDefault(event1, baseURI, false, "text/xml",
                "inputStream");
        new HttpTestUtil().httpPublishEventDefault(event2, baseURI, false, "text/xml",
                "inputStream");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }

    /**
     * Creating test for publishing events from PUT method.
     * @throws Exception Interrupted exception
     */
    @Test(expectedExceptions = {Exception.class})
    public void testHTTPInputTransportPutMethod() throws Exception {
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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
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
        new HttpTestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false,
                "text/xml", "PUT");
        new HttpTestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "" +
                "text/xml", "PUT");
        final List<LoggingEvent> log = appender.getLog();
        org.hamcrest.MatcherAssert.assertThat(log.get(2).getMessage(), is("Event response code 400"));
        org.hamcrest.MatcherAssert.assertThat(log.get(4).getMessage(), is("Event response code 400"));
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }

    /**
     * Creating test for publishing events without URL multiple events with same url.
     */
    @Test(expectedExceptions = {Exception.class})
    public void testHTTPInputTransportMultipleListenersSameURL() {
        logger.info("Creating test for publishing events without URL multiple events with same url.");
        SiddhiManager siddhiManager = new SiddhiManager();
        List<String> receivedEventNameListA = new ArrayList<>(2);
        List<String> receivedEventNameListB = new ArrayList<>(2);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinitionA =
                "" + "@source(type='http', @map(type='xml'),receiver.url='http://localhost:8005/endpoints/" +
                        "RecPro', basic.auth.enabled='false')"
                        + "define stream inputStreamA (name string, age int, country string);";
        String queryA = (
                "@info(name = 'queryA') "
                        + "from inputStreamA "
                        + "select *  "
                        + "insert into outputStreamA;"
                         );
        String inStreamDefinitionB = "@source(type='http', @map(type='xml'), receiver.url='http://localhost:8005" +
                "/endpoints/RecPro', basic.auth.enabled='false')"
                + "define stream inputStreamB (name string, age int, country string);";
        String queryB = (
                "@info(name = 'queryB') "
                        + "from inputStreamB "
                        + "select *  "
                        + "insert into outputStreamB;"
                        );
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinitionA +  queryA);
        try {
            executionPlanRuntime.addCallback("queryA", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    for (Event event : inEvents) {
                        receivedEventNameListA.add(event.getData(0).toString());
                    }
                }
            });
            executionPlanRuntime = siddhiManager
                    .createExecutionPlanRuntime(inStreamDefinitionB + queryB);
            executionPlanRuntime.addCallback("queryB", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    for (Event event : inEvents) {
                        receivedEventNameListB.add(event.getData(0).toString());
                    }
                }
            });
            //To check weather only one is deployed
            Assert.assertEquals(1, executionPlanRuntime.getSources().size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        executionPlanRuntime.shutdown();
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
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition1 + inStreamDefinition2 + query1 + query2);
        executionPlanRuntime.addCallback("queryA", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameListA.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.addCallback("queryB", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameListB.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
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
        new HttpTestUtil().httpPublishEvent(event1, baseURIA, "/endpoints/RecPro", false, "text/xml",
                "POST");
        new HttpTestUtil().httpPublishEvent(event2, baseURIA, "/endpoints/RecPro", false, "text/xml",
                "POST");
        new HttpTestUtil().httpPublishEvent(event3, baseURIB, "/endpoints/RecPro", false, "text/xml",
                "POST");
        new HttpTestUtil().httpPublishEvent(event4, baseURIB, "/endpoints/RecPro", false, "text/xml",
                "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameListA.toString(), expectedA.toString());
        Assert.assertEquals(receivedEventNameListB.toString(), expectedB.toString());
        executionPlanRuntime.shutdown();
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
                "/endpoints/RecPro', basic.auth.enabled='true')"
                + "define stream inputStream (name string, age int, country string);";
        String query = (
                "@info(name = 'query') "
                        + "from inputStream "
                        + "select *  "
                        + "insert into outputStream;"
                        );
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        new HttpTestUtil().httpPublishEmptyPayload(baseURI, false, "text/xml", "POST");
        new HttpTestUtil().httpPublishEmptyPayload(baseURI, false, "text/xml", "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
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
