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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multiple event synchronously run.
 */

public class HttpMultipleFormatSourcesTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(HttpMultipleFormatSourcesTestCase.class);
    private AtomicInteger eventCountA = new AtomicInteger(0);
    private AtomicInteger eventCountB = new AtomicInteger(0);
    private AtomicInteger eventCountC = new AtomicInteger(0);
    private AtomicInteger eventCountD = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCountA.set(0);
        eventCountB.set(0);
        eventCountC.set(0);
        eventCountD.set(0);
    }

    /**
     * Creating test for publishing events with multiple formats synchronously.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransportDifferentFormat() throws Exception {
        logger.info("Creating test case for different configuration sources together.");
        URI baseURIA = URI.create(String.format("http://%s:%d", "localhost", 8005));
        URI baseURIC = URI.create(String.format("http://%s:%d", "localhost", 8009));
        List<String> receivedEventNameListA = new ArrayList<>(2);
        List<String> receivedEventNameListB = new ArrayList<>(2);
        List<String> receivedEventNameListC = new ArrayList<>(2);
        List<String> receivedEventNameListD = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinitionA = "@source(type='http', @map(type='xml'), " +
                "receiver.url='http://localhost:8005/endpoints/RecPro', basic.auth.enabled='false', server.bootstrap" +
                ".boss.group.size='20', server." +
                "bootstrap.worker.group.size='20')"
                + "define stream inputStreamA (name string, age int, country string);";
        String queryA = (
                "@info(name = 'queryA') "
                        + "from inputStreamA "
                        + "select *  "
                        + "insert into outputStreamA;"
                        );

        String inStreamDefinitionB = "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro1', basic.auth.enabled='false')"
                + "define stream inputStreamB (name string, age int, country string);";
        String queryB = (
                "@info(name = 'queryB') "
                        + "from inputStreamB "
                        + "select *  "
                        + "insert into outputStreamB;"
                        );

        String inStreamDefinitionC = "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8009/endpoints/RecPro', basic.auth.enabled='false')"
                + "define stream inputStreamC (name string, age int, country string);";
        String queryC = (
                "@info(name = 'queryC') "
                        + "from inputStreamC "
                        + "select *  "
                        + "insert into outputStreamC;"
                        );

        String inStreamDefinitionD = "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro2', basic.auth.enabled='false')"
                + "define stream inputStreamD (name string, age int, country string);";
        String queryD = (
                "@info(name = 'queryD') "
                        + "from inputStreamD "
                        + "select *  "
                        + "insert into outputStreamD;"
                         );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinitionA + inStreamDefinitionB +
                        inStreamDefinitionC + inStreamDefinitionD + queryA + queryB + queryC + queryD);

        siddhiAppRuntime.addCallback("queryA", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCountA.incrementAndGet();
                    receivedEventNameListA.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.addCallback("queryC", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCountC.incrementAndGet();
                    receivedEventNameListC.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.addCallback("queryB", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCountB.incrementAndGet();
                    receivedEventNameListB.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.addCallback("queryD", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCountD.incrementAndGet();
                    receivedEventNameListD.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expectedA = new ArrayList<>(2);
        expectedA.add("JohnA");
        expectedA.add("MikeA");
        String event1 = "<events>"
                            + "<event>"
                                + "<name>JohnA</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event2 = "<events>"
                            + "<event>"
                                + "<name>MikeA</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpPublishEvent(event1, baseURIA, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event2, baseURIA, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCountA, timeout);
        Assert.assertEquals(receivedEventNameListA.toString(), expectedA.toString());
        // publishing events
        List<String> expectedB = new ArrayList<>(2);
        expectedB.add("JohnB");
        expectedB.add("MikeB");
        String event3 = "<events>"
                            + "<event>"
                                + "<name>JohnB</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event4 = "<events>"
                            + "<event>"
                                + "<name>MikeB</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpPublishEvent(event3, baseURIA, "/endpoints/RecPro1",
                "POST");
        HttpTestUtil.httpPublishEvent(event4, baseURIA, "/endpoints/RecPro1",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCountB, timeout);
        Assert.assertEquals(receivedEventNameListB.toString(), expectedB.toString());
        // publishing events
        List<String> expectedC = new ArrayList<>(2);
        expectedC.add("JohnC");
        expectedC.add("MikeC");
        String event5 = "<events>"
                            + "<event>"
                                + "<name>JohnC</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event6 = "<events>"
                            + "<event>"
                                + "<name>MikeC</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpPublishEvent(event5, baseURIC, "/endpoints/RecPro",
                "POST");
        HttpTestUtil.httpPublishEvent(event6, baseURIC, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCountC, timeout);
        Assert.assertEquals(receivedEventNameListC.toString(), expectedC.toString());
        // publishing events
        List<String> expectedD = new ArrayList<>(2);
        expectedD.add("JohnD");
        expectedD.add("MikeD");
        String event7 = "<events>"
                            + "<event>"
                                + "<name>JohnD</name>"
                                + "<age>100</age>"
                                + "<country>AUS</country>"
                            + "</event>"
                        + "</events>";
        String event8 = "<events>"
                            + "<event>"
                                + "<name>MikeD</name>"
                                + "<age>20</age>"
                                + "<country>USA</country>"
                            + "</event>"
                        + "</events>";
        HttpTestUtil.httpPublishEvent(event7, baseURIA, "/endpoints/RecPro2",
                "POST");
        HttpTestUtil.httpPublishEvent(event8, baseURIA, "/endpoints/RecPro2",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCountD, timeout);
        Assert.assertEquals(receivedEventNameListD.toString(), expectedD.toString());
        siddhiAppRuntime.shutdown();
    }
}
