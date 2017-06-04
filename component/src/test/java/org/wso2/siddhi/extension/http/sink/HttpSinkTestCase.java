package org.wso2.siddhi.extension.http.sink;

import com.sun.net.httpserver.Headers;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;


import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Test cases for content type header.
 */
public class HttpSinkTestCase {
    private static final Logger log = Logger.getLogger(HttpSinkTestCase.class);

    /**
     * Creating test for publishing events without Content-Type header include.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPContentTypeNotIncluded() throws Exception {
        log.info("Creating test for publishing events without Content-Type header include.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers=" +
                "'{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
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
        ArrayList<String> headerName = new ArrayList<>();
        headerName.add("John");
        LinkedList<String> headerAge = new LinkedList<>();
        headerAge.add("23");
        ArrayList<String> headerContentType = new ArrayList<>();
        headerContentType.add("application/xml");
        Headers headers = lst.getServerListner().getHeaders();
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals("<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n", eventData);
        Assert.assertEquals(headers.get("Name").toString(), headerName.toString());
        Assert.assertEquals(headers.get("Age").toString(), headerAge.toString());
        Assert.assertEquals(headers.get("Content-Type").toString(), headerContentType.toString());
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }
    /**
     * Creating test for publishing events including Content-Type header at header list.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPContentTypeAtHeaders() throws Exception {
        log.info("Creating test for publishing events including Content-Type header at header list.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:9009'," + "method='{{method}}'," +
                "" + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " +
                "from FooStream select message,method,headers insert into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(9009, "application/xml");
        fooStream.send(new Object[]{"<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", "GET",
                "Name:John#Age:23#Content-Type:text"});
        while (!lst.getServerListner().iaMessageArrive()) {
        }
        ArrayList<String> headerName = new ArrayList<>();
        headerName.add("John");
        LinkedList<String> headerAge = new LinkedList<>();
        headerAge.add("23");
        ArrayList<String> headerContentType = new ArrayList<>();
        headerContentType.add("text");
        Headers headers = lst.getServerListner().getHeaders();
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals("<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n", eventData);
        Assert.assertEquals(headers.get("Name").toString(), headerName.toString());
        Assert.assertEquals(headers.get("Age").toString(), headerAge.toString());
        Assert.assertEquals(headers.get("Content-Type").toString(), headerContentType.toString());
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }

}

