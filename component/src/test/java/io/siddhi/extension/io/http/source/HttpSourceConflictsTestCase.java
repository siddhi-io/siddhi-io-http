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
import io.siddhi.core.util.config.InMemoryConfigManager;
import io.siddhi.extension.io.http.source.util.HttpTestUtil;
import io.siddhi.extension.map.xml.sourcemapper.XmlSourceMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multiple event synchronously run.
 */

public class HttpSourceConflictsTestCase {
    private static final Logger logger = LogManager.getLogger(HttpSourceConflictsTestCase.class);
    private AtomicInteger eventCountA = new AtomicInteger(0);
    private AtomicInteger eventCountB = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCountA.set(0);
        eventCountB.set(0);
    }

    /**
     * Creating test for publishing events with multiple formats synchronously.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPDifferentFormat() throws Exception {
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
        String inStreamDefinitionA = "@source(type='http', @map(type='xml'), receiver.url='https://localhost:8005"
                + "/endpoints/RecPro2')"
                + "define stream inputStream (name string, age int, country string);";
        String queryA = ("@info(name = 'query') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinitionA + queryA);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCountA.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        try {
            siddhiAppRuntime.start();
            masterConfigs.put("source.http.keyStorePassword", "wso2carbon2");
            masterConfigs.put("source.http.certPassword", "wso2carbon2");
            String inStreamDefinitionB = "@source(type='http', @map(type='xml'), receiver.url='https://localhost:8005"
                    + "/endpoints/RecPro')"
                    + "define stream inputStream2 (name string, age int, country string);";
            String queryB = ("@info(name = 'query2') "
                    + "from inputStream2 "
                    + "select *  "
                    + "insert into outputStream2;"
            );
            SiddhiAppRuntime siddhiAppRuntime2 = siddhiManager
                    .createSiddhiAppRuntime(inStreamDefinitionB + queryB);
            siddhiAppRuntime2.start();
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
            SiddhiTestHelper.waitForEvents(waitTime, 2, eventCountA, timeout);
            Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        } catch (InterruptedException t) {
            logger.error(t.getMessage(), t);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
