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

import io.siddhi.extension.io.http.sink.HttpSink;
import io.siddhi.extension.io.http.util.HTTPSourceRegistry;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Connector Listener for HttpResponseSource.
 * This receives responses for the requests sent by corresponding http-request sink and forward them to the
 * http-responses sources considering their http status code.
 */
public class HttpResponseMessageListener implements HttpConnectorListener {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseMessageListener.class);

    private HttpCallResponseConnectorListener responseConnectorListener;
    private Map<String, Object> trpProperties;
    private boolean isDownloadEnabled;
    private String sinkId;
    private HttpCarbonMessage carbonMessages;
    private CountDownLatch latch;
    private int tryCount;
    private String authType;
    private boolean isBlockingIO;
    private HttpSink sink;

    public HttpResponseMessageListener(HttpSink sink, Map<String, Object> trpProperties, String sinkId,
                                       boolean isDownloadEnabled, CountDownLatch latch, int tryCount, String authType,
                                       boolean isBlockingIO) {
        this.trpProperties = trpProperties;
        this.isDownloadEnabled = isDownloadEnabled;
        this.sinkId = sinkId;
        this.latch = latch;
        this.tryCount = tryCount;
        this.authType = authType;
        this.isBlockingIO = isBlockingIO;
        this.sink = sink;
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        trpProperties.forEach((k, v) -> {
            carbonMessage.setProperty(k, v);
        });
        carbonMessage.setProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT, isDownloadEnabled);
        this.carbonMessages = carbonMessage;
        String statusCode = Integer.toString(carbonMessage.getNettyHttpResponse().status().code());
        if (carbonMessage.getNettyHttpResponse().status().code() == (HttpConstants.SUCCESS_CODE) ||
                HttpConstants.MAXIMUM_TRY_COUNT == tryCount) {
            List<HttpCallResponseSource> result = HTTPSourceRegistry.getInstance()
                    .getCallResponseSourcesMatchingHttpCodeRegex(sinkId, statusCode);
            if (result.size() != 0) {
                for (HttpCallResponseSource responseSource : result) {
                    responseSource.getConnectorListener().onMessage(carbonMessage);
                }
            } else {
                log.error("No source of type 'http-response' that matches with the status code '" + statusCode +
                        "' has been defined. Hence dropping the response message.");
            }
        }
        if (isBlockingIO || HttpConstants.OAUTH.equals(authType)) {
            latch.countDown();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof IOException) {
            sink.initClientConnector(null);
        }

        HttpCallResponseSource source = HTTPSourceRegistry.getInstance().getCallResponseSource(sinkId,
                HttpConstants.DEFAULT_HTTP_ERROR_CODE);
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

    public HttpCarbonMessage getHttpResponseMessage() {
        return carbonMessages;
    }

}
