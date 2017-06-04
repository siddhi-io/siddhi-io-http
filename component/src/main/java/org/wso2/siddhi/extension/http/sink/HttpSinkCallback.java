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
package org.wso2.siddhi.extension.http.sink;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

/**
 *{@code HttpSinkCallback } Handle all http callbacks.
 */
public class HttpSinkCallback implements CarbonCallback {
    private static final Logger log = Logger.getLogger(HttpSinkCallback.class);
    private String payload;

    HttpSinkCallback(String payload) {
        this.payload = payload;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        String statusCode = carbonMessage.getHeaders().get(Constants.HTTP_STATUS_CODE);
        if (log.isDebugEnabled()) {
            log.debug("Event send with payload " + payload + " Response status code " + statusCode);
        }
    }
}
