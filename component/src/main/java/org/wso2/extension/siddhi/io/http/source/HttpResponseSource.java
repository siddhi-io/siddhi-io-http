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
package org.wso2.extension.siddhi.io.http.source;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.http.util.HTTPSourceRegistry;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.Map;

import static org.wso2.extension.siddhi.io.http.util.HttpConstants.DEFAULT_WORKER_COUNT;

/**
 * Http source for receive responses for the requests sent by http-request sinks
 */
@Extension(
        name = "http-response",
        namespace = "source",
        description = "" +
                "The http-response source co-relates with http-request sink  with the parameter 'sink.id'.\n" +
                "This receives responses for the requests sent by the http-request sink which has the same " +
                "sink id.\n" +
                "Response messages can be in formats such as TEXT, JSON and XML.\n" +
                "In order to handle the responses with different http status codes, user is allowed to defined the " +
                "acceptable response source code using the parameter 'http.status.code'\n",
        parameters = {
                @Parameter(name = "sink.id",
                        description = "This parameter is used to map the http-response source to a " +
                                "http-request sink. Then this source will accepts the response messages for " +
                                "the requests sent by corresponding http-request sink.",
                        type = {DataType.STRING}),
                @Parameter(name = "http.status.code",
                        description = "Acceptable http status code for the responses.\n" +
                                "This can be a complete string or a regex.\n" +
                                "Only the responses with matching status codes to the defined value, will be received" +
                                " by the http-response source.\n" +
                                "Eg: 'http.status.code = '200', http.status.code = '2\\\\d+''",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "200"),
                @Parameter(name = "allow.streaming.responses",
                        description = "If responses can be received multiple times for a single request, " +
                                "this option should be enabled. If this is not enabled, for every request, response " +
                                "will be extracted only once.",
                        type = {DataType.BOOL}

                )},
        examples = {
                @Example(syntax = "" +
                        "@sink(type='http-request', \n" +
                        "downloading.enabled='true',\n" +
                        "publisher.url='http://localhost:8005/registry/employee',\n" +
                        "method='POST', " +
                        "headers='{{headers}}',sink.id='employee-info',\n" +
                        "@map(type='json')) \n" +
                        "define stream BarStream (name String, id int, headers String, downloadPath string);\n\n" +
                        "" +
                        "@source(type='http-response' , sink.id='employee-info', http.status.code='2\\\\d+',\n" +
                        "@map(type='text', regex.A='((.|\\n)*)', @attributes(message='A[1]'))) \n" +
                        "define stream responseStream2xx(message string);" +
                        "" +
                        "@source(type='http-response' , sink.id='employee-info', http.status.code='4\\\\d+' ,\n" +
                        "@map(type='text', regex.A='((.|\\n)*)', @attributes(message='A[1]')))  \n" +
                        "define stream responseStream4xx(message string);",

                        description = "" +
                                "In above example, the defined http-request sink will send a POST requests to the " +
                                "endpoint defined by 'publisher.url'.\n" +
                                "Then for those requests, the source with the response code '2\\\\d+' and sink.id " +
                                "'employee-info' will receive the responses with 2xx status codes. \n" +
                                "The http-response source which has 'employee-info' as the 'sink.id' and '4\\\\d+' " +
                                "as the http.response.code will receive all the responses with 4xx status codes.\n. " +
                                "Then the body of the response message will be extracted using text mapper and " +
                                "converted into siddhi events.\n.")}
        )

public class HttpResponseSource extends Source {

    private static final Logger log = Logger.getLogger(HttpResponseSource.class);
    private String sinkId;
    private SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;
    private String siddhiAppName;
    private String workerThread;
    private HttpResponseConnectorListener httpResponseSourceListener;
    private HttpResponseSourceConnectorRegistry httpConnectorRegistry;
    private String httpStatusCode;
    private boolean shouldAllowStreamingResponses;


    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {

        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.sinkId = optionHolder.validateAndGetStaticValue(HttpConstants.SINK_ID);
        this.httpConnectorRegistry = HttpResponseSourceConnectorRegistry.getInstance();
        this.siddhiAppName = siddhiAppContext.getName();
        this.workerThread = optionHolder
                .validateAndGetStaticValue(HttpConstants.WORKER_COUNT, DEFAULT_WORKER_COUNT);
        this.httpStatusCode = optionHolder.validateAndGetStaticValue(HttpConstants.HTTP_STATUS_CODE,
                HttpConstants.DEFAULT_HTTP_SUCCESS_CODE);
        this.shouldAllowStreamingResponses = Boolean.parseBoolean(
                optionHolder.validateAndGetStaticValue(HttpConstants.ALLOW_STREAMING_RESPONSES, HttpConstants.FALSE));
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        this.httpResponseSourceListener =
                new HttpResponseConnectorListener(Integer.parseInt(workerThread), sourceEventListener,
                        shouldAllowStreamingResponses, sinkId, requestedTransportPropertyNames, siddhiAppName);
        this.httpConnectorRegistry.registerSourceListener(httpResponseSourceListener, sinkId, httpStatusCode);

        HTTPSourceRegistry.registerResponseSource(sinkId, httpStatusCode, this);
    }

    @Override
    public void disconnect() {
        this.httpConnectorRegistry.unregisterSourceListener(sinkId, httpStatusCode, siddhiAppName);
        HTTPSourceRegistry.removeResponseSource(sinkId, httpStatusCode);
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

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }

    public HttpResponseConnectorListener getConnectorListener() {
        return httpResponseSourceListener;
    }
}
