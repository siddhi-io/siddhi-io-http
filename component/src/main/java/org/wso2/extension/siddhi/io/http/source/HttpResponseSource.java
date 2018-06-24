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
 * Http source for receive the http and https request.
 */
@Extension(
        name = "http-response",
        namespace = "source",
        description = "The HTTP request is correlated with the " +
        "HTTP response sink, through a unique `source.id`, and for each POST requests it receives via " +
        "HTTP or HTTPS in format such as `text`, `XML` and `JSON` it sends the response via the HTTP response sink. " +
        "The individual request and response messages are correlated at the sink using the `message.id` of " +
        "the events. " +
        "If required, you can enable basic authentication at the source " +
        "to ensure that events are received only from users who are authorized to access the service.",
        parameters = {
                @Parameter(name = "receiver.url",
                        description = "The URL to which the events should be received. " +
                                "User can provide any valid url and if the url is not provided the system will use" +
                                " the " +
                                "following format `http://0.0.0.0:9763/<appNAme>/<streamName>`" +
                                "If the user want to use SSL the url should be given in following format " +
                                "`https://localhost:8080/<streamName>`",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "http://0.0.0.0:9763/<appNAme>/<streamName>"),
                @Parameter(name = "sink.id",
                        description = "Identifier need to map the source to sink.",
                        type = {DataType.STRING})},
        examples = {
                @Example(syntax = "" +
                        "@sink(type='http-request',publisher.url='http://localhost:8080/hello/message', " +
                        "method='POST', connection.timeout='1000', " +
                        "headers='{{headers}}',sink.id='employee-info'," +
                        "@map(type='json', @payload('{{message}}'))) " +
                        "define stream requestStream (body String,headers String);" +
                        "" +
                        "@source(type='http-response', sink.id='employee-info', " +
                        "@map(type='json'))" +
                        "define stream responseStream(name String, id int);",

                        description = "" +
                                "This source will receive the response of the request which is sent by the sink " +
                                " which has 'employee-info' as the 'sink.id'. The response body is expected to be in " +
                                "json format. After response is processed, relavent event will be sent to " +
                                "responseStream.\n" +
                                "A sample response is given below.\n" +
                                "   {\"event\":\n" +
                                "       \"symbol\":WSO2,\n" +
                                "       \"price\":55.6,\n"  +
                                "       \"volume\":100,\n" +
                                "   }\n")}
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
                HttpConstants.HTTP_CODE_2XX);
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        this.httpResponseSourceListener =
                new HttpResponseConnectorListener(Integer.parseInt(workerThread), sourceEventListener, sinkId,
                        requestedTransportPropertyNames, siddhiAppName);
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
