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
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.extension.io.http.source.util.HttpTestUtil;
import io.siddhi.extension.map.json.sourcemapper.JsonSourceMapper;
import io.siddhi.extension.map.text.sourcemapper.TextSourceMapper;
import io.siddhi.extension.map.xml.sourcemapper.XmlSourceMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases for mapping types.
 */
public class HttpSourceMappingTestCase {
    private static final Logger logger = LogManager.getLogger(HttpSourceMappingTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Creating test for publishing events with XML mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testXmlMapping() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8006));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8006/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with Text mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testXmlMapping")
    public void testTextMappingSingle() throws Exception {
        AtomicInteger eventCount = new AtomicInteger(0);
        int waitTime = 50;
        int timeout = 30000;
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8006));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http',  @map(type='text'), "
                + "receiver.url='http://localhost:8006/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        expected.add("John");
        expected.add("Mike");
        String event1 = "name:\'John\',\n" +
                "age:100,\n" +
                "country:\'USA\'";
        String event2 = "name:\'Mike\',\n" +
                "age:100,\n" +
                "country:\'USA\'";
        HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with Text mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testTextMappingSingle")
    public void testTextMapping() throws Exception {
        logger.info("Creating test for publishing events with Text mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8006));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http',  @map(type='text',fail.on.missing.attribute='false'), "
                + "receiver.url='http://localhost:8006/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        expected.add("John");
        expected.add("Mike");
        String event1 = "name:\"John\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        String event2 = "name:\"Mike\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 1, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with Json mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test(dependsOnMethods = "testTextMapping")
    public void testJsonMapping() throws Exception {
        logger.info("Creating test for publishing events with Json mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8006));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", JsonSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='json'), "
                + "receiver.url='http://localhost:8006/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        List<String> expected = new ArrayList<>(2);
        try {
            siddhiAppRuntime.start();
            // publishing events
            expected.add("John");
            expected.add("Mike");
            String event1 = " {\n" +
                    "      \"event\":{\n" +
                    "         \"name\":\"John\",\n" +
                    "         \"age\":55,\n" +
                    "         \"country\":\"US\"\n" +
                    "      }\n" +
                    " }";
            String event2 = " {\n" +
                    "      \"event\":{\n" +
                    "         \"name\":\"Mike\",\n" +
                    "         \"age\":55,\n" +
                    "         \"country\":\"US\"\n" +
                    "      }\n" +
                    " }";
            HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                    "POST");
            HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                    "POST");
            SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        } catch (InterruptedException t) {
            logger.error(t.getMessage(), t);
        } finally {
            siddhiAppRuntime.shutdown();
        }
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
    }
}
