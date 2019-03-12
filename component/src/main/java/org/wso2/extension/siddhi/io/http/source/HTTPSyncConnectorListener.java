/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * HTTP connector listener for Siddhi.
 */
public class HTTPSyncConnectorListener extends HTTPConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(HTTPSyncConnectorListener.class);

    protected boolean isValidRequest(HttpCarbonMessage carbonMessage) {

        return HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                HttpSyncConnectorRegistry.getInstance().getServerConnectorPool().containsKey(getInterface
                        (carbonMessage));
    }

    protected HttpSourceListener getSourceListener(StringBuilder sourceListenerKey) {

        return HttpSyncConnectorRegistry.getInstance().getSyncSourceListenersMap().get(sourceListenerKey.toString());
    }

}
