/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.http.sink;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.event.Event;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import io.siddhi.extension.io.http.source.HttpResponseMessageListener;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.http.util.HttpConstants.EMPTY_STRING;


/**
 * {@code HttpCallSink} Handle the HTTP calling tasks.
 */
@Extension(name = "http-call", namespace = "sink",
        description = "The http-call sink publishes messages to endpoints via HTTP or HTTPS protocols using methods" +
                " such as POST, GET, PUT, and DELETE on formats `text`, `XML` or `JSON` and consume responses " +
                "through its corresponding http-call-response source. It also supports " +
                "calling endpoints protected with basic authentication or OAuth 2.0.",
        parameters = {
                @Parameter(
                        name = "publisher.url",
                        description = "The URL which should be called.\n" +
                                "Examples:\n" +
                                "`http://localhost:8080/endpoint`,\n" +
                                "`https://localhost:8080/endpoint`",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "sink.id",
                        description = "Identifier to correlate the http-call sink to its corresponding " +
                                "http-call-response sources to retrieved the responses.",
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
                        name = "headers",
                        description = "HTTP request headers in format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "When the `Content-Type` header is not provided the system decides the " +
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
                        name = "downloading.enabled",
                        description = "Enable response received by the http-call-response " +
                                "source to be written to a file. When this is enabled the `download.path` property " +
                                "should be also set.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "download.path",
                        description = "The absolute file path along with the file name where the downloads should " +
                                "be saved.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-",
                        dynamic = true),
                @Parameter(
                        name = "blocking.io",
                        description = "Blocks the request thread until a response it received from HTTP " +
                                "call-response source before sending any other request.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
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
                        name = "ssl.configurations",
                        description = "SSL/TSL configurations.\n" +
                                "Expected format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
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
                        description = "Client bootstrap configurations in " +
                                "format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
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
                        description = "Enable hostname verification",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "true"),
        },
        examples = {
                @Example(syntax = "" +
                        "@sink(type='http-call', sink.id='foo',\n" +
                        "      publisher.url='http://localhost:8009/foo',\n" +
                        "      @map(type='xml', @payload('{{payloadBody}}')))\n" +
                        "define stream FooStream (payloadBody string);\n" +
                        "\n" +
                        "@source(type='http-call-response', sink.id='foo',\n" +
                        "        @map(type='text', regex.A='((.|\\n)*)',\n" +
                        "             @attributes(headers='trp:headers', message='A[1]')))\n" +
                        "define stream ResponseStream(message string, headers string);",
                        description = "" +
                                "When events arrive in `FooStream`, http-call sink makes calls to endpoint on " +
                                "url `http://localhost:8009/foo` with " +
                                "`POST` method and Content-Type `application/xml`.\n" +
                                "If the event `payloadBody` attribute contains following XML:\n" +
                                "```<item>\n" +
                                "    <name>apple</name>\n" +
                                "    <price>55</price>\n" +
                                "    <quantity>5</quantity>\n" +
                                "</item>```" +
                                "the http-call sink maps that and sends it to the endpoint.\n" +
                                "When endpoint sends a " +
                                "response it will be consumed by the corresponding http-call-response source " +
                                "correlated via the same `sink.id` `foo` and that will map the response message " +
                                "and send it via `ResponseStream` steam by assigning the message body " +
                                "as `message` attribute and response headers as `headers` attribute " +
                                "of the event."),

                @Example(syntax = "" +
                        "@sink(type='http-call', publisher.url='http://localhost:8005/files/{{name}}'\n" +
                        "      downloading.enabled='true', download.path='{{downloadPath}}{{name}}',\n" +
                        "      method='GET', sink.id='download', @map(type='json'))\n" +
                        "define stream DownloadRequestStream(name String, id int, downloadPath string);\n" +
                        "\n" +
                        "@source(type='http-call-response', sink.id='download',\n" +
                        "        http.status.code='2\\\\d+',\n" +
                        "        @map(type='text', regex.A='((.|\\n)*)',\n" +
                        "             @attributes(name='trp:name', id='trp:id', file='A[1]')))\n" +
                        "define stream ResponseStream2xx(name string, id string, file string);\n" +
                        "\n" +
                        "@source(type='http-call-response', sink.id='download',\n" +
                        "        http.status.code='4\\\\d+',\n" +
                        "        @map(type='text', regex.A='((.|\\n)*)', @attributes(errorMsg='A[1]')))\n" +
                        "define stream ResponseStream4xx(errorMsg string);",
                        description = "" +
                                "When events arrive in `DownloadRequestStream` with `name`:`foo.txt`, `id`:`75` and " +
                                "`downloadPath`:`/user/download/` the http-call sink sends a " +
                                "GET request to the url `http://localhost:8005/files/foo.txt` to download the file " +
                                "to the given path `/user/download/foo.txt` and capture the response via its " +
                                "corresponding http-call-response source based on the response status code.\n" +
                                "If the response status code is in the range of 200 the message will be received by " +
                                "the http-call-response source associated with the `ResponseStream2xx` stream which " +
                                "expects `http.status.code` with regex `2\\\\d+` while downloading the file " +
                                "to the local file system on the path `/user/download/foo.txt` and mapping the " +
                                "response message having the absolute file path to event's `file` attribute.\n" +
                                "If the response status code is in the range of 400 then the message will be " +
                                "received by the http-call-response source associated with the `ResponseStream4xx` " +
                                "stream which expects `http.status.code` with regex `4\\\\d+` while mapping the " +
                                "error response to the `errorMsg` attribute of the event."
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
                )
        }
)
public class HttpCallSink extends HttpSink {

