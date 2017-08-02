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

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the send data to source listener.
 */
public class HttpWorkerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpWorkerThread.class);
    private CarbonMessage carbonMessage;
    private CarbonCallback carbonCallback;
    private SourceEventListener sourceEventListener;
    private String sourceID;
    private String[] trpProperties;

    HttpWorkerThread(CarbonMessage cMessage, CarbonCallback cCallback, SourceEventListener sourceEventListener,
                     String sourceID, String[] trpProperties) {
        this.carbonMessage = cMessage;
        this.carbonCallback = cCallback;
        this.sourceEventListener = sourceEventListener;
        this.sourceID = sourceID;
        this.trpProperties = trpProperties;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = carbonMessage.getInputStream();
            String payload = new String(ByteStreams.toByteArray(inputStream));
            if (!payload.equals(HttpConstants.EMPTY_STRING)) {
                sourceEventListener.onEvent(payload, trpProperties);
                HttpSourceUtil.handleCallback("OK", carbonCallback, 200);
                if (logger.isDebugEnabled()) {
                    logger.debug("Submitted Event " + payload + " Stream");
                }
            } else {
                HttpSourceUtil.handleCallback("Empty payload event", carbonCallback, 405);
                if (logger.isDebugEnabled()) {
                    logger.debug("Empty payload event, hence dropping the event chunk in " + sourceID);
                }
            }
        } catch (IOException e) {
            logger.error("Event format is not equal to the expected in " + sourceID);
            HttpSourceUtil.handleCallback("Event format " +
                    "is not equal to the expected", carbonCallback, 405);
        } finally {
            carbonMessage.release();
        }
    }
}
