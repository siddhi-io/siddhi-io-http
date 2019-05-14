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
package io.siddhi.extension.io.http.source.util;

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.io.http.util.HttpIoUtil;
import io.siddhi.extension.io.http.util.TrpPropertyTypes;
import org.apache.log4j.Logger;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportProperty;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_SIZE_VALIDATION;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_MAXIMUM_CHUNK_SIZE;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_MAXIMUM_REQUEST_LINE;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_MAXIMUM_SIZE;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_REJECT_MESSAGE;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE;
import static io.siddhi.extension.io.http.util.HttpConstants.HEADER_VALIDATION_REJECT_STATUS_CODE;
import static io.siddhi.extension.io.http.util.HttpConstants.LATENCY_METRICS_ENABLED;
import static io.siddhi.extension.io.http.util.HttpConstants.PORT_HOST_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpConstants.REQUEST_SIZE_VALIDATION;
import static io.siddhi.extension.io.http.util.HttpConstants.REQUEST_SIZE_VALIDATION_MAXIMUM_VALUE;
import static io.siddhi.extension.io.http.util.HttpConstants.REQUEST_SIZE_VALIDATION_REJECT_MESSAGE;
import static io.siddhi.extension.io.http.util.HttpConstants.REQUEST_SIZE_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE;
import static io.siddhi.extension.io.http.util.HttpConstants.REQUEST_SIZE_VALIDATION_REJECT_STATUS_CODE;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_CONNECT_TIMEOUT_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_KEEPALIVE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_NODELAY_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_RECIEVEBUFFERSIZE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_SENDBUFFERSIZE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_SOCKET_BACKLOG_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_SOCKET_REUSE_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_SOCKET_TIMEOUT_PARAM;
import static io.siddhi.extension.io.http.util.HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE_PARAM;

/**
 * Handles the http source util functions.
 */
public class HttpSourceUtil {
    private static final Logger log = Logger.getLogger(HttpSourceUtil.class);

    private HttpSourceUtil() {
    }

    /**
     * Populate transport properties.
     *
     * @param serverHeaderValidationList header validation properties
     * @param transportProperties        bootstrap transport properties.
     * @return transport property map
     */
    public static Set<TransportProperty> populateTransportProperties(
            Map<String, String> serverHeaderValidationList, Set<TransportProperty> transportProperties) {
        serverHeaderValidationList.forEach((key, value) ->
                transportProperties.add(getTransportProperty(key, value)));
        return transportProperties;
    }

    /**
     * Create single transport property based on it's type.
     *
     * @param name  name of the property.
     * @param value value of the property.
     * @return created transport property.
     */
    private static TransportProperty getTransportProperty(String name, String value) {
        TransportProperty trpProperty = new TransportProperty();
        Map<String, TrpPropertyTypes> tryMap = trpPropertyTypeMap();
        trpProperty.setName(name);
        switch (tryMap.get(name).name()) {
            case "BOOLEAN":
                trpProperty.setValue(Boolean.valueOf(value));
                break;
            case "STRING":
                trpProperty.setValue(value);
                break;
            case "INTEGER":
                trpProperty.setValue(Integer.valueOf(value));
                break;
            case "DOUBLE":
                trpProperty.setValue(Double.valueOf(value));
                break;
            default:
                trpProperty.setValue(value);
                break;
        }
        return trpProperty;
    }

    /**
     * Method is responsible for set transportation configuration values.
     *
     * @param serverBootstrapConfigurationList server bootstrap configuration list
     * @param transportProperties              transport properties set
     * @return return the set of config transportation configuration.
     */
    public static Set<TransportProperty> populateBootstrapConfigurations(
            Map<String, String> serverBootstrapConfigurationList, Set<TransportProperty> transportProperties) {
        serverBootstrapConfigurationList.forEach((key, value) ->
                transportProperties.add(getTransportProperty(key, value)));
        return transportProperties;
    }


