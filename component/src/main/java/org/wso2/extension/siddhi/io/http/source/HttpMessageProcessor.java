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
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.extension.siddhi.io.http.source.auth.HttpAuthenticator;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code HttpMessageProcessor } Code is responsible for handling the http message which is sent by http carbon
 * transport.
 */
public class HttpMessageProcessor implements CarbonMessageProcessor {
    private ExecutorService executorService;
    private boolean paused;
    private ReentrantLock lock;
    private Condition condition;
    private static final Logger logger = LoggerFactory.getLogger(HttpMessageProcessor.class);
    private String id;

    HttpMessageProcessor(String id, String workerThread) {
        this.id = id;
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.executorService = Executors.newFixedThreadPool(Integer.valueOf(workerThread));
        logger.info("Message processor for url " + id + "has deployed.");
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws IOException {
        if (paused) {
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while pausing ", ie);
            } finally {
                lock.unlock();
            }
        }
        try {
            String channelID = (String) carbonMessage.getProperty(HttpConstants.CHANNEL_ID);
            //Check the channel type is http or https
            if ((channelID != null) && ((channelID.equals(HttpConstants.PROTOCOL_HTTP)) || (channelID.equals
                    (HttpConstants.PROTOCOL_HTTPS)))) {
                //Check the message is a response or direct message
                String direction = (String) carbonMessage.getProperty(Constants.DIRECTION);
                if (!((direction != null) && ((direction.equals(Constants.DIRECTION_RESPONSE))))) {
                    if (HttpConstants.HTTP_METHOD_POST.equalsIgnoreCase((String)
                            carbonMessage.getProperty((HttpConstants.HTTP_METHOD)))) {
                        //For Context Handling
                        StringBuilder url = new StringBuilder((String) carbonMessage.getProperty(HttpConstants.
                                LISTENER_INTERFACE_ID)).append(HttpConstants.PROTOCOL_HOST_SEPERATOR).
                                append(carbonMessage.getHeader(HttpConstants.CARBONMESSAGE_HOST))
                                .append(carbonMessage.getProperty(HttpConstants.TO));
                        if (!HttpSource.getRegisteredListenerURL().containsKey(url.toString())) {
                            throw new HttpSourceAdaptorRuntimeException("Resource not found.", carbonCallback, 404);
                        } else {
                            if (HttpSource.getRegisteredListenerAuthentication().get(HttpSource.
                                    getRegisteredListenerURL().get(url.toString()))) {
                                try {
                                    HttpAuthenticator.authenticate(carbonMessage, carbonCallback);
                                } catch (HttpSourceAdaptorRuntimeException e) {
                                    throw new HttpSourceAdaptorRuntimeException("Failed in authentication ",
                                            carbonCallback, 401, e);
                                }
                            }
                            executorService.execute(new HttpWorkerThread(carbonMessage, carbonCallback,
                                    HttpSource.getRegisteredListenerURL().get(url.toString()), HttpSource
                                    .getRegisteredListenerURL().get(url.toString()).getStreamDefinition().toString()));
                        }
                    } else {
                        throw new HttpSourceAdaptorRuntimeException("Request type is not a type of POST ",
                                carbonCallback, 400);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new HttpSourceAdaptorRuntimeException("Failed to process HTTP message.", carbonCallback,
                    500, e);
        }
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    public void setClientConnector(ClientConnector clientConnector) {
    }

    @Override
    public String getId() {
        return id + " HTTP-message-processor";
    }

    boolean isRunning() {
        return !paused;
    }

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
            logger.info("Event input has paused for " + id);
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
            logger.info("Event input has resume for " + id);
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
