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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;

import java.util.List;

/**
 * {@code HttpServiceResponseSink} Handle the HTTP Service Response publishing tasks.
 */
@Extension(
        name = "http-service-response",
        namespace = "sink",
        description = "" +
                "The http-service-response sink send responses of the requests consumed by its corresponding " +
                "http-service source, by mapping the response messages to formats such as `text`, `XML` and `JSON`.",
        parameters = {
                @Parameter(
                        name = "source.id",
                        description = "Identifier to correlate the http-service-response sink to its corresponding " +
                                "http-service source which consumed the request.",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "message.id",
                        description = "Identifier to correlate the response with the request received " +
                                "by http-service source.",
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
                        "@source(type='http-service', receiver.url='http://localhost:5005/add',\n" +
                        "        source.id='adder',\n" +
                        "        @map(type='json, @attributes(messageId='trp:messageId',\n" +
                        "                                     value1='$.event.value1',\n" +
                        "                                     value2='$.event.value2')))\n" +
                        "define stream AddStream (messageId string, value1 long, value2 long);\n" +
                        "\n" +
                        "@sink(type='http-service-response', source.id='adder',\n" +
                        "      message.id='{{messageId}}', @map(type = 'json'))\n" +
                        "define stream ResultStream (messageId string, results long);\n" +
                        "\n" +
                        "@info(name = 'query1')\n" +
                        "from AddStream \n" +
                        "select messageId, value1 + value2 as results \n" +
                        "insert into ResultStream;",
                        description = "The http-service source on stream `AddStream` listens on " +
                                "url `http://localhost:5005/stocks` for JSON messages with format:\n" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"value1\": 3,\n" +
                                "    \"value2\": 4\n" +
                                "  }\n" +
                                "}```\n" +
                                "and when events arrive it maps to `AddStream` events and pass " +
                                "them to query `query1` for processing. The query results produced on `ResultStream` " +
                                "are sent as a response via http-service-response sink with format:" +
                                "```{\n" +
                                "  \"event\": {\n" +
                                "    \"results\": 7\n" +
                                "  }\n" +
                                "}```" +
                                "Here the request and response are correlated by passing the `messageId` " +
                                "produced by the http-service to the respective http-service-response sink."
                ),

        }
)
public class HttpServiceResponseSink extends Sink {

    private static final Logger log = Logger.getLogger(HttpServiceResponseSink.class);
    private Option messageIdOption;
    private String sourceId;
    private Option httpHeaderOption;
    private String mapType;

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
        return new Class[]{String.class};
    }

    /**
     * Give information to the deployment about the service exposed by the sink.
     *
     * @return ServiceDeploymentInfo  Service related information to the deployment
     */
    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * Returns a list of supported dynamic options (that means for each event value of the option can change) by
     * the transport
     *
     * @return the list of supported dynamic option keys
     */
    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{
                HttpConstants.HEADERS,
                HttpConstants.MESSAGE_ID
        };
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
        this.messageIdOption = optionHolder.validateAndGetOption(HttpConstants.MESSAGE_ID);
        this.sourceId = optionHolder.validateAndGetStaticValue(HttpConstants.SOURCE_ID);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.mapType = outputStreamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                .getValue();
        return null;
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        String headers = httpHeaderOption.getValue(dynamicOptions);
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);
        String messageId = messageIdOption.getValue(dynamicOptions);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        HTTPSourceRegistry.getInstance()
                .getServiceSource(sourceId).handleCallback(messageId, (String) payload, headersList, contentType);
    }

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     *
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    /**
     * Called after all publishing is done, or when {@link ConnectionUnavailableException} is thrown
     * Implementation of this method should contain the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {

    }

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that has to be done when removing the receiver has to be done here.
     */
    @Override
    public void destroy() {

    }
}
