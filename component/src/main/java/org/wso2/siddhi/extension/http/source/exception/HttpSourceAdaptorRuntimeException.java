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
package org.wso2.siddhi.extension.http.source.exception;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

/**
 * HTTP source adaptor specific exception.
 */
public class HttpSourceAdaptorRuntimeException extends RuntimeException {

    public HttpSourceAdaptorRuntimeException(String message) {
        super(message);
    }

    public HttpSourceAdaptorRuntimeException(String message, CarbonCallback carbonCallback, int code) {
        super(message);
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(message);
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, code);
        carbonCallback.done(defaultCarbonMessage);
    }

    public HttpSourceAdaptorRuntimeException(String message, Throwable cause, CarbonCallback carbonCallback, int code) {
        super(message, cause);
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(message + cause.getMessage());
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, (code));
        carbonCallback.done(defaultCarbonMessage);
    }


    public HttpSourceAdaptorRuntimeException(Throwable cause, CarbonCallback carbonCallback, int code) {
        super(cause);
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(cause.getMessage());
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, (code));
        carbonCallback.done(defaultCarbonMessage);
    }
}
