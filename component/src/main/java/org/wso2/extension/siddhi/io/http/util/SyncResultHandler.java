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
package org.wso2.extension.siddhi.io.http.util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Result handler
 */
public class SyncResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(SyncResultHandler.class);

    private static Map<String, Map<String, HTTPCarbonMessage>> resultContainerMap = new ConcurrentHashMap<>();

    private static HashedWheelTimer timer = new HashedWheelTimer();
    private static WeakHashMap<String, Timeout> schedularMap = new WeakHashMap<>();

    public static void registerCallback(HTTPCarbonMessage carbonMessage, String sourceId, String messageId) {

        // Add timeout handler to the timer.
        // TODO : set proper value for message timeout
        addTimeout(sourceId, messageId, 10000);

        if (resultContainerMap.get(sourceId) == null) {
            resultContainerMap.put(sourceId, new ConcurrentHashMap<>());
        }
        resultContainerMap.get(sourceId).put(messageId, carbonMessage);
    }

    public static void handleCallback(String sourceId, String messageId, String payload, List<Header> headersList,
                                      String contentType) {

        Map<String, HTTPCarbonMessage> messageMap = resultContainerMap.get(sourceId);
        if (messageMap != null) {
            HTTPCarbonMessage carbonMessage = messageMap.get(messageId);
            // Remove the message from the map as we are going to reply to the message.
            messageMap.remove(messageId);
            // Remove the timeout task as are replying to the message.
            removeTimeout(messageId);
            // Send the response to the correlating message.
            handleResponse(carbonMessage, 200, payload, headersList, contentType);
        } else {
            logger.warn("No source message found for source: " + sourceId + " and message: " + messageId);
        }
    }

    private static void addTimeout(String sourceId, String messageId, long messageTimeout) {

        Timeout timeout = timer.newTimeout(new TimerTaskImpl(sourceId, messageId), messageTimeout,
                TimeUnit.MILLISECONDS);
        schedularMap.put(messageId, timeout);
    }

    private static void removeTimeout(String messageId) {

        schedularMap.get(messageId).cancel();
    }

    private static void handleResponse(HTTPCarbonMessage requestMsg, HTTPCarbonMessage responseMsg) {

        try {
            requestMsg.respond(responseMsg);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new HttpSourceAdaptorRuntimeException("Error occurred during response", e);
        }
    }

    /**
     * Handle failure.
     *
     * @param requestMessage
     * @param code
     * @param payload
     */
    private static void handleResponse(HTTPCarbonMessage requestMessage, Integer code, String payload, List<Header>
            headers, String contentType) {

        int statusCode = (code == null) ? 500 : code;
        String responsePayload = (payload != null) ? payload : "";
        handleResponse(requestMessage, createResponseMessage(responsePayload, statusCode, headers, contentType));
    }

    /**
     * Create new HTTP carbon message.
     *
     * @param payload
     * @param statusCode
     * @return
     */
    private static HTTPCarbonMessage createResponseMessage(String payload, int statusCode, List<Header> headers,
                                                           String contentType) {

        HTTPCarbonMessage response = new HTTPCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload
                .getBytes(Charset.defaultCharset()))));

        HttpHeaders httpHeaders = response.getHeaders();

        response.setProperty(org.wso2.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);

        // Set the Content-Type header as the system generated value. If the user has defined a specific Content-Type
        // header this will be overridden.
        if (contentType != null) {
            httpHeaders.set(HttpConstants.HTTP_CONTENT_TYPE, contentType);
        }

        if (headers != null) {
            for (Header header : headers) {
                httpHeaders.set(header.getName(), header.getValue());
            }
        }

        return response;
    }

    static class TimerTaskImpl implements TimerTask {

        String messageId;
        String sourceId;

        TimerTaskImpl(String sourceId, String messageId) {

            this.sourceId = sourceId;
            this.messageId = messageId;
        }

        @Override
        public void run(Timeout timeout) {

            Map<String, HTTPCarbonMessage> messageMap = resultContainerMap.get(sourceId);
            if (messageMap != null) {
                HTTPCarbonMessage carbonMessage = messageMap.get(messageId);
                messageMap.remove(messageId);
                schedularMap.remove(messageId);
                handleResponse(carbonMessage, 504, null, null, null);
            }
        }
    }
}