    /**
     * Key value generated for Source Listener using host and port.
     *
     * @param listenerUrl the listener URL
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
     *
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
     *
     * @param listenerUrl        the listenerUrl of source
     * @param sourceConfigReader the config reader of source
     * @return listener configuration.
     */
    public static ListenerConfiguration getListenerConfiguration(String listenerUrl, ConfigReader sourceConfigReader) {
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
                case HttpConstants.SCHEME_HTTP:
                    listenerConfig = new ListenerConfiguration(HttpConstants.SCHEME_HTTP, host, port);
                    listenerConfig.setId(host + PORT_HOST_SEPARATOR + port);
                    listenerConfig.setScheme(protocol);
                    listenerConfig.setMessageProcessorId(sourceConfigReader
                            .readConfig(HttpConstants.MESSAGE_PROCESSOR_ID, HttpConstants.MESSAGE_PROCESSOR_ID_VALUE));
                    break;
                case HttpConstants.SCHEME_HTTPS:
                    listenerConfig = new ListenerConfiguration(HttpConstants.SCHEME_HTTPS, host, port);
                    listenerConfig.setId(host + PORT_HOST_SEPARATOR + port);
                    listenerConfig.setScheme(protocol);
                    listenerConfig.setKeyStoreFile(sourceConfigReader
                            .readConfig(HttpConstants.KEYSTORE_FILE, HttpConstants.KEYSTORE_FILE_VALUE));
                    listenerConfig.setKeyStorePass(sourceConfigReader
                            .readConfig(HttpConstants.KEYSTORE_PASSWORD, HttpConstants.KEYSTORE_PASSWORD_VALUE));
                    listenerConfig.setMessageProcessorId(sourceConfigReader
                            .readConfig(HttpConstants.MESSAGE_PROCESSOR_ID, HttpConstants.MESSAGE_PROCESSOR_ID_VALUE));
                    break;
                default:
                    throw new HttpSourceAdaptorRuntimeException("Invalid protocol " + protocol);
            }
        } catch (MalformedURLException e) {
            log.error("Receiver url malformed." + listenerUrl, e);
        }
        return listenerConfig;
    }

    /**
     * This method handle the response including the status of request.
     *
     * @param carbonMessage the carbon callback that should send the status.
     * @param code          the http status code.
     */
    public static void handleCallback(HttpCarbonMessage carbonMessage, int code) {
        HttpIoUtil.handleFailure(carbonMessage, null, code, null);
    }

    /**
     * This map contains the properties other than String
     *
     * @return
     */
    private static Map<String, TrpPropertyTypes> trpPropertyTypeMap() {
        Map<String, TrpPropertyTypes> trpPropertyTypes = new HashMap<>();
        trpPropertyTypes.put(LATENCY_METRICS_ENABLED, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(REQUEST_SIZE_VALIDATION, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(REQUEST_SIZE_VALIDATION_MAXIMUM_VALUE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(REQUEST_SIZE_VALIDATION_REJECT_STATUS_CODE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(REQUEST_SIZE_VALIDATION_REJECT_MESSAGE, TrpPropertyTypes.STRING);
        trpPropertyTypes.put(REQUEST_SIZE_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE, TrpPropertyTypes.STRING);
        trpPropertyTypes.put(HEADER_SIZE_VALIDATION, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(HEADER_VALIDATION_MAXIMUM_REQUEST_LINE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(HEADER_VALIDATION_MAXIMUM_SIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(HEADER_VALIDATION_MAXIMUM_CHUNK_SIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(HEADER_VALIDATION_REJECT_STATUS_CODE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(HEADER_VALIDATION_REJECT_MESSAGE, TrpPropertyTypes.STRING);
        trpPropertyTypes.put(HEADER_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE, TrpPropertyTypes.STRING);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_BOSS_GROUP_SIZE_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_WORKER_GROUP_SIZE_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_NODELAY_PARAM, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_KEEPALIVE_PARAM, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_SENDBUFFERSIZE_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_RECIEVEBUFFERSIZE_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_CONNECT_TIMEOUT_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_SOCKET_REUSE_PARAM, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_SOCKET_TIMEOUT_PARAM, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SERVER_BOOTSTRAP_SOCKET_BACKLOG_PARAM, TrpPropertyTypes.INTEGER);
        return trpPropertyTypes;
    }

    /**
     * This method handles OPTIONS requests received by the http-source
     *
     * @param requestMessage OPTIONS request which needs to be handled.
     */
    public static void handleCORS(HttpCarbonMessage requestMessage) {
        HttpIoUtil.handleResponse(requestMessage, HttpIoUtil.createOptionsResponseMessage(requestMessage));
    }
}

