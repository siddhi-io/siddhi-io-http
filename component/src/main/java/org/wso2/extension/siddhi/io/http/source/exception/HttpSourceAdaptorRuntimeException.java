/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.http.source.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP source adaptor specific exception.
 */
public class HttpSourceAdaptorRuntimeException extends RuntimeException {

    public HttpSourceAdaptorRuntimeException(String message, Throwable e) {
        super(message, e);
    }

    public HttpSourceAdaptorRuntimeException(String message) {
        super(message);
    }

    public HttpSourceAdaptorRuntimeException(CarbonMessage carbonMessage, String message, CarbonCallback
            carbonCallback, int code) {
        super(message);
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(message);
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, code);
        defaultCarbonMessage.setProperty(Constants.HTTP_REASON_PHRASE, HttpResponseStatus.valueOf(code).reasonPhrase());
        defaultCarbonMessage.setHeader(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        defaultCarbonMessage.setHeader(Constants.HTTP_VERSION, HTTP_1_1.text());
        defaultCarbonMessage.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        carbonCallback.done(defaultCarbonMessage);
        carbonMessage.release();
    }

    public HttpSourceAdaptorRuntimeException(String message, Throwable cause, CarbonCallback carbonCallback, int code
    , CarbonMessage carbonMessage) {
        super(message, cause);
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(message + cause.getMessage());
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, (code));
        defaultCarbonMessage.setProperty(Constants.HTTP_REASON_PHRASE, HttpResponseStatus.valueOf(code).reasonPhrase());
        defaultCarbonMessage.setHeader(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        defaultCarbonMessage.setHeader(Constants.HTTP_VERSION, HTTP_1_1.text());
        defaultCarbonMessage.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        carbonCallback.done(defaultCarbonMessage);
        carbonMessage.release();
    }
}
