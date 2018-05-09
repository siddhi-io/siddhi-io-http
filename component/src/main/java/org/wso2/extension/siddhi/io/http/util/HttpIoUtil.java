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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.extension.siddhi.io.http.util.HttpConstants.PARAMETER_SEPARATOR;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.VALUE_SEPARATOR;

/**
 * Util class which is use for handle HTTP util function.
 */
public class HttpIoUtil {
    private static final Logger log = Logger.getLogger(HttpIoUtil.class);
    
    /**
     * Handle response from http message.
     *
     * @param requestMsg  request carbon message.
     * @param responseMsg response carbon message.
     */
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
     * @param requestMessage request message.
     * @param ex             throwable exception.
     * @param code           error code.
     * @param payload        response payload.
     */
    public static void handleFailure(HTTPCarbonMessage requestMessage, HttpSourceAdaptorRuntimeException ex, Integer
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
    private static HTTPCarbonMessage createErrorMessage(String responseValue, int statusCode) {
        
        HTTPCarbonMessage response = createHttpCarbonMessage();
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
        response.setProperty(org.wso2.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        return response;
    }
    
    /**
     * Create new HTTP carbon messge.
     *
     * @return carbon message.
     */
    private static HTTPCarbonMessage createHttpCarbonMessage() {
        HTTPCarbonMessage httpCarbonMessage;
        httpCarbonMessage = new HTTPCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        return httpCarbonMessage;
    }
    
    /**
     * @param parameterList transport property list in format of 'key1:val1','key2:val2',....
     * @return transport property list
     */
    public static List<Parameter> populateParameters(String parameterList) {
        List<org.wso2.transport.http.netty.config.Parameter> parameters = new ArrayList<>();
        if (!HttpConstants.EMPTY_STRING.equals(parameterList.trim())) {
            String[] valueList = parameterList.trim().substring(1, parameterList.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            Arrays.stream(valueList).forEach(valueEntry ->
                    {
                        org.wso2.transport.http.netty.config.Parameter parameter = new Parameter();
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
}
