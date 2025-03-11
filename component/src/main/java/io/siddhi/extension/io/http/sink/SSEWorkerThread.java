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
package io.siddhi.extension.io.http.sink;

import io.siddhi.extension.io.http.source.HttpWorkerThread;
import io.siddhi.extension.io.http.util.HTTPSinkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * Handles the send data to source listener.
 */
public class SSEWorkerThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpWorkerThread.class);
    private HttpCarbonMessage carbonMessage;
    private String streamID;

    SSEWorkerThread(HttpCarbonMessage cMessage, String streamID) {
        this.carbonMessage = cMessage;
        this.streamID = streamID;
    }

    @Override
    public void run() {
        BufferedReader buf = new BufferedReader(
                new InputStreamReader(
                        new HttpMessageDataStreamer(carbonMessage).getInputStream(), Charset.defaultCharset()));
        String payload = buf.lines().collect(Collectors.joining("\n"));
        carbonMessage.setStreaming(true);
        HTTPSinkRegistry.findAndGetSSESource(streamID).registerCallback(carbonMessage);
        if (logger.isDebugEnabled()) {
            logger.debug("Submitted Event {} Stream", payload);
        }

        try {
            buf.close();
        } catch (IOException e) {
            logger.error("Error occurred when closing the byte buffer in source {}", streamID, e);
        } finally {
            carbonMessage.waitAndReleaseAllEntities();
        }
    }
}
