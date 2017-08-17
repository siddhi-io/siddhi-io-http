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
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.extension.siddhi.io.http.test.osgi.source.util.TestUtil;
import org.wso2.extension.siddhi.map.text.sourcemapper.TextSourceMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;


import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
public class HttpSourceBasicAuth {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpSourceBasicAuth.class);
    private List<String> receivedEventNameList;
    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String CLIENTTRUSTSTORE_FILENAME = "client-truststore.jks";
    private static final String KEYSTORESTORE_FILENAME = "wso2carbon.jks";
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    protected BundleContext bundleContext;

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
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "security",
                CLIENTTRUSTSTORE_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("resources", "security", CLIENTTRUSTSTORE_FILENAME));
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
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "security",
                KEYSTORESTORE_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("resources", "security", KEYSTORESTORE_FILENAME));
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{copyCarbonYAMLOption(),
                copyCarbonClientTrustStoreOption(),
                copyCarbonKeyStoreOption(),
                CarbonDistributionOption.carbonDistribution(maven()
                        .groupId("org.wso2.extension.siddhi.io.http")
                        .artifactId("org.wso2.extension.io.http.test.distribution")
                        .type("zip")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("siddhi-io-http")
                        .groupId("org.wso2.extension.siddhi.io.http")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("siddhi-map-xml")
                        .groupId("org.wso2.extension.siddhi.map.xml")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("siddhi-map-text")
                        .groupId("org.wso2.extension.siddhi.map.text")
                        .versionAsInProject()),
                systemProperty("java.security.auth.login.config")
                        .value(Paths.get("conf", "security", "carbon-jaas.config").toString())
                //CarbonDistributionOption.debug(5005)
        };
    }

    @Test
    public void testHTTPInputTransportBasicAuthFalse() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8039));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8039/endpoints/RecPro' " + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        String event1 = "name:\"John\",\n" +
                        "age:100,\n" +
                        "country:\"USA\"";
        String event2 = "name:\"Mike\",\n" +
                        "age:100,\n" +
                        "country:\"USA\"";
        new TestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", false, "plain/text",
                "POST");
        new TestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", false, "plain/text",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        logger.info(receivedEventNameList);
        Assert.assertEquals(receivedEventNameList, expected);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testHTTPInputTransportBasicAuthTrue() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8039));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8039/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query') " + "from inputStream " + "select *  " + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        String event1 = "name:\"John\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        String event2 = "name:\"Mike\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        new TestUtil().httpPublishEvent(event1, baseURI, "/endpoints/RecPro", true, "plain/text",
                "POST");
        new TestUtil().httpPublishEvent(event2, baseURI, "/endpoints/RecPro", true, "plain/text",
                "POST");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        logger.info(receivedEventNameList);
        Assert.assertEquals(receivedEventNameList, expected);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testBasicAuthTrueWrongConf() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8039));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8039/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " + "from inputStream " + "select *  " + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        // publishing events
        List<String> expected = new ArrayList<>();
        String event1 = "name:\"John\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        String event2 = "name:\"Mike\",\n" +
                "age:100,\n" +
                "country:\"USA\"";
        new TestUtil().httpPublishEventAuthIncorrect(event1, baseURI, true, "plain/text");
        new TestUtil().httpPublishEventAuthIncorrect(event2, baseURI, true, "plain/text");
        SiddhiTestHelper.waitForEvents(waitTime, 0, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList, expected);
        siddhiAppRuntime.shutdown();
    }
}
