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

import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.table.Table;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code HttpSourceListener } This class maintain the states of each and every source listener which are created
 * such as is currently paused or not,  need isAuthEnabled or not.
 */
public class HttpSourceListener {
    private static final Logger logger = LoggerFactory.getLogger(HttpSourceListener.class);
    private static final char QUERY_PARAMS_IDENTIFIER = '_'; //Query params are given a prefix of _ in the SiddhiApp
    private static final String QUERY_PARAMS_CONTAINING_PROPERTY = "TO";
    private static final char QUERY_PARAMS_SEPARATOR = '&';
    private static final String QUERY_PARAMS_SPLITTER_FROM_URLSTRING = "\\?";
    private static final char QUERY_PARAMS_KEY_AND_VALUE_SEPARATOR = '=';
    protected ExecutorService executorService;
    protected boolean paused;
    protected ReentrantLock lock;
    protected Condition condition;
    protected String url;
    protected Boolean isAuthEnabled;
    protected SourceEventListener sourceEventListener;
    protected String[] requestedTransportPropertyNames;
    protected SourceMetrics metrics;
    protected String urlString;
    boolean isWebSub = false;
    Table table;
    String hubId;
    private String siddhiAppName;
    private SiddhiAppContext siddhiAppContext;
    private List<String> topics;

    protected HttpSourceListener(int workerThread, String url, Boolean auth, SourceEventListener sourceEventListener,
                                 String[] requestedTransportPropertyNames, String siddhiAppName,
                                 SourceMetrics metrics) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.siddhiAppName = siddhiAppName;
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthEnabled = auth;
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
        this.metrics = metrics;
    }

    protected HttpSourceListener(int workerThread, String url, Boolean auth, SourceEventListener sourceEventListener,
                                 String[] requestedTransportPropertyNames, String siddhiAppName,
                                 SourceMetrics metrics, Table table, String hubId, SiddhiAppContext siddhiAppContext,
                                 List<String> topics) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.siddhiAppName = siddhiAppName;
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthEnabled = auth;
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
        this.metrics = metrics;
        this.isWebSub = true;
        this.table = table;
        this.hubId = hubId;
        this.siddhiAppContext = siddhiAppContext;
        this.topics = topics;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    /**
     * This method is handle the submit carbon message to executor service.
     *
     * @param carbonMessage the carbon message received from carbon transport.
     */
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
        populateTransportHeaders(carbonMessage, trpProperties);
        populateTransportProperties(carbonMessage, trpProperties);
        if (isWebSub) {
            executorService.execute(new HttpWebSubResponseProcessor(carbonMessage,
                    sourceEventListener, sourceEventListener.getStreamDefinition().toString(), trpProperties,
                    metrics, table, hubId, siddhiAppContext, topics));
        } else {
            executorService.execute(new HttpWorkerThread(carbonMessage,
                    sourceEventListener, sourceEventListener.getStreamDefinition().toString(), trpProperties, metrics));
        }
    }

    protected void populateTransportHeaders(HttpCarbonMessage carbonMessage, String[] properties) {
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
                if (property.startsWith(String.valueOf(QUERY_PARAMS_IDENTIFIER))) {
                    String[] param = property.split(String.valueOf(QUERY_PARAMS_IDENTIFIER), -2);
                    urlString = carbonMessage.getProperty(QUERY_PARAMS_CONTAINING_PROPERTY).toString();
                    String[] temp1 = urlString.split(QUERY_PARAMS_SPLITTER_FROM_URLSTRING, -2);
                    String[] temp2 = temp1[1].split(String.valueOf(QUERY_PARAMS_SEPARATOR), -2);
                    for (String temp3 : temp2) {
                        String[] temp4 = temp3.split(String.valueOf(QUERY_PARAMS_KEY_AND_VALUE_SEPARATOR), -2);
                        if (temp4[0].equals(param[1])) {
                            properties[i] = temp4[1];
                        }
                    }
                } else {
                    Object value = carbonMessage.getProperty(property);
                    if (value != null) {
                        properties[i] = String.valueOf(value);
                    }
                }
                i++;
            }
        }
    }

    /**
     * State that current Source Listener is paused or not.
     *
     * @return state of pause.
     */
    boolean isRunning() {
        return !paused;
    }

    /**
     * State that current Source Listener is running or not.
     *
     * @return state of pause.
     */
    boolean isPaused() {
        return paused;
    }

    /**
     * Pause the execution.
     */
    void pause() {
        lock.lock();
        try {
            paused = true;
            logger.info("Event input has paused for " + url);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resume pool execution.
     */
    void resume() {
        lock.lock();
        try {
            paused = false;
            logger.info("Event input has resume for " + url);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Disconnect pool execution.
     */
    void disconnect() {
        executorService.shutdown();
    }
}
