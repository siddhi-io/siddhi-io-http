/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.http.test.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.extension.siddhi.io.http.test.osgi.util.TestUtil;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.extension.input.mapper.xml.XmlSourceMapper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;


import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;


/**
 * JAAS OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class BasicAuthTrue {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BasicAuthTrue.class);
    private List<String> receivedEventNameList;
    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String CLIENTTRUSTSTORE_FILENAME = "client-truststore.jks";
    private static final String KEYSTORESTORE_FILENAME = "wso2carbon.jks";
    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "netty", DEPLOYMENT_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", DEPLOYMENT_FILENAME));
    }

    /**
     * Place default client  trusts-sore.jks file in conf security folder.
     */
    private Option copyCarbonClientTrustStoreOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "conf", "security",
                CLIENTTRUSTSTORE_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "security", CLIENTTRUSTSTORE_FILENAME));
    }

    /**
     * Place default client  key-store.jks file in conf security folder.
     */
    private Option copyCarbonKeyStoreOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "conf", "security",
                KEYSTORESTORE_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "security", KEYSTORESTORE_FILENAME));
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{copyCarbonYAMLOption(), copyCarbonClientTrustStoreOption(), copyCarbonKeyStoreOption(),
                copyOSGiLibBundle(maven().artifactId("siddhi-io-http").groupId("org.wso2.extension.siddhi.io.http")
                        .versionAsInProject()), systemProperty("java.security.auth.login.config").value(Paths.get
                ("conf", "security", "carbon-jaas.config").toString())};
    }

    @Test
    public void testHTTPInputTransportBasicAuthFalse() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8055));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8055/endpoints/RecPro', " + "basic.auth.enabled='false'" + ")"
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
        new TestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false, "text/xml",
                "POST");
        new TestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "text/xml",
                "POST");
        Thread.sleep(200);
        logger.info(receivedEventNameList);
        Assert.assertEquals(receivedEventNameList, expected);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testHTTPInputTransportBasicAuthTrue() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8005));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        //siddhiManager.setExtension("json-input-mapper", JsonSourceMapper.class);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:8005/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
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
        new TestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", true, "text/xml",
                "POST");
        new TestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", true, "text/xml",
                "POST");
        Thread.sleep(200);
        logger.info(receivedEventNameList);
        Assert.assertEquals(receivedEventNameList, expected);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testBasicAuthTrueWrongConf() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9055));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("xml-input-mapper", XmlSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='xml'), "
                + "receiver.url='http://localhost:9055/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
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
        List<String> expected = new ArrayList<>();
        String event1 =
                "<events><event><name>John</name>" + "<age>100</age><country>Sri Lanka</country></event></events>";
        String event2 = "<events><event><name>Mike</name>" + "<age>20</age><country>USA</country></event></events>";
        new TestUtil().httpPublishEventAuthIncorrect(event1, baseURI, true, "text/xml");
        new TestUtil().httpPublishEventAuthIncorrect(event2, baseURI, true, "text/xml");
        Thread.sleep(100);
        Assert.assertEquals(receivedEventNameList, expected);
        executionPlanRuntime.shutdown();
    }
}
