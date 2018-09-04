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

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.extension.siddhi.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.extension.siddhi.io.http.util.TrpPropertyTypes;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_CONNECT_TIMEOUT;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_KEEPALIVE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_NODELAY;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_RECIEVEBUFFERSIZE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SENDBUFFERSIZE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SOCKET_REUSE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SOCKET_TIMEOUT;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_CONNECTION_POOL_COUNT;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_MAX_ACTIVE_CONNECTIONS_PER_POOL;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_MAX_IDLE_CONNECTIONS_PER_POOL;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_MIN_EVICTION_IDLE_TIME;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.CLIENT_MIN_IDLE_CONNECTIONS_PER_POOL;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.EVENT_GROUP_EXECUTOR_THREAD_SIZE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.HTTP_TRACE_LOG_ENABLED;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.LATENCY_METRICS_ENABLED;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.LOG_TRACE_ENABLE_DEFAULT_VALUE;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.MAX_WAIT_FOR_TRP_CLIENT_CONNECTION_POOL;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.PARAMETER_SEPARATOR;
import static org.wso2.extension.siddhi.io.http.util.HttpConstants.SENDER_THREAD_COUNT;
import static org.wso2.extension.siddhi.io.http.util.HttpIoUtil.populateParameterMap;

/**
 * {@code HttpSinkUtil } responsible of the all configuration reading and input formatting of http transport.
 */
public class HttpSinkUtil {
    private static final Logger log = Logger.getLogger(HttpSinkUtil.class);
    
    private HttpSinkUtil() {
    }
    
    /**
     * Method is responsible for separate publisher url to host,port and context.
     *
     * @param publisherURL the publisher url.
     * @return map that contains the host,port,context and complete url.
     */
    public static Map<String, String> getURLProperties(String publisherURL) {
        Map<String, String> httpStaticProperties;
        try {
            URL url = new URL(publisherURL);
            httpStaticProperties = new HashMap<>();
            httpStaticProperties.put(Constants.TO, url.getFile());
            String protocol = url.getProtocol();
            httpStaticProperties.put(Constants.PROTOCOL, protocol);
            httpStaticProperties.put(Constants.HTTP_HOST, url.getHost());
            int port;
            if (Constants.HTTPS_SCHEME.equalsIgnoreCase(protocol)) {
                port = url.getPort() != -1 ? url.getPort() : HttpConstants.DEFAULT_HTTPS_PORT;
            } else {
                port = url.getPort() != -1 ? url.getPort() : HttpConstants.DEFAULT_HTTP_PORT;
            }
            httpStaticProperties.put(Constants.HTTP_PORT, Integer.toString(port));
            httpStaticProperties.put(Constants.REQUEST_URL, url.toString());
        } catch (MalformedURLException e) {
            throw new HttpSinkAdaptorRuntimeException(" Receiver url mandatory. Please insert valid url .", e);
        }
        return httpStaticProperties;
    }
    
    /**
     * Method is responsible of to convert string of headers to list of headers.
     * Example header format : 'name1:value1','name2:value2'
     *
     * @param headers string of headers list.
     * @return list of headers.
     */
    public static List<Header> getHeaders(String headers) {
        if (headers != null) {
            headers = headers.trim();
            headers = headers.substring(1, headers.length() - 1);
            List<Header> headersList = new ArrayList<>();
            if (!HttpConstants.EMPTY_STRING.equals(headers)) {
                String[] spam = headers.split(HttpConstants.HEADER_SPLITTER_REGEX);
                for (String headerValue : spam) {
                    String[] header = headerValue.split(HttpConstants.HEADER_NAME_VALUE_SPLITTER, 2);
                    if (header.length > 1) {
                        headersList.add(new Header(header[0], header[1]));
                    } else {
                        throw new HttpSinkAdaptorRuntimeException(
                                "Invalid header format. Please include as 'key1:value1','key2:value2',..");
                    }
                }
            }
            return headersList;
        } else {
            return null;
        }
    }
    
