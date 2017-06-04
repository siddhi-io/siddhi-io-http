package org.wso2.siddhi.extension.http.sink;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;

/**
 * Test cases for different method types.
 */
public class HttpMethodType {
    private static final Logger log = Logger.getLogger(HttpMethodType.class);

    /**
     * Creating test for publishing events from GET method.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTestGetMethod() throws Exception {
        log.info("Creating test for publishing events from GET method.");
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
     * Creating test for publishing events from PUT method.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTestPutMethod() throws Exception {
        log.info("Creating test for publishing events from PUT method.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);

        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers='" +
                "{{headers}}',"
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
                "<price>55.645</price><volume>100</volume></event></events>", "PUT", "Name:John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {
        }
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData,
                "<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n");
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }
    /**
     * Creating test for publishing events from DELETE method.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPTestDeleteMethod() throws Exception {
        log.info("Creating test for publishing events from DELETE method.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);

        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers='" +
                "{{headers}}',"
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
                "<price>55.645</price><volume>100</volume></event></events>", "DELETE", "Name:John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {

        }
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData,
                "<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n");
        executionPlanRuntime.shutdown();
        lst.shutdown();

    }

}
