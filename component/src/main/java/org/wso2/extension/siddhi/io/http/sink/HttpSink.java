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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientConnector;
import org.wso2.extension.siddhi.io.http.sink.util.HttpPayloadDataSource;
import org.wso2.extension.siddhi.io.http.sink.util.HttpSinkUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@code HttpSink } Handle the HTTP publishing tasks.
 */
@Extension(name = "http", namespace = "sink",
        description = "This is description for http sink extension. This extension publish the http events in any " +
                "method types such as POST, GET, PUT, DELETE  via http or https protocols. As the additional features" +
                " this component can provide basic authentication as well as user can publish events using custom " +
                "client truststore files when publishing events via https protocol. And also user can add any number" +
                " of headers for each event dynamically.",
        parameters = {
                @Parameter(name = "publisher.url", description = "The URL to which the outgoing events published via " +
                        "HTTP must be sent. If this is not specified, an error is logged in the CLI. If you want to " +
                        "enable SSO authentication for the event flow, use `https` instead of `http` in the URL." +
                        "e.g., `http://localhost:8080/EndPoint`, `https://localhost:8080/EndPoint`",
                        type = {DataType.STRING}),
                @Parameter(name = "basic.auth.username", description = "The username to include in the authentication" +
                        " header of the events to be published if basic authentication is enabled for the event flow" +
                        " (i.e., via the `basic.auth.enabled` parameter). It is required to specify a username when " +
                        "basic authentication is enabled.", type = {DataType.STRING}, optional = true),
                @Parameter(name = "basic.auth.password", description = "The password to include in the authentication" +
                        " header of the events to be published if basic authentication is enabled for the event flow " +
                        "(i.e., via the `basic.auth.enabled` parameter). It is required to specify a password when " +
                        "basic authentication is enabled.", type = {DataType.STRING}, optional = true),
                @Parameter(name = "client.truststore.path", description = "The file path to the location of the " +
                        "truststore of the WSO2 DAS client at sends the HTTP events. A custom client trust store can " +
                        "be specified if required. If no custom trust store is specified, the system uses the default" +
                        "client-trustore in the`${carbon.home}/conf/security` directory.", type = {DataType.STRING},
                        optional = true),
                @Parameter(name = "client.truststore.pass", description = "The password to access the client " +
                        "truststore. A custom password can be specified if required. If no custom password is " +
                        "specified, the system uses the default password in the deployment YML.",
                        type = {DataType.STRING}, optional = true)},
        examples = {
                @Example(syntax = "@sink(type='http',publisher.url='http://localhost:8009', method='{{method}}'," +
                        "headers='{{headers}}', @map(type='xml' , @payload('{{payloadBody}}')))" +
                        "define stream FooStream (payloadBody String, method string, headers string);\n", description =
                        "Expected input should be in following format:" +
                                "{" +
                                "<events>\n"
                                + "    <event>\n"
                                + "        <symbol>WSO2</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>\n"
                                + ","
                                + "POST"
                                + "Content-Length:24#Content-Location:USA#Retry-After:120"
                                + "}"
                                + "Above configuration will do a default XML input mapping which will "
                                + "generate as below "
                                + "~Output payload"
                                + "<events>\n"
                                + "    <event>\n"
                                + "        <symbol>WSO2</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>\n"
                                + "~Output headers"
                                + "Content-Length:24,Content-Location:USA,Retry-After:120,"
                                + "Content-Type:application/xml"
                                + "~Output property"
                                + "HTTP_METHOD:POST"
                                + "If user wish to have basic authentication enabled then it is expected to have "
                                + "parameter set basic.auth.enabled='true' parameter along with basic.auth"
                                + ".username='userName' , basic.auth.password='passWord'. Then output contains the "
                                + "Authorization header as well")},
        systemParameter = {
                @SystemParameter(
                        name = "latency.metrics.enabled",
                        description = "Netty transportation property.",
                        defaultValue = "true",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "Netty transportation property.",
                        defaultValue = "15",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "client.bootstrap.socket.timeout",
                        description = "Netty transportation property.",
                        defaultValue = "15",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.host",
                        description = "The default host.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.port",
                        description = "The default port.",
                        defaultValue = "9763",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "default.protocol",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.truststore.file",
                        description = "The default truststore file path.",
                        defaultValue = "${carbon.home}/conf/security/client-truststore.jks",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.truststore.pass",
                        description = "The default truststore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "N/A"
                )
        }
)
public class HttpSink extends Sink {
    private static final Logger log = Logger.getLogger(HttpSink.class);
    private String streamID;
    private ExecutorService executorService;
    private HTTPClientConnector clientConnector;
    private Set<SenderConfiguration> senderConfig;
    private String mapType;
    private Map<String, String> httpStaticProperties;
    private Set<TransportProperty> nettyTransportProperty;
    private Option httpHeaderOption;
    private Option httpMethodOption;
    private String authorizationHeader;
    private String userName;
    private String userPassword;
    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions)
            throws ConnectionUnavailableException {
        String headers = httpHeaderOption.getValue(transportOptions);
        String httpMethod = HttpConstants.EMPTY_STRING.equals(httpMethodOption.getValue(transportOptions)) ?
                HttpConstants.METHOD_DEFAULT : httpMethodOption.getValue(transportOptions);
        List<Header> headersList = new HttpSinkUtil().getHeaders(headers);
        String contentType = new HttpSinkUtil().getContentType(mapType, headersList);
        String messageBody = (String) payload;
        CarbonMessage cMessage = new DefaultCarbonMessage();
        HttpPayloadDataSource messageDataSource = new HttpPayloadDataSource(messageBody, cMessage.getOutputStream());
        messageDataSource.setOutputStream(cMessage.getOutputStream());
        cMessage = generateCarbonMessage(headersList, messageDataSource, contentType, httpMethod, cMessage);
        try {
            clientConnector.send(cMessage, new HttpSinkCallback(messageBody), httpStaticProperties);
        } catch (ClientConnectorException e) {
            log.error("Error sending the HTTP message with payload " + payload + " in " +
                    HttpConstants.HTTP_SINK_ID + streamID, e);
        }
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{HttpConstants.HEADERS, HttpConstants.METHOD};
    }

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ConfigReader sinkConfigReader, ExecutionPlanContext executionPlanContext) {
        streamID = outputStreamDefinition.toString();
        if (executorService == null) {
            this.mapType = outputStreamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                    .getValue();
            String publisherURL = optionHolder.validateAndGetStaticValue(HttpConstants.PUBLISHER_URL,
                    HttpConstants.
                            EMPTY_STRING);
            httpHeaderOption = optionHolder.validateAndGetOption(HttpConstants.HEADERS);
            httpMethodOption = optionHolder.validateAndGetOption(HttpConstants.METHOD);
            userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME,
                    HttpConstants.EMPTY_STRING);
            userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD,
                    HttpConstants.EMPTY_STRING);
            String[] defaultTrustStoreValues = new HttpSinkUtil().trustStoreValues(sinkConfigReader);
            String clientStoreFile = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PATH,
                    defaultTrustStoreValues[0]);
            String clientStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PASS,
                    defaultTrustStoreValues[1]);
            if (publisherURL.equals(HttpConstants.EMPTY_STRING)) {
                throw new ExceptionInInitializerError("Receiver URL found empty but it is Mandatory field in " +
                        "" + HttpConstants.HTTP_SINK_ID + streamID);
            }
            if ((userName.equals(HttpConstants.EMPTY_STRING) ^
                    userPassword.equals(HttpConstants.EMPTY_STRING))) {
                throw new ExceptionInInitializerError("Please provide user name and password in " +
                        HttpConstants.HTTP_SINK_ID + streamID);
            } else if (!(userName.equals(HttpConstants.EMPTY_STRING) || userPassword.equals
                    (HttpConstants.EMPTY_STRING))) {
                byte[] val = (userName + ":" + userPassword).getBytes(Charset.defaultCharset());
                this.authorizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                        (Unpooled.copiedBuffer(val));
            }
            this.httpStaticProperties = new HttpSinkUtil().getHttpStaticProperties(publisherURL);
            this.executorService = executionPlanContext.getExecutorService();
            this.senderConfig = new HttpSinkUtil().getSenderConfigurations(httpStaticProperties, clientStoreFile,
                    clientStorePass);
            this.nettyTransportProperty = new HttpSinkUtil().getTransportConfigurations(sinkConfigReader);
        }
    }

    private CarbonMessage generateCarbonMessage(List<Header> headers, HttpPayloadDataSource payload, String contentType,
                                                String httpMethod, CarbonMessage cMessage) {
        //if Authentication enabled
        if (!(userName.equals(HttpConstants.EMPTY_STRING) || userPassword.equals
                (HttpConstants.EMPTY_STRING))) {
            cMessage.setHeader(HttpConstants.AUTHORIZATION_HEADER, authorizationHeader);
        }
        // Set meta data
        cMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL,
                httpStaticProperties.get(HttpConstants.PROTOCOL));
        // Set url
        cMessage.setProperty(org.wso2.carbon.messaging.Constants.TO, httpStaticProperties.get(HttpConstants.TO));
        //set host and port
        cMessage.setProperty(HttpConstants.HOST, httpStaticProperties.get(HttpConstants.HOST));
        cMessage.setProperty(HttpConstants.PORT,
                Integer.parseInt(httpStaticProperties.get(HttpConstants.PORT)));
        // Set method
        cMessage.setProperty(HttpConstants.HTTP_METHOD, httpMethod);
        //Set Dynamic properties
        // Set Headers
        if (headers != null) {
            cMessage.setHeaders(headers);
        }
        // Set content type if content type s not included in headers
        if (contentType.contains(mapType)) {
            cMessage.setHeader(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }
        // Set message body
        if (payload != null) {
            payload.setOutputStream(cMessage.getOutputStream());
            cMessage.setMessageDataSource(payload);
            cMessage.setAlreadyRead(true);
        }
        //Handel Empty Messages
        if (cMessage.isEmpty() && cMessage.getMessageDataSource() == null) {
            cMessage.setEndOfMsgAdded(true);
        }
        return cMessage;
    }

    @Override
    public void connect() {
        this.clientConnector = new HTTPClientConnector(senderConfig, nettyTransportProperty);
        log.info(streamID + " has successfully connected to ");
    }

    @Override
    public void disconnect() {
        clientConnector = null;
    }

    @Override
    public void destroy() {
        try {
            executorService.awaitTermination(60, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Failed to shutdown server in " + HttpConstants.HTTP_SINK_ID + streamID, e);
        }
    }
}
