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
package org.wso2.extension.siddhi.io.http.source.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the http source util functions.
 */
public class HttpSourceUtil {
    private static final Logger log = Logger.getLogger(HttpSourceUtil.class);

    public HttpSourceUtil() {
    }

    /**
     * Method is responsible for set transportation configuration values.
     *
     * @return return the set of netty transportation configuration.
     */
    public Set<TransportProperty> getTransportConfigurations(ConfigReader sourceConfigReader, String
            serverBootstrapWorkerThread, String serverBootstrapBossThread) {
        ArrayList<TransportProperty> properties = new ArrayList<>();
        TransportProperty var = new TransportProperty();
        var.setName(HttpConstants.LATENCY_METRICS);
        var.setValue(sourceConfigReader.readConfig(HttpConstants.LATENCY_METRICS,
                HttpConstants.LATENCY_METRICS_VALUE));
        properties.add(var);
        var = new TransportProperty();
        var.setName(HttpConstants.SERVER_BOOTSTRAP_SOCKET_TIMEOUT);
        var.setValue(Integer.valueOf(sourceConfigReader.readConfig(HttpConstants.SERVER_BOOTSTRAP_SOCKET_TIMEOUT,
                HttpConstants.SERVER_BOOTSTRAP_SOCKET_TIMEOUT_VALUE)));
        properties.add(var);
        var = new TransportProperty();
        var.setName(HttpConstants.CLIENT_BOOTSTRAP_SOCKET_TIMEOUT);
        var.setValue(Integer.valueOf(sourceConfigReader.readConfig(HttpConstants.CLIENT_BOOTSTRAP_SOCKET_TIMEOUT,
                HttpConstants.CLIENT_BOOTSTRAP_SOCKET_TIMEOUT_VALUE)));
        properties.add(var);
        if (!HttpConstants.EMPTY_STRING.equals(serverBootstrapBossThread)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE);
            var.setValue(Integer.valueOf(serverBootstrapBossThread));
            properties.add(var);
        }
        if (!HttpConstants.EMPTY_STRING.equals(serverBootstrapWorkerThread)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE);
            var.setValue(Integer.valueOf(serverBootstrapWorkerThread));
            properties.add(var);
        }
        return new HashSet<>(properties);
    }

    /**
     * set http listener properties.
     */
    public Object[] setListenerProperty(String listenerUrl, ConfigReader sourceConfigReader) {
        //Decoding parameters
        String protocol;
        String host;
        int port;
        String context = "";
        ListenerConfiguration listenerConfig = new ListenerConfiguration();
        try {
            URL aURL = new URL(listenerUrl);
            protocol = aURL.getProtocol();
            host = aURL.getHost();
            port = (aURL.getPort());
            context = aURL.getPath();
            switch (protocol) {
                case HttpConstants.PROTOCOL_HTTP:
                    listenerConfig = new ListenerConfiguration(HttpConstants.PROTOCOL_HTTP, host, port);
                    listenerConfig.setScheme(protocol);
                    break;
                case HttpConstants.PROTOCOL_HTTPS:
                    listenerConfig = new ListenerConfiguration(HttpConstants.PROTOCOL_HTTPS, host, port);
                    listenerConfig.setScheme(protocol);
                    listenerConfig.setKeyStoreFile(sourceConfigReader.readConfig(HttpConstants.KEYSTORE_FILE,
                            HttpConstants.KEYSTORE_FILE_VALUE));
                    listenerConfig.setKeyStorePass(sourceConfigReader.readConfig(HttpConstants.KEYSTORE_PASS,
                            HttpConstants.KEYSTORE_PASS_VALUE));
                    listenerConfig.setCertPass(sourceConfigReader.readConfig(HttpConstants.CERT_PASS,
                            HttpConstants.CERT_PASS_VALUE));
                    break;
                default:
                    throw new HttpSourceAdaptorRuntimeException("Invalid protocol " + protocol);
            }
        } catch (MalformedURLException e) {
            log.error("Receiver url malformed." + listenerUrl);
        }
        return new Object[]{listenerConfig, context};
    }
}

