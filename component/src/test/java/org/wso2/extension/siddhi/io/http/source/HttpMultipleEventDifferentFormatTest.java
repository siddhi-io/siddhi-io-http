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

/**
 * Multiple event synchronously run.
 */

public class HttpMultipleEventDifferentFormatTest {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(HttpMultipleEventDifferentFormatTest.class);
    private List<String> receivedEventNameListA;
    private List<String> receivedEventNameListB;
    private List<String> receivedEventNameListC;

    /**
     * Creating test for publishing events with multiple formats synchronously.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPInputTransport() throws Exception {
        logger.info("Creating test for publishing events with multiple formats synchronously.");
        URI baseURIA = URI.create(String.format("http://%s:%d", "localhost", 9005));
        URI baseURIC = URI.create(String.format("http://%s:%d", "localhost", 7005));
        receivedEventNameListA = new ArrayList<>(2);
        receivedEventNameListB = new ArrayList<>(2);
        receivedEventNameListC = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinitionA = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:9005/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
                + "define stream inputStreamA (name string, age int, country string);";
        String queryA = ("@info(name = 'queryA') " + "from inputStreamA " + "select *  " + "insert into " +
                "outputStreamA;");
        String inStreamDefinitionB = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:9005/endpoints/RecPro1', " + "basic.auth.enabled='false'" + ")"
                + "define stream inputStreamB (name string, age int, country string);";
        String queryB = ("@info(name = 'queryB') " + "from inputStreamB " + "select *  " + "insert into " +
                "outputStreamB;");

        String inStreamDefinitionC = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:7005/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
                + "define stream inputStreamC (name string, age int, country string);";
        String queryC = ("@info(name = 'queryC') " + "from inputStreamC " + "select *  " + "insert into" +
                " outputStreamC;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinitionA + inStreamDefinitionB + inStreamDefinitionC +
                        queryA + queryB
                        + queryC);

        executionPlanRuntime.addCallback("queryA", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameListA.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.addCallback("queryC", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameListC.add(event.getData(0).toString());
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
        expectedA.add("JohnA");
        expectedA.add("MikeA");
        String event1 =
                "<events><event><name>JohnA</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>MikeA</name>" + "<age>20</age><country>USA</country></event></events>";
        new HttpTestUtil().httpPublishEvent(event1, baseURIA, "/endpoints/RecPro", false,
                "application/xml", "POST");
        new HttpTestUtil().httpPublishEvent(event2, baseURIA, "/endpoints/RecPro", false,
                "application/xml", "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameListA.toString(), expectedA.toString());
        // publishing events
        List<String> expectedB = new ArrayList<>(2);
        expectedB.add("JohnB");
        expectedB.add("MikeB");
        String event3 =
                "<events><event><name>JohnB</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event4 = "<events><event><name>MikeB</name>" + "<age>20</age><country>USA</country></event></events>";
        new HttpTestUtil().httpPublishEvent(event3, baseURIA, "/endpoints/RecPro1", false,
                "application/xml", "POST");
        new HttpTestUtil().httpPublishEvent(event4, baseURIA, "/endpoints/RecPro1", false,
                "application/xml", "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameListB.toString(), expectedB.toString());
        // publishing events
        List<String> expectedC = new ArrayList<>(2);
        expectedC.add("JohnC");
        expectedC.add("MikeC");
        String event5 =
                "<events><event><name>JohnC</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event6 = "<events><event><name>MikeC</name>" + "<age>20</age><country>USA</country></event></events>";
        new HttpTestUtil().httpPublishEvent(event5, baseURIC, "/endpoints/RecPro", false,
                "application/xml", "POST");
        new HttpTestUtil().httpPublishEvent(event6, baseURIC, "/endpoints/RecPro", false,
                "application/xml", "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameListC.toString(), expectedC.toString());

        executionPlanRuntime.shutdown();
    }

}
