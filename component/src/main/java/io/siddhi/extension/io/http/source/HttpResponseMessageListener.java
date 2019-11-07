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

import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.extension.io.http.sink.HttpSink;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Connector Listener for HttpResponseSource.
 * This receives responses for the requests sent by corresponding http-request sink and forward them to the
 * http-responses sources considering their http status code.
 */
public class HttpResponseMessageListener implements HttpConnectorListener {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseMessageListener.class);

    private Map<String, Object> trpProperties;
    private boolean isDownloadEnabled;
    private String sinkId;
    private HttpCarbonMessage carbonMessages;
    private CountDownLatch latch;
    private HttpSink sink;
    private final Object payload;
    private final DynamicOptions dynamicOptions;
    private String siddhiAppName;
    private String publisherURL;

    public HttpResponseMessageListener(HttpSink sink, Map<String, Object> trpProperties, String sinkId,
                                       boolean isDownloadEnabled, CountDownLatch latch,
                                       Object payload, DynamicOptions dynamicOptions,
                                       String siddhiAppName, String publisherURL) {
        this.trpProperties = trpProperties;
        this.isDownloadEnabled = isDownloadEnabled;
        this.sinkId = sinkId;
        this.latch = latch;
        this.sink = sink;
        this.payload = payload;
        this.dynamicOptions = dynamicOptions;
        this.siddhiAppName = siddhiAppName;
        this.publisherURL = publisherURL;
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        trpProperties.forEach((k, v) -> {
            carbonMessage.setProperty(k, v);
        });
        carbonMessage.setProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT, isDownloadEnabled);
        this.carbonMessages = carbonMessage;
        if (latch != null) {
            latch.countDown();
        }
        String statusCode = Integer.toString(carbonMessage.getNettyHttpResponse().status().code());
        HttpCallResponseSource responseSource = HTTPSourceRegistry.findAndGetResponseSource(sinkId, statusCode);
        if (responseSource != null) {
            HttpCallResponseConnectorListener responseConnectorListener = responseSource.getConnectorListener();
            responseConnectorListener.onMessage(carbonMessage);
        } else {
            log.error("No source of type 'http-call-response' with sink.id '" + sinkId +
                    "' for the status code '" + statusCode +
                    "' defined. Hence dropping the response message.");
        }

    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof IOException) {
            sink.createClientConnector(null);
        }
        if (latch != null) {
            latch.countDown();
        }
        if (throwable instanceof ClientConnectorException || throwable instanceof ServerConnectorException) {
            sink.onError(payload, dynamicOptions, new ConnectionUnavailableException(
                    "HTTP call sink on stream '" + sink.getStreamDefinition().getId() +
                            "' of Siddhi App '" + siddhiAppName +
                            "' failed to publish events to endpoint '" + publisherURL + "'. " +
                            throwable.getMessage(), throwable));
        } else {
            sink.onError(payload, dynamicOptions,
                    new SiddhiAppRuntimeException("HTTP call sink on stream '" +
                            sink.getStreamDefinition().getId() +
                            "' of Siddhi App '" + siddhiAppName +
                            "' failed to publish events to endpoint '" + publisherURL + "'. " +
                            throwable.getMessage(), throwable));
        }
    }

    public int getHttpResponseStatusCode() {
        if (carbonMessages != null) {
            return carbonMessages.getNettyHttpResponse().status().code();
        } else {
            return HttpConstants.CLIENT_REQUEST_TIMEOUT;
        }
    }

}
