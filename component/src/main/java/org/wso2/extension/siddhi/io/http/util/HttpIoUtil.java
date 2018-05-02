package org.wso2.extension.siddhi.io.http.util;


import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Util class which is use for handle HTTP util function.
 */
public class HttpIoUtil {
    private static final Logger log = Logger.getLogger(HttpIoUtil.class);
    
    /**
     * Adding header to Http Carbon message
     *
     * @param httpCarbonMessage
     */
    public static void addHeader(HTTPCarbonMessage httpCarbonMessage) {
        String headerName = "";
        String headerValue = "";
        
        HttpHeaders httpHeaders = httpCarbonMessage.getHeaders();
        httpHeaders.add(headerName, headerValue);
        
        if (log.isDebugEnabled()) {
            log.debug("Add " + headerName + " to header with value: " + headerValue);
        }
    }
    
    /**
     * Geeting header from HTTP carbon message
     *
     * @param httpCarbonMessage
     * @return
     */
    public static String getHeader(HTTPCarbonMessage httpCarbonMessage) {
        String headerName = "";
        String headerValue = httpCarbonMessage.getHeader(headerName);
        boolean headerExists = headerValue != null;
        
        // Reset the header value to siddhi string default value if the header doesn't exist
        headerValue = !headerExists ? "" : headerValue;
        
        return headerValue;
    }
    
    /**
     * Handle response from http message.
     *
     * @param requestMsg
     * @param responseMsg
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
     * @param requestMessage
     * @param ex
     * @param code
     * @param payload
     */
    public static void handleFailure(HTTPCarbonMessage requestMessage, HttpSourceAdaptorRuntimeException ex, Integer
            code, String payload) {
        int statusCode = (code == null) ? 500 : code;
        String responsePayload = (payload != null) ? payload : "";
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
     * @param statusCode
     * @return
     */
    private static HTTPCarbonMessage createErrorMessage(String responseValue, int statusCode) {
        
        HTTPCarbonMessage response = createHttpCarbonMessage(false);
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
     * @param isRequest
     * @return
     */
    private static HTTPCarbonMessage createHttpCarbonMessage(boolean isRequest) {
        HTTPCarbonMessage httpCarbonMessage;
        if (isRequest) {
            httpCarbonMessage = new HTTPCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, HttpConstants.EMPTY_STRING));
        } else {
            httpCarbonMessage = new HTTPCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        }
        return httpCarbonMessage;
    }
}
