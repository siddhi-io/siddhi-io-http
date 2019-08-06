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
package io.siddhi.extension.io.http.sink;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;

/**
 * {@code HttpResponseSink} Handle the HTTP publishing tasks.
 */
@Extension(name = "http-response", namespace = "sink",
        deprecated = true,
        description = "" +
                "_(Use http-service-response sink instead)._\n" +
                "The http-response sink send responses of the requests consumed by its corresponding " +
                "http-request source, by mapping the response messages to formats such as `text`, `XML` and `JSON`.",
        parameters = {
                @Parameter(
                        name = "source.id",
                        description = "Identifier to correlate the http-response sink to its corresponding " +
                                "http-request source which consumed the request.",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "message.id",
                        description = "Identifier to correlate the response with the request received " +
                                "by http-request source.",
                        dynamic = true,
                        type = {DataType.STRING}),
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
                        description = "The http-request source on stream `AddStream` listens on " +
                                "url `http://localhost:5005/stocks` for JSON messages with format:\n" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"value1\": 3,\n" +
                                "    \"value2\": 4\n" +
                                "  }\n" +
                                "}```\n" +
                                "and when events arrive it maps to `AddStream` events and pass " +
                                "them to query `query1` for processing. The query results produced on `ResultStream` " +
                                "are sent as a response via http-response sink with format:" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"results\": 7\n" +
                                "  }\n" +
                                "}```" +
                                "Here the request and response are correlated by passing the `messageId` " +
                                "produced by the http-request to the respective http-response sink."
                ),

        }
)
@Deprecated
public class HttpResponseSink extends HttpServiceResponseSink {

}
