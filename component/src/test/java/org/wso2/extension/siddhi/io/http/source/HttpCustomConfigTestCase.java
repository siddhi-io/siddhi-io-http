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
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Http test case with custom configurations.
 */
public class HttpCustomConfigTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger
            (HttpCustomConfigTestCase.class);
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
    public void testCustomConfiguration() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro', " +
                "basic.auth.enabled='false'," +
                "socket.idle.timeout='150000'," +
                "verify.client='require'," +
                "ssl.protocol='TLS'," +
                "tls.store.type='JKS'," +
                "parameters=\"'ciphers : TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256','sslEnabledProtocols : TLSv1.1," +
                "TLSv1.2'\"," +
                "request.size.validation.configurations=\"'header.size.validation:true','header.validation.maximum" +
                ".request.line:4096'\"," +
                "server.bootstrap.configurations=\"'server.bootstrap.nodelay:true','server.bootstrap" +
                ".sendbuffersize:1048576'\")"
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
     * Creating test for publishing events with XML mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testPauseResume() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        siddhiAppRuntime.restoreLastRevision();
        HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with XML mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testPauseResume2() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        List<String> receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        Thread thread1 = new Thread() {
            public void run() {
                for (List<Source> sourceList : siddhiAppRuntime.getSources()) {
                    for (Source source : sourceList) {
                        source.pause();
                    }
                }
            }
        };
        thread1.start();
        Thread thread3 = new Thread() {
            public void run() {
                HttpTestUtil.httpPublishEvent(event1, baseURI, "/endpoints/RecPro",
                        "POST");
                HttpTestUtil.httpPublishEvent(event2, baseURI, "/endpoints/RecPro",
                        "POST");
            }
        };
        thread3.start();

        Thread thread2 = new Thread() {
            public void run() {
                for (List<Source> sourceList : siddhiAppRuntime.getSources()) {
                    for (Source source : sourceList) {
                        source.resume();
                    }
                }
            }
        };
        thread2.start();
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        siddhiAppRuntime.shutdown();
    }
}
