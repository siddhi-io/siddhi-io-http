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
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            URL aURL = new URL(publisherURL);
            String scheme = aURL.getProtocol();
            String host = aURL.getHost();
            String port = Integer.toString(aURL.getPort());
            String path = aURL.getPath();
            httpStaticProperties = new HashMap<>();
            httpStaticProperties.put(HttpConstants.TO, path);
            httpStaticProperties.put(HttpConstants.HOST, host);
            httpStaticProperties.put(HttpConstants.PORT, port);
            httpStaticProperties.put(HttpConstants.SCHEME, scheme);
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
            if (!"".equals(headers)) {
                String[] spam = headers.split(HttpConstants.HEADER_SPLITTER_REGEX);
                for (String aSpam : spam) {
                    String[] header = aSpam.split(HttpConstants.HEADER_NAME_VALUE_SPLITTER, 2);
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
     * @return default trust password.
     */
    public static String trustStorePassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD,
                HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_VALUE);
    }

    /**
     * Method is responsible for set sender configuration values .
     *
     * @param httpStaticProperties the map that url details.
     * @param clientStoreFile      the client trust store file path.
     * @param clientStorePass      the client trust store pass path.
     * @return set of sender configurations.
     */
    public static SenderConfiguration getSenderConfigurations(Map<String, String> httpStaticProperties, String
            clientStoreFile, String clientStorePass, ConfigReader configReader) {
        SenderConfiguration httpSender = new SenderConfiguration(httpStaticProperties
                .get(HttpConstants.PORT));
        if (httpStaticProperties.get(HttpConstants.SCHEME).equals(HttpConstants.SCHEME_HTTPS)) {
            httpSender.setTrustStoreFile(clientStoreFile);
            httpSender.setTrustStorePass(clientStorePass);
            httpSender.setId(httpStaticProperties.get(HttpConstants.TO));
            httpSender.setScheme(httpStaticProperties.get(HttpConstants.SCHEME));
        } else {
            httpSender.setScheme(httpStaticProperties.get(HttpConstants.SCHEME));
        }
        if (isHTTPTraceLoggerEnabled(configReader)) {
            httpSender.setHttpTraceLogEnabled(true);
        }
        return httpSender;
    }

    private static boolean isHTTPTraceLoggerEnabled(ConfigReader configReader) {
        return Boolean.parseBoolean(configReader.readConfig("httpTraceLogEnabled", "false"));
    }

    /**
     * @param parameterList
     * @return
     */
    public static List<org.wso2.transport.http.netty.config.Parameter> populateParameters(String parameterList) {
        List<org.wso2.transport.http.netty.config.Parameter> parameters = new ArrayList<>();
        if (!HttpConstants.EMPTY_STRING.equals(parameterList.trim())) {
            try {
                String[] valueList = parameterList.trim().substring(1,
                        parameterList.length
                                () - 1).split("','");
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
    public static Map<String, Object> populateTransportConfiguration(String clientBootstrapConfigurationList, String
            clientConnectionConfiguration) {
        Map<String, Object> properties = new HashMap<>();
        if (!HttpConstants.EMPTY_STRING.equals(clientBootstrapConfigurationList.trim())) {
            try {
                String[] valueList = clientBootstrapConfigurationList.trim().substring(1,
                        clientBootstrapConfigurationList.length
                                () - 1).split("','");
                Map<String, Object> bootstrapValueMap = Arrays.stream(valueList).collect(Collectors.toMap(
                        (valueEntry) -> valueEntry.split(":")[0],
                        (valueEntry) -> valueEntry.split(":")[1]
                        )
                );
                properties.putAll(populateClientConnectionConfiguration(bootstrapValueMap));
            } catch (ArrayIndexOutOfBoundsException ex) {
                log.error("Client Bootstrap configuration is not in expected format please insert them as " +
                        "'key1:val1'," + "'key2:val2' format", ex);
            }
        }

        if (!HttpConstants.EMPTY_STRING.equals(clientConnectionConfiguration.trim())) {
            try {
                String[] valueList = clientConnectionConfiguration.trim().substring(1,
                        clientConnectionConfiguration.length() - 1).split("','");
                Map<String, Object> clientConnectionConfigurationValueMap = Arrays.stream(valueList)
                        .collect(Collectors.toMap(
                                (valueEntry) -> valueEntry.split(":")[0],
                                (valueEntry) -> valueEntry.split(":")[1]
                                )
                        );
                properties.putAll(populateClientConnectionConfiguration(clientConnectionConfigurationValueMap));
            } catch (ArrayIndexOutOfBoundsException ex) {
                log.error("client Connection Configuration is not in expected format please insert them as " +
                        "'key1:val1','key2:val2' format", ex);
            }
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

    public static Map<String, Object> populateClientConnectionConfiguration(
            Map<String, Object> clientConnectionConfigurationList) {
        Map<String, Object> properties = new HashMap<>();
        Map<String, TrpPropertyTypes> tryMap = trpPropertyTypeMap();
        clientConnectionConfigurationList.forEach((key, value) -> {
            switch (tryMap.get(key).name()) {
                case "BOOLEAN":
                    properties.put(key, Boolean.valueOf((String) value));
                    break;
                case "STRING":
                    properties.put(key, value);
                    break;
                case "INTEGER":
                    properties.put(key, Integer.valueOf((String) value));
                    break;
                case "DOUBLE":
                    properties.put(key, Double.valueOf((String) value));
                    break;
                default:
                    log.error("Transport property is no defined.");
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
    public static Map<String, TrpPropertyTypes> trpPropertyTypeMap() {
        Map<String, TrpPropertyTypes> trpPropertyTypes = new HashMap<>();
        trpPropertyTypes.put("client.connection.pool.count", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.max.active.connections.per.pool", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.min.idle.connections.per.pool", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.max.idle.connections.per.pool", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.min.eviction.idle.time", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("sender.thread.count", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("event.group.executor.thread.size", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("max.wait.for.trp.client.connection.pool", TrpPropertyTypes.INTEGER);

        trpPropertyTypes.put("client.bootstrap.nodelay", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("client.bootstrap.keepalive", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("client.bootstrap.sendbuffersize", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.bootstrap.recievebuffersize", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.bootstrap.connect.timeout", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("client.bootstrap.socket.reuse", TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put("client.bootstrap.socket.timeout", TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put("latency.metrics.enabled", TrpPropertyTypes.BOOLEAN);

        return trpPropertyTypes;
    }
}
