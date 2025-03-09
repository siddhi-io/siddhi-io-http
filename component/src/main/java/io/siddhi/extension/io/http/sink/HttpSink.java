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
package io.siddhi.extension.io.http.sink;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.metrics.EndpointStatus;
import io.siddhi.extension.io.http.metrics.SinkMetrics;
import io.siddhi.extension.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import io.siddhi.extension.io.http.sink.updatetoken.AccessTokenCache;
import io.siddhi.extension.io.http.sink.updatetoken.DefaultListener;
import io.siddhi.extension.io.http.sink.updatetoken.HttpsClient;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.io.http.util.HttpIoUtil;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.si.metrics.core.internal.MetricsDataHolder;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ProxyServerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.http.sink.util.HttpSinkUtil.createConnectorFactory;
import static io.siddhi.extension.io.http.sink.util.HttpSinkUtil.createPoolConfigurations;
import static io.siddhi.extension.io.http.sink.util.HttpSinkUtil.createProxyServerConfiguration;
import static io.siddhi.extension.io.http.util.HttpConstants.EMPTY_STRING;
import static io.siddhi.extension.io.http.util.HttpConstants.FALSE;
import static io.siddhi.extension.io.http.util.HttpConstants.PORT_HOST_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpConstants.SOCKET_IDEAL_TIMEOUT_VALUE;
import static io.siddhi.extension.io.http.util.HttpConstants.TRUE;
import static org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants.REQUEST_URL;

/**
 * {@code HttpSink } Handle the HTTP publishing tasks.
 */
