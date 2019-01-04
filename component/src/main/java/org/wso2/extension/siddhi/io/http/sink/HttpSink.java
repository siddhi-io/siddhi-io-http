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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.http.sink.updatetoken.AccessTokenCache;
import org.wso2.extension.siddhi.io.http.sink.updatetoken.DefaultListener;
import org.wso2.extension.siddhi.io.http.sink.updatetoken.HttpsClient;
import org.wso2.extension.siddhi.io.http.sink.util.HttpSinkUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.extension.siddhi.io.http.util.HttpIoUtil;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.wso2.extension.siddhi.io.http.util.HttpConstants.EMPTY_STRING;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.PORT_HOST_SEPARATOR;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.SOCKET_IDEAL_TIMEOUT_VALUE;

/**
 * {@code HttpSink } Handle the HTTP publishing tasks.
 */
@Extension(name = "http", namespace = "sink",
        description = "This extension publish the HTTP events in any HTTP method  POST, GET, PUT, DELETE  via HTTP " +
                "or https protocols. As the additional features this component can provide basic authentication " +
                "as well as user can publish events using custom client truststore files when publishing events " +
                "via https protocol. And also user can add any number of headers including HTTP_METHOD header for " +
                "each event dynamically.\n" +
                "Following content types will be set by default according to the type of sink mapper used.\n" +
                "You can override them by setting the new content types in headers.\n" +
                "     - TEXT : text/plain\n" +
                "     - XML : application/xml\n" +
                "     - JSON : application/json\n" +
                "     - KEYVALUE : application/x-www-form-urlencoded",
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
                                "by user then an error is logged in the CLI.",
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
                        name = "https.truststore.file",
                        description = "The file path to the location of the truststore of the client that sends " +
                                "the HTTP events through 'https' protocol. A custom client-truststore can be " +
                                "specified if required.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "${carbon.home}/resources/security/client-truststore.jks"),
                @Parameter(
                        name = "https.truststore.password",
                        description = "The password for the client-truststore. A custom password can be specified " +
                                "if required. If no custom password is specified and the protocol of URL is 'https' " +
                                "then, the system uses default password.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "wso2carbon"),
                @Parameter(
                        name = "headers",
                        description = "The headers that should be included as HTTP request headers. \n" +
                                "There can be any number of headers concatenated in following format. " +
                                "\"'header1:value1','header2:value2'\". User can include Content-Type header if he " +
                                "needs to use a specific content-type for the payload. Or else, system decides the " +
                                "Content-Type by considering the type of sink mapper, in following way.\n" +
                                " - @map(xml):application/xml\n" +
                                " - @map(json):application/json\n" +
                                " - @map(text):plain/text )\n" +
                                " - if user does not include any mapping type then the system gets 'plain/text' " +
                                "as default Content-Type header.\n" +
                                "Note that providing content-length as a header is not supported. The size of the " +
                                "payload will be automatically calculated and included in the content-length header.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = " "),
                @Parameter(
                        name = "method",
                        description = "For HTTP events, HTTP_METHOD header should be included as a request header." +
                                " If the parameter is null then system uses 'POST' as a default header.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "POST"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "Socket timeout value in millisecond",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "6000"),
                @Parameter(
                        name = "chunk.disabled",
                        description = "This parameter is used to disable/enable chunked transfer encoding",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "The SSL protocol version",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TLS"),
                @Parameter(
                        name = "parameters",
                        description = "Parameters other than basics such as ciphers,sslEnabledProtocols,client.enable" +
                                ".session.creation. Expected format of these parameters is as follows: " +
                                "\"'ciphers:xxx','sslEnabledProtocols,client.enable:xxx'\"",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "ciphers",
                        description = "List of ciphers to be used. This parameter should include under parameters Ex:" +
                                " 'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "ssl.enabled.protocols",
                        description = "SSL/TLS protocols to be enabled. This parameter should be in camel case format" +
                                "(sslEnabledProtocols) under parameters. Ex 'sslEnabledProtocols:true'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "client.enable.session.creation",
                        description = "Enable HTTP session creation.This parameter should include under parameters " +
                                "Ex:" +
                                " 'client.enable.session.creation:true'",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "follow.redirect",
                        description = "Redirect related enabled.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "max.redirect.count",
                        description = "Maximum redirect count.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TLS store type to be used.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "JKS"),
                @Parameter(
                        name = "proxy.host",
                        description = "Proxy server host",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "proxy.port",
                        description = "Proxy server port",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "proxy.username",
                        description = "Proxy server username",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(
                        name = "proxy.password",
                        description = "Proxy server password",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "null"),
                //bootstrap configurations
                @Parameter(
                        name = "client.bootstrap.configuration",
                        description = "Client bootsrap configurations. Expected format of these parameters is as " +
                                "follows:" +
                                " \"'client.bootstrap.nodelay:xxx','client.bootstrap.keepalive:xxx'\"",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TODO"),
                @Parameter(
                        name = "client.bootstrap.nodelay",
                        description = "Http client no delay.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "client.bootstrap.keepalive",
                        description = "Http client keep alive.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "client.bootstrap.sendbuffersize",
                        description = "Http client send buffer size.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "client.bootstrap.recievebuffersize",
                        description = "Http client receive buffer size.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "client.bootstrap.connect.timeout",
                        description = "Http client connection timeout.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "15000"),
                @Parameter(
                        name = "client.bootstrap.socket.reuse",
                        description = "To enable http socket reuse.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "client.bootstrap.socket.timeout",
                        description = "Http client socket timeout.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "15"),

                @Parameter(
                        name = "client.threadpool.configurations",
                        description = "Thread pool configuration. Expected format of these parameters is as follows:" +
                                " \"'client.connection.pool.count:xxx','client.max.active.connections.per.pool:xxx'\"",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TODO"),
                @Parameter(
                        name = "client.connection.pool.count",
                        description = "Connection pool count.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "0"),
                @Parameter(
                        name = "client.max.active.connections.per.pool",
                        description = "Active connections per pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "-1"),
                @Parameter(
                        name = "client.min.idle.connections.per.pool",
                        description = "Minimum ideal connection per pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "0"),
                @Parameter(
                        name = "client.max.idle.connections.per.pool",
                        description = "Maximum ideal connection per pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "100"),
                @Parameter(
                        name = "client.min.eviction.idle.time",
                        description = "Minimum eviction idle time.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "5 * 60 * 1000"),
                @Parameter(
                        name = "sender.thread.count",
                        description = "Http sender thread count.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "20"),
                @Parameter(
                        name = "event.group.executor.thread.size",
                        description = "Event group executor thread size.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "15"),
                @Parameter(
                        name = "max.wait.for.client.connection.pool",
                        description = "Maximum wait for client connection pool.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "60000")
        },
        examples = {
                @Example(syntax =
                        "@sink(type='http',publisher.url='http://localhost:8009/foo', method='{{method}}',"
                                + "headers=\"'content-type:xml','content-length:94'\", client.bootstrap" +
                                ".configuration=\"'client" +
                                ".bootstrap" +
                                ".socket" +
                                ".timeout:20', 'client.bootstrap.worker.group.size:10'\", client.pool" +
                                ".configuration=\"'client.connection.pool.count:10','client.max.active.connections" +
                                ".per.pool:1'\", "
                                + "@map(type='xml', @payload('{{payloadBody}}')))\n"
                                + "define stream FooStream (payloadBody String, method string, headers string);\n",
                        description =
                                "If it is xml mapping expected input should be in following format for FooStream:\n"
                                        + "{\n"
                                        + "<events>\n"
                                        + "    <event>\n"
                                        + "        <symbol>WSO2</symbol>\n"
                                        + "        <price>55.6</price>\n"
                                        + "        <volume>100</volume>\n"
                                        + "    </event>\n"
                                        + "</events>,\n"
                                        + "POST,\n"
                                        + "Content-Length:24#Content-Location:USA#Retry-After:120\n"
                                        + "}\n\n"
                                        + "Above event will generate output as below.\n"
                                        + "~Output http event payload\n"
                                        + "<events>\n"
                                        + "    <event>\n"
                                        + "        <symbol>WSO2</symbol>\n"
                                        + "        <price>55.6</price>\n"
                                        + "        <volume>100</volume>\n"
                                        + "    </event>\n"
                                        + "</events>\n\n"
                                        + "~Output http event headers\n"
                                        + "Content-Length:24,\n"
                                        + "Content-Location:'USA',\n"
                                        + "Retry-After:120,\n"
                                        + "Content-Type:'application/xml',\n"
                                        + "HTTP_METHOD:'POST',\n\n"
                                        + "~Output http event properties\n"
                                        + "HTTP_METHOD:'POST',\n"
                                        + "HOST:'localhost',\n"
                                        + "PORT:8009,\n"
                                        + "PROTOCOL:'http',\n"
                                        + "TO:'/foo'"
                )},
        systemParameter = {
                @SystemParameter(
                        name = "clientBootstrapBossGroupSize",
                        description = "property to configure number of boss threads, which accepts incoming " +
                                "connections until the ports are unbound. Once connection accepts successfully, " +
                                "boss thread passes the accepted channel to one of the worker threads.",
                        defaultValue = "Number of available processors",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "clientBootstrapWorkerGroupSize",
                        description = "property to configure number of worker threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "(Number of available processors)*2",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "clientBootstrapClientGroupSize",
                        description = "property to configure number of client threads, which performs non " +
                                "blocking read and write for one or more channels in non-blocking mode.",
                        defaultValue = "(Number of available processors)*2",
                        possibleParameters = "Any integer"
                ),
                @SystemParameter(
                        name = "trustStoreLocation",
                        description = "The default truststore file path.",
                        defaultValue = "${carbon.home}/resources/security/client-truststore.jks",
                        possibleParameters = "Path to client-truststore.jks"
                ),
                @SystemParameter(
                        name = "trustStorePassword",
                        description = "The default truststore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Truststore password"
                )
        }
)
public class HttpSink extends Sink {
    private static final Logger log = Logger.getLogger(HttpSink.class);
    private String streamID;
    HttpClientConnector clientConnector;
    String mapType;
    private static Map<String, String> httpURLProperties;
    Option httpHeaderOption;
    Option httpMethodOption;
    private String consumerKey;
    private String consumerSecret;
    private String authorizationHeader;
    private String userName;
    private String userPassword;
    private String publisherURL;
    private Option publisherURLOption;
    private String clientStoreFile;
    private String clientStorePass;
    private int socketIdleTimeout;
    private String sslProtocol;
    private String tlsStoreType;
    private String chunkDisabled;
    private String followRedirect;
    private String maxRedirectCount;
    private String parametersList;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String clientBootstrapConfiguration;
    private String clientPoolConfiguration;
    private String bootstrapWorker;
    private String bootstrapBoss;
    private String bootstrapClient;
    private ConfigReader configReader;
    private SiddhiAppContext siddhiAppContext;
    private String oauthUsername;
    private String oauthUserPassword;
    private String accessToken;
    private Option refreshToken;
    private String availableRefreshToken;
    private String authType;
    private Boolean accesstokenAvalable = false;
    private AccessTokenCache accessTokenCache = new AccessTokenCache();


    /**
     * Returns the list of classes which this sink can consume.
     * Based on the type of the sink, it may be limited to being able to publish specific type of classes.
     * For example, a sink of type file can only write objects of type String .
     *
     * @return array of supported classes , if extension can support of any types of classes
     * then return empty array .
     */
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class, Map.class};
    }

    /**
     * Returns a list of supported dynamic options (that means for each event value of the option can change) by
     * the transport
     *
     * @return the list of supported dynamic option keys
     */
    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{HttpConstants.HEADERS, HttpConstants.METHOD, HttpConstants.PUBLISHER_URL,
                HttpConstants.RECEIVER_REFRESH_TOKEN};
    }

    /**
     * The initialization method for {@link Sink}, which will be called before other methods and validate
     * the all configuration and getting the intial values.
     *
     * @param outputStreamDefinition containing stream definition bind to the {@link Sink}
     * @param optionHolder           Option holder containing static and dynamic configuration related
     *                               to the {@link Sink}
     * @param configReader           to read the sink related system configuration.
     * @param siddhiAppContext       the context of the {@link org.wso2.siddhi.query.api.SiddhiApp} used to
     *                               get siddhi related utilty functions.
     */
    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        //read configurations
        this.configReader = configReader;
        this.siddhiAppContext = siddhiAppContext;
        this.streamID = siddhiAppContext.getName() + PORT_HOST_SEPARATOR + outputStreamDefinition.toString();
        this.mapType = outputStreamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                .getValue();
        this.publisherURLOption = optionHolder.validateAndGetOption(HttpConstants.PUBLISHER_URL);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.httpMethodOption = optionHolder.getOrCreateOption(HttpConstants.METHOD, HttpConstants.DEFAULT_METHOD);
        this.consumerKey = optionHolder.validateAndGetStaticValue(HttpConstants.CONSUMER_KEY, EMPTY_STRING);
        this.consumerSecret = optionHolder.validateAndGetStaticValue(HttpConstants.CONSUMER_SECRET, EMPTY_STRING);
        this.userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME, EMPTY_STRING);
        this.userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD, EMPTY_STRING);
        this.oauthUsername = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_OAUTH_USERNAME,
                EMPTY_STRING);
        this.oauthUserPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_OAUTH_PASSWORD,
                EMPTY_STRING);
        this.refreshToken = optionHolder.getOrCreateOption(HttpConstants.RECEIVER_REFRESH_TOKEN, EMPTY_STRING); //todo
        clientStoreFile = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PATH_PARAM,
                HttpSinkUtil.trustStorePath(configReader));
        clientStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_PARAM,
                HttpSinkUtil.trustStorePassword(configReader));
        socketIdleTimeout = Integer.parseInt(optionHolder.validateAndGetStaticValue
                (HttpConstants.SOCKET_IDEAL_TIMEOUT, SOCKET_IDEAL_TIMEOUT_VALUE));
        sslProtocol = optionHolder.validateAndGetStaticValue(HttpConstants.SSL_PROTOCOL, EMPTY_STRING);
        tlsStoreType = optionHolder.validateAndGetStaticValue(HttpConstants.TLS_STORE_TYPE, EMPTY_STRING);
        chunkDisabled = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_CHUNK_ENABLED, EMPTY_STRING);
        followRedirect = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_FOLLOW_REDIRECT,
                EMPTY_STRING);
        maxRedirectCount = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_MAX_REDIRECT_COUNT,
                EMPTY_STRING);
        parametersList = optionHolder.validateAndGetStaticValue(HttpConstants.SINK_PARAMETERS, EMPTY_STRING);
        proxyHost = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_HOST, EMPTY_STRING);
        proxyPort = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_PORT, EMPTY_STRING);
        proxyUsername = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_USERNAME,
                EMPTY_STRING);
        proxyPassword = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_PASSWORD,
                EMPTY_STRING);
        clientBootstrapConfiguration = optionHolder
                .validateAndGetStaticValue(HttpConstants.CLIENT_BOOTSTRAP_CONFIGURATION, EMPTY_STRING);
        clientPoolConfiguration = optionHolder
                .validateAndGetStaticValue(HttpConstants.CLIENT_POOL_CONFIGURATION, EMPTY_STRING);
        //read trp globe configuration
        bootstrapWorker = configReader
                .readConfig(HttpConstants.CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE, EMPTY_STRING);
        bootstrapBoss = configReader.readConfig(HttpConstants.CLIENT_BOOTSTRAP_BOSS_GROUP_SIZE, EMPTY_STRING);
        bootstrapClient = configReader.readConfig(HttpConstants.CLIENT_BOOTSTRAP_CLIENT_GROUP_SIZE,
                EMPTY_STRING);

        if (publisherURLOption.isStatic()) {
            initClientConnector(null);
        }

        if (!HttpConstants.EMPTY_STRING.equals(userName) && !HttpConstants.EMPTY_STRING.equals(userPassword)) {
            authType = HttpConstants.BASIC_AUTH;
        } else if ((!HttpConstants.EMPTY_STRING.equals(consumerKey)
                && !HttpConstants.EMPTY_STRING.equals(consumerSecret)) ||
                (!HttpConstants.EMPTY_STRING.equals(oauthUsername)
                        && !HttpConstants.EMPTY_STRING.equals(oauthUserPassword))) {
            authType = HttpConstants.OAUTH;
        } else {
            authType = HttpConstants.NO_AUTH;
        }
    }


    /**
     * This method will be called when events need to be published via this sink
     *
     * @param payload        payload of the event based on the supported event class exported by the extensions
     * @param dynamicOptions holds the dynamic options of this sink and Use this object to obtain dynamic options.
     */
    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) {
        //get the dynamic parameter
        String headers = httpHeaderOption.getValue(dynamicOptions);
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);

        if (authType.equals(HttpConstants.BASIC_AUTH) || authType.equals(HttpConstants.NO_AUTH)) {
            sendRequest(payload, dynamicOptions, headersList);
        } else {
            sendOauthRequest(payload, dynamicOptions, headersList);
        }
        disconnect();
    }

    private void sendOauthRequest(Object payload, DynamicOptions dynamicOptions, List<Header> headersList) {
        //generate encoded base64 auth for getting refresh token
        String consumerKeyValue = consumerKey + ":" + consumerSecret;
        String encodedRefreshAuth = "Basic " + encodeBase64(consumerKeyValue);
        //check the availability of access token in the header
        checkAccessToken(encodedRefreshAuth, dynamicOptions, headersList);
        //send a request to API and get the response
        int response = sendRequest(payload, dynamicOptions, headersList);
        //if authentication fails then get the new access token
        if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            oauthFails(payload, dynamicOptions, headersList, encodedRefreshAuth);
        } else if (response == HttpConstants.SUCCESS_CODE) {
            log.info("Request send successfully.");
        } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
            log.error(response + ": Internal server connection failure. Please pass the valid parameters. ");
        } else {
            log.error(response + ": Can not obtain access token.");
        }
    }

    private void oauthFails(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                            String encodedRefreshAuth) {
        Boolean checkFromCache = accessTokenCache.checkAvailableKey(encodedRefreshAuth);
        if (checkFromCache) {
            getNewAccessTokenWithCache(payload, dynamicOptions, headersList, encodedRefreshAuth);
        } else {
            requestForNewAccessToken(payload, dynamicOptions, headersList, encodedRefreshAuth);
        }
    }

    private void getNewAccessTokenWithCache(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                            String encodedRefreshAuth) {
        accessToken = accessTokenCache.getAccessToken(encodedRefreshAuth);
        for (Header header : headersList) {
            if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                header.setValue(accessToken);
            }
        }
        //send a request to API with a new access token
        int response = sendRequest(payload, dynamicOptions, headersList);
        if (response == HttpConstants.SUCCESS_CODE) {
            log.info("Request send successfully.");
        } else if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            requestForNewAccessToken(payload, dynamicOptions, headersList, encodedRefreshAuth);
        } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
            log.error(response + ": Internal server error.");
        } else {
            log.error(response + ": Can not obtain access token.");
        }
    }

    private void requestForNewAccessToken(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                               String encodedRefreshAuth) {
        Boolean checkRefreshToken = accessTokenCache.checkRefreshAvailableKey(encodedRefreshAuth);
        if (checkRefreshToken) {
            availableRefreshToken = accessTokenCache.getRefreshtoken(encodedRefreshAuth);
            for (Header header : headersList) {
                if (header.getName().equals(HttpConstants.RECEIVER_REFRESH_TOKEN)) {
                    header.setValue(availableRefreshToken);
                }
            }
        }
        ArrayList<String> newAccessTokenArray = getAccessToken(dynamicOptions, encodedRefreshAuth);
        int newAccessResponseCode = Integer.parseInt(newAccessTokenArray.get(0));
        if (newAccessResponseCode == HttpConstants.SUCCESS_CODE) {
            String newAccessToken = "Bearer " + newAccessTokenArray.get(1);
            accessTokenCache.setAccessToken(encodedRefreshAuth, newAccessToken);
            if (!newAccessTokenArray.get(2).equals(HttpConstants.EMPTY_STRING)) {
                accessTokenCache.setRefreshtoken(encodedRefreshAuth, newAccessTokenArray.get(2));
            }
            for (Header header : headersList) {
                if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                    header.setValue(newAccessToken);
                }
            }
            //send a request to API with a new access token
            int response = sendRequest(payload, dynamicOptions, headersList);
            if (response == HttpConstants.SUCCESS_CODE) {
                log.info("Request send successfully.");
            } else if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
                log.error(response + ": Authentication Failure. Please apply valid authorization.");
            } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
                log.error(response + ": Internal server error.");
            } else {
                log.error(response + ": Can not obtain new access token.");
            }

        } else if (newAccessResponseCode == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            log.error(newAccessResponseCode + ": Authentication Failure. Please apply valid Consumer key, " +
                    "Consumer secret for generate new access token. ");
        } else {
            log.error(newAccessResponseCode + ": Can not obtain new access token.");
        }

    }

    public ArrayList<String> getAccessToken(DynamicOptions dynamicOptions, String encodedRefreshAuth) {
        HttpsClient httpsClient = new HttpsClient();
        ArrayList<String> newAccessTokenArray;
        if (!HttpConstants.EMPTY_STRING.equals(oauthUsername) &&
                !HttpConstants.EMPTY_STRING.equals(oauthUserPassword)) {
            newAccessTokenArray = httpsClient.getPasswordGrandAccessToken(publisherURL, clientStoreFile,
                    clientStorePass, oauthUsername, oauthUserPassword, encodedRefreshAuth);
        } else if (!HttpConstants.EMPTY_STRING.equals(refreshToken.getValue(dynamicOptions)) ||
                !availableRefreshToken.equals(HttpConstants.EMPTY_STRING)) {
            newAccessTokenArray = httpsClient.getRefreshGrandAccessToken(publisherURL, clientStoreFile,
                    clientStorePass, encodedRefreshAuth, refreshToken.getValue(dynamicOptions));
        } else {
            newAccessTokenArray = httpsClient.getClientGrandAccessToken(publisherURL, clientStoreFile,
                    clientStorePass, encodedRefreshAuth);
        }
        return newAccessTokenArray;
    }

    public void checkAccessToken(String encodedRefreshAuth, DynamicOptions dynamicOptions,
                                 List<Header> headersList) {
        //check the availability of the authorization
        boolean authAvailability = false;
        for (Header header : headersList) {
            if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                authAvailability = true;
            }
        }
        if (!authAvailability) {
            //generate encoded base64 auth for getting refresh token
            if (!accesstokenAvalable) {
                ArrayList<String> accessTokenArray = getAccessToken(dynamicOptions, encodedRefreshAuth);
                int responseCode = Integer.parseInt(accessTokenArray.get(0));

                if (responseCode == HttpConstants.SUCCESS_CODE) {
                    accessToken = "Bearer " + accessTokenArray.get(1);
                    availableRefreshToken = accessTokenArray.get(2);
                    accesstokenAvalable = true;
                    accessTokenCache.setRefreshtoken(encodedRefreshAuth, availableRefreshToken);
                    accessTokenCache.setAccessToken(encodedRefreshAuth, accessToken);
                } else if (responseCode == HttpConstants.AUTHENTICATION_FAIL_CODE) {
                    accessToken = HttpConstants.EMPTY_STRING;
                    log.error(responseCode + ": Provide valid Consumer key, " +
                            "Consumer secret for generate new access token.");
                } else if (responseCode == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
                    accessToken = HttpConstants.EMPTY_STRING;
                    log.error(responseCode + ": Internal server Error.");
                } else {
                    log.error(responseCode + ": Can not obtain access token.");
                }
            }
            headersList.add(new Header(HttpConstants.AUTHORIZATION_HEADER, accessToken));
            headersList.add(new Header(HttpConstants.RECEIVER_REFRESH_TOKEN, availableRefreshToken));
        } else {
            //check the cache and update new access token into header
            Boolean checkFromCache = accessTokenCache.checkAvailableKey(encodedRefreshAuth);
            if (checkFromCache) {
                accessToken = accessTokenCache.getAccessToken(encodedRefreshAuth);
                for (Header header : headersList) {
                    if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                        header.setValue(accessToken);
                    }
                }
            }
        }
    }

    private int sendRequest(Object payload, DynamicOptions dynamicOptions, List<Header> headersList) {
        if (publisherURLOption.isStatic()) {
            if (clientConnector != null) {
                clientConnector.close();
            }
        }
        initClientConnector(dynamicOptions);
        String httpMethod = EMPTY_STRING.equals(httpMethodOption.getValue(dynamicOptions)) ?
                HttpConstants.METHOD_DEFAULT : httpMethodOption.getValue(dynamicOptions);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        String messageBody = getMessageBody(payload);
        HttpMethod httpReqMethod = new HttpMethod(httpMethod);
        HTTPCarbonMessage cMessage = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpReqMethod, EMPTY_STRING));
        cMessage = generateCarbonMessage(headersList, contentType, httpMethod, cMessage);
        if (!Constants.HTTP_GET_METHOD.equals(httpMethod)) {
            cMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(messageBody
                    .getBytes(Charset.defaultCharset()))));
        }
        cMessage.completeMessage();
        CountDownLatch latch = new CountDownLatch(1);
        DefaultListener listener = new DefaultListener(latch, authType);
        HttpResponseFuture responseFuture = clientConnector.send(cMessage);
        responseFuture.setHttpConnectorListener(listener);

        if (HttpConstants.OAUTH.equals(authType)) {
            try {
                latch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.debug("Time out due to getting new access token. " + e);
                disconnect();
            }
        }
        HTTPCarbonMessage response = listener.getHttpResponseMessage();
        return response.getNettyHttpResponse().status().code();
    }

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     * such that the  system will take care retrying for connection
     */
    @Override
    public void connect() {
        if (publisherURLOption.isStatic()) {
            log.info(streamID + " has successfully connected to " + publisherURL);
        }
    }

    /**
     * Called after all publishing is done, the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {
        if (clientConnector != null) {
            clientConnector = null;
            log.info("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that has to be done when removing the receiver has to be done here.
     */
    @Override
    public void destroy() {
        if (clientConnector != null) {
            clientConnector = null;
            log.info("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for reconstructing the element to the same state on a different point of time
     * This is also used to identify the internal states and debuging
     *
     * @return all internal states should be return as an map with meaning full keys
     */
    @Override
    public Map<String, Object> currentState() {
        //no current state.
        return null;
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the processing element as a map.
     *              This map will have the  same keys that is created upon calling currentState() method.
     */
    @Override
    public void restoreState(Map<String, Object> state) {
        //no need to maintain.
    }

    /**
     * The method is responsible of generating carbon message to send.
     *
     * @param headers     the headers set.
     * @param contentType the content type. Value is if user has to given it as a header or if not it is map type.
     * @param httpMethod  http method type.
     * @param cMessage    carbon message to be send to the endpoint.
     * @return generated carbon message.
     */
    HTTPCarbonMessage generateCarbonMessage(List<Header> headers, String contentType,
                                            String httpMethod, HTTPCarbonMessage cMessage) {
        /*
         * set carbon message properties which is to be used in carbon transport.
         */
        // Set protocol type http or https
        cMessage.setProperty(Constants.PROTOCOL, httpURLProperties.get(Constants.PROTOCOL));
        // Set uri
        cMessage.setProperty(Constants.TO, httpURLProperties.get(Constants.TO));
        // set Host
        cMessage.setProperty(Constants.HTTP_HOST, httpURLProperties.get(Constants.HTTP_HOST));
        //set port
        cMessage.setProperty(Constants.HTTP_PORT, Integer.valueOf(httpURLProperties.get(Constants.HTTP_PORT)));
        // Set method
        cMessage.setProperty(Constants.HTTP_METHOD, httpMethod);
        //Set request URL
        cMessage.setProperty(Constants.REQUEST_URL, httpURLProperties.get(Constants.REQUEST_URL));
        HttpHeaders httpHeaders = cMessage.getHeaders();
        //if Authentication enabled
        if (!(userName.equals(EMPTY_STRING)) && !(userPassword.equals
                (EMPTY_STRING))) {
            httpHeaders.set(HttpConstants.AUTHORIZATION_HEADER, authorizationHeader);
        } else if (!(userName.equals(EMPTY_STRING)) || !(userPassword.equals
                (EMPTY_STRING))) {
            log.error("One of the basic authentication username or password missing. Hence basic authentication not " +
                    "supported.");
        }

        httpHeaders.set(Constants.HTTP_HOST, cMessage.getProperty(Constants.HTTP_HOST));
        /*
         *set request headers.
         */
        // Set user given Headers
        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }
        // Set content type if content type is not included in headers
        if (contentType.contains(mapType)) {
            httpHeaders.set(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }
        //set method-type header
        httpHeaders.set(HttpConstants.HTTP_METHOD, httpMethod);
        return cMessage;
    }

    String getMessageBody(Object payload) {
        if (HttpConstants.MAP_KEYVALUE.equals(mapType)) {
            Map<String, Object> params = (HashMap) payload;
            return params.entrySet().stream()
                    .map(p -> encodeMessage(p.getKey()) + "=" + encodeMessage(p.getValue()))
                    .reduce("", (p1, p2) -> p1 + "&" + p2);
        } else {
            return (String) payload;
        }
    }

    void initClientConnector(DynamicOptions dynamicOptions) {
        if (publisherURLOption.isStatic()) {
            publisherURL = publisherURLOption.getValue();
        } else {
            publisherURL = publisherURLOption.getValue(dynamicOptions);
        }

        String scheme = HttpSinkUtil.getScheme(publisherURL);
        this.httpURLProperties = HttpSinkUtil.getURLProperties(publisherURL);
        //Generate basic sender configurations
        SenderConfiguration senderConfig = HttpSinkUtil
                .getSenderConfigurations(httpURLProperties, clientStoreFile, clientStorePass, configReader);
        if (EMPTY_STRING.equals(publisherURL)) {
            throw new SiddhiAppCreationException("Receiver URL found empty but it is Mandatory field in " +
                    "" + HttpConstants.HTTP_SINK_ID + " in " + streamID);
        }
        if (HttpConstants.SCHEME_HTTPS.equals(scheme) && ((clientStoreFile == null) || (clientStorePass == null))) {
            throw new ExceptionInInitializerError("Client trustStore file path or password are empty while " +
                    "default scheme is 'https'. Please provide client " +
                    "trustStore file path and password in " + streamID);
        }
        //if username and password both not equal to null consider as basic auth enabled if only one is null take it
        // as exception
        if ((EMPTY_STRING.equals(userName) ^
                EMPTY_STRING.equals(userPassword))) {
            throw new SiddhiAppCreationException("Please provide user name and password in " +
                    HttpConstants.HTTP_SINK_ID + " with the stream " + streamID + " in Siddhi app " +
                    siddhiAppContext.getName());
        } else if (!(EMPTY_STRING.equals(userName) || EMPTY_STRING.equals
                (userPassword))) {
            byte[] val = (userName + HttpConstants.AUTH_USERNAME_PASSWORD_SEPARATOR + userPassword).getBytes(Charset
                    .defaultCharset());
            this.authorizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                    (Unpooled.copiedBuffer(val));
        }
        //if bootstrap configurations are given then pass it if not let take default value of transport
        HttpWsConnectorFactory httpConnectorFactory;
        if (!EMPTY_STRING.equals(bootstrapBoss) && !EMPTY_STRING.equals(bootstrapWorker)) {
            if (!EMPTY_STRING.equals(bootstrapClient)) {
                httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss),
                        Integer.parseInt(bootstrapWorker), Integer.parseInt(bootstrapClient));
            } else {
                httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss),
                        Integer.parseInt(bootstrapWorker), Integer.parseInt(bootstrapWorker));
            }
        } else {
            httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        }

        //if proxy username and password not equal to null then create proxy configurations
        if (!EMPTY_STRING.equals(proxyHost) && !EMPTY_STRING.equals(proxyPort)) {
            try {
                ProxyServerConfiguration proxyServerConfiguration = new ProxyServerConfiguration(proxyHost, Integer
                        .parseInt(proxyPort));
                if (!EMPTY_STRING.equals(proxyPassword) && !EMPTY_STRING.equals
                        (proxyUsername)) {
                    proxyServerConfiguration.setProxyPassword(proxyPassword);
                    proxyServerConfiguration.setProxyUsername(proxyUsername);
                }
                senderConfig.setProxyServerConfiguration(proxyServerConfiguration);
            } catch (UnknownHostException e) {
                log.error("Proxy url and password is invalid in sink " + streamID + " Siddhi app " +
                        siddhiAppContext.getName(), e);
            }
        }
        //add advanced sender configurations
        if (socketIdleTimeout != -1) {
            senderConfig.setSocketIdleTimeout(socketIdleTimeout);
        }
        if (!EMPTY_STRING.equals(sslProtocol)) {
            senderConfig.setSSLProtocol(sslProtocol);
        }
        if (!EMPTY_STRING.equals(tlsStoreType)) {
            senderConfig.setTLSStoreType(tlsStoreType);
        }
        if (!EMPTY_STRING.equals(chunkDisabled)) {
            if (chunkDisabled != null) {
                if (Boolean.parseBoolean(chunkDisabled)) {
                    senderConfig.setChunkingConfig(ChunkConfig.NEVER);
                } else {
                    senderConfig.setChunkingConfig(ChunkConfig.ALWAYS);
                }
            }
        }
        if (!EMPTY_STRING.equals(followRedirect)) {
            senderConfig.setFollowRedirect(Boolean.parseBoolean(followRedirect));
        }
        if (!EMPTY_STRING.equals(maxRedirectCount)) {
            senderConfig.setMaxRedirectCount(Integer.parseInt(maxRedirectCount));
        }
        if (!EMPTY_STRING.equals(parametersList)) {
            senderConfig.setParameters(HttpIoUtil.populateParameters(parametersList));
        }

        //overwrite default transport configuration
        Map<String, Object> properties = HttpSinkUtil
                .populateTransportConfiguration(clientBootstrapConfiguration, clientPoolConfiguration);

        clientConnector = httpConnectorFactory.createHttpClientConnector(properties, senderConfig);
    }

    private String encodeMessage(Object s) {
        try {
            return URLEncoder.encode((String) s, HttpConstants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SiddhiAppRuntimeException("Execution of Siddhi app " + siddhiAppContext.getName() +
                    " failed due to " + e.getMessage(), e);
        }
    }

    private String encodeBase64(String consumerKeyValue) {

        ByteBuf byteBuf = Unpooled.wrappedBuffer(consumerKeyValue.getBytes(StandardCharsets.UTF_8));
        ByteBuf encodedByteBuf = Base64.encode(byteBuf);
        return encodedByteBuf.toString(StandardCharsets.UTF_8);
    }
}
