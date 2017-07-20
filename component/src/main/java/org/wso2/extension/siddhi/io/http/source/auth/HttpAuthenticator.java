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
package org.wso2.extension.siddhi.io.http.source.auth;


import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * {@code HttpAuthenticator}Handling the basic authentication.
 */
public class HttpAuthenticator {

    private HttpAuthenticator() {
    }

    /**
     * Method is used to authenticate http message with basic authentication.
     *
     * @param carbonMessage  carbon message which send through carbon transport.
     * @param carbonCallback callback send through carbon transport which is used to send status of http request.
     * @throws HttpSourceAdaptorRuntimeException if authorization header is missing or if user name and password is
     *                                           incorrect
     */
    public static synchronized void authenticate(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws
            HttpSourceAdaptorRuntimeException {
        if (carbonMessage.getHeaders().contains(HttpConstants.ATHORIZATION_HEADER)) {
            ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
            LoginContext loginContext;
            try {
                loginContext = new LoginContext(HttpConstants.CARBON_SECURITY_CONFIGURATION, callbackHandler);
                loginContext.login();
            } catch (LoginException e) {
                carbonMessage.release();
                throw new HttpSourceAdaptorRuntimeException("Username and password is not included when trying to " +
                        "Authentication fail ", e, carbonCallback, 401);
            }
        } else {
            carbonMessage.release();
            throw new HttpSourceAdaptorRuntimeException(carbonMessage , "Authentication header not found when trying " +
                    "to " +
                    "authenticate with basic authentication ", carbonCallback, 401);
        }
    }
}
