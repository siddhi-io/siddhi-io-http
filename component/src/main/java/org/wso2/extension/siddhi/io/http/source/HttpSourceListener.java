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
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.extension.siddhi.io.http.source.auth.HttpAuthenticator;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code HttpSourceListener } This class maintain the states of each and every source listener which are created
 * such as is currently paused or not,  need isAuthenticated or not.
 */
class HttpSourceListener {
    private static final Logger logger = LoggerFactory.getLogger(HttpSourceListener.class);
    private ExecutorService executorService;
    private boolean paused;
    private ReentrantLock lock;
    private Condition condition;
    private String url;
    private Boolean isAuthenticated;
    private SourceEventListener sourceEventListener;

    HttpSourceListener(int workerThread, String url, Boolean auth, SourceEventListener sourceEventListener) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthenticated = auth;
        this.sourceEventListener = sourceEventListener;
        logger.info("Source Listener has created for url " + this.url);
    }

    /**
     * This method is handle the submit carbon message to executor service.
     *
     * @param carbonMessage  the carbon message received from carbon transport.
     * @param carbonCallback the carbon callback received from carbon transport.
     * @param url            the url that carbon message have to be send.
     */
    void send(CarbonMessage carbonMessage, CarbonCallback carbonCallback, StringBuilder url) {
        if (paused) {
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while pausing ", ie);
                HttpSourceUtil.handleCallback("Internal Error", carbonCallback , 500);
            } finally {
                lock.unlock();
            }
        }
        try {
            if (!HttpConnectorRegistry.getInstance().isContainedListener(url.toString())) {
                throw new HttpSourceAdaptorRuntimeException("Resource not found.", carbonCallback, 404);
            } else {
                if (isAuthenticated) {
                    try {
                        HttpAuthenticator.authenticate(carbonMessage, carbonCallback);
                    } catch (HttpSourceAdaptorRuntimeException e) {
                        throw new HttpSourceAdaptorRuntimeException("Failed in isAuthenticated ",
                                e , carbonCallback, 401);
                    }
                }
                executorService.execute(new HttpWorkerThread(carbonMessage, carbonCallback,
                        sourceEventListener, sourceEventListener.getStreamDefinition().toString()));
            }
        } catch (RuntimeException e) {
            throw new HttpSourceAdaptorRuntimeException("Internal Error. Failed to process HTTP message.",
                    e , carbonCallback, 500);
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