@Extension(name = "http", namespace = "sink",
        description = "" +
                "HTTP sink publishes messages via HTTP or HTTPS protocols using methods such as POST, GET, PUT, " +
                "and DELETE on formats `text`, `XML` and `JSON`. It can also publish " +
                "to endpoints protected by basic authentication or OAuth 2.0.",
        parameters = {
                @Parameter(
                        name = "publisher.url",
                        description = "The URL to which the outgoing events should be published.\n" +
                                "Examples:\n" +
                                "`http://localhost:8080/endpoint`,\n" +
                                "`https://localhost:8080/endpoint`",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "basic.auth.username",
                        description = "The username to be included in the authentication header when calling " +
                                "endpoints protected by basic authentication. `basic.auth.password` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "basic.auth.password",
                        description = "The password to be included in the authentication header when calling " +
                                "endpoints protected by basic authentication. `basic.auth.username` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "https.truststore.file",
                        description = "The file path of the client truststore when sending messages through `https`" +
                                " protocol.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "`${carbon.home}/resources/security/client-truststore.jks`"),
                @Parameter(
                        name = "https.truststore.password",
                        description = "The password for the client-truststore.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "wso2carbon"),
                @Parameter(
                        name = "https.keystore.file",
                        description = "The file path of the keystore when sending messages through `https`" +
                                " protocol.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "`${carbon.home}/resources/security/wso2carbon.jks`"),
                @Parameter(
                        name = "https.keystore.password",
                        description = "The password for the keystore.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "wso2carbon"),
                @Parameter(
                        name = "https.keystore.key.password",
                        description = "The password for the keys in the path of the keystore when sending messages " +
                                "through `https` protocol.",
                        type = {DataType.STRING},
                        optional = true, defaultValue = "wso2carbon"),
                @Parameter(
                        name = "oauth.username",
                        description = "The username to be included in the authentication header when calling " +
                                "endpoints protected by OAuth 2.0. `oauth.password` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "oauth.password",
                        description = "The password to be included in the authentication header when calling " +
                                "endpoints protected by OAuth 2.0. `oauth.username` property " +
                                "should be also set when using this property.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "consumer.key",
                        description = "Consumer key used for calling endpoints protected by OAuth 2.0",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "consumer.secret",
                        description = "Consumer secret used for calling endpoints protected by OAuth 2.0",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "body.consumer.key",
                        description = "Consumer key used for calling endpoints protected by OAuth 2.0 if it's " +
                                "required to be sent in token request body",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "body.consumer.secret",
                        description = "Consumer secret used for calling endpoints protected by OAuth 2.0",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "token.url",
                        description = "Token URL to generate a new access tokens " +
                                "when calling endpoints protected by OAuth 2.0",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "refresh.token",
                        description = "Refresh token used for generating new access tokens " +
                                "when calling endpoints protected by OAuth 2.0",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = HttpConstants.OAUTH2_SCOPE_PARAMETER_NAME,
                        description = "Standard OAuth 2.0 scope parameter",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "default"
                ),
                @Parameter(
                        name = "headers",
                        description = "HTTP request headers in format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "When `Content-Type` header is not provided the system derives the " +
                                "Content-Type based on the provided sink mapper as following: \n" +
                                " - `@map(type='xml')`: `application/xml`\n" +
                                " - `@map(type='json')`: `application/json`\n" +
                                " - `@map(type='text')`: `plain/text`\n" +
                                " - `@map(type='keyvalue')`: `application/x-www-form-urlencoded`\n" +
                                " - For all other cases system defaults to `plain/text`\n" +
                                "Also the `Content-Length` header need not to be provided, as the system " +
                                "automatically defines it by calculating the size of the payload.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Content-Type and Content-Length headers"),
                @Parameter(
                        name = "method",
                        description = "The HTTP method used for calling the endpoint.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "POST"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "Socket timeout in millis.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "6000"),
                @Parameter(
                        name = "chunk.disabled",
                        description = "Disable chunked transfer encoding.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "SSL/TLS protocol.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TLS"),
                @Parameter(
                        name = "ssl.verification.disabled",
                        description = "Disable SSL verification.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TLS store type.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "JKS"),
                @Parameter(
                        name = "ssl.configurations",
                        description = "SSL/TSL configurations in format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported parameters:\n" +
                                " - SSL/TLS protocols: `'sslEnabledProtocols:TLSv1.1,TLSv1.2'`\n" +
                                " - List of ciphers: `'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'`\n" +
                                " - Enable session creation: `'client.enable.session.creation:true'`\n" +
                                " - Supported server names: `'server.suported.server.names:server'`\n" +
                                " - Add HTTP SNIMatcher: `'server.supported.snimatchers:SNIMatcher'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "proxy.host",
                        description = "Proxy server host",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "proxy.port",
                        description = "Proxy server port",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "proxy.username",
                        description = "Proxy server username",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "proxy.password",
                        description = "Proxy server password",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "client.bootstrap.configurations",
                        description = "Client bootstrap configurations in format " +
                                "`\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Client connect timeout in millis: `'client.bootstrap.connect.timeout:15000'`\n" +
                                " - Client socket timeout in seconds: `'client.bootstrap.socket.timeout:15'`\n" +
                                " - Client socket reuse: `'client.bootstrap.socket.reuse:true'`\n" +
                                " - Enable TCP no delay: `'client.bootstrap.nodelay:true'`\n" +
                                " - Enable client keep alive: `'client.bootstrap.keepalive:true'`\n" +
                                " - Send buffer size: `'client.bootstrap.sendbuffersize:1048576'`\n" +
                                " - Receive buffer size: `'client.bootstrap.recievebuffersize:1048576'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "max.pool.active.connections",
                        description = "Maximum possible number of active connection per client pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "-1"),
                @Parameter(
                        name = "min.pool.idle.connections",
                        description = "Minimum number of idle connections that can exist per client pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "0"),
                @Parameter(
                        name = "max.pool.idle.connections",
                        description = "Maximum number of idle connections that can exist per client pool.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "100"),
                @Parameter(
                        name = "executor.service.threads",
                        description = "Thread count for the executor service.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "20"),
                @Parameter(
                        name = "min.evictable.idle.time",
                        description = "Minimum time (in millis) a connection may sit idle in the " +
                                "client pool before it become eligible for eviction.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "300000"),
                @Parameter(
                        name = "time.between.eviction.runs",
                        description = "Time between two eviction operations (in millis) on the client pool.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "30000"),
                @Parameter(
                        name = "max.wait.time",
                        description = "The maximum time (in millis) the pool will wait (when there are no " +
                                "available connections) for a connection to be returned to the pool.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "60000"),
                @Parameter(
                        name = "test.on.borrow",
                        description = "Enable connections to be validated " +
                                "before being borrowed from the client pool.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "test.while.idle",
                        description = "Enable connections to be validated during the eviction operation (if any).",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
                @Parameter(
                        name = "exhausted.action",
                        description = "Action that should be taken when the maximum number of active connections " +
                                "are being used. This action should be indicated as an int and possible " +
                                "action values are following.\n" +
                                "0 - Fail the request.\n" +
                                "1 - Block the request, until a connection returns to the " +
                                "pool.\n" +
                                "2 - Grow the connection pool size.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1 (Block when exhausted)"),
                @Parameter(
                        name = "hostname.verification.enabled",
                        description = "Enable hostname verification.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
        },
        examples = {
                @Example(syntax = "" +
                        "@sink(type = 'http', publisher.url = 'http://stocks.com/stocks',\n" +
                        "      @map(type = 'json'))\n" +
                        "define stream StockStream (symbol string, price float, volume long);",
                        description = "" +
                                "Events arriving on the StockStream will be published to the HTTP endpoint " +
                                "`http://stocks.com/stocks` using `POST` method with Content-Type `application/json` " +
                                "by converting those events to the default JSON format as following:\n"
                                + "```{\n" +
                                "  \"event\": {\n" +
                                "    \"symbol\": \"FB\",\n" +
                                "    \"price\": 24.5,\n" +
                                "    \"volume\": 5000\n" +
                                "  }\n" +
                                "}```"
                ),
                @Example(syntax = "" +
                        "@sink(type='http', publisher.url = 'http://localhost:8009/foo',\n" +
                        "      client.bootstrap.configurations = \"'client.bootstrap.socket.timeout:20'\",\n" +
                        "      max.pool.active.connections = '1', headers = \"{{headers}}\",\n" +
                        "      @map(type='xml', @payload(\"\"\"<stock>\n{{payloadBody}}\n</stock>\"\"\")))\n" +
                        "define stream FooStream (payloadBody String, headers string);",
                        description = "" +
                                "Events arriving on FooStream will be published to the HTTP endpoint " +
                                "`http://localhost:8009/foo` using `POST` method with Content-Type `application/xml` " +
                                "and setting `payloadBody` and `header` attribute values.\n" +
                                "If the `payloadBody` contains\n" +
                                "```<symbol>WSO2</symbol>\n" +
                                "<price>55.6</price>\n" +
                                "<volume>100</volume>```" +
                                "and `header` contains `'topic:foobar'` values, then the system will generate " +
                                "an output with the body:\n" +
                                "```<stock>\n" +
                                "<symbol>WSO2</symbol>\n" +
                                "<price>55.6</price>\n" +
                                "<volume>100</volume>\n" +
                                "</stock>```" +
                                "and HTTP headers:\n" +
                                "`Content-Length:xxx`,\n" +
                                "`Content-Location:'xxx'`,\n" +
                                "`Content-Type:'application/xml'`,\n" +
                                "`HTTP_METHOD:'POST'`"
                )},
        systemParameter = {
                @SystemParameter(
                        name = "clientBootstrapClientGroupSize",
                        description = "Number of client threads to perform non-blocking read and write to " +
                                "one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "clientBootstrapBossGroupSize",
                        description = "Number of boss threads to accept incoming connections.",
                        defaultValue = "Number of available processors",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "clientBootstrapWorkerGroupSize",
                        description = "Number of worker threads to accept the connections from boss threads and " +
                                "perform non-blocking read and write from one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "trustStoreLocation",
                        description = "The default truststore file path.",
                        defaultValue = "`${carbon.home}/resources/security/client-truststore.jks`",
                        possibleParameters = "Path to client truststore `.jks` file"
                ),
                @SystemParameter(
                        name = "trustStorePassword",
                        description = "The default truststore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Truststore password as string"
                ),
                @SystemParameter(
                        name = "keyStoreLocation",
                        description = "The default keystore file path.",
                        defaultValue = "${carbon.home}/resources/security/wso2carbon.jks",
                        possibleParameters = "Path to client keystore `.jks` file"
                ),
                @SystemParameter(
                        name = "keyStorePassword",
                        description = "The default keystore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Keystore password as string"
                ),
                @SystemParameter(
                        name = "keyPassword",
                        description = "The default keystore key password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Keystore key password as string"
                )
        }
)
public class HttpSink extends Sink {
    private static final Logger log = LogManager.getLogger(HttpSink.class);
    protected String streamID;
    protected String consumerKey;
    protected String consumerSecret;
    protected String userName;
    protected String userPassword;
    protected ClientConnector staticClientConnector;
    protected Option publisherURLOption;
    protected SiddhiAppContext siddhiAppContext;
    protected String oauthUsername;
    protected String oauthUserPassword;
    protected String authType;
    protected String tokenURL;
    String mapType;
    Option httpHeaderOption;
    Option httpMethodOption;
    private String authorizationHeader;
    private String clientTrustStoreFile;
    private String clientTrustStorePass;
    private String keyStorePath;
    private String keyStorePass;
    private String keyPassword;
    private int socketIdleTimeout;
    private String sslProtocol;
    private String tlsStoreType;
    private String chunkDisabled;
    private String parametersList;
    private String clientBootstrapConfiguration;
    private ConfigReader configReader;
    private Option refreshToken;
    private AccessTokenCache accessTokenCache = AccessTokenCache.getInstance();
    private String hostnameVerificationEnabled;
    private String sslVerificationDisabled;
    private Executor executor = null;
    private String publisherURL;
    protected SinkMetrics metrics;
    protected long startTime;
    protected long endTime;
    private String bodyConsumerKey;
    private String bodyConsumerSecret;
    private String oauth2Scope;

    private DefaultHttpWsConnectorFactory httpConnectorFactory;
    private ProxyServerConfiguration proxyServerConfiguration;
    private PoolConfiguration connectionPoolConfiguration;

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
     * the transport.
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
     * @param siddhiAppContext       the context of the {@link io.siddhi.query.api.SiddhiApp} used to
     *                               get siddhi related utilty functions.
     */
    @Override
    protected StateFactory init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                                ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        //read configurations
        this.configReader = configReader;
        this.siddhiAppContext = siddhiAppContext;
        this.streamID = siddhiAppContext.getName() + PORT_HOST_SEPARATOR + outputStreamDefinition.toString();
        this.publisherURLOption = optionHolder.validateAndGetOption(HttpConstants.PUBLISHER_URL);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.httpMethodOption = optionHolder.getOrCreateOption(HttpConstants.METHOD, HttpConstants.DEFAULT_METHOD);
        this.consumerKey = optionHolder.validateAndGetStaticValue(HttpConstants.CONSUMER_KEY, EMPTY_STRING);
        this.consumerSecret = optionHolder.validateAndGetStaticValue(HttpConstants.CONSUMER_SECRET, EMPTY_STRING);
        this.bodyConsumerKey = optionHolder.validateAndGetStaticValue(HttpConstants.BODY_CONSUMER_KEY, EMPTY_STRING);
        this.bodyConsumerSecret = optionHolder.validateAndGetStaticValue(HttpConstants.BODY_CONSUMER_SECRET,
                EMPTY_STRING);
        this.oauthUsername = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_OAUTH_USERNAME,
                EMPTY_STRING);
        this.oauthUserPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_OAUTH_PASSWORD,
                EMPTY_STRING);
        this.refreshToken = optionHolder.getOrCreateOption(HttpConstants.RECEIVER_REFRESH_TOKEN, EMPTY_STRING);
        this.tokenURL = optionHolder.validateAndGetStaticValue(HttpConstants.TOKEN_URL, EMPTY_STRING);
        this.clientTrustStoreFile = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PATH_PARAM,
                HttpSinkUtil.trustStorePath(configReader));
        this.oauth2Scope = optionHolder.validateAndGetStaticValue(HttpConstants.OAUTH2_SCOPE_PARAMETER_NAME,
                "default");
        clientTrustStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_PARAM,
                HttpSinkUtil.trustStorePassword(configReader));
        socketIdleTimeout = Integer.parseInt(optionHolder.validateAndGetStaticValue
                (HttpConstants.SOCKET_IDEAL_TIMEOUT, SOCKET_IDEAL_TIMEOUT_VALUE));
        sslProtocol = optionHolder.validateAndGetStaticValue(HttpConstants.SSL_PROTOCOL, EMPTY_STRING);
        tlsStoreType = optionHolder.validateAndGetStaticValue(HttpConstants.TLS_STORE_TYPE, EMPTY_STRING);
        chunkDisabled = optionHolder.validateAndGetStaticValue(HttpConstants.CLIENT_CHUNK_DISABLED, EMPTY_STRING);
        this.keyStorePath = optionHolder.validateAndGetStaticValue(HttpConstants.KEYSTORE_PATH_PARAM,
                HttpSinkUtil.keyStorePath(configReader));
        this.keyStorePass = optionHolder.validateAndGetStaticValue(HttpConstants.KEYSTORE_PASSWORD_PARAM,
                HttpSinkUtil.keyStorePassword(configReader));
        this.keyPassword = optionHolder.validateAndGetStaticValue(HttpConstants.KEYSTORE_KEY_PASSWORD_PARAM,
                HttpSinkUtil.keyPassword(configReader));
        //pool configurations
        connectionPoolConfiguration = createPoolConfigurations(optionHolder);

        executor = Executors.newFixedThreadPool(connectionPoolConfiguration.getExecutorServiceThreads());

        parametersList = optionHolder.validateAndGetStaticValue(HttpConstants.SINK_PARAMETERS, EMPTY_STRING);

        clientBootstrapConfiguration = optionHolder
                .validateAndGetStaticValue(HttpConstants.CLIENT_BOOTSTRAP_CONFIGURATION, EMPTY_STRING);
        hostnameVerificationEnabled = optionHolder.validateAndGetStaticValue(
                HttpConstants.HOSTNAME_VERIFICATION_ENABLED, TRUE);
        sslVerificationDisabled = optionHolder.validateAndGetStaticValue(HttpConstants.SSL_VERIFICATION_DISABLED,
                FALSE);

        userName = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_USERNAME, EMPTY_STRING);
        userPassword = optionHolder.validateAndGetStaticValue(HttpConstants.RECEIVER_PASSWORD, EMPTY_STRING);

        if (!HttpConstants.EMPTY_STRING.equals(userName) && !HttpConstants.EMPTY_STRING.equals(userPassword)) {
            authType = HttpConstants.BASIC_AUTH;
        } else if ((!HttpConstants.EMPTY_STRING.equals(consumerKey)
                && !HttpConstants.EMPTY_STRING.equals(consumerSecret)) ||
                (!HttpConstants.EMPTY_STRING.equals(bodyConsumerKey)
                        && !HttpConstants.EMPTY_STRING.equals(bodyConsumerSecret)) ||
                (!HttpConstants.EMPTY_STRING.equals(oauthUsername)
                        && !HttpConstants.EMPTY_STRING.equals(oauthUserPassword))) {
            authType = HttpConstants.OAUTH;
        } else {
            authType = HttpConstants.NO_AUTH;
        }

        // if username and password both not equal to null consider as basic auth enabled if only one is null take it
        // as exception
        if ((EMPTY_STRING.equals(userName) ^
                EMPTY_STRING.equals(userPassword))) {
            throw new SiddhiAppCreationException("Please provide user name and password in " +
                    HttpConstants.HTTP_SINK_ID + " with the stream " + streamID + " in Siddhi app " +
                    siddhiAppContext.getName());
        } else if (!(EMPTY_STRING.equals(userName))) {
            byte[] val = (userName + HttpConstants.AUTH_USERNAME_PASSWORD_SEPARATOR + userPassword).getBytes(Charset
                    .defaultCharset());
            this.authorizationHeader = HttpConstants.AUTHORIZATION_METHOD + Base64.encode
                    (Unpooled.copiedBuffer(val));
        }

        proxyServerConfiguration = createProxyServerConfiguration(optionHolder, streamID, siddhiAppContext.getName());

        httpConnectorFactory = createConnectorFactory(configReader);
        if (publisherURLOption.isStatic()) {
            staticClientConnector = createClientConnector(null);
        }
        initMetrics(outputStreamDefinition.getId());
        return null;
    }


    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * Sending events via output transport.
     *
     * @param payload        payload of the event
     * @param dynamicOptions one of the event constructing the payload
     * @param state          current state of the sink
     * @throws ConnectionUnavailableException throw when connections are unavailable.
     */
    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        startTime = System.currentTimeMillis();

        //get the dynamic parameter
        String headers = httpHeaderOption.getValue(dynamicOptions);
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);

        ClientConnector clientConnector;
        if (staticClientConnector != null) {
            clientConnector = staticClientConnector;
        } else {
            clientConnector = createClientConnector(dynamicOptions);
        }

        if (mapType == null) {
            mapType = getMapper().getType();
        }

        if (authType.equals(HttpConstants.BASIC_AUTH) || authType.equals(HttpConstants.NO_AUTH)) {
            sendRequest(payload, dynamicOptions, headersList, clientConnector);
        } else {
            sendOauthRequest(payload, dynamicOptions, headersList, clientConnector);
        }
    }

    protected int sendRequest(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                              ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        String httpMethod = EMPTY_STRING.equals(httpMethodOption.getValue(dynamicOptions)) ?
                HttpConstants.METHOD_DEFAULT : httpMethodOption.getValue(dynamicOptions);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        String messageBody = getMessageBody(payload);
        HttpMethod httpReqMethod = new HttpMethod(httpMethod);
        HttpCarbonMessage cMessage = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpReqMethod, EMPTY_STRING));
        cMessage = generateCarbonMessage(headersList, contentType, httpMethod, cMessage,
                clientConnector.getHttpURLProperties());

        if (publisherURLOption.isStatic()) {
            publisherURL = publisherURLOption.getValue();
        } else {
            publisherURL = publisherURLOption.getValue(dynamicOptions);
        }

        if (metrics != null) {
            metrics.getTotalWritesMetric().inc();
            metrics.getTotalHttpWritesMetric(publisherURL).inc();
            metrics.getRequestSizeMetric(publisherURL).inc(HttpSinkUtil.getByteSize(messageBody));
        }

        if (!Constants.HTTP_GET_METHOD.equals(httpMethod)) {
            cMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(messageBody
                    .getBytes(Charset.defaultCharset()))));
        }
        cMessage.completeMessage();
        if (HttpConstants.OAUTH.equals(authType)) {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultListener listener = new DefaultListener(latch, authType);
            HttpResponseFuture responseFuture = clientConnector.send(cMessage);
            responseFuture.setHttpConnectorListener(listener);
            try {
                boolean latchCount = latch.await(30, TimeUnit.SECONDS);
                if (!latchCount) {
                    log.debug("Time out due to getting getting response from " + clientConnector.getPublisherURL() +
                            ". Message dropped.");
                    throw new ConnectionUnavailableException("Time out due to getting getting response from "
                            + clientConnector.getPublisherURL() + ". Message dropped.");
                }
            } catch (InterruptedException e) {
                log.debug("Failed to get a response from " + clientConnector.getPublisherURL() + "," + e +
                        ". Message dropped.");
                throw new ConnectionUnavailableException("Failed to get a response from " +
                        clientConnector.getPublisherURL() + ", " + e + ". Message dropped.");
            }
            HttpCarbonMessage response = listener.getHttpResponseMessage();
            log.info(" Response: *********** " + response.getNettyHttpResponse().status());
            return response.getNettyHttpResponse().status().code();
        } else {
            HttpResponseFuture responseFuture = clientConnector.send(cMessage);
            HTTPResponseListener responseListener = new HTTPResponseListener(payload, dynamicOptions, this,
                    clientConnector.getPublisherURL());
            responseFuture.setHttpConnectorListener(responseListener);
            return HttpConstants.SUCCESS_CODE;
        }
    }

    private String getStringFromInputStream(InputStream in) {
        BufferedInputStream bis = new BufferedInputStream(in);
        String result;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            result = bos.toString(StandardCharsets.UTF_8.toString());
        } catch (IOException ioe) {
            log.error("Couldn't read the complete input stream");
            return "";
        }
        return result;
    }


    protected void sendOauthRequest(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                    ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        //generate encoded base64 auth for getting refresh token
        String consumerKeyValue;

        if (!HttpConstants.EMPTY_STRING.equals(this.consumerKey)
                && !HttpConstants.EMPTY_STRING.equals(this.consumerSecret)) {
            consumerKeyValue = consumerKey + ":" + consumerSecret;
        } else {
            consumerKeyValue = bodyConsumerKey + ":" + bodyConsumerSecret;
        }
        String encodedAuth = "Basic " + encodeBase64(consumerKeyValue)
                .replaceAll(HttpConstants.NEW_LINE, HttpConstants.EMPTY_STRING);
        //check the availability of access token in the header
        setAccessToken(encodedAuth, dynamicOptions, headersList, clientConnector.getPublisherURL());
        //send a request to API and get the response
        int response = sendRequest(payload, dynamicOptions, headersList, clientConnector);
        //if authentication fails then get the new access token
        if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            handleOAuthFailure(payload, dynamicOptions, headersList, encodedAuth, clientConnector);
        } else if (HttpConstants.SUCCESS_CODE <= response && response < HttpConstants.MULTIPLE_CHOICES) {
            log.info("Request sent successfully to " + clientConnector.getPublisherURL());
        } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
            log.error("Error at sending oauth request to API endpoint " + clientConnector.getPublisherURL() +
                    "', with response code: " +
                    response + "- Internal server error. Message dropped");
            throw new HttpSinkAdaptorRuntimeException("Error at sending oauth request to API endpoint, " +
                    clientConnector.getPublisherURL() + "', with response code: " + response +
                    "- Internal server error. Message dropped.");
        } else {
            log.error("Error at sending oauth request to API endpoint " +
                    clientConnector.getPublisherURL() + "', with response code: " + response + ". Message dropped.");
            throw new ConnectionUnavailableException("Error at sending oauth request to API endpoint " +
                    clientConnector.getPublisherURL() + "', and response code: " + response + ". Message dropped.");
        }
    }

    private void handleOAuthFailure(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                    String encodedAuth, ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        boolean checkFromCache = accessTokenCache.checkAvailableKey(encodedAuth);
        if (checkFromCache) {
            getNewAccessTokenWithCache(payload, dynamicOptions, headersList, encodedAuth, clientConnector);
        } else {
            requestForNewAccessToken(payload, dynamicOptions, headersList, encodedAuth, clientConnector);
        }
    }

    private void getNewAccessTokenWithCache(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                            String encodedAuth, ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        String accessToken = accessTokenCache.getAccessToken(encodedAuth);
        for (Header header : headersList) {
            if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                header.setValue(accessToken);
                break;
            }
        }
        //send a request to API with a new access token
        int response = sendRequest(payload, dynamicOptions, headersList, clientConnector
        );
        if (response == HttpConstants.SUCCESS_CODE) {
            log.info("Request sent successfully to " + clientConnector.getPublisherURL());
        } else if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            requestForNewAccessToken(payload, dynamicOptions, headersList, encodedAuth, clientConnector);
        } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
            log.error("Error at sending oauth request to API endpoint, " + clientConnector.getPublisherURL() +
                    "', with response code: " +
                    response + "- Internal server error. Message dropped");
            throw new HttpSinkAdaptorRuntimeException("Error at sending oauth request to API endpoint, " +
                    clientConnector.getPublisherURL() + "', with response code: " + response +
                    "- Internal server error. Message dropped");
        } else {
            log.error("Error at sending oauth request to API endpoint " + clientConnector.getPublisherURL() +
                    "', with response code: " +
                    response + ". Message dropped.");
            throw new ConnectionUnavailableException("Error at sending oauth request to API endpoint " +
                    clientConnector.getPublisherURL() +
                    "', with response code: " + response + ". Message dropped.");
        }
    }

    private void requestForNewAccessToken(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                                          String encodedAuth, ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        Boolean checkRefreshToken = accessTokenCache.checkRefreshAvailableKey(encodedAuth);
        if (checkRefreshToken) {
            for (Header header : headersList) {
                if (header.getName().equals(HttpConstants.RECEIVER_REFRESH_TOKEN)) {
                    if (accessTokenCache.getRefreshtoken(encodedAuth) != null) {
                        header.setValue(accessTokenCache.getRefreshtoken(encodedAuth));
                    }
                    break;
                }
            }
        }
        getAccessToken(dynamicOptions, encodedAuth, tokenURL);
        if (accessTokenCache.getResponseCode(encodedAuth) == HttpConstants.SUCCESS_CODE) {
            String newAccessToken = accessTokenCache.getAccessToken(encodedAuth);
            accessTokenCache.setAccessToken(encodedAuth, newAccessToken);
            if (accessTokenCache.getRefreshtoken(encodedAuth) != null) {
                accessTokenCache.setRefreshtoken(encodedAuth, accessTokenCache.getRefreshtoken(encodedAuth));
            }
            for (Header header : headersList) {
                if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                    header.setValue(newAccessToken);
                    break;
                }
            }
            //send a request to API with a new access token
            int response = sendRequest(payload, dynamicOptions, headersList, clientConnector
            );
            if (response == HttpConstants.SUCCESS_CODE) {
                log.info("Request sent successfully to " + clientConnector.getPublisherURL());
            } else if (response == HttpConstants.AUTHENTICATION_FAIL_CODE) {
                log.error("Error at sending oauth request to API endpoint " + clientConnector.getPublisherURL() +
                        "', with response code: " +
                        response + "- Authentication Failure. Please provide a valid Consumer key, Consumer secret" +
                        " and token endpoint URL . Message dropped");
                throw new HttpSinkAdaptorRuntimeException("Error at sending oauth request to API endpoint " +
                        clientConnector.getPublisherURL() + "', with response code: " + response +
                        "- Authentication Failure." +
                        " Please provide a valid Consumer key, Consumer secret and token endpoint URL." +
                        " Message dropped");
            } else if (response == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
                log.error("Error at sending oauth request to API endpoint " + clientConnector.getPublisherURL() +
                        "', with response code: " +
                        response + "- Internal server error. Message dropped");
                throw new HttpSinkAdaptorRuntimeException("Error at sending oauth request to API endpoint "
                        + clientConnector.getPublisherURL() + "', with response code: " + response +
                        "- Internal server error. Message dropped");
            } else {
                log.error("Error at sending oauth request to API endpoint " + clientConnector.getPublisherURL() +
                        "', with response code: " +
                        response + ". Message dropped.");
                throw new ConnectionUnavailableException("Error at sending oauth request to API endpoint " +
                        clientConnector.getPublisherURL() + "', with response code: " + response +
                        ". Message dropped.");
            }
        } else if (accessTokenCache.getResponseCode(encodedAuth) == HttpConstants.AUTHENTICATION_FAIL_CODE) {
            log.error("Failed to generate new access token for the expired access token to " +
                    clientConnector.getPublisherURL() + "', " +
                    accessTokenCache.getResponseCode(encodedAuth) + ": Authentication Failure.cPlease provide a " +
                    "valid Consumer key, Consumer secret and token endpoint URL . Message dropped");
            throw new HttpSinkAdaptorRuntimeException("Failed to generate new access token for the expired access " +
                    "token to " + clientConnector.getPublisherURL() + "', " +
                    accessTokenCache.getResponseCode(encodedAuth) +
                    ": Authentication Failure.Please provide a valid Consumer key, Consumer secret" +
                    " and token endpoint URL . Message dropped");
        } else {
            log.error("Failed to generate new access token for the expired access token. Error code: " +
                    accessTokenCache.getResponseCode(encodedAuth) + ". Message dropped.");
            throw new ConnectionUnavailableException("Failed to generate new access token for the expired" +
                    " access token. Error code: " + accessTokenCache.getResponseCode(encodedAuth)
                    + ". Message dropped.");
        }
    }

    void getAccessToken(DynamicOptions dynamicOptions, String encodedAuth, String tokenURL) {
        this.tokenURL = tokenURL;
        HttpsClient httpsClient = new HttpsClient();
        if (!HttpConstants.EMPTY_STRING.equals(refreshToken.getValue(dynamicOptions)) ||
                accessTokenCache.getRefreshtoken(encodedAuth) != null) {
            httpsClient.getRefreshGrantAccessToken(tokenURL, keyStorePath, keyStorePass, keyPassword,
                    clientTrustStoreFile, clientTrustStorePass, encodedAuth, refreshToken.getValue(dynamicOptions),
                    oauthUsername, oauthUserPassword, bodyConsumerKey, bodyConsumerSecret, oauth2Scope);
        } else if (!HttpConstants.EMPTY_STRING.equals(oauthUsername) &&
                !HttpConstants.EMPTY_STRING.equals(oauthUserPassword)) {
            httpsClient.getPasswordGrantAccessToken(tokenURL, keyStorePath, keyStorePass, keyPassword,
                    clientTrustStoreFile, clientTrustStorePass, oauthUsername, oauthUserPassword, encodedAuth,
                    bodyConsumerKey, bodyConsumerSecret, oauth2Scope);
        } else {
            httpsClient.getClientGrantAccessToken(tokenURL, keyStorePath, keyStorePass, keyPassword,
                    clientTrustStoreFile, clientTrustStorePass, encodedAuth);
        }
    }

    void setAccessToken(String encodedAuth, DynamicOptions dynamicOptions,
                        List<Header> headersList, String publisherURL)
            throws ConnectionUnavailableException {
        //check the availability of the authorization
        String accessToken;
        boolean authAvailability = false;
        for (Header header : headersList) {
            if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                authAvailability = true;
                break;
            }
        }

        if (!authAvailability) {
            //generate encoded base64 auth for getting refresh token
            getAccessToken(dynamicOptions, encodedAuth, tokenURL);
            if (accessTokenCache.getResponseCode(encodedAuth) == HttpConstants.SUCCESS_CODE) {
                headersList.add(new Header(HttpConstants.AUTHORIZATION_HEADER,
                        accessTokenCache.getAccessToken(encodedAuth)));
                if (accessTokenCache.getRefreshtoken(encodedAuth) != null) {
                    headersList.add(new Header(HttpConstants.RECEIVER_REFRESH_TOKEN,
                            accessTokenCache.getRefreshtoken(encodedAuth)));
                }
            } else if (accessTokenCache.getResponseCode(encodedAuth) == HttpConstants.AUTHENTICATION_FAIL_CODE) {
                log.error("Failed to generate new access token for the expired access token to " + publisherURL +
                        "', with response code: " + accessTokenCache.getResponseCode(encodedAuth) +
                        "- Authentication Failure.Please provide a valid Consumer key, Consumer secret" +
                        " and token endpoint URL . Message dropped");
                throw new HttpSinkAdaptorRuntimeException("Failed to generate new access token for the expired" +
                        " access token to " + publisherURL + "', with response code: " +
                        accessTokenCache.getResponseCode(encodedAuth) + "- Authentication Failure." +
                        "Please provide a valid Consumer key, Consumer secret and token endpoint URL ." +
                        " Message dropped");
            } else if (accessTokenCache.getResponseCode(encodedAuth) == HttpConstants.INTERNAL_SERVER_FAIL_CODE) {
                log.error("Failed to generate new access token for the expired access token to " + publisherURL +
                        "', with response code: " + accessTokenCache.getResponseCode(encodedAuth) +
                        "- Internal server error. Message dropped");
                throw new HttpSinkAdaptorRuntimeException("Failed to generate new access token for the expired" +
                        " access token to " + publisherURL + "', with response code: " +
                        accessTokenCache.getResponseCode(encodedAuth) + "- Internal server error. Message dropped");
            } else {
                log.error("Failed to generate new access token for the expired access token. Error code: " +
                        accessTokenCache.getResponseCode(encodedAuth) + ". Message dropped.");
                throw new ConnectionUnavailableException("Failed to generate new access token for the expired" +
                        " access token. Error code: " + accessTokenCache.getResponseCode(encodedAuth) +
                        ". Message dropped.");
            }
        } else {
            //check the cache and update new access token into header
            if (accessTokenCache.checkAvailableKey(encodedAuth)) {
                accessToken = accessTokenCache.getAccessToken(encodedAuth);
                for (Header header : headersList) {
                    if (header.getName().equals(HttpConstants.AUTHORIZATION_HEADER)) {
                        header.setValue(accessToken);
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     * such that the  system will take care retrying for connection
     */
    @Override
    public void connect() {

    }

    /**
     * Called after all publishing is done, the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {
        if (staticClientConnector != null) {
            String publisherURL = staticClientConnector.getPublisherURL();
            staticClientConnector = null;
            log.debug("Server connector for url " + publisherURL + " disconnected.");
        }

        if (httpConnectorFactory != null) {
            httpConnectorFactory.shutdownNow();
            httpConnectorFactory = null;
        }
    }

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that has to be done when removing the receiver has to be done here.
     */
    @Override
    public void destroy() {
        if (staticClientConnector != null) {
            String publisherURL = staticClientConnector.getPublisherURL();
            staticClientConnector = null;
            log.debug("Server connector for url " + publisherURL + " disconnected.");
        }
    }

    /**
     * Initialize metrics.
     * @param streamName name of the stream
     */
    protected void initMetrics(String streamName) {
        if (MetricsDataHolder.getInstance().getMetricService() != null
                && MetricsDataHolder.getInstance().getMetricManagementService().isEnabled()) {
            try {
                if (MetricsDataHolder.getInstance().getMetricManagementService()
                        .isReporterRunning(HttpConstants.PROMETHEUS_REPORTER_NAME)) {
                    metrics = new SinkMetrics(siddhiAppContext.getName(), streamName);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Prometheus reporter is not running. Hence http sink metrics will not be initialized for "
                        + siddhiAppContext.getName());
            }
        }
    }

    /**
     * The method is responsible of generating carbon message to send.
     *
     * @param headers           the headers set.
     * @param contentType       the content type. Value is if user has to given it as a header or if not it is map type.
     * @param httpMethod        http method type.
     * @param cMessage          carbon message to be send to the endpoint.
     * @param httpURLProperties
     * @return generated carbon message.
     */
    HttpCarbonMessage generateCarbonMessage(List<Header> headers, String contentType,
                                            String httpMethod, HttpCarbonMessage cMessage,
                                            Map<String, String> httpURLProperties) {
        // Set protocol type http or https
        cMessage.setProperty(Constants.PROTOCOL, httpURLProperties.get(Constants.PROTOCOL));
        // Set uri
        cMessage.setProperty(Constants.TO, httpURLProperties.get(Constants.TO));
        // set Host
        cMessage.setProperty(Constants.HTTP_HOST, httpURLProperties.get(Constants.HTTP_HOST));
        //set port
        cMessage.setProperty(Constants.HTTP_PORT, Integer.valueOf(httpURLProperties.get(Constants.HTTP_PORT)));
        // Set method
        cMessage.setHttpMethod(httpMethod);
        //Set request URL
        cMessage.setRequestUrl(httpURLProperties.get(REQUEST_URL));
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

        //Set request headers.
        httpHeaders.set(Constants.HTTP_HOST, cMessage.getProperty(Constants.HTTP_HOST));
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
        cMessage.setHttpMethod(httpMethod);
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

    public ClientConnector createClientConnector(DynamicOptions dynamicOptions) {
        if (publisherURLOption.isStatic()) {
            publisherURL = publisherURLOption.getValue();
        } else {
            publisherURL = publisherURLOption.getValue(dynamicOptions);
        }
        if (authType.equals(HttpConstants.OAUTH)) {
            if ((EMPTY_STRING.equals(consumerSecret) || EMPTY_STRING.equals(consumerKey))
                    && (EMPTY_STRING.equals(bodyConsumerKey) || EMPTY_STRING.equals(bodyConsumerSecret))) {
                throw new SiddhiAppCreationException(HttpConstants.CONSUMER_KEY + " and " +
                        HttpConstants.CONSUMER_SECRET + " found empty but it is Mandatory field in " +
                        HttpConstants.HTTP_SINK_ID + " in " + streamID);
            }
            if (EMPTY_STRING.equals(tokenURL)) {
                throw new SiddhiAppCreationException(HttpConstants.TOKEN_URL + " found empty but it is Mandatory " +
                        "field in " + HttpConstants.HTTP_SINK_ID + " in " + streamID);
            }
        }
        String scheme = HttpSinkUtil.getScheme(publisherURL);
        Map<String, String> httpURLProperties = HttpSinkUtil.getURLProperties(publisherURL);
        //Generate basic sender configurations
        SenderConfiguration senderConfig =
                HttpSinkUtil.getSenderConfigurations(httpURLProperties, clientTrustStoreFile, clientTrustStorePass,
                        configReader);
        if (EMPTY_STRING.equals(publisherURL)) {
            throw new SiddhiAppCreationException("Receiver URL found empty but it is Mandatory field in " +
                    "" + HttpConstants.HTTP_SINK_ID + " in " + streamID);
        }
        if (HttpConstants.SCHEME_HTTPS.equals(scheme) &&
                ((clientTrustStoreFile == null) || (clientTrustStorePass == null))) {
            throw new ExceptionInInitializerError("Client trustStore file path or password are empty while " +
                    "default scheme is 'https'. Please provide client " +
                    "trustStore file path and password in " + streamID);
        }
        if (proxyServerConfiguration != null) {
            senderConfig.setProxyServerConfiguration(proxyServerConfiguration);
        }
        senderConfig.setPoolConfiguration(connectionPoolConfiguration);

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
        if (!EMPTY_STRING.equals(parametersList)) {
            senderConfig.setParameters(HttpIoUtil.populateParameters(parametersList));
        }
        if (!TRUE.equalsIgnoreCase(hostnameVerificationEnabled)) {
            senderConfig.setHostNameVerificationEnabled(false);
        }

        if (TRUE.equalsIgnoreCase(sslVerificationDisabled)) {
            senderConfig.disableSsl();
        }

        //overwrite default transport configuration
        Map<String, Object> bootStrapProperties = HttpSinkUtil
                .populateTransportConfiguration(clientBootstrapConfiguration);
        return new ClientConnector(publisherURL, httpURLProperties,
                httpConnectorFactory.createHttpClientConnector(bootStrapProperties, senderConfig));
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

    private class HTTPResponseListener implements HttpConnectorListener {
        Object payload;
        DynamicOptions dynamicOptions;
        HttpSink httpSink;
        private final String publisherURL;

        HTTPResponseListener(Object payload, DynamicOptions dynamicOptions, HttpSink httpSink, String publisherURL) {
            this.payload = payload;
            this.dynamicOptions = dynamicOptions;
            this.httpSink = httpSink;
            this.publisherURL = publisherURL;
        }

        @Override
        public void onMessage(HttpCarbonMessage httpCarbonMessage) {
            endTime = System.currentTimeMillis();

            if (metrics != null) {
                metrics.setEndpointStatusMetric(publisherURL, EndpointStatus.ONLINE);
                metrics.setLatencyMetric(publisherURL, endTime - startTime);
                metrics.setLastEventTime(publisherURL, endTime);

                // Catch unsuccessful requests
                if (httpCarbonMessage.getHttpStatusCode() / 100 != 2) {
                    metrics.getTotalHttpErrorsMetric(publisherURL).inc();
                }
            }

            if (executor != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                            /*
                            Read content from the input stream of the HTTP Carbon message
                            and make sure that the Carbon message is cleaned up,
                            for preventing leaks.
                             */
                        getStringFromInputStream(
                                new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());
                    }
                });
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (metrics != null) {
                metrics.getTotalHttpErrorsMetric(publisherURL).inc();
                metrics.setEndpointStatusMetric(publisherURL, EndpointStatus.OFFLINE);
            }

            httpSink.onError(payload, dynamicOptions,
                    new ConnectionUnavailableException("HTTP sink on stream " + httpSink.streamID +
                            " of Siddhi App '" + httpSink.siddhiAppContext.getName() +
                            "' failed to publish events to endpoint '" + publisherURL + "'. " +
                            throwable.getMessage(), throwable));
        }
    }
}
