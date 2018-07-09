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
import org.wso2.extension.siddhi.io.http.util.HTTPSourceRegistry;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.extension.siddhi.io.http.util.ResponseSourceId;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Map;

/**
 * Connector Listener for HttpResponseSource.
 * This receives responses for the requests sent by corresponding http-request sink and forward them to the
 * http-responses sources considering their http status code.
 */
public class HttpResponseMessageListener implements HttpConnectorListener {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseMessageListener.class);

    private HttpResponseConnectorListener responseConnectorListener;
    private Map<String, Object> trpProperties;
    private boolean isDownloadEnabled;
    private String sinkId;

    public HttpResponseMessageListener(Map<String, Object> trpProperties, String sinkId, boolean isDownloadEnabled) {
        this.trpProperties = trpProperties;
        this.isDownloadEnabled = isDownloadEnabled;
        this.sinkId = sinkId;
    }

    @Override
    public void onMessage(HTTPCarbonMessage carbonMessage) {
        trpProperties.forEach((k, v) -> { carbonMessage.setProperty(k, v); });
        carbonMessage.setProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT, isDownloadEnabled);

        String statusCode = Integer.toString(carbonMessage.getNettyHttpResponse().status().code());
        HttpResponseSource responseSource = findAndGetResponseSource(statusCode);
        if (responseSource != null) {
            responseConnectorListener = responseSource.getConnectorListener();
            responseConnectorListener.onMessage(carbonMessage);
        } else {
            log.error("No source of type 'http-response' that matches with the status code '" + statusCode + "' has " +
                    "been defined. Hence dropping the response message.");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        HttpResponseSource source = HTTPSourceRegistry.getResponseSource(sinkId, HttpConstants.DEFAULT_HTTP_ERROR_CODE);
        if (source != null) {
            responseConnectorListener = source.getConnectorListener();
        } else {
            log.error("No source of type 'http-response' for status code '500' has been " +
                    "defined. Hence dropping the response message.");
        }
        if (responseConnectorListener != null) {
            responseConnectorListener.onError(throwable);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No connector listener for the response source with sink id '" + sinkId + "' and http " +
                        "status code 500 found.");
            }
        }
    }

    /**
     * Disconnect pool execution.
     */
    void disconnect() {
        responseConnectorListener.disconnect();
    }

    private HttpResponseSource findAndGetResponseSource(String statusCode) {
        ResponseSourceId id = new ResponseSourceId(sinkId, statusCode);
        for (Map.Entry entry : HTTPSourceRegistry.getResponseSourceRegistry().entrySet()) {
            ResponseSourceId key = (ResponseSourceId) entry.getKey();
            if (id.equals(key)) {
                return (HttpResponseSource) entry.getValue();
            }
        }
        return null;
    }

}
