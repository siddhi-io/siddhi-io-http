package org.wso2.siddhi.extension.http.sink;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.extension.output.mapper.json.JsonSinkMapper;
import org.wso2.siddhi.extension.output.mapper.text.TextSinkMapper;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;


/**
 * Test case for mapping type.
 */
public class HttpMappingTest {
    private static final Logger log = Logger.getLogger(HttpMappingTest.class);

    /**
     * Creating test for publishing events with XML mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingXML() throws Exception {
        log.info("Creating test for publishing events with XML mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);

        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers=" +
                "'{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8009, "application/xml");
        fooStream.send(new Object[]{"<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", "GET", "Name:John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {
        }
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData, "<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n");
        executionPlanRuntime.shutdown();
        lst.shutdown();

    }

    /**
     * Creating test for publishing events with JSON mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingJson() throws Exception {

        log.info("Creating test for publishing events with JSON mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", JsonSinkMapper.class);

        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers='" +
                "{{headers}}',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8009, "application/json");
        fooStream.send(new Object[]{"{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}", "GET", "Name:" +
                "John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {
        }
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData, "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}\n");
        lst.shutdown();
        executionPlanRuntime.shutdown();
    }

    /**
     * Creating test for publishing events with TEXT mapping.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTextMappingText() throws Exception {

        log.info("Creating test for publishing events with TEXT mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("text-output-mapper", TextSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8005'," + "method='{{method}}'," +
                "" + "headers='{{headers}}',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005, "application/json");
        fooStream.send(new Object[]{"WSO2,55.6,100", "GET", "Name:John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {
        }

        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData, "WSO2,55.6,100\n");
        lst.shutdown();
        ;
        executionPlanRuntime.shutdown();
    }
}
