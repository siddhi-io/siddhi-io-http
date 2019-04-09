/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.http.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.util.HttpFileServerListenerHandler;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpRequestResponseTestCase {
    private static final Logger log = Logger.getLogger(HttpRequestResponseTestCase.class);

    private AtomicInteger eventCount = new AtomicInteger(0);
    private String downloadPath;
    private String rootPath;


    @BeforeClass
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        rootPath = classLoader.getResource("files").getFile();
        downloadPath = rootPath + "/downloads";
    }

    @Test
    public void testHTTPRequestResponse1() throws Exception {
        log.info("Send a POST request with a json body message and receive the response");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "define stream FooStream (message String,headers String);"
                + "@sink(type='http-request',publisher.url='http://localhost:8005/abc'," +
                " method='POST',"
                + "headers='{{headers}}',sink.id='source-1',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String, headers String);" +
                "" +
                "@source(type='http-response', sink.id='source-1', " +
                "@map(type='json',@attributes(name='name', id='id')))" +
                "define stream responseStream(name String, id int);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,headers "
                        + "insert into BarStream;"
        );

        String payload = "{\"name\":\"wso2\", \"id\":\"1234\"}";
        String headers = "'comapny:wso2', country:sl'";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (int i = 0; i < events.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals("wso2", (String) events[i].getData()[0]);
                            Assert.assertEquals(1234, events[i].getData()[1]);
                            break;

                        default:
                            Assert.fail();
                    }
                }
            }
        };

        siddhiAppRuntime.addCallback("responseStream", streamCallback);
        HttpServerListenerHandler httpServerListenerHandler = new HttpServerListenerHandler(8005);
        httpServerListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{payload, headers});
        Thread.sleep(1000);
        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpServerListenerHandler.shutdown();
    }

    @Test
    public void testHTTPRequestResponse2() throws Exception {
        log.info("Send a POST request with a json body message and receive the response along with attributes in the " +
                "request");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "define stream FooStream (name String, id int, headers String, volume long, price float);"
                + "@sink(type='http-request',publisher.url='http://localhost:8005/abc'," +
                " method='POST',"
                + "headers='{{headers}}',sink.id='source-1',"
                + "@map(type='json', @payload(\"\"\"{\"name\":\"{{name}}\",\"id\":{{id}}}\"\"\"))) "
                + "Define stream BarStream (name String, id int, headers String, volume long, price float);" +
                "" +
                "@source(type='http-response', sink.id='source-1', " +
                "@map(type='json',@attributes(name='name', id='id', headers='trp:headers', volume='trp:volume', " +
                "price='trp:price')))" +
                "define stream responseStream(name String, id int, headers String, volume long, price float);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select * "
                        + "insert into BarStream;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (int i = 0; i < events.length; i++) {
                    EventPrinter.print(events);
                    switch (eventCount.getAndIncrement()) {
                        case 0:
                            Assert.assertEquals(events[i].getData()[0].toString(), "wso2");
                            Assert.assertEquals(events[i].getData()[1], 100);
                            Assert.assertEquals(events[i].getData()[2].toString(), "'country:sri-lanka'");
                            Assert.assertEquals(events[i].getData(3), "1000");
                            Assert.assertEquals(events[i].getData(4).toString(), "111.11");
                            break;

                        default:
                            Assert.fail();
                    }
                }
            }
        };

        siddhiAppRuntime.addCallback("responseStream", streamCallback);
        HttpServerListenerHandler httpServerListenerHandler = new HttpServerListenerHandler(8005);
        httpServerListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{"wso2", 100, "'country:sri-lanka'", 1000L, 111.11f});

        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpServerListenerHandler.shutdown();
    }

    @Test
    public void testHTTPRequestResponse3() throws Exception {
        log.info("Download a file");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "define stream FooStream (name String, id int, headers String, downloadPath string);"
                + "@sink(type='http-request'," +
                "downloading.enabled='true'," +
                "download.path='{{downloadPath}}'," +
                "publisher.url='http://localhost:8005/files'," +
                " method='GET',"
                + "headers='{{headers}}',sink.id='source-1',"
                + "@map(type='json')) "
                + "Define stream BarStream (name String, id int, headers String, downloadPath string);" +
                "" +
                "@source(type='http-response', sink.id='source-1', http.status.code='2\\d+', " +
                "@map(type='text', regex.A='((.|\\n)*)', @attributes(headers='trp:headers', fileName='A[1]'))) " +
                "define stream responseStream(fileName string, headers string);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select * "
                        + "insert into BarStream;"
        );

        String downloadPath = rootPath + File.separator + "downloadedFile.txt";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (int i = 0; i < events.length; i++) {
                    EventPrinter.print(events);
                    switch (eventCount.getAndIncrement()) {
                        case 0:
                            Assert.assertEquals(events[i].getData()[0].toString(), downloadPath);
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        };

        siddhiAppRuntime.addCallback("responseStream", streamCallback);
        HttpFileServerListenerHandler httpFileServerListenerHandler = new HttpFileServerListenerHandler(8005);
        httpFileServerListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{"wso2", 100, "'country:sri-lanka'", downloadPath});

        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        File file = new File(downloadPath);
        Assert.assertTrue(file != null);
        Assert.assertTrue(file.isFile());
        Assert.assertEquals(file.getName(), "downloadedFile.txt");

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpFileServerListenerHandler.shutdown();
    }

    @Test
    public void testHTTPRequestResponse4() throws Exception {
        log.info("Try to download a file that not exists.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "define stream FooStream (name String, id int, headers String, downloadPath string);" +
                "@sink(type='http-request'," +
                "downloading.enabled='true'," +
                "download.path='{{downloadPath}}'," +
                "publisher.url='http://localhost:8005/files2', " +
                "method='GET'," +
                "headers='{{headers}}',sink.id='source-1'," +
                "@map(type='json')) " +
                "Define stream BarStream (name String, id int, headers String, downloadPath string);" +
                "" +
                "@source(type='http-response', sink.id='source-1', http.status.code='2\\d+', " +
                "@map(type='text', regex.A='((.|\\n)*)', @attributes(headers='trp:headers', fileName='A[1]'))) " +
                "define stream responseStream2xx(fileName string, headers string);" +

                "@source(type='http-response', sink.id='source-1', http.status.code='4\\d+', " +
                "@map(type='text', regex.A='((.|\\n)*)', @attributes(headers='trp:headers', errorMsg='A[1]'))) " +
                "define stream responseStream4xx(errorMsg string);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select * "
                        + "insert into BarStream;"
        );

        String downloadPath = rootPath + File.separator + "downloadedFile.txt";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback2xx = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventCount.getAndIncrement();
                Assert.fail();
            }
        };

        StreamCallback streamCallback4xx = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (int i = 0; i < events.length; i++) {
                    switch (eventCount.getAndIncrement()) {
                        case 0:
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        };

        siddhiAppRuntime.addCallback("responseStream2xx", streamCallback2xx);
        siddhiAppRuntime.addCallback("responseStream4xx", streamCallback4xx);
        HttpFileServerListenerHandler httpFileServerListenerHandler = new HttpFileServerListenerHandler(8005, 400);
        httpFileServerListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{"wso2", 100, "'country:sri-lanka'", downloadPath});

        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        File file = new File(downloadPath);
        Assert.assertTrue(file != null);
        Assert.assertTrue(file.isFile());
        Assert.assertEquals(file.getName(), "downloadedFile.txt");

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpFileServerListenerHandler.shutdown();
    }
}
