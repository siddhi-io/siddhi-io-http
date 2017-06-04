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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientConnector;
import org.wso2.siddhi.extension.http.util.HttpConstants;

import java.util.HashMap;
import java.util.Map;

/**
 *{@code HttpPublisher }This thread is Handling publishing data to outer endpoint via http.
 */
public class HttpPublisher implements Runnable {
    private static final Logger log = Logger.getLogger(HttpPublisher.class);
    private Map<String, String> httpProperties;
    private HTTPClientConnector httpClientConnector;
    private CarbonMessage message;
    private String payload;
    private String streamID;

    HttpPublisher(CarbonMessage message, Map<String, String> staticHTTPProperties,
                  HTTPClientConnector httpClientConnector, String payload, String streamID) {
        this.message = message;
        httpProperties = new HashMap<>();
        this.httpProperties.putAll(staticHTTPProperties);
        this.httpClientConnector = httpClientConnector;
        this.payload = payload;
        this.streamID = streamID;
    }

    @Override
    public void run() {
        try {
            httpClientConnector.send(message, new HttpSinkCallback(payload), httpProperties);
        } catch (ClientConnectorException e) {
            log.error("Error sending the HTTP message with payload " + payload + " in " +
                    HttpConstants.HTTP_SINK_ID + streamID, e);
        }
    }
}
