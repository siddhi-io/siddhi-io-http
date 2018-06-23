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

import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Map;

/**
 * Connector Listener for HttpResponseSource
 */
public class HttpResponseMessageListener implements HttpConnectorListener {
    private HttpResponseConnectorListener responseConnectorListener;
    private Map<String, Object> trpProperties;
    private boolean isDownloadEnabled;

    public HttpResponseMessageListener(Map<String, Object> trpProperties, HttpResponseSource source,
                                       boolean isDownloadEnabled) {
        this.responseConnectorListener = source.getConnectorListener();
        this.trpProperties = trpProperties;
        this.isDownloadEnabled = isDownloadEnabled;
    }

    @Override
    public void onMessage(HTTPCarbonMessage carbonMessage) {
        trpProperties.forEach((k, v) -> { carbonMessage.setProperty(k, v); });
        carbonMessage.setProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT, isDownloadEnabled);
        responseConnectorListener.onMessage(carbonMessage);
    }

    @Override
    public void onError(Throwable throwable) {
        responseConnectorListener.onError(throwable);
    }

    /**
     * Disconnect pool execution.
     */
    void disconnect() {
        responseConnectorListener.disconnect();
    }
}
