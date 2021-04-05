/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.siddhi.extension.io.http.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.extension.map.json.sinkmapper.JsonSinkMapper;
import io.siddhi.extension.map.json.sourcemapper.JsonSourceMapper;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests http sse sink.
 */
public class HttpSSESinkTestCase {
    private static final Logger log = Logger.getLogger(HttpSSESinkTestCase.class);
    private static final int SLEEP_TIME = 50;
    private static final int EVENT_COUNT = 10;
    private static final int TIMEOUT = 30000;
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void testSSESource() throws Exception {
        List<String> receivedEventList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("json-output-mapper", JsonSinkMapper.class);
        siddhiManager.setExtension("json-input-mapper", JsonSourceMapper.class);
        String sinkStreamDefinition = "@sink(type = 'sse', event.sink.url='http://localhost:8010/send', " +
                "@map(type='json'))\n" +
                "define stream PublishStream (param1 string);\n";
        String sourceStreamDefinition = "@Source(type = 'sse', event.source.url='http://localhost:8010/send', " +
                "@map(type='json'))\n" +
                "@sink(type='log')\n" +
                "define stream ListenStream (param1 string);\n";
        String logStreamDefinition = "@sink(type='log')\n" +
                "define stream LogStream(param1 string);\n";
        String query = "@info(name='log')\n" +
                "from ListenStream\n" +
                "select *\n" +
                "insert into LogStream";
        SiddhiAppRuntime sinkAppRuntime = siddhiManager.createSiddhiAppRuntime(sinkStreamDefinition);
        InputHandler publishStream = sinkAppRuntime.getInputHandler("PublishStream");
        SiddhiAppRuntime sourceAppRuntime = siddhiManager.createSiddhiAppRuntime(sourceStreamDefinition +
                logStreamDefinition + query);
        sourceAppRuntime.addCallback("log", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventList.add(event.getData(0).toString());
                }
            }
        });
        sinkAppRuntime.start();
        sourceAppRuntime.start();
        List<String> expected = new ArrayList<>(10);
        for (int i = 0; i < EVENT_COUNT; i++) {
            Thread.sleep(SLEEP_TIME * 10);
            String timestamp = String.valueOf(new Date().getTime());
            publishStream.send(new Object[]{timestamp});
            expected.add(timestamp);
        }

        SiddhiTestHelper.waitForEvents(SLEEP_TIME, EVENT_COUNT, eventCount, TIMEOUT);
        Assert.assertEquals(receivedEventList.toString(), expected.toString());
        sinkAppRuntime.shutdown();
        sourceAppRuntime.shutdown();
    }
}
