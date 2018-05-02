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
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.extension.siddhi.io.http.util.HttpIoUtil;
import org.wso2.extension.siddhi.io.http.util.TrpPropertyTypes;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.TransportProperty;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles the http source util functions.
 */
public class HttpSourceUtil {
    private static final Logger log = Logger.getLogger(HttpSourceUtil.class);
    private static HttpSourceUtil instance = new HttpSourceUtil();

    public HttpSourceUtil() {
    }

    public static HttpSourceUtil getInstance() {
        return instance;
    }

    public Set<TransportProperty> populateHeaderValidationConfigurations(
            Map<String, String> serverHeaderValidationList, Set<TransportProperty> transportProperties) {
        Map<String, TrpPropertyTypes> tryMap = trpPropertyTypeMap();
        serverHeaderValidationList.forEach((key, value) -> {
            TransportProperty trpProperty = new TransportProperty();
            trpProperty.setName(key);
            switch (tryMap.get(key).name()) {
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
            transportProperties.add(trpProperty);
        });
        return transportProperties;
    }

    /**
     * @param parameterList
     * @return
     */
    public List<Parameter> populateParameters(String parameterList) {
        List<org.wso2.transport.http.netty.config.Parameter> parameters = new ArrayList<>();
        if (!HttpConstants.EMPTY_STRING.equals(parameterList.trim())) {
            try {
                String[] valueList = parameterList.trim().substring(1, parameterList.length() - 1).split("','");
                Arrays.stream(valueList).forEach(valueEntry ->
                        {
                            org.wso2.transport.http.netty.config.Parameter parameter = new Parameter();
                            parameter.setName(valueEntry.split(":")[0]);
                            parameter.setValue(valueEntry.split(":")[1]);
                            parameters.add(parameter);
                        }
                );

            } catch (ArrayIndexOutOfBoundsException ex) {
                log.error("Bootstrap configuration is not in expected format please insert them as 'key1:val1'," +
                        "'key2:val2' format");
            }
        }
        return parameters;
    }

    /**
     * Method is responsible for set transportation configuration values.
     *
     * @return return the set of config transportation configuration.
     */
    public Set<TransportProperty> populateServerBootstrapConfigurations(
            Map<String, String> serverBootstrapConfigurationList, Set<TransportProperty> transportProperties) {
        Map<String, TrpPropertyTypes> tryMap =
                trpPropertyTypeMap();
        serverBootstrapConfigurationList.forEach((key, value) -> {
            TransportProperty trpProperty = new TransportProperty();
            trpProperty.setName(key);
            switch (tryMap.get(key).name()) {
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
            transportProperties.add(trpProperty);
        });
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
                    listenerConfig.setId(host + ":" + port);
                    listenerConfig.setScheme(protocol);
                    listenerConfig.setMessageProcessorId(sourceConfigReader.readConfig(HttpConstants
                            .MESSAGE_PROCESSOR_ID, HttpConstants.MESSAGE_PROCESSOR_ID_VALUE));
                    break;
                case HttpConstants.SCHEME_HTTPS:
                    listenerConfig = new ListenerConfiguration(HttpConstants.SCHEME_HTTPS, host, port);
                    listenerConfig.setId(host + ":" + port);
                    listenerConfig.setScheme(protocol);
                    listenerConfig.setKeyStoreFile(sourceConfigReader.readConfig(HttpConstants.KEYSTORE_FILE,
                            HttpConstants.KEYSTORE_FILE_VALUE));
                    listenerConfig.setKeyStorePass(sourceConfigReader.readConfig(HttpConstants.KEYSTORE_PASSWORD,
                            HttpConstants.KEYSTORE_PASSWORD_VALUE));
                    listenerConfig.setCertPass(sourceConfigReader.readConfig(HttpConstants.CERT_PASSWORD,
                            HttpConstants.CERT_PASSWORD_VALUE));
                    listenerConfig.setMessageProcessorId(sourceConfigReader.readConfig(HttpConstants
                            .MESSAGE_PROCESSOR_ID, HttpConstants.MESSAGE_PROCESSOR_ID_VALUE));
                    break;
                default:
                    throw new HttpSourceAdaptorRuntimeException("Invalid protocol " + protocol);
            }
        } catch (MalformedURLException e) {
            log.error("Receiver url malformed." + listenerUrl);
        }
        return listenerConfig;
    }

    /**
     * This method handle the response including the status of request.
     *
     * @param carbonMessage the carbon callback that should send the status.
     * @param code          the http status code.
     */
    public static void handleCallback(HTTPCarbonMessage carbonMessage, int code) {
        HttpIoUtil.handleFailure(carbonMessage, null, code, null);
    }

    /**
     * This map contains the properties other than String
     *
     * @return
     */
    public static Map<String, TrpPropertyTypes> trpPropertyTypeMap() {
        Map<String, TrpPropertyTypes> trpPropertyTypes = new HashMap<>();
        trpPropertyTypes.put("latency.metrics.enabled", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("request.size.validation", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("request.size.validation.maximum.value", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("request.size.validation.reject.status.code", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("request.size.validation.reject.message", TrpPropertyTypes.STRING);
        trpPropertyTypes.put("request.size.validation.reject.message.content.type", TrpPropertyTypes.STRING);
        trpPropertyTypes.put("header.size.validation", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("header.validation.maximum.request.line", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("header.validation.maximum.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("header.validation.maximum.chunk.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("header.validation.reject.status.code", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("header.validation.reject.message", TrpPropertyTypes.STRING);
        trpPropertyTypes.put("header.validation.reject.message.content.type", TrpPropertyTypes.STRING);
        trpPropertyTypes.put("server.bootstrap.boss.group.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.worker.group.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.client.group.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.nodelay", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("server.bootstrap.keepalive", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("server.bootstrap.sendbuffersize", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.recievebuffersize", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.connect.timeout", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.socket.reuse", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("server.bootstrap.socket.timeout", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("server.bootstrap.socket.backlog", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("latency.metrics.enabled", TrpPropertyTypes.BOOLEAN);
        return trpPropertyTypes;
    }


}

