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

import org.apache.log4j.Logger;
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
import org.wso2.siddhi.extension.input.mapper.text.TextSourceMapper;
import org.wso2.siddhi.extension.input.mapper.xml.XmlSourceMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test case for HTTPS protocol.
 */
public class HttpCustomThreadpoolConfigTest {
    private static final Logger logger = Logger
            .getLogger(HttpCustomThreadpoolConfigTest.class);
    private List<String> receivedEventNameList;
    private Map<String, String> masterConfigs = new HashMap<>();
    /**
     * Creating test for publishing events with XML mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testCustomPoolConfig() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        siddhiManager.setExtension("xml-input-mapper", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro', " + "basic.auth.enabled='false',worker" +
                ".count='8',server.bootstrap.boss.group.size='10',server.bootstrap.worker.group.size='10'" + ")"
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
        new HttpTestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false, "text/xml",
                "POST");
        new HttpTestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "text/xml",
                "POST");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }


}
