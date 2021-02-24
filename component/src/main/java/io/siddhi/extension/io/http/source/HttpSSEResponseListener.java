/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.siddhi.extension.io.http.source;

import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Connector Listener for HttpSSESource.
 */
public class HttpSSEResponseListener implements HttpConnectorListener {
    private static final Logger log = LoggerFactory.getLogger(HttpSSEResponseListener.class);
    private String streamId;
    private CountDownLatch latch;
    private HttpSSESource source;
    private SourceMetrics metrics;

    public HttpSSEResponseListener(HttpSSESource source, String streamId, CountDownLatch latch,
                                   SourceMetrics metrics) {
        this.streamId = streamId;
        this.latch = latch;
        this.source = source;
        this.metrics = metrics;
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        carbonMessage.setProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT, false);
        if (latch != null) {
            latch.countDown();
        }

        HttpSSESource responseSource = HTTPSourceRegistry.findAndGetSSESource(streamId);
        if (responseSource != null) {
            HttpSSEResponseConnectorListener responseConnectorListener = responseSource.getConnectorListener();
            responseConnectorListener.onMessage(carbonMessage);
        } else {
            log.error("No sse source is registered for the stream '" + streamId +
                    "'. Hence dropping the response message.");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof IOException) {
            source.createClientConnector();
        }

        if (latch != null) {
            latch.countDown();
        }
    }
}
