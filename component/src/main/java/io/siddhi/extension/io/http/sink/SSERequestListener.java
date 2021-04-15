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

import io.siddhi.extension.io.http.source.HttpAuthenticator;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
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
public class SSERequestListener {
    private static final Logger logger = LoggerFactory.getLogger(SSERequestListener.class);
    private ExecutorService executorService;
    private boolean paused;
    private ReentrantLock lock;
    private Condition condition;
    private String url;
    private Boolean isAuthEnabled;
    private String siddhiAppName;
    private String streamId;

    public SSERequestListener(int workerThread, String url, Boolean auth,
                              String streamId, String siddhiAppName) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.siddhiAppName = siddhiAppName;
        this.paused = false;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.url = url;
        this.isAuthEnabled = auth;
        this.streamId = streamId;
    }

    protected void send(HttpCarbonMessage carbonMessage) {
        if (isAuthEnabled) {
            if (!HttpAuthenticator.authenticate(carbonMessage)) {
                throw new HttpSourceAdaptorRuntimeException(carbonMessage, "Authorisation fails", 401);
            }
        }

        executorService.execute(new SSEWorkerThread(carbonMessage, streamId));
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    void disconnect() {
        executorService.shutdown();
    }
}
