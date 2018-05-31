/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.extension.siddhi.io.http.source;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.source.util.HttpTestUtil;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.extension.siddhi.map.xml.sourcemapper.XmlSourceMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.net.URI;

/**
 * Tests http request source and http response source.
 */

public class HttpRequestResponseTestCase {

    @Test
    public void testHTTPTextMappingXML() throws Exception {

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8010));
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "@Source(type = 'http-request', source.id='testsource', basic.auth" +
                ".enabled='false',receiver.url='http://localhost:8010/TestHTTPRequestResponse/InputStream', @map" +
                "(type='xml', @attributes(messageId='trp:messageId',param1='/event/param1'," +
                "param2='/event/param2')))\n" +
                "define stream InputStream (messageId string, param1 string, param2 string);\n" +
                "@sink(type='http-response', source.id='testsource', message.id='{{messageId}}', @map(type='xml', " +
                "@payload('<event><param1>{{param1}}</param1></event>')))\n" +
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
