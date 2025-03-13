/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.extension.io.http.sink.util.HttpOAuthListenerHandler;
import io.siddhi.extension.map.xml.sinkmapper.XMLSinkMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpOAuthTestCase {
    public static final String CARBON_HOME = "carbon.home";
    private static final Logger log = LogManager.getLogger(HttpAuthTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);

    @BeforeMethod
    public void init() {
        eventCount.set(0);
        setCarbonHome();
    }


    /**
     * Set carbon-home
     */
    private void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty(CARBON_HOME, carbonHome.toString());
        log.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());
    }

    /**
     * Creating test for publishing events with valid access token.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthTrue() throws Exception {
        log.info("Creating test for publishing events with valid access token.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret', " +
                "hostname.verification.enabled='false'," +
                "token.url='https://localhost:8005/token', @map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'," +
                "'Authorization: Bearer yyyyy'"});
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for publishing events without access token. The system will generate new access token
     * using the password/client-credential/refresh grant. The grant type depend on the user input.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthTrueWithoutAccessToken() throws Exception {
        log.info(" Creating test for publishing events with OAuth without access token.");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret', " +
                "hostname.verification.enabled='false'," +
                "token.url='https://localhost:8005/token', @map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'"});
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();

        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for publishing events with expired access token. At this time the system will
     * generate new access token using the client credentials grant then using that newly generated access
     * token and validate the API-endpoint again
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthClientCredentialGrant() throws Exception {
        log.info(" Creating test for publishing events with expired access token and generate new access token " +
                "using client credential grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret', " +
                "token.url='https://localhost:8005/token', hostname.verification.enabled='false'," +
                "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'," +
                "'Authorization:  Bearer xxxxx'"});
        int i = 1;
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive() && i < 1000) {
            Thread.sleep(10);
            i += 1;
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for publishing events with expired access token. At this time the system will
     * generate new access token and refresh token using Password grant then using that newly generated access
     * token and validate the API-endpoint again
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthPasswordGrant() throws Exception {
        log.info(" Creating test for publishing events with expired access token and generate new access token " +
                "using Password grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret'," +
                " token.url='https://localhost:8005/token', oauth.username='admin', oauth.password='admin'," +
                "hostname.verification.enabled='false'," +
                "@map(type='xml', @payload('{{message}}'))) "
                + "Define stream BarStream (message String,method String,headers String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'," +
                "'Authorization:  Bearer xxxxx'"});
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for publishing events with expired access token. At this time the system it will
     * generate new access token and refresh token using Refresh token grant then using that newly generated access
     * token and validate the API-endpoint again
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthRefreshGrant() throws Exception {
        log.info(" Creating test for publishing events with expired access token and generate new access token " +
                "using Refresh grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String," +
                "refreshToken String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret', " +
                "token.url='https://localhost:8005/token', refresh.token='{{refreshToken}}'," +
                "hostname.verification.enabled='false'," +
                " @map(type='xml', @payload('{{message}}'))) " +
                "Define stream BarStream (message String,method String,headers String,refreshToken String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers,refreshToken "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'" +
                "'Authorization:  Bearer xxxxx'", "refreshToken: ppppp"});
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for publishing events with expired access token and refresh token.
     * In this situation the system creates a new access token using client credential grant then using that
     * newly generated access token and validate the API-endpoint again
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPOAuthWithExpiredTokens() throws Exception {
        log.info(" Creating test for publishing events with expired access token, refresh token and generate " +
                "new access token using client credential grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("xml-output-mapper", XMLSinkMapper.class);
        String inStreamDefinition = "Define stream FooStream (message String,method String,headers String," +
                "refreshToken String);" +
                "@sink(type='http',publisher.url='https://localhost:8015/abc',method='{{method}}'" +
                ",headers='{{headers}}',consumer.key='addConsumerKey', consumer.secret='addConsumerSecret'," +
                " token.url='https://localhost:8005/token', refresh.token='{{refreshToken}}', " +
                "hostname.verification.enabled='false'," +
                "@map(type='xml', @payload('{{message}}'))) " +
                "Define stream BarStream (message String,method String,headers String,refreshToken String);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,method,headers,refreshToken "
                        + "insert into BarStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        String payload = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>";
        fooStream.send(new Object[]{payload, "POST", "'Name:John','Age:23','Country:USA'," +
                "'Authorization:  Bearer xxxxx'", "refreshToken: zzzzz"});
        while (!httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().isMessageArrive()) {
            Thread.sleep(10);
        }
        String eventData = httpOAuthListenerHandler.getHttpOAuthTokenEndpointListener().getData();
        String expected = "<events>"
                + "<event>"
                + "<symbol>WSO2</symbol>"
                + "<price>55.645</price>"
                + "<volume>100</volume>"
                + "</event>"
                + "</events>\n";
        Assert.assertEquals(eventData, expected);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for send a request and get a response with expired access token. From the system it will generate
     * new access token using client credential grant
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPRequestResponse() throws Exception {
        log.info(" Creating test for send a request and get a response with expired access token and generate" +
                " new access token using client credential grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream FooStream (message String,headers String);"
                + "@sink(type='http-request',publisher.url='https://localhost:8015/abc', method='POST',"
                + "headers='{{headers}}',sink.id='source-1',consumer.key='addConsumerKey',"
                + " consumer.secret='addConsumerSecret', token.url='https://localhost:8005/token', "
                + "hostname.verification.enabled='false',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String, headers String);"
                + "@source(type='http-response', sink.id='source-1', "
                + "@map(type='json',@attributes(name='name', id='id')))"
                + "define stream responseStream(name String, id int);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,headers "
                        + "insert into BarStream;"
        );

        String payload = "{\"name\":\"wso2\", \"id\":\"1234\"}";
        String headers = "'company:wso2','country:sl','Content-Type:plan/text','Authorization: Bearer xxxxx'";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (int i = 0; i < events.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals("wso2", (String) events[i].getData()[0]);
                            Assert.assertEquals(1234, events[i].getData()[1]);
                            break;

                        default:
                            Assert.fail();
                    }
                }
            }
        };
        siddhiAppRuntime.addCallback("responseStream", streamCallback);
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8015);
        httpOAuthListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{payload, headers});
        Thread.sleep(1000);
        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

    /**
     * Creating test for send a request and get a response with expired access token. From the system it will generate
     * new access token using client credential grant
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void testHTTPCallResponse() throws Exception {
        log.info(" Creating test for send a request and get a response with expired access token and generate" +
                " new access token using client credential grant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream FooStream (message String,headers String);"
                + "@sink(type='http-call',publisher.url='https://localhost:8016/abc', method='POST',"
                + "headers='{{headers}}',sink.id='source-1',consumer.key='addConsumerKey',"
                + " consumer.secret='addConsumerSecret', token.url='https://localhost:8005/token', "
                + "hostname.verification.enabled='false',"
                + "@map(type='json', @payload('{{message}}'))) "
                + "Define stream BarStream (message String, headers String);"
                + "@source(type='http-call-response', sink.id='source-1', "
                + "@map(type='json',@attributes(name='name', id='id')))"
                + "define stream responseStream(name String, id int);";
        String query = (
                "@info(name = 'query') "
                        + "from FooStream "
                        + "select message,headers "
                        + "insert into BarStream;"
        );

        String payload = "{\"name\":\"wso2\", \"id\":\"1234\"}";
        String headers = "'company:wso2','country:sl','Content-Type:plan/text','Authorization: Bearer xxxxx'";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (int i = 0; i < events.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals("wso2", (String) events[i].getData()[0]);
                            Assert.assertEquals(1234, events[i].getData()[1]);
                            break;

                        default:
                            Assert.fail();
                    }
                }
            }
        };
        siddhiAppRuntime.addCallback("responseStream", streamCallback);
        HttpOAuthListenerHandler httpOAuthListenerHandler = new HttpOAuthListenerHandler(8005, 8016);
        httpOAuthListenerHandler.run();
        siddhiAppRuntime.start();

        fooStream.send(new Object[]{payload, headers});
        Thread.sleep(1000);
        SiddhiTestHelper.waitForEvents(1000, 1, eventCount, 1000);

        Assert.assertEquals(eventCount.get(), 1);
        siddhiAppRuntime.shutdown();
        httpOAuthListenerHandler.shutdown();
    }

}
