package org.wso2.siddhi.extension.http.source;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.extension.http.source.util.HttpTestUtil;
import org.wso2.siddhi.extension.input.mapper.xml.XmlSourceMapper;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for basic authentication.
 */
public class HttpSourceBasicAuthTestCases {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger
            (HttpSourceBasicAuthTestCases.class);
    private List<String> receivedEventNameList;
    /**
     * Creating test for publishing events with basic auth false.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testBasicAuthFalse() throws Exception {
        logger.info(" Creating test for publishing events with basic auth false.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9005));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:9005/endpoints/RecPro', " + "is.basic.auth.enabled='false'" + ")"
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
        new HttpTestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false, "text/xml", "POST", 200);
        new HttpTestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "text/xml", "POST", 200);
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with basic auth is not provided.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testBasicAuthNotProvided() throws Exception {
        logger.info("Creating test for publishing events with basic auth is not provided.");
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9005));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:9005/endpoints/RecPro' " + ")"
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
        new HttpTestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false, "text/xml", "POST", 200);
        new HttpTestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "text/xml", "POST", 200);
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList.toString(), expected.toString());
        executionPlanRuntime.shutdown();
    }
}

