/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package io.siddhi.extension.io.http.source;

import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ClientClosedConnectionException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * HTTP connector listener for Siddhi.
 */
public class HTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(HTTPConnectorListener.class);
    private TransportsConfiguration configuration;
    private HttpClientConnector clientConnector;

    public HTTPConnectorListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    public HTTPConnectorListener() {
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        if (isValidRequest(carbonMessage)) {
            //Check the message is a response or direct message
            if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null &&
                    carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                            .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
                try {
                    carbonMessage.respond(carbonMessage);
                } catch (ServerConnectorException e) {
                    log.error("Error occurred during message notification: " + e.getMessage(), e);
                }
            } else {
                if (HttpConstants.HTTP_METHOD_POST.equalsIgnoreCase(carbonMessage.getHttpMethod())) {
                    //get the required source listener
                    StringBuilder sourceListenerKey = new StringBuilder().append(String
                            .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)))
                            .append(HttpConstants.PORT_CONTEXT_KEY_SEPARATOR)
                            .append(carbonMessage.getProperty(HttpConstants.TO));
                    HttpSourceListener sourceListener = getSourceListener(sourceListenerKey);
                    if (sourceListener != null) {
                        sourceListener.send(carbonMessage);
                    } else {
                        HttpSourceUtil.handleCallback(carbonMessage, 404);
                    }
                } else if (HttpConstants.HTTP_METHOD_OPTIONS.equalsIgnoreCase(carbonMessage.getHttpMethod())) {
                    HttpSourceUtil.handleCORS(carbonMessage);
                } else {
                    throw new HttpSourceAdaptorRuntimeException(carbonMessage, "Request type is not a type of POST ",
                            400);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Event is not type of http which has received to the uri '" + String
                        .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)) +
                        carbonMessage.getProperty(HttpConstants.TO));
            }
            HttpSourceUtil.handleCallback(carbonMessage, 404);
        }
    }

    protected boolean isValidRequest(HttpCarbonMessage carbonMessage) {

        return HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                HttpConnectorRegistry.getInstance().getServerConnectorPool().containsKey(getInterface(carbonMessage));
    }

    protected HttpSourceListener getSourceListener(StringBuilder sourceListenerKey) {

        return HttpConnectorRegistry.getInstance().getSourceListenersMap().get(sourceListenerKey.toString());
    }

    protected String getInterface(HttpCarbonMessage cMsg) {
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
        if (throwable instanceof ClientClosedConnectionException) {
            log.debug("Error in http server connector", throwable);
        } else {
            log.error("Error in http server connector", throwable);
        }
    }
}
