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
package org.wso2.extension.siddhi.io.http.sink.util;

import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * {@code HttpConstants } Handling the setter and getter methods of truststore File and truststore Pass which is
 * currently private.
 */
public class SiddhiHttpSenderConfiguration extends SenderConfiguration {

    private String getTrustStoreFile() {
        return trustStoreFile;
    }

    private String getTrustStorePass() {
        return trustStorePass;
    }

    @XmlAttribute
    private String trustStoreFile;
    @XmlAttribute
    private String trustStorePass;

    SiddhiHttpSenderConfiguration(String trustStoreFile, String trustStorePass) {
        super();
        this.trustStoreFile = trustStoreFile;
        this.trustStorePass = trustStorePass;
    }

    @Override
    public SSLConfig getSslConfig() {
        return getScheme() != null && getScheme().equalsIgnoreCase(HttpConstants.PROTOCOL_HTTPS) ?
                org.wso2.carbon.transport.http.netty.common.Util
                        .getSSLConfigForSender(getCertPass(), getKeyStorePass(), getKeyStoreFile(),
                                getTrustStoreFile(), getTrustStorePass(), getParameters()) : null;
    }

}
