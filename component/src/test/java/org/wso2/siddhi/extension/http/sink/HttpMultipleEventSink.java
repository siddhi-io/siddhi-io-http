package org.wso2.siddhi.extension.http.sink;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.http.sink.util.HttpServerListenerHandler;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;


/**
 * Test cases for multiple event sink synchronously.
 */
public class HttpMultipleEventSink {
    private static final Logger logger = Logger.getLogger(HttpMultipleEventSink.class);

    /**
     * Test cases for multiple event sink synchronously.
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPMultipleEvents() throws Exception {
        logger.info("Creating test for multiple event sink synchronously.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);

        String inStreamDefinition = "Define stream FooStreamA (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers=" +
                "'{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStreamA (message String,method String,headers String);";
        String query = ("@info(name = 'queryA') " +
                "from FooStreamA select message,method,headers insert into BarStreamA;");

        String inStreamDefinition2 = "Define stream FooStreamB (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='http://localhost:8009'," + "method='{{method}}'," + "headers=" +
                "'{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStreamB (message String,method String,headers String);";
        String query2 = ("@info(name = 'queryB') " +
                "from FooStreamB select message,method,headers insert into BarStreamB;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                inStreamDefinition2 + query + query2);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStreamA");
        InputHandler fooStream2 = executionPlanRuntime.getInputHandler("FooStreamB");
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8009, "application/xml");
        executionPlanRuntime.start();
        fooStream.send(new Object[]{"<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", "GET", "Name:John#Age:23"});
        fooStream2.send(new Object[]{"<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", "GET", "Name:John#Age:23"});

        Thread.sleep(500);
        executionPlanRuntime.shutdown();
        lst.shutdown();
    }
}
