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
package io.siddhi.extension.io.http.source;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.siddhi.extension.io.http.source.internal.HttpIODataHolder;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static io.siddhi.extension.io.http.util.HttpConstants.CREDENTIAL_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpConstants.EMPTY_STRING;

/**
 * Basic authentication handler of http io implementation.
 */
public class HttpAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(HttpAuthenticator.class);

    public static boolean authenticate(HttpCarbonMessage httpCarbonMessage) {
        if (HttpIODataHolder.getInstance().getBundleContext() == null) {
            //this will handle the events at non osgi mode.
            return true;
        } else {
            String authHeader = httpCarbonMessage.getHeaders().get(HttpConstants.AUTHORIZATION_HEADER);
            if (authHeader != null) {
                String usernamePasswordEncoded = authHeader.replace(HttpConstants.AUTHORIZATION_METHOD, EMPTY_STRING);
                ByteBuf usernamePasswordBuf = Base64.decode(Unpooled.copiedBuffer(usernamePasswordEncoded.getBytes
                        (Charset.defaultCharset())));
                String[] credentials = usernamePasswordBuf.toString(Charset.defaultCharset())
                        .split(CREDENTIAL_SEPARATOR);
                IdPClient idPClient = HttpIODataHolder.getInstance().getClient();
                if ((idPClient != null) && (credentials.length == 2)) {
                    try {
                        Map<String, String> loginProperties = new HashMap<>();
                        loginProperties.put(IdPClientConstants.USERNAME, credentials[0]);
                        loginProperties.put(IdPClientConstants.PASSWORD, credentials[1]);
                        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
                        Map<String, String> login = idPClient.login(loginProperties);
                        String loginStatus = login.get(IdPClientConstants.LOGIN_STATUS);
                        if (loginStatus.equals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS)) {
                            return true;
                        } else {
                            logger.error("Authentication failed for username '" + credentials[0] + "'. Error : '"
                                    + login.get(IdPClientConstants.ERROR) + "'. Error Description : '"
                                    + login.get(IdPClientConstants.ERROR_DESCRIPTION) + "'");
                            return false;
                        }
                    } catch (IdPClientException e) {
                        logger.error("Authorization process fails for user '" + credentials[0] + "'", e);
                        return false;
                    }
                } else {
                    logger.error("Authorization header in incorrect format. header: " + usernamePasswordEncoded);
                    return false;
                }
            } else {
                logger.error("Authorization header 'null' ");
                return false;
            }
        }
    }

}
