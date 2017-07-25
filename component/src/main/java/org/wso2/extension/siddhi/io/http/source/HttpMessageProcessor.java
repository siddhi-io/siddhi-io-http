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
package org.wso2.extension.siddhi.io.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;

import java.io.IOException;

/**
 * {@code HttpMessageProcessor } Code is responsible for handling the http message which is sent by http carbon
 * transport.
 */
public class HttpMessageProcessor implements CarbonMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HttpMessageProcessor.class);

    HttpMessageProcessor() { }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws IOException {
         //Check the channel type is http
         if (HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                 HttpConnectorRegistry.getInstance().getServerConnectorMap().containsKey(String.valueOf(carbonMessage
                         .getProperty(HttpConstants.LISTENER_PORT)))) {
             //Check the message is a response or direct message
             if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null &&
                     carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                             .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
                 carbonCallback.done(carbonMessage);
                 carbonMessage.release();
             } else {
                 if (HttpConstants.HTTP_METHOD_POST.equalsIgnoreCase((String)
                         carbonMessage.getProperty((HttpConstants.HTTP_METHOD)))) {
                     //get the required source listener
                     StringBuilder sourceListenerKey = new StringBuilder().append(String
                             .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)))
                             .append(HttpConstants.PORT_CONTEXT_KEY_SEPARATOR)
                             .append(carbonMessage.getProperty(HttpConstants.TO));
                     HttpSourceListener sourceListener = HttpConnectorRegistry.getInstance()
                             .getSourceListenersMap().get(sourceListenerKey.toString());
                     if (sourceListener != null) {
                         sourceListener.send(carbonMessage, carbonCallback);
                     } else {
                         HttpSourceUtil.handleCallback("Resource not found.", carbonCallback, 404);
                         carbonMessage.release();
                     }
                 } else {
                     throw new HttpSourceAdaptorRuntimeException(carbonMessage, "Request type is not a type of POST ",
                             carbonCallback, 400);
                 }
             }
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("Event is not type of http which has received to the uri '" + String
                         .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)) +
                         carbonMessage.getProperty(HttpConstants.TO));
             }
             HttpSourceUtil.handleCallback("Resource not found.", carbonCallback, 404);
             carbonMessage.release();
         }
        return false;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
    }

    @Override
    public String getId() {
        return HttpConstants.MESSAGEPROCESSOR_ID_DEFAULT;
    }
}
