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

package io.siddhi.extension.io.http.source;

import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Connector Listener for HttpResponseSource
 */
public class HttpCallResponseConnectorListener implements HttpConnectorListener {
    private static final Logger log = LogManager.getLogger(HttpCallResponseConnectorListener.class);
    private SourceEventListener sourceEventListener;
    private String sinkId;
    private ExecutorService executorService;
    private String siddhiAppName;
    private String[] trpPropertyNames;
    private boolean shouldAllowStreamingResponses;
    private SourceMetrics metrics;

    public HttpCallResponseConnectorListener(int numberOfThreads, SourceEventListener sourceEventListener,
                                             boolean shouldAllowStreamingResponses,
                                             String sinkId, String[] trpPropertyNames, String siddhiAppName,
                                             SourceMetrics metrics) {
        this.sourceEventListener = sourceEventListener;
        this.sinkId = sinkId;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.siddhiAppName = siddhiAppName;
        this.trpPropertyNames = trpPropertyNames.clone();
        this.shouldAllowStreamingResponses = shouldAllowStreamingResponses;
        this.metrics = metrics;
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        String[] properties = new String[trpPropertyNames.length];
        for (int i = 0; i < trpPropertyNames.length; i++) {
            Object property = carbonMessage.getProperty(trpPropertyNames[i]);
            if (property != null) {
                properties[i] = carbonMessage.getProperty(trpPropertyNames[i]).toString();
            }
        }
        HttpResponseProcessor workerThread =
                new HttpResponseProcessor(carbonMessage, sourceEventListener, shouldAllowStreamingResponses,
                        sinkId, properties, metrics);
        executorService.execute(workerThread);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error occurred during processing response for the request sent by http-call sink with 'sink.id' = " +
                        "{} in Siddhi app {}.", sinkId, siddhiAppName, throwable);
    }

    /**
     * Returns the siddhi app name
     *
     * @return siddhi app name
     */
    String getSiddhiAppName() {
        return siddhiAppName;
    }

    /**
     * Disconnect pool execution.
     */
    void disconnect() {
        executorService.shutdown();
    }
}
