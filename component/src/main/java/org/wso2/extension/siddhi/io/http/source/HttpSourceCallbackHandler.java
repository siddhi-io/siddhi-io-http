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
package org.wso2.extension.siddhi.io.http.source;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

/**
 * Handling handling the callback of http source.
 */
class HttpSourceCallbackHandler {
    private String message;
    private CarbonCallback carbonCallback;
    private int code;

    HttpSourceCallbackHandler(String message, CarbonCallback carbonCallback, int code) {
        this.message = message;
        this.carbonCallback = carbonCallback;
        this.code = code;
    }

    void handleCallback() {
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(this.message);
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, (this.code));
        defaultCarbonMessage.setProperty(Constants.HTTP_REASON_PHRASE, this.message);
        this.carbonCallback.done(defaultCarbonMessage);
    }
}
