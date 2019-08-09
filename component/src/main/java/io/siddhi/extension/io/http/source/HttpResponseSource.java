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
import io.siddhi.annotation.util.DataType;

/**
 * Http source for receive responses for the requests sent by http-request sinks
 */
@Extension(
        name = "http-response",
        namespace = "source",
        deprecated = true,
        description = "" +
                "_(Use http-call-response source instead)._\n" +
                "The http-response source receives the responses for the calls made by its corresponding " +
                "http-request sink, and maps them from formats such as `text`, `XML` and `JSON`.\n" +
                "To handle messages with different http status codes having different formats, multiple " +
                "http-response sources are allowed to associate with a single http-request sink. " +
                "It allows accessing the attributes of the event that initiated the call, " +
                "and the response headers and properties via transport properties " +
                "in the format `trp:<attribute name>` and `trp:<header/property>` respectively.",
        parameters = {
                @Parameter(
                        name = "sink.id",
                        description = "Identifier to correlate the http-response source with its corresponding " +
                                "http-request sink that published the messages.",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "http.status.code",
                        description = "The matching http responses status code regex, that is used to filter the " +
                                "the messages which will be processed by the source." +
                                "Eg: `http.status.code = '200'`,\n" +
                                "`http.status.code = '4\\\\d+'`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "200"),
                @Parameter(
                        name = "allow.streaming.responses",
                        description = "Enable consuming responses on a streaming manner.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"
                )},
        examples = {
                @Example(
                        syntax = "" +
                                "@sink(type='http-request', method='POST',\n" +
                                "      publisher.url='http://localhost:8005/registry/employee',\n" +
                                "      sink.id='employee-info', @map(type='json')) \n" +
                                "define stream EmployeeRequestStream (name string, id int);\n" +
                                "\n" +
                                "@source(type='http-response', sink.id='employee-info',\n" +
                                "        http.status.code='2\\\\d+',\n" +
                                "        @map(type='json',\n" +
                                "             @attributes(name='trp:name', id='trp:id',\n" +
                                "                         location='$.town', age='$.age')))\n" +
                                "define stream EmployeeResponseStream(name string, id int,\n" +
                                "                                     location string, age int);\n" +
                                "\n" +
                                "@source(type='http-response', sink.id='employee-info',\n" +
                                "        http.status.code='4\\\\d+',\n" +
                                "        @map(type='text', regex.A='((.|\\n)*)',\n" +
                                "             @attributes(error='A[1]')))\n" +
                                "define stream EmployeeErrorStream(error string);",

                        description = "" +
                                "When events arrive in `EmployeeRequestStream`, http-request sink makes calls to " +
                                "endpoint on url `http://localhost:8005/registry/employee` with " +
                                "`POST` method and Content-Type `application/json`.\n" +
                                "If the arriving event has attributes `name`:`John` and `id`:`1423` it will send a " +
                                "message with default JSON mapping as follows:\n" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"name\": \"John\",\n" +
                                "    \"id\": 1423\n" +
                                "  }\n" +
                                "}```" +
                                "When the endpoint responds with status code in the range of 200 " +
                                "the message will be received by the http-response source associated with the " +
                                "`EmployeeResponseStream` stream, because it is correlated with the sink by the " +
                                "same `sink.id` `employee-info` and as that expects messages with " +
                                "`http.status.code` in regex format `2\\\\d+`. " +
                                "If the response message is in the format\n" +
                                "```{\n" +
                                "  \"town\": \"NY\",\n" +
                                "  \"age\": 24\n" +
                                "}```" +
                                "the source maps the `location` and `age` attributes by executing JSON path on the " +
                                "message and maps the `name` and `id` attributes by extracting them from the " +
                                "request event via as transport properties.\n" +
                                "If the response status code is in the range of 400 then the message will be " +
                                "received by the http-response source associated with the `EmployeeErrorStream` " +
                                "stream, because it is correlated with the sink by the same `sink.id` " +
                                "`employee-info` and it expects messages with `http.status.code` in regex " +
                                "format `4\\\\d+`, and maps the error response to the `error` attribute of the event."
                )}
)
@Deprecated
public class HttpResponseSource extends HttpCallResponseSource {

}