    private static final Logger log = LogManager.getLogger(HttpCallSink.class);
    private String sinkId;
    private boolean isDownloadEnabled;
    private Option downloadPath;
    private boolean isBlockingIO;
    private StreamDefinition outputStreamDefinition;

    @Override
    protected StateFactory init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                                ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        StateFactory stateFactory = super.init(outputStreamDefinition, optionHolder, configReader, siddhiAppContext);
        this.outputStreamDefinition = outputStreamDefinition;
        this.sinkId = optionHolder.validateAndGetStaticValue(HttpConstants.SINK_ID);
        this.isDownloadEnabled = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(HttpConstants
                .DOWNLOAD_ENABLED, HttpConstants.DEFAULT_DOWNLOAD_ENABLED_VALUE));
        if (isDownloadEnabled) {
            this.downloadPath = optionHolder.validateAndGetOption(HttpConstants.DOWNLOAD_PATH);
        }
        isBlockingIO = Boolean.parseBoolean(
                optionHolder.validateAndGetStaticValue(HttpConstants.BLOCKING_IO, HttpConstants.FALSE));
        initMetrics(outputStreamDefinition.getId());
        return stateFactory;
    }

    @Override
    protected int sendRequest(Object payload, DynamicOptions dynamicOptions, List<Header> headersList,
                              ClientConnector clientConnector)
            throws ConnectionUnavailableException {
        if (!publisherURLOption.isStatic()) {
            super.createClientConnector(dynamicOptions);
        }

        String publisherURL;
        if (publisherURLOption.isStatic()) {
            publisherURL = publisherURLOption.getValue();
        } else {
            publisherURL = publisherURLOption.getValue(dynamicOptions);
        }

        if (mapType == null) {
            mapType = getMapper().getType();
        }

        String httpMethod = EMPTY_STRING.equals(httpMethodOption.getValue(dynamicOptions)) ?
                HttpConstants.METHOD_DEFAULT : httpMethodOption.getValue(dynamicOptions);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        String messageBody = getMessageBody(payload);

        if (metrics != null) {
            metrics.getTotalWritesMetric().inc();
            metrics.getTotalHttpWritesMetric(publisherURL).inc();
            metrics.getRequestSizeMetric(publisherURL).inc(HttpSinkUtil.getByteSize(messageBody));
        }

        HttpMethod httpReqMethod = new HttpMethod(httpMethod);
        HttpCarbonMessage cMessage = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpReqMethod, EMPTY_STRING));
        cMessage = generateCarbonMessage(headersList, contentType, httpMethod, cMessage,
                clientConnector.getHttpURLProperties());
        if (!Constants.HTTP_GET_METHOD.equals(httpMethod)) {
            cMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(messageBody
                    .getBytes(Charset.defaultCharset()))));
        }
        cMessage.completeMessage();
        HttpResponseFuture httpResponseFuture = clientConnector.send(cMessage);
        CountDownLatch latch = null;
        if (isBlockingIO || HttpConstants.OAUTH.equals(authType)) {
            latch = new CountDownLatch(1);
        }
        HttpResponseMessageListener httpListener = new HttpResponseMessageListener(this,
                getTrpProperties(dynamicOptions), sinkId, isDownloadEnabled, latch,
                payload, dynamicOptions, siddhiAppContext.getName(),
                clientConnector.getPublisherURL(), metrics, startTime);
        httpResponseFuture.setHttpConnectorListener(httpListener);

        if (latch != null) {
            try {
                boolean latchCount = latch.await(30, TimeUnit.SECONDS);
                if (!latchCount) {
                    log.debug("Timeout due to getting response from {}. Message dropped.",
                            clientConnector.getPublisherURL());
                    throw new ConnectionUnavailableException("Time out due to getting response from " +
                            clientConnector.getPublisherURL() + ". Message dropped.");

                }
            } catch (InterruptedException e) {
                log.debug("Failed to get a response from {},{}. Message dropped.", clientConnector.getPublisherURL(),
                        e);
                throw new ConnectionUnavailableException("Failed to get a response from " +
                        clientConnector.getPublisherURL() + ", " + e + ". Message dropped.");
            }
            if (isBlockingIO) {
                return HttpConstants.SUCCESS_CODE;
            }
            return httpListener.getHttpResponseStatusCode();
        } else {
            return HttpConstants.SUCCESS_CODE;
        }
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{HttpConstants.HEADERS, HttpConstants.METHOD, HttpConstants.PUBLISHER_URL,
                HttpConstants.DOWNLOAD_PATH, HttpConstants.PUBLISHER_URL, HttpConstants.RECEIVER_REFRESH_TOKEN};
    }

    private Map<String, Object> getTrpProperties(DynamicOptions dynamicOptions) {
        Event event = dynamicOptions.getEvent();
        Object[] data = event.getData();
        List<Attribute> attributes = outputStreamDefinition.getAttributeList();
        Map<String, Object> trpProperties = new HashMap<>();
        for (int i = 0; i < attributes.size(); i++) {
            trpProperties.put(attributes.get(i).getName(), data[i]);
        }
        if (isDownloadEnabled) {
            trpProperties.put(HttpConstants.DOWNLOAD_PATH, downloadPath.getValue(dynamicOptions));
        }
        return trpProperties;
    }

}
