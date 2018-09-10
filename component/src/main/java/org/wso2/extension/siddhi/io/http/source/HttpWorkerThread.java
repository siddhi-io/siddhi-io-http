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

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.charset.Charset;

/**
 * Handles the send data to source listener.
 */
public class HttpWorkerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpWorkerThread.class);
    private HTTPCarbonMessage carbonMessage;
    private SourceEventListener sourceEventListener;
    private String sourceID;
    private String[] trpProperties;
    
    HttpWorkerThread(HTTPCarbonMessage cMessage, SourceEventListener sourceEventListener,
                     String sourceID, String[] trpProperties) {
        this.carbonMessage = cMessage;
        this.sourceEventListener = sourceEventListener;
        this.sourceID = sourceID;
        this.trpProperties = trpProperties;
    }
    
    @Override
    public void run() {
        HttpContent content;
        do {
            content = carbonMessage.getHttpContent();
            if (content != null) {
                String payload = content.content().toString(Charset.defaultCharset());
                if (!payload.equals(HttpConstants.EMPTY_STRING)) {
                    sourceEventListener.onEvent(payload, trpProperties);
                    HttpSourceUtil.handleCallback(carbonMessage, 200);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Submitted Event " + payload + " Stream");
                    }
                } else {
                    HttpSourceUtil.handleCallback(carbonMessage, 405);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Empty payload event, hence dropping the event chunk in " + sourceID);
                    }
                }
            }
        } while (!(content instanceof LastHttpContent));
    }
}
