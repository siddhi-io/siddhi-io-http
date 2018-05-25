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
package org.wso2.extension.siddhi.io.http.sink;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.http.sink.util.HttpSinkUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.extension.siddhi.io.http.util.SyncResultHandler;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code HttpSyncSink} Handle the HTTP publishing tasks.
 */
@Extension(name = "http-sync", namespace = "sink",
        description = "This extension send the response to the http-sync source having the same source.id. user can " +
                "add any number of response headers for each event dynamically.",
        parameters = {
                @Parameter(
                        name = "source.id",
                        description = "Identifier of the source.",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "message.id",
                        description = "Identifier of the message.",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "headers",
                        description = "The headers that should be included as HTTP response headers. There can be any" +
                                " number of headers concatenated on following format. \"'header1:value1'," +
                                "'header2:value2'\" User can include content-type header if he/she need to have any " +
                                "specific type for payload. If not system get the mapping type as the content-Type " +
                                "header (ie. @map(xml):application/xml, @map(json):application/json, @map(text)" +
                                ":plain/text) and if user does not include any mapping type then system gets the " +
                                "'plain/text' as default Content-Type header. If user does not include Content-Length" +
                                " header then system calculate the bytes size of payload and include it  as " +
                                "content-length header.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = " "),
        },
        examples = {
                @Example(syntax =
                        "@sink(type='http-sync',source.id='sampleSourceId',message.id='{{messageId}}',"
                                + "headers=\"'content-type:json','content-length:94'\""
                                + "@map(type='json' , @payload('{{payloadBody}}')))"
                                + "define stream FooStream (payloadBody String, messageId string, headers string);\n",
                        description =
                                "If it is json mapping expected input should be in following format for FooStream:"
                                        + "{"
                                        + "{\"events\":\n"
                                        + "    {\"event\":\n"
                                        + "        \"symbol\":WSO2,\n"
                                        + "        \"price\":55.6,\n"
                                        + "        \"volume\":100,\n"
                                        + "    }\n"
                                        + "},"
                                        + "0cf708b1-7eae-440b-a93e-e72f801b486a,"
                                        + "Content-Length:24#Content-Location:USA"
                                        + "}"

                                        + "Above event will generate response for the matching source message as below."
                                        + "~Output http event payload"
                                        + "{\"events\":\n"
                                        + "    {\"event\":\n"
                                        + "        \"symbol\":WSO2,\n"
                                        + "        \"price\":55.6,\n"
                                        + "        \"volume\":100,\n"
                                        + "    }\n"
                                        + "}\n"
                                        + "~Output http event headers"
                                        + "Content-Length:24,"
                                        + "Content-Location:'USA',"
                                        + "Content-Type:'application/json',"
                )}
)
public class HttpSyncSink extends Sink {

    private static final Logger log = Logger.getLogger(HttpSyncSink.class);
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
     * @param siddhiAppContext       the context of the {@link org.wso2.siddhi.query.api.SiddhiApp} used to
     *                               get siddhi related utilty functions.
     */
    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        //read configurations
        this.messageIdOption = optionHolder.getOrCreateOption(HttpConstants.MESSAGE_ID, UUID.randomUUID().toString());
        this.sourceId = optionHolder.validateAndGetStaticValue(HttpConstants.SOURCE_ID);
        this.httpHeaderOption = optionHolder.getOrCreateOption(HttpConstants.HEADERS, HttpConstants.DEFAULT_HEADER);
        this.mapType = outputStreamDefinition.getAnnotations().get(0).getAnnotations().get(0).getElements().get(0)
                .getValue();
    }


    /**
     * This method will be called when events need to be published via this sink
     *
     * @param payload        payload of the event based on the supported event class exported by the extensions
     * @param dynamicOptions holds the dynamic options of this sink and Use this object to obtain dynamic options.
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {

        String headers = httpHeaderOption.getValue(dynamicOptions);
        List<Header> headersList = HttpSinkUtil.getHeaders(headers);
        String messageId = messageIdOption.getValue(dynamicOptions);
        String contentType = HttpSinkUtil.getContentType(mapType, headersList);
        SyncResultHandler.handleCallback(sourceId, messageId, (String) payload, headersList, contentType);
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
}
