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

package org.wso2.siddhi.extension.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.siddhi.extension.http.source.auth.HttpAuthenticator;
import org.wso2.siddhi.extension.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.siddhi.extension.http.source.util.HttpPausableThreadPoolExecutor;
import org.wso2.siddhi.extension.http.util.HttpConstants;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@code HttpMessageProcessor } Code is responsible for handling the http message which is sent by http carbon
 * transport.
 */
public class HttpMessageProcessor implements CarbonMessageProcessor {
    private HttpPausableThreadPoolExecutor executorService;
    //TODO:From Configuration
    private static final long KEEP_ALIVE_TIME = 10;
    private static final int MAX_THREAD_POOL_SIZE_MULTIPLIER = 2;
    private static final int DEFAULT_THREAD_POOL_SIZE = 1;
    private static final Logger logger = LoggerFactory.getLogger(HttpMessageProcessor.class);
    private String id;

    HttpMessageProcessor(String id) {
        this.id = id;
        int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        int maxThreadPoolSize = MAX_THREAD_POOL_SIZE_MULTIPLIER * threadPoolSize;
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        this.executorService = new HttpPausableThreadPoolExecutor(threadPoolSize, maxThreadPoolSize, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, queue);
        logger.info("Message processor for url " + id + "has deployed.");
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws IOException {
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
                            LISTENER_INTERFACE_ID)).append(HttpConstants.HTTP_ADDRESS_COMPONENT).
                            append(carbonMessage.getHeader(HttpConstants.CARBONMESSAGE_HOST))
                            .append(carbonMessage.getProperty(HttpConstants.TO));
                    if (!HttpSource.getRegisteredListenerURL().containsKey(url.toString())) {
                        throw new HttpSourceAdaptorRuntimeException("Resource not found.", carbonCallback, 404);
                    } else {
                        if (HttpSource.getRegisteredListenerAuthentication().get(HttpSource.getRegisteredListenerURL().
                                get(url.toString()))) {
                            try {
                                HttpAuthenticator.authenticate(carbonMessage, carbonCallback);
                            } catch (HttpSourceAdaptorRuntimeException e) {
                                throw new HttpSourceAdaptorRuntimeException("Failed in authentication ", carbonCallback,
                                        401);
                            }
                        }
                        executorService.execute(new HttpWorkerThread(carbonMessage, carbonCallback,
                                HttpSource.getRegisteredListenerURL().get(url.toString()), HttpSource
                                .getRegisteredListenerURL().get(url.toString()).getStreamDefinition().
                                        toString()));
                    }
                } else {
                    throw new HttpSourceAdaptorRuntimeException("Request type is not a type of POST ", carbonCallback,
                            400);
                }
            }
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

    void pause() {
        if (!executorService.isPaused()) {
            executorService.pause();
        }
    }

    void resume() {
        if (!executorService.isRunning()) {
            executorService.resume();
        }
    }


    void disconnect() {
        executorService.shutdown();
    }
}
