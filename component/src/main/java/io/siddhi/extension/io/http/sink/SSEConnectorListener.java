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

import io.siddhi.extension.io.http.source.HTTPConnectorListener;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.exceptions.ClientClosedConnectionException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * This class is responsible for listening to SSE connection.
 */
public class SSEConnectorListener implements HttpConnectorListener {

    private static final Logger log = LogManager.getLogger(HTTPConnectorListener.class);

    public SSEConnectorListener() {
    }

    @Override
    public void onMessage(HttpCarbonMessage carbonMessage) {
        try {
            if (isValidRequest(carbonMessage)) {
                //Check the message type to match GET or POST
                if (HttpConstants.HTTP_METHOD_GET.equalsIgnoreCase(carbonMessage.getHttpMethod())
                        || HttpConstants.HTTP_METHOD_POST.equalsIgnoreCase(carbonMessage.getHttpMethod())) {
                    StringBuilder sourceListenerKey = new StringBuilder().append(String
                            .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)))
                            .append(HttpConstants.PORT_CONTEXT_KEY_SEPARATOR)
                            .append(carbonMessage.getProperty(HttpConstants.TO));
                    SSERequestListener requestListener = getSourceListener(sourceListenerKey);
                    if (requestListener != null) {
                        requestListener.send(carbonMessage);
                    } else {
                        HttpSourceUtil.handleCallback(carbonMessage, 404);
                    }
                } else if (HttpConstants.HTTP_METHOD_OPTIONS.equalsIgnoreCase(carbonMessage.getHttpMethod())) {
                    HttpSourceUtil.handleCORS(carbonMessage);
                } else {
                    throw new HttpSourceAdaptorRuntimeException(carbonMessage,
                            "Request type is not a type of GET or POST ", 400);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Event is not type of http which has received to the uri '{}{}",
                            carbonMessage.getProperty(HttpConstants.LISTENER_PORT),
                            carbonMessage.getProperty(HttpConstants.TO));
                }
                HttpSourceUtil.handleCallback(carbonMessage, 404);
            }
        } finally {
            carbonMessage.waitAndReleaseAllEntities();
        }
    }

    protected boolean isValidRequest(HttpCarbonMessage carbonMessage) {

        return HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                SSEConnectorRegistry.getInstance().getServerConnectorPool().containsKey(getInterface(carbonMessage));
    }

    protected SSERequestListener getSourceListener(StringBuilder sourceListenerKey) {

        return SSEConnectorRegistry.getInstance().getSourceListenersMap().get(sourceListenerKey.toString());
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
