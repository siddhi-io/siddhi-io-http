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

package io.siddhi.extension.io.http.sink;

import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.HttpAuthenticator;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for listening to incoming events.
 */
public class HttpSSERequestListener {
    private static final Logger logger = LoggerFactory.getLogger(HttpSSERequestListener.class);
    private ExecutorService executorService;
    private boolean paused;
    private ReentrantLock lock;
    private Condition condition;
    private String url;
    private Boolean isAuthEnabled;
    private String[] requestedTransportPropertyNames;
    private String siddhiAppName;
    private SourceMetrics metrics;
    private String streamId;

    public HttpSSERequestListener(int workerThread, String url, Boolean auth,
                                  String[] requestedTransportPropertyNames,
                                  String streamId, String siddhiAppName, SourceMetrics metrics) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.siddhiAppName = siddhiAppName;
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthEnabled = auth;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
        this.metrics = metrics;
        this.streamId = streamId;
    }

    protected void send(HttpCarbonMessage carbonMessage) {
        if (paused) {
            lock.lock();
            try {
                while (paused) {
                    condition.await();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                if (metrics != null) {
                    metrics.getTotalHttpErrorsMetric().inc();
                }

                logger.error("Thread interrupted while pausing ", ie);
                HttpSourceUtil.handleCallback(carbonMessage, 500);
            } finally {
                lock.unlock();
            }
        }
        if (isAuthEnabled) {
            if (!HttpAuthenticator.authenticate(carbonMessage)) {
                if (metrics != null) {
                    metrics.getTotalHttpErrorsMetric().inc();
                }

                throw new HttpSourceAdaptorRuntimeException(carbonMessage, "Authorisation fails", 401);
            }
        }
        String[] trpProperties = new String[requestedTransportPropertyNames.length];
        executorService.execute(new HttpSSEWorkerThread(carbonMessage, streamId, trpProperties, metrics));

    }

    private void populateTransportHeaders(HttpCarbonMessage carbonMessage, String[] properties) {
        if (requestedTransportPropertyNames.length > 0) {      //cannot be null according to siddhi impl
            int i = 0;
            for (String property : requestedTransportPropertyNames) {
                String value = carbonMessage.getHeader(property);
                if (value != null) {
                    properties[i] = value;
                }
                i++;
            }
        }
    }

    protected void populateTransportProperties(HttpCarbonMessage carbonMessage, String[] properties) {
        if (requestedTransportPropertyNames.length > 0) {      //cannot be null according to siddhi impl
            int i = 0;
            for (String property : requestedTransportPropertyNames) {
                Object value = carbonMessage.getProperty(property);
                if (value != null) {
                    properties[i] = String.valueOf(value);
                }
                i++;
            }
        }
    }

    private void populateTransportProperties(String[] properties, String messageId) {

        if (requestedTransportPropertyNames.length > 0) {      //cannot be null according to siddhi impl
            int i = 0;
            for (String property : requestedTransportPropertyNames) {
                if ("messageId".equalsIgnoreCase(property)) {
                    properties[i] = messageId;
                    break;
                }
                i++;
            }
        }
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    void disconnect() {
        executorService.shutdown();
    }
}
