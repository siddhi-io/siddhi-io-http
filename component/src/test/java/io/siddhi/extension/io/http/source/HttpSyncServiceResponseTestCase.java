/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.http.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.extension.io.http.source.util.HttpTestUtil;
import io.siddhi.extension.map.xml.sinkmapper.XMLSinkMapper;
import io.siddhi.extension.map.xml.sourcemapper.XmlSourceMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

/**
 * Tests http request source and http response source.
 */

public class HttpSyncServiceResponseTestCase {

    @Test
    public void testHTTPTextMappingXML() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8010));
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@Source(type = 'http-service', source.id='testsource', basic.auth" +
                ".enabled='false',receiver.url='http://localhost:8010/TestHTTPRequestResponse/InputStream', @map" +
                "(type='xml', @attributes(messageId='trp:messageId',param1='/event/param1'," +
                "param2='/event/param2')))\n" +
                "define stream InputStream (messageId string, param1 string, param2 string);\n" +
                "@sink(type='http-service-response', source.id='testsource', message.id='{{messageId}}', " +
                "@map(type='xml', @payload('<event><param1>{{param1}}</param1></event>')))\n" +
                "define stream OutputStream (messageId string, param1 string);\n" +
                "from InputStream\n" +
                "select messageId, param1\n" +
                "insert into OutputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        siddhiAppRuntime.start();

        String event1 = "<event>\n" +
                "  <param1>param1</param1>\n" +
                "  <param2>param2</param2>\n" +
                "</event>";


        String response = HttpTestUtil.sendHttpEvent(event1, baseURI, "/TestHTTPRequestResponse/InputStream",
                false, "application/xml");
        Assert.assertEquals(response, "<event><param1>param1</param1></event>");

        siddhiAppRuntime.shutdown();
    }
}
