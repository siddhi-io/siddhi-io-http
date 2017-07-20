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
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.extension.siddhi.io.http.test.osgi.sink.util.HttpServerListenerHandler;
import org.wso2.extension.siddhi.map.xml.sinkmapper.XMLSinkMapper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class HttpSink {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpSink.class);
    private static final String DEPLOYMENT_FILENAME = "deployment.yaml";
    private static final String CLIENTTRUSTSTORE_FILENAME = "client-truststore.jks";
    private static final String KEYSTORESTORE_FILENAME = "wso2carbon.jks";

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
                systemProperty("java.security.auth.login.config")
                        .value(Paths.get("conf", "security", "carbon-jaas.config").toString())
                //CarbonDistributionOption.debug(5005)
        };
    }

    @Test
    public void testHTTPTextMappingXML() throws Exception {
        logger.info("Creating test for publishing events with XML mapping.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);"
                + "@sink(type='http',publisher.url='http://localhost:8005/abc',method='{{method}}',"
                + "headers='{{headers}}',"
                + "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
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
        fooStream.send(new Object[]{payload, "GET", "'Name:John','Age:23'"});
        while (!lst.getServerListener().iaMessageArrive()) {
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
