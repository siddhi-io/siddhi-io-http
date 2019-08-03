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
package io.siddhi.extension.io.http.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.log4j.Logger;

import static io.siddhi.extension.io.http.util.HttpConstants.DEFAULT_WORKER_COUNT;

/**
 * Http source for receive responses for the requests sent by http-call sinks
 */
@Extension(
        name = "http-call-response",
        namespace = "source",
        description = "" +
                "The http-call-response source receives the responses for the calls made by its corresponding " +
                "http-call sink, and maps them from formats such as `text`, `XML` and `JSON`.\n" +
                "To handle messages with different http status codes having different formats, multiple " +
                "http-call-response sources are allowed to associate with a single http-call sink. " +
                "It also allows accessing the attributes of the event that " +
                "initiated the call via transport properties and map them with the " +
                "format `trp:<attribute name>`'.",
        parameters = {
                @Parameter(
                        name = "sink.id",
                        description = "Identifier to correlate the http-call-response source with its corresponding " +
                                "http-call sink that published the messages.",
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
                                "@sink(type='http-call', method='POST',\n" +
                                "      publisher.url='http://localhost:8005/registry/employee',\n" +
                                "      sink.id='employee-info', @map(type='json')) \n" +
                                "define stream EmployeeRequestStream (name string, id int);\n" +
                                "\n" +
                                "@source(type='http-call-response', sink.id='employee-info',\n" +
                                "        http.status.code='2\\\\d+',\n" +
                                "        @map(type='json',\n" +
                                "             @attributes(name='trp:name', id='trp:id',\n" +
                                "                         location='$.town', age='$.age')))\n" +
                                "define stream EmployeeResponseStream(name string, id int,\n" +
                                "                                     location string, age int);\n" +
                                "\n" +
                                "@source(type='http-call-response', sink.id='employee-info',\n" +
                                "        http.status.code='4\\\\d+',\n" +
                                "        @map(type='text', regex.A='((.|\\n)*)',\n" +
                                "             @attributes(error='A[1]')))\n" +
                                "define stream EmployeeErrorStream(error string);",

                        description = "" +
                                "When events arrive in `EmployeeRequestStream`, http-call sink makes calls to " +
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
                                "the message will be received by the http-call-response source associated with the " +
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
                                "received by the http-call-response source associated with the `EmployeeErrorStream` " +
                                "stream, because it is correlated with the sink by the same `sink.id` " +
                                "`employee-info` and it expects messages with `http.status.code` in regex " +
                                "format `4\\\\d+`, and maps the error response to the `error` attribute of the event."
                )}
)
public class HttpCallResponseSource extends Source {

    private static final Logger log = Logger.getLogger(HttpCallResponseSource.class);
    private String sinkId;
    private SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;
    private String siddhiAppName;
    private String workerThread;
    private HttpCallResponseConnectorListener httpCallResponseSourceListener;
    private HttpCallResponseSourceConnectorRegistry httpConnectorRegistry;
    private String httpStatusCode;
    private boolean shouldAllowStreamingResponses;


    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {

        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.sinkId = optionHolder.validateAndGetStaticValue(HttpConstants.SINK_ID);
        this.httpConnectorRegistry = HttpCallResponseSourceConnectorRegistry.getInstance();
        this.siddhiAppName = siddhiAppContext.getName();
        this.workerThread = optionHolder
                .validateAndGetStaticValue(HttpConstants.WORKER_COUNT, DEFAULT_WORKER_COUNT);
        this.httpStatusCode = optionHolder.validateAndGetStaticValue(HttpConstants.HTTP_STATUS_CODE,
                HttpConstants.DEFAULT_HTTP_SUCCESS_CODE);
        this.shouldAllowStreamingResponses = Boolean.parseBoolean(
                optionHolder.validateAndGetStaticValue(HttpConstants.ALLOW_STREAMING_RESPONSES, HttpConstants.FALSE));
        return null;
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    /**
     * Called to connect to the source backend for receiving events
     *
     * @param connectionCallback Callback to pass the ConnectionUnavailableException for connection failure after
     *                           initial successful connection
     * @param state              current state of the source
     * @throws ConnectionUnavailableException if it cannot connect to the source backend
     */
    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        this.httpCallResponseSourceListener =
                new HttpCallResponseConnectorListener(Integer.parseInt(workerThread), sourceEventListener,
                        shouldAllowStreamingResponses, sinkId, requestedTransportPropertyNames, siddhiAppName);
        this.httpConnectorRegistry.registerSourceListener(httpCallResponseSourceListener, sinkId, httpStatusCode);

        HTTPSourceRegistry.registerCallResponseSource(sinkId, httpStatusCode, this);
    }

    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(sinkId, httpStatusCode, siddhiAppName);
        HTTPSourceRegistry.removeCallResponseSource(sinkId, httpStatusCode);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public HttpCallResponseConnectorListener getConnectorListener() {
        return httpCallResponseSourceListener;
    }
}
