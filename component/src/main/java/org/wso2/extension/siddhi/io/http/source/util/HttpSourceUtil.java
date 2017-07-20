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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
    public Set<TransportProperty> getTransportConfigurations(ConfigReader sourceConfigReader) {
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
        String bootstrapBoss = sourceConfigReader.readConfig(HttpConstants
                .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        if (!HttpConstants.EMPTY_STRING.equals(bootstrapBoss)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE);
            var.setValue(Integer.valueOf(bootstrapBoss));
            properties.add(var);
        }
        String bootstrapWorker = sourceConfigReader.readConfig(HttpConstants
                .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        if (!HttpConstants.EMPTY_STRING.equals(bootstrapWorker)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE);
            var.setValue(Integer.valueOf(bootstrapWorker));
            properties.add(var);
        }
        return new HashSet<>(properties);
    }

    /**
     * Key value generated for Source Listener using host and port.
     * @param listenerUrl  the listener URL
     * @return key value for source Listener
     */
    public static String getSourceListenerKey(String listenerUrl) {
        URL aURL;
        try {
            aURL = new URL(listenerUrl);
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("ListenerUrl is not in a proper format ", e);
        }
        return String.valueOf(aURL.getPort()) + HttpConstants.PORT_CONTEXT_KEY_SEPARATOR + aURL.getPath();
    }

    /**
     * Get port from listenerUrl.
     * @param listenerUrl the listener URL.
     * @return port of the listener URL.
     */
    public static String getPort(String listenerUrl) {
        URL aURL;
        try {
            aURL = new URL(listenerUrl);
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("ListenerUrl is not in a proper format ", e);
        }
        return String.valueOf(aURL.getPort());
    }

    /**
     * Set Listener Configuration from given url.
     * @param listenerUrl the listenerUrl of source
     * @param sourceConfigReader the config reader of source
     * @return listener configuration.
     */
    public ListenerConfiguration setListenerProperty(String listenerUrl, ConfigReader sourceConfigReader) {
        //Decoding parameters
        String protocol;
        String host;
        int port;
        ListenerConfiguration listenerConfig = new ListenerConfiguration();
        try {
            URL aURL = new URL(listenerUrl);
            protocol = aURL.getProtocol();
            host = aURL.getHost();
            port = (aURL.getPort());
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
                    listenerConfig.setKeyStorePass(sourceConfigReader.readConfig(HttpConstants.KEYSTORE_PASSWORD,
                            HttpConstants.KEYSTORE_PASSWORD_VALUE));
                    listenerConfig.setCertPass(sourceConfigReader.readConfig(HttpConstants.CERT_PASSWORD,
                            HttpConstants.CERT_PASSWORD_VALUE));
                    break;
                default:
                    throw new HttpSourceAdaptorRuntimeException("Invalid protocol " + protocol);
            }
        } catch (MalformedURLException e) {
            log.error("Receiver url malformed." + listenerUrl);
        }
        return  listenerConfig;
    }

    /**
     * This method handle the response including the status of request.
     * @param message the carbon message.
     * @param carbonCallback the carbon callback that should send the status.
     * @param code the http status code.
     */
    public static void handleCallback(String message, CarbonCallback carbonCallback, int
            code) {
        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        defaultCarbonMessage.setStringMessageBody(message);
        defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, (code));
        defaultCarbonMessage.setProperty(Constants.HTTP_REASON_PHRASE, HttpResponseStatus.valueOf(code).reasonPhrase());
        defaultCarbonMessage.setHeader(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        defaultCarbonMessage.setHeader(Constants.HTTP_VERSION, HTTP_1_1.text());
        defaultCarbonMessage.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        defaultCarbonMessage.setEndOfMsgAdded(true);
        carbonCallback.done(defaultCarbonMessage);
    }
}

