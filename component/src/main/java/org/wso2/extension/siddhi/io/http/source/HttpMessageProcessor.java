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
import org.wso2.carbon.messaging.Constants;
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
        if (HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL))) {
                //Check the message is a response or direct message
            if (!Constants.DIRECTION_RESPONSE.equals(carbonMessage.getProperty(Constants.DIRECTION))) {
                    if (HttpConstants.HTTP_METHOD_POST.equalsIgnoreCase((String)
                            carbonMessage.getProperty((HttpConstants.HTTP_METHOD)))) {
                        //get the required source listener
                        StringBuilder url = new StringBuilder(String.valueOf(carbonMessage.getProperty
                                (HttpConstants.LISTENER_PORT))).append(HttpConstants.PORT_CONTEXT_KEY_SEPARATOR)
                                .append(carbonMessage.getProperty(HttpConstants.TO));
                        HttpSourceListener sourceListener = HttpSource.getRegisteredSourceListenersMap().get(url
                                .toString());
                        if (sourceListener != null) {
                            HttpSource.getRegisteredSourceListenersMap().get(url.toString()).send(carbonMessage,
                                    carbonCallback, url);
                        } else {
                            HttpSourceUtil.handleCallback("Resource not found ", carbonCallback, 404);
                        }
                    } else {
                        throw new HttpSourceAdaptorRuntimeException("Request type is not a type of POST ",
                                carbonCallback, 400);
                    }
            } else {
                carbonCallback.done(carbonMessage);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is not type of http which has received to the uri '" + String
                        .valueOf(carbonMessage.getProperty(HttpConstants.LISTENER_PORT)) +
                        carbonMessage.getProperty(HttpConstants.TO));
            }
        }
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
    }

    @Override
    public String getId() {
        return HttpConstants.MESSAGEPROCESSOR_ID;
    }
}
