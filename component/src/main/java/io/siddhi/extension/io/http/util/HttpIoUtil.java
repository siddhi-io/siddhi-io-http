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
package io.siddhi.extension.io.http.util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.event.state.MetaStateEvent;
import io.siddhi.core.event.stream.MetaStreamEvent;
import io.siddhi.core.table.Table;
import io.siddhi.core.util.collection.operator.CompiledCondition;
import io.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.definition.TableDefinition;
import io.siddhi.query.api.expression.Expression;
import io.siddhi.query.api.expression.Variable;
import io.siddhi.query.api.expression.condition.And;
import io.siddhi.query.api.expression.condition.Compare;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.siddhi.extension.io.http.util.HttpConstants.ACCEPTED_CODE;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_CALLBACK;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_CALLBACK_COLUMN_NAME;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_MODE;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_MODE_DENIED;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_REASON;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_TOPIC;
import static io.siddhi.extension.io.http.util.HttpConstants.HUB_TOPIC_COLUMN_NAME;
import static io.siddhi.extension.io.http.util.HttpConstants.PARAMETER_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpConstants.PERSISTENT_ACCESS_FAIL_CODE;
import static io.siddhi.extension.io.http.util.HttpConstants.VALUE_SEPARATOR;
import static org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.DIRECTION;

/**
 * Util class which is use for handle HTTP util function.
 */
public class HttpIoUtil {
    private static final Logger log = LogManager.getLogger(HttpIoUtil.class);
    private static String clientStoreFile;
    private static String clientStorePass;
    private static ConfigReader configReader;
    private static PoolConfiguration connectionPoolConfiguration;
    private static DefaultHttpWsConnectorFactory httpConnectorFactory;
    private static Map<String, Boolean> webSuHubSubscriptionUpdate = new ConcurrentHashMap<>();

    /**
     * Handle response from http message.
     *
     * @param requestMsg  request carbon message.
     * @param responseMsg response carbon message.
     */
    public static void handleResponse(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg) {
        try {
            requestMsg.respond(responseMsg);
        } catch (ServerConnectorException e) {
            throw new HttpSourceAdaptorRuntimeException("Error occurred during response", e);
        }
    }

    /**
     * Handle failure.
     *
     * @param requestMessage request message.
     * @param ex             throwable exception.
     * @param code           error code.
     * @param payload        response payload.
     */
    public static void handleFailure(HttpCarbonMessage requestMessage, HttpSourceAdaptorRuntimeException ex, Integer
            code, String payload) {
        int statusCode = (code == null) ? 500 : code;
        String responsePayload = (payload != null) ? payload : HttpConstants.EMPTY_STRING;
        if (statusCode == 404) {
            if (ex != null) {
                responsePayload = ex.getMessage();
                log.error(responsePayload, ex);
            }
        }
        handleResponse(requestMessage, createErrorMessage(responsePayload, statusCode));
    }

    /**
     * Create new HTTP carbon message.
     *
     * @param statusCode error code
     * @return HTTP Response
     */
    private static HttpCarbonMessage createErrorMessage(String responseValue, int statusCode) {

        HttpCarbonMessage response = createHttpCarbonMessage();
        if (responseValue != null) {
            byte[] array;
            try {
                array = responseValue.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new HttpSourceAdaptorRuntimeException("Error sending response.", e);
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
            byteBuffer.put(array);
            response.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(array.length));
            byteBuffer.flip();
            response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        }
        response.setHttpStatusCode(statusCode);
        response.setProperty(DIRECTION, DIRECTION_RESPONSE);
        return response;
    }

