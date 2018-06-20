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

package org.wso2.extension.siddhi.io.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Connector Listener for HttpResponseSource
 */
public class HttpResponseConnectorListener implements HttpConnectorListener {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseConnectorListener.class);
    private HttpWorkerThread httpWorkerThread;
    private SourceEventListener sourceEventListener;
    private String sourceId;
    private ExecutorService executorService;
    private String siddhiAppName;

    public HttpResponseConnectorListener(int numnerOfThreads, SourceEventListener sourceEventListener, String sourceId,
                                         String[] trpPropertyNames, String siddhiAppName) {
        this.sourceEventListener = sourceEventListener;
        this.sourceId = sourceId;
        this.executorService = Executors.newFixedThreadPool(numnerOfThreads);
        this.siddhiAppName = siddhiAppName;
    }

    @Override
    public void onMessage(HTTPCarbonMessage carbonMessage) {
        HttpWorkerThread workerThread = new HttpWorkerThread(carbonMessage, sourceEventListener, sourceId, null);
        executorService.execute(workerThread);
    }

    protected boolean isValidRequest(HTTPCarbonMessage carbonMessage) {

        return HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                HttpConnectorRegistry.getInstance().getServerConnectorPool().containsKey(getInterface(carbonMessage));
    }

    protected HttpSourceListener getSourceListener(StringBuilder sourceListenerKey) {

        return HttpConnectorRegistry.getInstance().getSourceListenersMap().get(sourceListenerKey.toString());
    }

    protected String getInterface(HTTPCarbonMessage cMsg) {
        String interfaceId = (String) cMsg.getProperty(Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = HttpConstants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http server connector", throwable);
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    /**
     * Disconnect pool execution.
     */
    void disconnect() {
        executorService.shutdown();
    }
}
