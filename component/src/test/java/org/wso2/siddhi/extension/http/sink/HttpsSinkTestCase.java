package org.wso2.siddhi.extension.http.sink;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.http.sink.util.HttpsServerListnerHandler;
import org.wso2.siddhi.extension.output.mapper.xml.XMLSinkMapper;


import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Https protocol function tests.
 */
public class HttpsSinkTestCase {
    private static final Logger logger = Logger
            .getLogger(HttpsSinkTestCase.class);

    /**
     * Test case for HTTP output publisher.
     */
    public void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test", "resources");
        System.setProperty(Constants.CARBON_HOME, carbonHome.toString());
        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

    }

    /**
     * Test case for HTTPS output publisher.
     */
    @Test
    public void testHTTPSPublisher() throws Exception {
        setCarbonHome();
        logger.info("Test case for HTTPS output publisher.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http'," + "publisher.url='https://localhost:8050/rec/pro'," + "method='{{method}}'," +
                "" + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query1') " + "from FooStream select message,method,headers insert " +
                "into BarStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream");
        executionPlanRuntime.start();
        HttpsServerListnerHandler lst = new HttpsServerListnerHandler(8050, "application/xml");
        fooStream.send(new Object[]{"<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", "POST", "Name:John#Age:23"});
        while (!lst.getServerListner().iaMessageArrive()) {
            Thread.sleep(100);
        }
        String eventData = lst.getServerListner().getData();
        Assert.assertEquals(eventData, "<events><event><symbol>WSO2</symbol>" +
                        "<price>55.645</price><volume>100</volume></event></events>\n");
        executionPlanRuntime.shutdown();
    }
}

