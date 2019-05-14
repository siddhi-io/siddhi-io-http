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
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.UUID;

/**
 * {@code HttpSourceListener } This class maintain the states of each and every source listener which are created
 * such as is currently paused or not,  need isAuthEnabled or not.
 */
public class HttpSyncSourceListener extends HttpSourceListener {

    private static final Logger logger = LoggerFactory.getLogger(HttpSyncSourceListener.class);
    private String sourceId;

    protected HttpSyncSourceListener(int workerThread, String url, Boolean auth,
                                     SourceEventListener sourceEventListener,
                                     String[] requestedTransportPropertyNames,
                                     String sourceId, String siddhiAppName) {

        super(workerThread, url, auth, sourceEventListener, requestedTransportPropertyNames, siddhiAppName);
        this.sourceId = sourceId;
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
        String messageId = UUID.randomUUID().toString();
        populateTransportProperties(trpProperties, messageId);
        executorService.execute(new HttpSyncWorkerThread(carbonMessage,
                sourceEventListener, sourceEventListener.getStreamDefinition().toString(), trpProperties,
                sourceId, messageId));

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

}
