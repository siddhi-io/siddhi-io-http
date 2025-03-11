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
package io.siddhi.extension.io.http.source;

import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
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
public class HttpWorkerThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpWorkerThread.class);
    private HttpCarbonMessage carbonMessage;
    private SourceEventListener sourceEventListener;
    private String sourceID;
    private String[] trpProperties;
    private SourceMetrics metrics;

    HttpWorkerThread(HttpCarbonMessage cMessage, SourceEventListener sourceEventListener,
                     String sourceID, String[] trpProperties, SourceMetrics metrics) {
        this.carbonMessage = cMessage;
        this.sourceEventListener = sourceEventListener;
        this.sourceID = sourceID;
        this.trpProperties = trpProperties;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        BufferedReader buf = new BufferedReader(
                new InputStreamReader(
                        new HttpMessageDataStreamer(carbonMessage).getInputStream(), Charset.defaultCharset()));
        try {
            String payload = buf.lines().collect(Collectors.joining("\n"));

            if (!payload.equals(HttpConstants.EMPTY_STRING)) {

                if (metrics != null) {
                    metrics.getTotalReadsMetric().inc();
                    metrics.getTotalHttpReadsMetric().inc();
                    metrics.getRequestSizeMetric().inc(HttpSourceUtil.getByteSize(payload));
                    metrics.setLastEventTime(System.currentTimeMillis());
                }

                sourceEventListener.onEvent(payload, trpProperties);
                HttpSourceUtil.handleCallback(carbonMessage, 200);
                if (logger.isDebugEnabled()) {
                    logger.debug("Submitted Event {} Stream", payload);
                }
            } else {
                if (metrics != null) {
                    metrics.getTotalHttpErrorsMetric().inc();
                }

                HttpSourceUtil.handleCallback(carbonMessage, 405);
                if (logger.isDebugEnabled()) {
                    logger.debug("Empty payload event, hence dropping the event chunk at source {}", sourceID);
                }
            }
        } finally {
            try {
                buf.close();
            } catch (IOException e) {
                if (metrics != null) {
                    metrics.getTotalHttpErrorsMetric().inc();
                }
                logger.error("Error occurred when closing the byte buffer in source {}", sourceID, e);
            } finally {
                carbonMessage.waitAndReleaseAllEntities();
            }
        }
    }
}
