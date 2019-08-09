/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;

/**
 * Http source for receive the http and https request.
 */
@Extension(name = "http-request", namespace = "source",
        deprecated = true,
        description = "" +
                "_(Use http-service source instead)._\n" +
                "The http-request source receives POST requests via HTTP and HTTPS protocols " +
                "in format such as `text`, `XML` and `JSON` and sends responses via its corresponding " +
                "http-response sink correlated through a unique `source.id`.\n" +
                "For request and response correlation, it generates a `messageId` upon each incoming request " +
                "and expose it via transport properties in the format `trp:messageId` to correlate them with " +
                "the responses at the http-response sink.\n" +
                "The request headers and properties can be accessed via transport properties in the format " +
                "`trp:<header>`.\n" +
                "It also supports basic authentication to ensure events are received from authorized users/systems.",
        parameters = {
                @Parameter(name = "receiver.url",
                        description = "The URL on which events should be received. " +
                                "To enable SSL use `https` protocol in the url.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "`http://0.0.0.0:9763/<appNAme>/<streamName>`"),
                @Parameter(name = "source.id",
                        description = "Identifier to correlate the http-request source to its corresponding " +
                                "http-response sinks to send responses.",
                        type = {DataType.STRING}),
                @Parameter(name = "connection.timeout",
                        description = "Connection timeout in millis. The system will send a timeout, " +
                                "if a corresponding response is not sent by an associated " +
                                "http-response sink within the given time.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(name = "basic.auth.enabled",
                        description = "This only works in VM, Docker and Kubernetes.\nWhere when enabled " +
                                "it authenticates each request using the " +
                                "`Authorization:'Basic encodeBase64(username:Password)'` header.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "worker.count",
                        description = "The number of active worker threads to serve the " +
                                "incoming events. By default the value is set to `1` to ensure events are processed " +
                                "in the same order they arrived. By increasing this value, " +
                                "higher performance can be achieved in the expense of loosing event ordering.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "1"),
                @Parameter(
                        name = "socket.idle.timeout",
                        description = "Idle timeout for HTTP connection in millis.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(
                        name = "ssl.verify.client",
                        description = "The type of client certificate verification. " +
                                "Supported values are `require`, `optional`.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "ssl.protocol",
                        description = "SSL/TLS protocol.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "TLS"),
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
                        name = "request.size.validation.configurations",
                        description = "Configurations to validate the HTTP request size.\n" +
                                "Expected format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Enable request size validation: `'request.size.validation:true'`\n" +
                                " If request size is validated\n" +
                                " - Maximum request size: `'request.size.validation.maximum.value:2048'`\n" +
                                " - Response status code when request size validation fails: " +
                                "`'request.size.validation.reject.status.code:401'`\n" +
                                " - Response message when request size validation fails: " +
                                "`'request.size.validation.reject.message:Message is bigger than the valid size'`\n" +
                                " - Response Content-Type when request size validation fails: " +
                                "`'request.size.validation.reject.message.content.type:plain/text'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "header.validation.configurations",
                        description = "Configurations to validate HTTP headers.\n" +
                                "Expected format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Enable header size validation: `'header.size.validation:true'`\n" +
                                " If header size is validated\n" +
                                " - Maximum length of initial line: `'header.validation.maximum.request.line:4096'`\n" +
                                " - Maximum length of all headers: `'header.validation.maximum.size:8192'`\n" +
                                " - Maximum length of the content or each chunk: " +
                                "`'header.validation.maximum.chunk.size:8192'`\n" +
                                " - Response status code when header validation fails: " +
                                "`'header.validation.reject.status.code:401'`\n" +
                                " - Response message when header validation fails: " +
                                "`'header.validation.reject.message:Message header is bigger than the valid size'`\n" +
                                " - Response Content-Type when header validation fails: " +
                                "`'header.validation.reject.message.content.type:plain/text'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "server.bootstrap.configurations",
                        description = "Server bootstrap configurations in " +
                                "format `\"'<key>:<value>','<key>:<value>'\"`.\n" +
                                "Some supported configurations :\n" +
                                " - Server connect timeout in millis:" +
                                " `'server.bootstrap.connect.timeout:15000'`\n" +
                                " - Server socket timeout in seconds:" +
                                " `'server.bootstrap.socket.timeout:15'`\n" +
                                " - Enable TCP no delay: `'server.bootstrap.nodelay:true'`\n" +
                                " - Enable server keep alive: `'server.bootstrap.keepalive:true'`\n" +
                                " - Send buffer size: `'server.bootstrap.sendbuffersize:1048576'`\n" +
                                " - Receive buffer size: `'server.bootstrap.recievebuffersize:1048576'`\n" +
                                " - Number of connections queued: `'server.bootstrap.socket.backlog:100'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "trace.log.enabled",
                        description = "Enable trace log for traffic monitoring.",
                        defaultValue = "false",
                        optional = true,
                        type = {DataType.BOOL}
                )
        },
        examples = {
                @Example(syntax = "" +
                        "@source(type='http-request', receiver.url='http://localhost:5005/add',\n" +
                        "        source.id='adder',\n" +
                        "        @map(type='json, @attributes(messageId='trp:messageId',\n" +
                        "                                     value1='$.event.value1',\n" +
                        "                                     value2='$.event.value2')))\n" +
                        "define stream AddStream (messageId string, value1 long, value2 long);\n" +
                        "\n" +
                        "@sink(type='http-response', source.id='adder',\n" +
                        "      message.id='{{messageId}}', @map(type = 'json'))\n" +
                        "define stream ResultStream (messageId string, results long);\n" +
                        "\n" +
                        "@info(name = 'query1')\n" +
                        "from AddStream \n" +
                        "select messageId, value1 + value2 as results \n" +
                        "insert into ResultStream;",
                        description = "Above sample listens events on `http://localhost:5005/stocks` url for " +
                                "JSON messages on the format:\n" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"value1\": 3,\n" +
                                "    \"value2\": 4\n" +
                                "  }\n" +
                                "}```\n" +
                                "Map the vents into AddStream, process the events through query `query1`, and " +
                                "sends the results produced on ResultStream via http-response sink " +
                                "on the message format:" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"results\": 7\n" +
                                "  }\n" +
                                "}```"),
        },
        systemParameter = {
                @SystemParameter(
                        name = "serverBootstrapBossGroupSize",
                        description = "Number of boss threads to accept incoming connections.",
                        defaultValue = "Number of available processors",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapWorkerGroupSize",
                        description = "Number of worker threads to accept the connections from boss threads and " +
                                "perform non-blocking read and write from one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "serverBootstrapClientGroupSize",
                        description = "Number of client threads to perform non-blocking read and write to " +
                                "one or more channels.",
                        defaultValue = "(Number of available processors) * 2",
                        possibleParameters = "Any positive integer"
                ),
                @SystemParameter(
                        name = "defaultHost",
                        description = "The default host of the transport.",
                        defaultValue = "0.0.0.0",
                        possibleParameters = "Any valid host"
                ),
                @SystemParameter(
                        name = "defaultScheme",
                        description = "The default protocol.",
                        defaultValue = "http",
                        possibleParameters = {"http", "https"}
                ),
                @SystemParameter(
                        name = "defaultHttpPort",
                        description = "The default HTTP port when default scheme is `http`.",
                        defaultValue = "8280",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "defaultHttpsPort",
                        description = "The default HTTPS port when default scheme is `https`.",
                        defaultValue = "8243",
                        possibleParameters = "Any valid port"
                ),
                @SystemParameter(
                        name = "keyStoreLocation",
                        description = "The default keystore file path.",
                        defaultValue = "`${carbon.home}/resources/security/wso2carbon.jks`",
                        possibleParameters = "Path to `.jks` file"
                ),
                @SystemParameter(
                        name = "keyStorePassword",
                        description = "The default keystore password.",
                        defaultValue = "wso2carbon",
                        possibleParameters = "Keystore password as string"
                )
        }
)
@Deprecated
public class HttpRequestSource extends HttpServiceSource {

}
