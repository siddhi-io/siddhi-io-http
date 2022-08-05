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
package io.siddhi.extension.io.http.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.core.util.config.InMemoryConfigManager;
import io.siddhi.extension.io.http.source.util.HttpTestUtil;
import io.siddhi.extension.map.xml.sourcemapper.XmlSourceMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    private static final Logger logger = (Logger) LogManager.getLogger(HttpsSSLSourceTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Creating test for publishing events with https protocol.
     *
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
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs, null);
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
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPSInputTransport")
    public void testHTTPSInputTransportInvalidKeyStore() throws Exception {
        final HttpsSSLSourceTestCaseTestAppender appender = new
                HttpsSSLSourceTestCaseTestAppender("HttpsSSLSourceTestCaseTestAppender", null);
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
        appender.start();
        loggerContext.getConfiguration().addAppender(appender);
        loggerContext.getRootLogger().addAppender(loggerContext.getConfiguration().getAppender(appender.getName()));
        final Logger logger = loggerContext.getRootLogger();
        logger.info("Creating test for publishing events with https protocol with invalid keystore.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/store.jks");
        masterConfigs.put("source.http.keyStorePassword", "wso2carbon");
        masterConfigs.put("source.http.certPassword", "wso2carbon");

        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs, null);
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
        final List<String> loggedEvents = ((HttpsSSLSourceTestCaseTestAppender) logger.getAppenders().
                get("HttpsSSLSourceTestCaseTestAppender")).getLog();
        Assert.assertEquals(loggedEvents.contains("Error on '" + siddhiAppRuntime.getName() +
                        "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                        "incorrect Error while connecting at Source 'http' at 'inputStream'.")
                , true);
        Assert.assertEquals(Collections.frequency(loggedEvents, "Error on '" + siddhiAppRuntime.getName() +
                "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                "incorrect Error while connecting at Source 'http' at 'inputStream'."), 1);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
        logger.removeAppender(appender);
    }

    /**
     * Creating test for publishing events with https protocol with invalid keystore pass.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testHTTPSInputTransportInvalidKeyStore")
    public void testHTTPSInputTransportInvalidKeyStorePass() throws Exception {
        final HttpsSSLSourceTestCaseTestAppender appender = new
                HttpsSSLSourceTestCaseTestAppender("HttpsSSLSourceTestCaseTestAppender", null);
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
        appender.start();
        loggerContext.getRootLogger().addAppender(loggerContext.getConfiguration().getAppender(appender.getName()));
        final Logger logger = loggerContext.getRootLogger();
        logger.info("Creating test for publishing events with https protocol with invalid keystore pass.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");
        masterConfigs.put("source.http.keyStorePassword", "wso2carbon123");
        masterConfigs.put("source.http.certPassword", "wso2carbon");
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs, null);
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
        List<String> expected = new ArrayList<>(2);

        try {
            // publishing events
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
            final List<String> loggedEvents = ((HttpsSSLSourceTestCaseTestAppender) logger.getAppenders().
                    get("HttpsSSLSourceTestCaseTestAppender")).getLog();
            Assert.assertEquals(loggedEvents.contains("Error on '" + siddhiAppRuntime.getName() +
                        "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                        "incorrect Error while connecting at Source 'http' at 'inputStream'.")
                , true);
            Assert.assertEquals(Collections.frequency(loggedEvents, "Error on '" + siddhiAppRuntime.getName() +
                "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                "incorrect Error while connecting at Source 'http' at 'inputStream'."), 1);
            SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        } catch (InterruptedException t) {
            logger.error(t.getMessage(), t);
        } finally {
            siddhiAppRuntime.shutdown();
            logger.removeAppender(appender);
        }
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
    }

    /**
     * Creating test for publishing events with https protocol with invalid cert pass.
     *
     * @throws Exception Interrupted exception
     */
    @Test(enabled = false, dependsOnMethods = "testHTTPSInputTransportInvalidKeyStorePass")
    public void testHTTPSInputTransportInvalidCertPassword() throws Exception {
        final HttpsSSLSourceTestCaseTestAppender appender = new
                HttpsSSLSourceTestCaseTestAppender("HttpsSSLSourceTestCaseTestAppender", null);
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(LogManager.class.getClassLoader(), false);
        appender.start();
        loggerContext.getRootLogger().addAppender(loggerContext.getConfiguration().getAppender(appender.getName()));
        final Logger logger = loggerContext.getRootLogger();
        logger.info(" Creating test for publishing events with https protocol with invalid cert pass.");
        HttpTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("source.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");
        masterConfigs.put("source.http.https.keystore,password", "wso2carbon");
        masterConfigs.put("source.http.certPassword", "wso2carbon123");
        List<String> receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs, null);
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
        final List<String> loggedEvents = ((HttpsSSLSourceTestCaseTestAppender) logger.getAppenders().
                get("HttpsSSLSourceTestCaseTestAppender")).getLog();
        Assert.assertEquals(loggedEvents.contains("Error on '" + siddhiAppRuntime.getName() +
                        "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                        "incorrect Error while connecting at Source 'http' at 'inputStream'.")
                , true);
        Assert.assertEquals(Collections.frequency(loggedEvents, "Error on '" + siddhiAppRuntime.getName() +
                "'. Failed to initialize the SSLContext: Keystore was tampered with, or password was " +
                "incorrect Error while connecting at Source 'http' at 'inputStream'."), 1);
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
        logger.removeAppender(appender);
    }

    @Plugin(name = "HttpsSSLSourceTestCaseTestAppender",
            category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
    private class HttpsSSLSourceTestCaseTestAppender extends AbstractAppender {

        private final List<String> log = new ArrayList<>();

        public HttpsSSLSourceTestCaseTestAppender(String name, Filter filter) {

            super(name, filter, null);
        }

        @PluginFactory
        public HttpsSSLSourceTestCaseTestAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Filter") Filter filter) {

            return new HttpsSSLSourceTestCaseTestAppender(name, filter);
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
