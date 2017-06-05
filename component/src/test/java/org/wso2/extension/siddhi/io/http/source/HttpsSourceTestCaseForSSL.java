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
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.source.util.HttpTestUtil;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;
import org.wso2.siddhi.extension.input.mapper.xml.XmlSourceMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test case for HTTPS protocol.
 */
public class HttpsSourceTestCaseForSSL {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(HttpsSourceTestCaseForSSL.class);
    private List<String> receivedEventNameList;
    private Map<String, String> masterConfigs = new HashMap<>();
    /**
     * Creating test for publishing events with https protocol.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransport() throws Exception {
        logger.info("Creating test for publishing events with https protocol.");
        new HttpTestUtil().setCarbonHome();
        masterConfigs.put("source.http.https.keyStoreFile", "${carbon.home}/conf/security/wso2carbon.jks");
        masterConfigs.put("source.http.https.keyStorePass", "wso2carbon");
        masterConfigs.put("source.http.https.certPass", "wso2carbon");
        receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='https://localhost:9090/endpoints/RecPro', " + "is.basic.auth.enabled='false'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
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
        String event1 =
                "<events><event><name>John</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>Mike</name>" + "<age>20</age><country>USA</country></event></events>";
        new HttpTestUtil().httpsPublishEvent(event1, "https://localhost:9090/endpoints/RecPro", false,
                "text/plain");
        new HttpTestUtil().httpsPublishEvent(event2, "https://localhost:9090/endpoints/RecPro", false,
                "text/plain");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }
    /**
     * Creating test for publishing events with https protocol with invalid keystore.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidKeyStore() throws Exception {
        logger.info("Creating test for publishing events with https protocol with invalid keystore.");
        new HttpTestUtil().setCarbonHome();
        masterConfigs.clear();
        masterConfigs.put("source.http.https.keyStoreFile", "${carbon.home}/conf/security/store.jks");
        masterConfigs.put("source.http.https.keyStorePass", "wso2carbon");
        masterConfigs.put("source.http.https.certPass", "wso2carbon");

        receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='https://localhost:9090/endpoints/RecPro', " + "is.basic.auth.enabled='false'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
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
        String event1 =
                "<events><event><name>John</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>Mike</name>" + "<age>20</age><country>USA</country></event></events>";
        try {
            new HttpTestUtil().httpsPublishEvent(event1, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
            new HttpTestUtil().httpsPublishEvent(event2, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
        } catch (IllegalArgumentException e) {
            e.getClass().getCanonicalName();
        }
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        try {
            Logger.getLogger(HttpsSourceTestCaseForSSL.class).error("Test");
        } finally {
            logger.removeAppender(appender);
        }
        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent firstLogEntry = log.get(0);
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLevel(), is(Level.ERROR));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getMessage(), is("Test"));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLoggerName(), is("org.wso2.extension.siddhi.io" +
                ".http.source.HttpsSourceTestCaseForSSL"));
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }
    /**
     * Creating test for publishing events with https protocol with invalid keystore pass.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidKeyStorePass() throws Exception {
        logger.info("Creating test for publishing events with https protocol with invalid keystore pass.");
        new HttpTestUtil().setCarbonHome();
        masterConfigs.clear();
        masterConfigs.put("source.http.https.keyStoreFile", "${carbon.home}/conf/security/wso2carbon.jks");
        masterConfigs.put("source.http.https.keyStorePass", "wso2carbon123");
        masterConfigs.put("source.http.https.certPass", "wso2carbon");
        receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='https://localhost:9090/endpoints/RecPro', " + "is.basic.auth.enabled='false'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
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
        String event1 =
                "<events><event><name>John</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>Mike</name>" + "<age>20</age><country>USA</country></event></events>";
        try {
            new HttpTestUtil().httpsPublishEvent(event1, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
            new HttpTestUtil().httpsPublishEvent(event2, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
        } catch (IllegalArgumentException e) {
            e.getClass().getCanonicalName();
        }
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        try {
            Logger.getLogger(HttpsSourceTestCaseForSSL.class).error("Test");
        } finally {
            logger.removeAppender(appender);
        }

        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent firstLogEntry = log.get(0);
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLevel(), is(Level.ERROR));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getMessage(), is("Test"));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLoggerName(), is("org.wso2.extension.siddhi.io" +
                ".http.source.HttpsSourceTestCaseForSSL"));
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }
    /**
     * Creating test for publishing events with https protocol with invalid cert pass.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPSInputTransportInvalidCertpass() throws Exception {
        logger.info(" Creating test for publishing events with https protocol with invalid cert pass.");
        new HttpTestUtil().setCarbonHome();
        masterConfigs.clear();
        masterConfigs.put("source.http.https.keyStoreFile", "${carbon.home}/conf/security/wso2carbon.jks");
        masterConfigs.put("source.http.https.keyStorePass", "wso2carbon");
        masterConfigs.put("source.http.https.certPass", "wso2carbon123");
        receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs);
        inMemoryConfigManager.generateConfigReader("source", "http");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='https://localhost:9090/endpoints/RecPro', " + "is.basic.auth.enabled='false'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
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
        String event1 =
                "<events><event><name>John</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>Mike</name>" + "<age>20</age><country>USA</country></event></events>";
        try {
            new HttpTestUtil().httpsPublishEvent(event1, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
            new HttpTestUtil().httpsPublishEvent(event2, "https://localhost:9090/endpoints/RecPro", false,
                    "text/plain");
        } catch (IllegalArgumentException e) {
            e.getClass().getCanonicalName();
        }
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        try {
            Logger.getLogger(HttpsSourceTestCaseForSSL.class).error("Test");
        } finally {
            logger.removeAppender(appender);
        }
        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent firstLogEntry = log.get(0);
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLevel(), is(Level.ERROR));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getMessage(), is("Test"));
        org.hamcrest.MatcherAssert.assertThat(firstLogEntry.getLoggerName(), is("org.wso2.extension.siddhi.io" +
                ".http.source.HttpsSourceTestCaseForSSL"));
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }
    class TestAppender extends AppenderSkeleton {
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