    /**
     * This method generate the appropriate response for the received OPTIONS request.
     *
     * @param request Received option request by the source
     * @return Generated HTTPCarbonMessage as the repsonse of OPTIONS request
     */
    public static HttpCarbonMessage createOptionsResponseMessage(HttpCarbonMessage request) {
        HttpCarbonMessage response = createHttpCarbonMessage();
        response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(ByteBuffer.allocate(0))));

        response.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(0));
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(),
                request.getHeader(HttpHeaderNames.ORIGIN.toString()));
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), HttpConstants.HTTP_METHOD_POST);
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                String.format("%s,%s,%s,%s,%s", HttpHeaderNames.CONTENT_TYPE.toString(),
                        HttpHeaderNames.USER_AGENT.toString(), HttpHeaderNames.ORIGIN.toString(),
                        HttpHeaderNames.REFERER.toString(), HttpHeaderNames.ACCEPT.toString()));
        response.setHttpStatusCode(Integer.parseInt(HttpConstants.DEFAULT_HTTP_SUCCESS_CODE));
        response.setProperty(DIRECTION, DIRECTION_RESPONSE);

        return response;
    }

    public static HttpCarbonMessage createResponseMessageForWebSub(HttpCarbonMessage request) {
        HttpCarbonMessage response = createHttpCarbonMessage();

        response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(ByteBuffer.allocate(0))));
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(),
                request.getHeaders().contains(HttpHeaderNames.ORIGIN.toString()) ?
                        request.getHeader(HttpHeaderNames.ORIGIN.toString()) : "*");
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), HttpConstants.HTTP_METHOD_POST);
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                String.format("%s,%s,%s,%s,%s", HttpHeaderNames.CONTENT_TYPE.toString(),
                        HttpHeaderNames.USER_AGENT.toString(), HttpHeaderNames.ORIGIN.toString(),
                        HttpHeaderNames.REFERER.toString(), HttpHeaderNames.ACCEPT.toString()));
        response.setHttpStatusCode(ACCEPTED_CODE);
        response.setProperty(DIRECTION, DIRECTION_RESPONSE);
        return response;
    }

    /**
     * Create new HTTP carbon messge.
     *
     * @return carbon message.
     */
    public static HttpCarbonMessage createHttpCarbonMessage() {
        HttpCarbonMessage httpCarbonMessage;
        httpCarbonMessage = new HttpCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        return httpCarbonMessage;
    }

    /**
     * @param parameterList transport property list in format of 'key1:val1','key2:val2',....
     * @return transport property list
     */
    public static List<Parameter> populateParameters(String parameterList) {
        List<Parameter> parameters = new ArrayList<>();
        if (!HttpConstants.EMPTY_STRING.equals(parameterList.trim())) {
            String[] valueList = parameterList.trim().substring(1, parameterList.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            Arrays.stream(valueList).forEach(valueEntry ->
                    {
                        Parameter parameter = new Parameter();
                        String[] entry = valueEntry.split(VALUE_SEPARATOR);
                        if (entry.length == 2) {
                            parameter.setName(entry[0]);
                            parameter.setValue(entry[1]);
                            parameters.add(parameter);
                        } else {
                            log.error("Bootstrap configuration is not in expected format please insert them as " +
                                    "'key1:val1','key2:val2' format in http source.");
                        }
                    }
            );

        }
        return parameters;
    }

    /**
     * @param valueList transport property list in format of 'key1:val1','key2:val2',....
     * @return transport property list
     */
    public static Map<String, String> populateParameterMap(String[] valueList) {
        Map<String, String> parameterMap = new HashMap<>();
        Arrays.stream(valueList).forEach(valueEntry ->
                {
                    String[] entry = valueEntry.split(VALUE_SEPARATOR);
                    if (entry.length == 2) {
                        parameterMap.put(entry[0], entry[1]);
                    } else {
                        log.error("Configuration parameter '" + valueEntry + "' is not in expected format. Please " +
                                "insert them as 'key:val' format");
                    }
                }
        );
        return parameterMap;
    }

    public static boolean validateAndVerifySubscriptionRequest(HttpCarbonMessage carbonMessage,
                                                               Map<String, Object> parameterMap,
                                                               String decodedPayload, List<String> topics) {

        Map<String, Object> responsePayloadMap = new HashMap<>();
        String responseMessage;
        int responseCode;

        if (!parameterMap.containsKey(HUB_CALLBACK) || !parameterMap.containsKey(HUB_MODE) ||
                !parameterMap.containsKey(HUB_TOPIC)) {

            responseCode = PERSISTENT_ACCESS_FAIL_CODE;
            responsePayloadMap.put(HUB_MODE, HUB_MODE_DENIED);
            responsePayloadMap.put(HUB_REASON, "Subscription request must contains hub.callback, hub.mode and " +
                    "hub.topic parameters. But only found:" + decodedPayload);
            responsePayloadMap.put(HUB_TOPIC, parameterMap.get(HUB_TOPIC));
            responseMessage = responsePayloadMap.keySet().stream().map(key -> key + "=" +
                    responsePayloadMap.get(key)).collect(Collectors.joining("&"));
            handleFailure(carbonMessage, null, responseCode, responseMessage);
            log.error("Subscription request must contains hub.callback, hub.mode and " +
                    "hub.topic parameters.");
            return false;
        } else if (!topics.contains(parameterMap.get(HUB_TOPIC))) {
            responseMessage = "Subscription request failed!. Subscribed topic " + parameterMap.get(HUB_TOPIC) +
                    " is not found in the WebSub hub ";
            handleFailure(carbonMessage, null, PERSISTENT_ACCESS_FAIL_CODE, responseMessage);
            log.error(responseMessage);
            return false;
        } else {
            return true;
        }
    }

    public static void notifyWebSubSink(String hubId) {
        if (webSuHubSubscriptionUpdate.containsKey(hubId)) {
            webSuHubSubscriptionUpdate.replace(hubId, true);
        } else {
            webSuHubSubscriptionUpdate.put(hubId, true);
        }
    }

    public static boolean isWebSubSinkUpdated(String hubId) {
        if (webSuHubSubscriptionUpdate.containsKey(hubId)) {
            if (webSuHubSubscriptionUpdate.get(hubId)) {
                webSuHubSubscriptionUpdate.replace(hubId, false);
                return true;
            }
        }
        return false;
    }

    public static Expression generateFilterConditionForWebSubHub(Table table) {
        Variable leftOperator = new Variable(HUB_CALLBACK_COLUMN_NAME);
        leftOperator.setStreamId(table.getTableDefinition().getId());
        Compare leftExpression = new Compare(leftOperator, Compare.Operator.EQUAL,
                new Variable(HUB_CALLBACK_COLUMN_NAME));
        Variable rightExpresionLeftOperator = new Variable(HUB_TOPIC_COLUMN_NAME);
        rightExpresionLeftOperator.setStreamId(table.getTableDefinition().getId());
        Compare rightExpression = new Compare(rightExpresionLeftOperator, Compare.Operator.EQUAL,
                new Variable(HUB_TOPIC_COLUMN_NAME));
        return new And(leftExpression, rightExpression);
    }

    public static CompiledCondition createTableDeleteResource(Map<String, Table> tableMap, String tableName,
                                                              SiddhiQueryContext siddhiQueryContext) {
        Table table = tableMap.get(tableName);
        Expression condition = HttpIoUtil.generateFilterConditionForWebSubHub(table);

        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        MetaStreamEvent tableMetaStreamEvent = new MetaStreamEvent();
        MetaStreamEvent inputStreamMetaStreamEvent = new MetaStreamEvent();

        tableMetaStreamEvent.addInputDefinition(table.getTableDefinition());
        tableMetaStreamEvent.setEventType(MetaStreamEvent.EventType.TABLE);

        TableDefinition tableDefinition = TableDefinition.id("");
        tableDefinition.attribute(HUB_CALLBACK, Attribute.Type.STRING);
        tableDefinition.attribute(HUB_TOPIC, Attribute.Type.STRING);
        inputStreamMetaStreamEvent.addInputDefinition(tableDefinition);
        inputStreamMetaStreamEvent.setEventType(MetaStreamEvent.EventType.TABLE);
        metaStateEvent.addEvent(inputStreamMetaStreamEvent);
        metaStateEvent.addEvent(tableMetaStreamEvent);

        MatchingMetaInfoHolder matchingMetaInfoHolder = new MatchingMetaInfoHolder(metaStateEvent, 0, 1,
                tableDefinition, table.getTableDefinition(), 0);

        return table.compileCondition(condition, matchingMetaInfoHolder, null,
                tableMap, siddhiQueryContext);
    }
}
