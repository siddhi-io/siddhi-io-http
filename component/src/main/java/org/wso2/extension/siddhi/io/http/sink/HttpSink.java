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
import org.wso2.carbon.transport.http.netty.common.Constants;
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
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code HttpSink } Handle the HTTP publishing tasks.
 */
@Extension(name = "http", namespace = "sink",
        description = "This extension publish the HTTP events in any HTTP method  POST, GET, PUT, DELETE  via HTTP " +
                "or https protocols. As the additional features this component can provide basic authentication " +
                "as well as user can publish events using custom client truststore files when publishing events " +
                "via https protocol. And also user can add any number of headers including HTTP_METHOD header for " +
                "each event dynamically.",
        parameters = {
                @Parameter(
                        name = "publisher.url",
                        description = "The URL to which the outgoing events should be published via HTTP. " +
                                "This is a mandatory parameter and if this is not specified, an error is logged in " +
                                "the CLI. If user wants to enable SSL for the events, use `https` instead of `http` " +
                                "in the publisher.url." +
                                "e.g., " +
                                    "`http://localhost:8080/endpoint`, "
                                    + "`https://localhost:8080/endpoint`",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "basic.auth.username",
                        description = "The username to be included in the authentication header of the basic " +
                                "authentication enabled events. It is required to specify both username and " +
                                "password to enable basic authentication. If one of the parameter is not given " +
                                "by user then an error is logged in the CLI." ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = " "),
                @Parameter(
                        name = "basic.auth.password",
                        description = "The password to include in the authentication header of the basic " +
                                "authentication enabled events. It is required to specify both username and " +
                                "password to enable basic authentication. If one of the parameter is not given " +
                                "by user then an error is logged in the CLI.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = " "),
                @Parameter(
                        name = "client.truststore.path",
                        description = "The file path to the location of the truststore of the client that sends " +
                                "the HTTP events through 'https' protocol. A custom client-truststore can be " +
                                "specified if required.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "${carbon.home}/resources/security/client-truststore.jks"),
                @Parameter(
                        name = "client.truststore.password",
                        description = "The password for the client-truststore. A custom password can be specified " +
                                "if required. If no custom password is specified and the protocol of URL is 'https' " +
                                "then, the system uses default password.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "wso2carbon"),
                @Parameter(
                        name = "headers",
                        description = "The headers that should be included as a HTTP request headers. There can be " +
                                "any number of headers concatenated on following format. " +
                                "header1:value1#header2:value2. User can include content-type header if he need to " +
                                "any specific type for payload if not system get the mapping type as the content-Type" +
                                " header (ie. @map(xml):application/xml,@map(json):application/json,@map(text)" +
                                ":plain/text ) and if user does not include any mapping type then system gets the " +
                                "'plain/text' as default Content-Type header. If user does not include " +
                                "Content-Length header then system calculate the bytes size of payload and include it" +
                                " as content-length header.",
                        type = {DataType.STRING},
                        optional = true,
                        dynamic = true, defaultValue = " "),
                @Parameter(
                        name = "method",
                        description = "For HTTP events, HTTP_METHOD header should be included as a request header." +
                                " If the parameter is null then system uses 'POST' as a default header.",
                        type = {DataType.STRING},
                        optional = true,
                        dynamic = true, defaultValue = "POST")
        },
        examples = {
                @Example(syntax =
                        "@sink(type='http',publisher.url='http://localhost:8009/foo', method='{{method}}',"
                        + "headers='{{headers}}', "
                        + "@map(type='xml' , @payload('{{payloadBody}}')))"
                        + "define stream FooStream (payloadBody String, method string, headers string);\n",
                        description =
                        "If it is xml mapping expected input should be in following format for FooStream:"
                                + "{"
                                + "<events>"
                                + "    <event>"
                                + "        <symbol>WSO2</symbol>"
                                + "        <price>55.6</price>"
                                + "        <volume>100</volume>"
                                + "    </event>"
                                + "</events>,"
                                + "POST,"
                                + "Content-Length:24#Content-Location:USA#Retry-After:120"
                                + "}"

                                + "Above event will generate output as below."
                                + "~Output http event payload"
                                            + "<events>\n"
                                            + "    <event>\n"
                                            + "        <symbol>WSO2</symbol>\n"
                                            + "        <price>55.6</price>\n"
                                            + "        <volume>100</volume>\n"
                                            + "    </event>\n"
                                            + "</events>\n"
                                + "~Output http event headers"
                                            + "Content-Length:24,"
                                            + "Content-Location:'USA',"
                                            + "Retry-After:120,"
                                            + "Content-Type:'application/xml',"
                                            + "HTTP_METHOD:'POST',"
                                + "~Output http event properties"
                                            + "HTTP_METHOD:'POST',"
                                            + "HOST:'localhost',"
                                            + "PORT:8009"
                                            + "PROTOCOL:'http'"
                                            + "TO:'/foo'"
                        )},
        systemParameter = {
                @SystemParameter(
                        name = "latency.metrics.enabled",
                        description = "Property to enable metrics logs to monitor transport latency for netty.",
                        defaultValue = "true",
                        possibleParameters = "Any Integer"
                ),
                @SystemParameter(
                        name = "server.bootstrap.socket.timeout",
                        description = "Property to configure specified timeout in milliseconds which server " +
                                "socket will block for this amount of time for http message content to be received.",
                        defaultValue = "15",
                        possibleParameters = "Any Integer"
                ),
                @SystemParameter(
                        name = "server.bootstrap.boss.group.size",
                        description = "Property to configure number of boss threads, which accepts incoming " +
                                "connections until the ports are unbound. Once connection accepts successfully, " +
                                "boss thread passes the accepted channel to one of the worker threads.",
                        defaultValue = "4",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "server.bootstrap.worker.group.size",
                        description = "Property to configure number of worker threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "8",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "default.protocol",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = {"http" , "https"}
                ),
                @SystemParameter(
                        name = "https.truststore.file",
                        description = "The default truststore file path.",
                        defaultValue = "${carbon.home}/resources/security/client-truststore.jks",
                        possibleParameters = "Path to client-truststore.jks"
                ),
                @SystemParameter(
                        name = "https.truststore.password",
                        description = "The default truststore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Truststore password"
                )
        }
)
public class HttpSink extends Sink {
    private static final Logger log = Logger.getLogger(HttpSink.class);
    private String streamID;
    private HTTPClientConnector clientConnector;
    private Set<SenderConfiguration> senderConfig;
    private String mapType;
    private Map<String, String> httpURLProperties;
    private Set<TransportProperty> nettyTransportProperty;
    private Option httpHeaderOption;
    private Option httpMethodOption;
    private String authorizationHeader;
    private String userName;
    private String userPassword;
    private String publisherURL;
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{HttpConstants.HEADERS, HttpConstants.METHOD};
    }

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.streamID = siddhiAppContext.getName() + ":" + outputStreamDefinition.toString();
        this.mapType = outputStreamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                .getValue();
        this.publisherURL = optionHolder.validateAndGetStaticValue(HttpConstants.PUBLISHER_URL);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS , HttpConstants.DEFAULT_HEADER);
        this.httpMethodOption = optionHolder.getOrCreateOption(HttpConstants.METHOD , HttpConstants.DEFAULT_METHOD);
        this.userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME,
                HttpConstants.EMPTY_STRING);
        this.userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD,
                HttpConstants.EMPTY_STRING);
        String clientStoreFile = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PATH, new
                HttpSinkUtil().trustStorePath(configReader));
        String clientStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD,
                new HttpSinkUtil().trustStorePassword(configReader));
        String scheme = configReader.readConfig(HttpConstants.DEFAULT_SINK_SCHEME, HttpConstants
                .DEFAULT_SINK_SCHEME_VALUE);
        if (HttpConstants.SCHEME_HTTPS.equals(scheme) && ((clientStoreFile == null) || (clientStorePass == null))) {
            throw new ExceptionInInitializerError("Client truststore file path or password are empty while " +
                    "default scheme is 'https'. Please provide client " +
                    "truststore file path and password in" + streamID);
        }
        if (HttpConstants.EMPTY_STRING.equals(publisherURL)) {
            throw new ExceptionInInitializerError("Receiver URL found empty but it is Mandatory field in " +
                    "" + HttpConstants.HTTP_SINK_ID + "in" + streamID);
        }
        if ((HttpConstants.EMPTY_STRING.equals(userName) ^
                HttpConstants.EMPTY_STRING.equals(userPassword))) {
            throw new ExceptionInInitializerError("Please provide user name and password in " +
                    HttpConstants.HTTP_SINK_ID + "in" + streamID);
        } else if (!(HttpConstants.EMPTY_STRING.equals(userName) || HttpConstants.EMPTY_STRING.equals
                (userPassword))) {
            byte[] val = (userName + HttpConstants.AUTH_USERNAME_PASSWORD_SEPARATOR + userPassword).getBytes(Charset
                    .defaultCharset());
            this.authorizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                    (Unpooled.copiedBuffer(val));
        }
        //to separate protocol, host, port and context
        this.httpURLProperties = new HttpSinkUtil().getURLProperties(publisherURL);
        this.senderConfig = new HttpSinkUtil().getSenderConfigurations(httpURLProperties, clientStoreFile,
                clientStorePass);
        this.nettyTransportProperty = new HttpSinkUtil().getTransportConfigurations(configReader);
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        String headers = httpHeaderOption.getValue(dynamicOptions);
        String httpMethod = HttpConstants.EMPTY_STRING.equals(httpMethodOption.getValue(dynamicOptions)) ?
                HttpConstants.METHOD_DEFAULT : httpMethodOption.getValue(dynamicOptions);
        List<Header> headersList = new HttpSinkUtil().getHeaders(headers);
        String contentType = new HttpSinkUtil().getContentType(mapType, headersList);
        String messageBody = (String) payload;
        CarbonMessage cMessage = new DefaultCarbonMessage();
        HttpPayloadDataSource messageDataSource = new HttpPayloadDataSource(messageBody, cMessage.getOutputStream());
        messageDataSource.setOutputStream(cMessage.getOutputStream());
        cMessage = generateCarbonMessage(headersList, messageDataSource, contentType, httpMethod, cMessage,
                messageBody);
        try {
            clientConnector.send(cMessage, new HttpSinkCallback(messageBody), httpURLProperties);
        } catch (ClientConnectorException e) {
            log.error("Error sending the HTTP message with payload " + payload + " in " +
                    HttpConstants.HTTP_SINK_ID + streamID, e);
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        this.clientConnector = new HTTPClientConnector(senderConfig, nettyTransportProperty);
        log.info(streamID + " has successfully connected to " + publisherURL);

    }

    @Override
    public void disconnect() {
        if (clientConnector != null) {
            clientConnector = null;
            log.info("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    @Override
    public void destroy() {
        if (clientConnector != null) {
            clientConnector = null;
            log.info("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    @Override
    public Map<String, Object> currentState() {
        //no current state.
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        //no need to maintain.
    }
    /**
     * The method is responsible of generating carbon message to send.
     *
     * @param headers     the headers set.
     * @param payload     the payload.
     * @param contentType the content type. Value is if user has to given it as a header or if not it is map type.
     * @param httpMethod  http method type.
     * @param cMessage    carbon message to be send to the endpoint.
     * @return generated carbon message.
     */
    private CarbonMessage generateCarbonMessage(List<Header> headers, HttpPayloadDataSource payload, String contentType,
                                                String httpMethod, CarbonMessage cMessage, String payloadString) {
        //if Authentication enabled
        if (!(userName.equals(HttpConstants.EMPTY_STRING) || userPassword.equals
                (HttpConstants.EMPTY_STRING))) {
            cMessage.setHeader(HttpConstants.AUTHORIZATION_HEADER, authorizationHeader);
        }

        /*
         * set carbon message properties which is to be used in carbon transport.
         */
        // Set protocol type http or https
        cMessage.setProperty(Constants.PROTOCOL, httpURLProperties.get(HttpConstants.SCHEME));
            // Set uri
        cMessage.setProperty(Constants.TO, httpURLProperties.get(HttpConstants.TO));
            // set Host
        cMessage.setProperty(Constants.HOST, httpURLProperties.get(HttpConstants.HOST));
            //set port
        cMessage.setProperty(Constants.PORT, Integer.valueOf(httpURLProperties.get(HttpConstants.PORT)));
            // Set method
        cMessage.setProperty(HttpConstants.HTTP_METHOD, httpMethod);

        /*
         *set request headers.
         */
            // Set user given Headers
        if (headers != null) {
            cMessage.setHeaders(headers);
        }
            // Set content type if content type s not included in headers
        if (contentType.contains(mapType)) {
            cMessage.setHeader(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }
            //set content length header
        if (cMessage.getHeaders().get(HttpConstants.CONTENT_LENGTH_HEADER) == null) {
            try {
                cMessage.setHeader(HttpConstants.CONTENT_LENGTH_HEADER, String.valueOf(payloadString.getBytes
                        (HttpConstants.DEFAULT_ENCODING).length));
            } catch (UnsupportedEncodingException e) {
                log.error("Unsupported content encoding.");
            }
        }
            //set method-type header
        cMessage.setHeader(HttpConstants.HTTP_METHOD, httpMethod);

        /*
         * set request payload.
         */
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
}
