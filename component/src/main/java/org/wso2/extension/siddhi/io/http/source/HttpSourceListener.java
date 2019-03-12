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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

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
    protected ExecutorService executorService;
    protected boolean paused;
    protected ReentrantLock lock;
    protected Condition condition;
    protected String url;
    protected Boolean isAuthEnabled;
    protected SourceEventListener sourceEventListener;
    protected String[] requestedTransportPropertyNames;
    private String siddhiAppName;

    protected HttpSourceListener(int workerThread, String url, Boolean auth, SourceEventListener sourceEventListener,
                                 String[] requestedTransportPropertyNames, String siddhiAppName) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.siddhiAppName = siddhiAppName;
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthEnabled = auth;
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
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
                logger.error("Thread interrupted while pausing ", ie);
                HttpSourceUtil.handleCallback(carbonMessage, 500);
            } finally {
                lock.unlock();
            }
        }
        if (isAuthEnabled) {
            if (!HttpAuthenticator.authenticate(carbonMessage)) {
                throw new HttpSourceAdaptorRuntimeException(carbonMessage, "Authorisation fails", 401);
            }
        }
        String[] trpProperties = new String[requestedTransportPropertyNames.length];
        populateTransportHeaders(carbonMessage, trpProperties);
        populateTransportProperties(carbonMessage, trpProperties);
        executorService.execute(new HttpWorkerThread(carbonMessage,
                sourceEventListener, sourceEventListener.getStreamDefinition().toString(), trpProperties));

    }

    protected void populateTransportHeaders(HttpCarbonMessage carbonMessage, String[] properties) {
        if (requestedTransportPropertyNames.length > 0) {      //cannot be null according to siddhi impl
            int i = 0;
            for (String property : requestedTransportPropertyNames) {
                properties[i] = carbonMessage.getHeader(property);      //can be null
                i++;
            }
        }
    }

    protected void populateTransportProperties(HttpCarbonMessage carbonMessage, String[] properties) {
        if (requestedTransportPropertyNames.length > 0) {      //cannot be null according to siddhi impl
            int i = 0;
            for (String property : requestedTransportPropertyNames) {
                properties[i] = String.valueOf(carbonMessage.getProperty(property));      //can be null
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
