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
package org.wso2.extension.siddhi.io.http.test.osgi;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import org.apache.log4j.Logger;
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
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.extension.siddhi.io.http.test.osgi.source.util.TestUtil;
import org.wso2.extension.siddhi.map.text.sourcemapper.TextSourceMapper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;


/**
 * JAAS OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class HttpSourceTestCase {
    private static final Logger logger = Logger.getLogger(HttpSourceTestCase.class);
    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String CLIENTTRUSTSTORE_FILENAME = "client-truststore.jks";
    private static final String KEYSTORESTORE_FILENAME = "wso2carbon.jks";
    @Inject
    protected BundleContext bundleContext;
    private List<String> receivedEventNameList;
    private AtomicInteger eventCount = new AtomicInteger(0);
    private int waitTime = 50;
    private int timeout = 30000;
    @Inject
    private CarbonServerInfo carbonServerInfo;

    // TODO: 9/19/17 Fix after set globe interceptor
    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "config", DEPLOYMENT_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "default", DEPLOYMENT_FILENAME));
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
                        .versionAsInProject())
        };
    }

    @Test
    public void testHTTPInputTransportBasicAuthFalse() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8009));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8009/endpoints/RecPro' " + ")"
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
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8009));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8009/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
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
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8009));
        receivedEventNameList = new ArrayList<>(2);
        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);
        siddhiManager.setExtension("text", TextSourceMapper.class);
        String inStreamDefinition = "" + "@source(type='http', @map(type='text'), "
                + "receiver.url='http://localhost:8009/endpoints/RecPro', " + "basic.auth.enabled='true'" + ")"
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
