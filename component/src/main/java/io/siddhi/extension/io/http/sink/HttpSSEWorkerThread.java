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

import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.HttpWorkerThread;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HTTPSinkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HttpSSEWorkerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpWorkerThread.class);
    private HttpCarbonMessage carbonMessage;
    private String streamID;
    private SourceMetrics metrics;

    HttpSSEWorkerThread(HttpCarbonMessage cMessage, String streamID, SourceMetrics metrics) {
        this.carbonMessage = cMessage;
        this.streamID = streamID;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        BufferedReader buf = new BufferedReader(
                new InputStreamReader(
                        new HttpMessageDataStreamer(carbonMessage).getInputStream(), Charset.defaultCharset()));
        String payload = buf.lines().collect(Collectors.joining("\n"));
        carbonMessage.setStreaming(true);
        HTTPSinkRegistry.findAndGetSSESource(streamID).registerCallback(carbonMessage);
        if (metrics != null) {
            metrics.getTotalReadsMetric().inc();
            metrics.getTotalHttpReadsMetric().inc();
            metrics.getRequestSizeMetric().inc(HttpSourceUtil.getByteSize(payload));
            metrics.setLastEventTime(System.currentTimeMillis());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Submitted Event " + payload + " Stream");
        }
        try {
            buf.close();
            carbonMessage.waitAndReleaseAllEntities();
        } catch (IOException e) {
            if (metrics != null) {
                metrics.getTotalHttpErrorsMetric().inc();
            }
            logger.error("Error occurred when closing the byte buffer in source " + streamID, e);
        }
    }
}