    /**
     * user can give custom truststore file if user did not give then custom then system read
     * the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader      configuration reader for sink.
     *
     * @return default trust store file path.
     */
    public static String trustStorePath(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.CLIENT_TRUSTSTORE_PATH,
                HttpConstants.CLIENT_TRUSTSTORE_PATH_VALUE);
    }
    
    /**
     * user can give custom truststore password if user did not give then custom then system read
     * the default values which is in the deployment yaml.
     *
     * @param sinkConfigReader      configuration reader for sink.
     *
     * @return default trust password.
     */
    public static String trustStorePassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD,
                HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_VALUE);
    }
    
    /**
     * Method is responsible for set sender configuration values .
     *
     * @param httpStaticProperties  the map that url details.
     * @param clientStoreFile       the client trust store file path.
     * @param clientStorePass       the client trust store pass path.
     * @param configReader          configuration reader.
     * @return set of sender configurations.
     */
    public static SenderConfiguration getSenderConfigurations(Map<String, String> httpStaticProperties, String
            clientStoreFile, String clientStorePass, ConfigReader configReader) {
        SenderConfiguration httpSender = new SenderConfiguration(httpStaticProperties
                .get(Constants.HTTP_PORT));
        if (httpStaticProperties.get(Constants.PROTOCOL).equals(HttpConstants.SCHEME_HTTPS)) {
            httpSender.setTrustStoreFile(clientStoreFile);
            httpSender.setTrustStorePass(clientStorePass);
            httpSender.setId(httpStaticProperties.get(Constants.TO));
            httpSender.setScheme(httpStaticProperties.get(Constants.PROTOCOL));
        } else {
            httpSender.setScheme(httpStaticProperties.get(Constants.PROTOCOL));
        }
        if (isHTTPTraceLoggerEnabled(configReader)) {
            httpSender.setHttpTraceLogEnabled(true);
        }
        return httpSender;
    }
    
    private static boolean isHTTPTraceLoggerEnabled(ConfigReader configReader) {
        return Boolean.parseBoolean(configReader.readConfig(HTTP_TRACE_LOG_ENABLED, LOG_TRACE_ENABLE_DEFAULT_VALUE));
    }
    
    /**
     * Method is responsible for set transportation configuration values.
     *
     * @param clientBootstrapConfigurationList  client bootstrap configuration list.
     * @param clientConnectionConfiguration     client connection configuration.
     *
     * @return return the set of config transportation configuration.
     */
    public static Map<String, Object> populateTransportConfiguration(String clientBootstrapConfigurationList, String
            clientConnectionConfiguration) {
        Map<String, Object> properties = new HashMap<>();
        //populate bootstrap configurations
        if (!HttpConstants.EMPTY_STRING.equals(clientBootstrapConfigurationList.trim())) {
            String[] valueList = clientBootstrapConfigurationList.trim()
                    .substring(1, clientBootstrapConfigurationList.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            properties.putAll(populateClientConnectionConfiguration(populateParameterMap(valueList)));
        }
        //populate connection configurations
        if (!HttpConstants.EMPTY_STRING.equals(clientConnectionConfiguration.trim())) {
            String[] valueList = clientConnectionConfiguration.trim()
                    .substring(1, clientConnectionConfiguration.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            properties.putAll(populateClientConnectionConfiguration(populateParameterMap(valueList)));
        }
        return properties;
    }
    
    /**
     * Responsible of get the content type of payload.
     *
     * @param mapType     user define map type.
     * @param headersList list of headers defines by user.
     * @return return the exact map type.
     */
    public static String getContentType(String mapType, List<Header> headersList) {
        if (headersList != null) {
            for (Header h : headersList) {
                if (HttpConstants.HTTP_CONTENT_TYPE.equals(h.getName())) {
                    return h.getValue();
                }
            }
        }
        switch (mapType) {
            case HttpConstants.MAP_TEXT:
                return HttpConstants.TEXT_PLAIN;
            
            case HttpConstants.MAP_XML:
                return HttpConstants.APPLICATION_XML;
            
            case HttpConstants.MAP_JSON:
                return HttpConstants.APPLICATION_JSON;
            
            default: {
                log.info("Invalid payload map type. System support only text," +
                        "Json and XML type hence proceed with default text mapping");
                return HttpConstants.TEXT_PLAIN;
            }
        }
    }
    
    /**
     * Get port from listenerUrl.
     *
     * @param senderUrl the listener URL.
     * @return port of the listener URL.
     */
    public static String getScheme(String senderUrl) {
        URL aURL;
        try {
            aURL = new URL(senderUrl);
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("SenderUrl is not in a proper format ", e);
        }
        return aURL.getProtocol();
    }
    
    private static Map<String, Object> populateClientConnectionConfiguration(
            Map<String, String> clientConnectionConfigurationList) {
        Map<String, Object> properties = new HashMap<>();
        Map<String, TrpPropertyTypes> tryMap = trpPropertyTypeMap();
        clientConnectionConfigurationList.forEach((key, value) -> {
            switch (tryMap.get(key).name()) {
                case "BOOLEAN":
                    properties.put(key, Boolean.valueOf(value));
                    break;
                case "STRING":
                    properties.put(key, value);
                    break;
                case "INTEGER":
                    properties.put(key, Integer.valueOf(value));
                    break;
                case "DOUBLE":
                    properties.put(key, Double.valueOf(value));
                    break;
                default:
                    log.error("Transport property:'" + tryMap.get(key).name() + "' is no defined.");
                    break;
            }
        });
        return properties;
    }
    
    /**
     * This map contains the properties other than String
     *
     * @return
     */
    private static Map<String, TrpPropertyTypes> trpPropertyTypeMap() {
        Map<String, TrpPropertyTypes> trpPropertyTypes = new HashMap<>();
        trpPropertyTypes.put(CLIENT_CONNECTION_POOL_COUNT, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_MAX_ACTIVE_CONNECTIONS_PER_POOL, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_MIN_IDLE_CONNECTIONS_PER_POOL, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_MAX_IDLE_CONNECTIONS_PER_POOL, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_MIN_EVICTION_IDLE_TIME, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(SENDER_THREAD_COUNT, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(EVENT_GROUP_EXECUTOR_THREAD_SIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(MAX_WAIT_FOR_TRP_CLIENT_CONNECTION_POOL, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_NODELAY, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_KEEPALIVE, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SENDBUFFERSIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_RECIEVEBUFFERSIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_CONNECT_TIMEOUT, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SOCKET_REUSE, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SOCKET_TIMEOUT, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(LATENCY_METRICS_ENABLED, TrpPropertyTypes.BOOLEAN);
        return trpPropertyTypes;
    }
}
