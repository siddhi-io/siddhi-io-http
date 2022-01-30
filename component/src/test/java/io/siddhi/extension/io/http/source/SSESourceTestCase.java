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

package io.siddhi.extension.io.http.source;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.map.json.sinkmapper.JsonSinkMapper;
import io.siddhi.extension.map.json.sourcemapper.JsonSourceMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests http sse source.
 */
public class SSESourceTestCase {
    private static final Logger log = LogManager.getLogger(SSESourceTestCase.class);
    private static final int SSE_SERVER_PORT = 8010;
    private static final int SLEEP_TIME = 50;
    private static final int EVENT_COUNT = 10;
    private static final int TIMEOUT = 30000;
    private AtomicInteger eventCount = new AtomicInteger(0);
    private HttpServer sseServer;
    private ThreadPoolExecutor threadPoolExecutor;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
        try {
            startSSEServer();
        } catch (IOException e) {
            log.error("Failed to start the SSE server");
        }
    }

    private void startSSEServer() throws IOException {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        sseServer = HttpServer.create(new InetSocketAddress("localhost", SSE_SERVER_PORT), 0);
        sseServer.createContext("/", new SSEHandler());
        sseServer.setExecutor(threadPoolExecutor);
        sseServer.start();
        log.info("SSE server started on PORT " + SSE_SERVER_PORT);
    }

    private class SSEHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                if (HttpConstants.HTTP_METHOD_GET.equals(httpExchange.getRequestMethod())) {
                    httpExchange.sendResponseHeaders(200, 500);
                    httpExchange.getResponseHeaders().set("connection", "keep-alive");
                    httpExchange.getResponseHeaders().set("cache-control", "no-cache");
                    httpExchange.getResponseHeaders().set("content-type", "application/json");
                    OutputStream outputStream = httpExchange.getResponseBody();
                    String responseBody = "{\"param1\":\"pizza\"}";
                    for (int i = 0; i < EVENT_COUNT; i++) {
                        outputStream.write(responseBody.getBytes());
                        outputStream.flush();
                        Thread.sleep(SLEEP_TIME * 10);
                    }

                    outputStream.close();
                }
            } catch (InterruptedException e) {
                log.info("Test Interrupted");
            }
        }
    }

    @Test
    public void testSSESource() throws Exception {
        List<String> receivedEventList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("json-output-mapper", JsonSinkMapper.class);
        siddhiManager.setExtension("json-input-mapper", JsonSourceMapper.class);
        String sourceStreamDefinition = "@Source(type='sse', receiver.url='http://localhost:8010/',\n" +
                "@map(type='json'))\n" +
                "define stream ReceiveProductionStream (param1 string);\n" +
                "\n" +
                "@sink(type='log') \n" +
                "define stream LogProductionStream(param1 string);\n" +
                "\n" +
                "/* Queries */\n" +
                "\n" +
                "@info(name='log') \n" +
                "from ReceiveProductionStream\n" +
                "select *\n" +
                "insert into LogProductionStream";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sourceStreamDefinition);
        siddhiAppRuntime.addCallback("log", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        List<String> expected = new ArrayList<>(10);
        for (int i = 0; i < EVENT_COUNT; i++) {
            expected.add("pizza");
        }

        SiddhiTestHelper.waitForEvents(SLEEP_TIME, EVENT_COUNT, eventCount, TIMEOUT);
        Assert.assertEquals(receivedEventList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    @AfterMethod
    public void destroy() {
        sseServer.stop(1);
        threadPoolExecutor.shutdownNow();
        log.info("SSE server stopped on PORT " + SSE_SERVER_PORT);
    }
}
