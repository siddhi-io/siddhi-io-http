/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.util.HttpServerListenerHandler;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;


/**
 * Test case for mapping type.
 */
public class HttpSinkCustomConfigurationTestCase {
    private static final Logger log = Logger.getLogger(HttpSinkCustomConfigurationTestCase.class);

    /**
     * Creating test for publishing events with XML mapping.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPCustomConfig() throws Exception {
        log.info("Creating test for publishing events with XML mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http'," +
                "publisher.url='http://localhost:8005/abc'," +
                "method='{{method}}'," +
                "headers='{{headers}}'," +
                "socket.idle.timeout='150000'," +
                "ssl.protocol='TLS'," +
                "chunk.disabled='false'," +
                "follow.redirect='false'," +
                "max.redirect.count='5'," +
                "parameters=\"'ciphers : TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256','sslEnabledProtocols : TLSv1.1'\"," +
                "client.pool.configurations=\"'client.connection.pool.count:1','sender.thread.count:20'\"," +
                "client.bootstrap.configurations=\"'client.bootstrap.nodelay:true','client.bootstrap" +
                ".recievebuffersize:1048576'\"," +
                "@map(type='xml', @payload('{{message}}'))) " +
                "Define stream BarStream (message String,method String,headers String);";
        String query = ("@info(name = 'query') " +
                "from FooStream select message,method,headers insert into BarStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpServerListenerHandler lst = new HttpServerListenerHandler(8005);
        lst.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23'"});
        while (!lst.getServerListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = lst.getServerListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        lst.shutdown();
    }
}
