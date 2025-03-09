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

import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * This class will synchronously listen to the connections.
 */
public class SSESyncConnectorListener extends SSEConnectorListener {

    private static final Logger log = LogManager.getLogger(SSESyncConnectorListener.class);

    protected boolean isValidRequest(HttpCarbonMessage carbonMessage) {
        return HttpConstants.PROTOCOL_ID.equals(carbonMessage.getProperty(HttpConstants.PROTOCOL)) &&
                SSESyncConnectorRegistry.getInstance().getServerConnectorPool().containsKey(getInterface
                        (carbonMessage));
    }

    protected SSERequestListener getSourceListener(StringBuilder sourceListenerKey) {

        return SSESyncConnectorRegistry.getInstance().getSyncSourceListenersMap().get(sourceListenerKey.toString());
    }
}
