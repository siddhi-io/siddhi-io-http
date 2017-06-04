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
package org.wso2.siddhi.extension.http.sink;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientConnector;
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
import org.wso2.siddhi.extension.http.sink.util.HttpPayloadDataSource;
import org.wso2.siddhi.extension.http.sink.util.HttpSinkUtil;
import org.wso2.siddhi.extension.http.util.HttpConstants;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@code HttpSink } Handle the HTTP publishing tasks.
 */
@Extension(name = "http", namespace = "sink",
        description = "This extension handle the output transport via http using carbon transport ", parameters = {
        @Parameter(name = "method", description = "Http method such as get,put,post. by default it is post", type =
                {DataType.BOOL}),
        @Parameter(name = "publisher.url", description = "URL of http end pont which events should be send. It is " +
                "mandatory field", type =
                {DataType.STRING}),
        @Parameter(name = "headers", description = "user can specify any number of header with comma separated and " +
                "colan separated header name and value Ex: header1:value1, header2:value2", type = {
                DataType.STRING}),
        @Parameter(name = "basic.auth.enabled", description = "by this user can enable the basic " +
                "authentication if ths parameter is true then basic authentication is enables", type =
                {DataType.STRING}),
        @Parameter(name = "basic.auth.username", description = "by this user can enable give their username. If basic" +
                " auth enable then this is a mandatory argument", type = {DataType.STRING}),
        @Parameter(name = "basic.auth.password", description = "by this user can enable give their password of server" +
                " to be send data. If basic auth enable then this is a mandatory argument", type =
                {DataType.STRING}),
        @Parameter(name = "client.truststore.path", description = "user can give custom client trusore if user never " +
                "mention such then system use default client-trustore in ${carbon.home}/conf/security folder", type =
                {DataType.STRING}),
        @Parameter(name = "client.truststore.pass", description = "user can give custom client trusore pass if user " +
                "never mention such then system use default in deployment YML", type =
                {DataType.STRING})},
        examples = {
                @Example(syntax = "@sink(type='http', topic='stock', @map(type='xml'))\n"
                        + "define stream FooStream (symbol string, price float, volume long);\n", description =
                        "Above configuration will do a default XML input mapping which will " + "generate below " +
                                "output"
                                + "<events>\n"
                                + "    <event>\n"
                                + "        <symbol>WSO2</symbol>\n"
                                + "        <price>55.6</price>\n"
                                + "        <volume>100</volume>\n"
                                + "    </event>\n"
                                + "</events>\n")},
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
                        name = "https.trustStoreFile",
                        description = "The default truststore file path.",
                        defaultValue = "${carbon.home}/conf/security/client-truststore.jks",
                        possibleParameters = "N/A"
                ),
                @SystemParameter(
                        name = "https.trustStorePass",
                        description = "The default truststore pass.",
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
    private Set<TransportProperty> nettyTrasportProperty;
    private Option httpHeaderOption;
    private Option httpMethodOption;
    private String athourizationHeader;
    private String isAuth;

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
        Future future = executorService.submit(new HttpPublisher(cMessage, httpStaticProperties, clientConnector,
                messageBody, streamID));
        try {
            future.get();
        } catch (InterruptedException e) {
            log.error("Thread interrupted while submitting", e);
        } catch (ExecutionException e) {
            log.error("Thread execution terminated unexpectedly while submitting", e);
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
            isAuth = optionHolder.validateAndGetStaticValue(HttpConstants.IS_AUTHENTICATION_REQUIRED,
                    HttpConstants.IS_AUTHENTICATION_REQUIRED_DEFAULT);
            String userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME,
                    HttpConstants.EMPTY_STRING);
            String userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD,
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
            if ((HttpConstants.TRUE.equalsIgnoreCase(isAuth)) && (userName.equals(HttpConstants.EMPTY_STRING) ||
                    userPassword.equals(HttpConstants.EMPTY_STRING))) {
                throw new ExceptionInInitializerError("Please provide user name and password properly in " +
                        HttpConstants.HTTP_SINK_ID + streamID);
            }
            this.httpStaticProperties = new HttpSinkUtil().getHttpStaticProperties(publisherURL);
            this.executorService = executionPlanContext.getExecutorService();
            this.senderConfig = new HttpSinkUtil().getSenderConfigurations(httpStaticProperties, clientStoreFile,
                    clientStorePass);
            this.nettyTrasportProperty = new HttpSinkUtil().getTransportConfigurations(sinkConfigReader);
            byte[] val = (userName + ":" + userPassword).getBytes(Charset.defaultCharset());
            this.athourizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                    (Unpooled.copiedBuffer(val));
        }
    }


    private CarbonMessage generateCarbonMessage(List<Header> headers, HttpPayloadDataSource payload, String contentType,
                                                String httpMethod, CarbonMessage cMessage) {
        //set Static Properties
        //if Authentication enabled
        if (isAuth.equalsIgnoreCase(HttpConstants.TRUE)) {
            cMessage.setHeader(HttpConstants.AUTHORIZATION_HEADER, athourizationHeader);
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
        this.clientConnector = new HTTPClientConnector(senderConfig, nettyTrasportProperty);
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
