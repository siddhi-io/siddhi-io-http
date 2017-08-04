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
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.extension.siddhi.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code HttpSinkUtil } responsible of the all configuration reading and input formatting of http transport.
 */
public class HttpSinkUtil {
    private static final Logger log = Logger.getLogger(HttpSinkUtil.class);

    public HttpSinkUtil() {
    }

    /**
     * Method is responsible for separate publisher url to host,port and context.
     *
     * @param publisherURL the publisher url.
     * @return map that contains the host,port,context and complete url.
     */
    public Map<String, String> getURLProperties(String publisherURL) {
        Map<String, String> httpStaticProperties;
        String protocol = HttpConstants.DEFAULT_PROTOCOL_VALUE;
        String host = HttpConstants.DEFAULT_HOST_VALUE;
        String port = HttpConstants.EMPTY_STRING;
        String path = HttpConstants.DEFAULT_CONTEXT_VALUE;
        if (!"".equals(publisherURL)) {
            try {
                URL aURL = new URL(publisherURL);
                protocol = aURL.getProtocol();
                host = aURL.getHost();
                port = Integer.toString(aURL.getPort());
                path = aURL.getPath();
            } catch (MalformedURLException e) {
                throw new HttpSinkAdaptorRuntimeException(" Receiver url mandatory. Please insert valid url .", e);
            }
        }
        httpStaticProperties = new HashMap<>();
        httpStaticProperties.put(HttpConstants.TO, path);
        httpStaticProperties.put(HttpConstants.HOST, host);
        httpStaticProperties.put(HttpConstants.PORT, port);
        httpStaticProperties.put(HttpConstants.PROTOCOL, protocol);
        return httpStaticProperties;
    }

    /**
     * Method is responsible of to convert string of headers to list of headers.
     *  Example header format : 'name1:value1','name2:value2'
     * @param headers string of headers list.
     * @return list of headers.
     */
    public List<Header> getHeaders(String headers) {
        if (headers != null) {
            headers = headers.trim();
            headers = headers.substring(1, headers.length() - 1);
            List<Header> headersList = new ArrayList<>();
            if (!"".equals(headers)) {
                String[] spam = headers.split(HttpConstants.HEADER_SPLITTER);
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
    public String trustStorePath(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.TRUSTSTORE_FILE, HttpConstants
                .TRUSTSTORE_FILE_VALUE);
    }

    /**
     * user can give custom truststore password if user did not give then custom then system read
     * the default values which is in the deployment yaml.
     *
     * @return default trust password.
     */
    public String trustStorePassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.TRUSTSTORE_PASSWORD, HttpConstants.
                        TRUSTSTORE_PASSWORD_VALUE);
    }
    /**
     * Method is responsible for set sender configuration values .
     *
     * @param httpStaticProperties the map that url details.
     * @param clientStoreFile      the client trust store file path.
     * @param clientStorePass      the client trust store pass path.
     * @return set of sender configurations.
     */
    public Set<SenderConfiguration> getSenderConfigurations(Map<String, String> httpStaticProperties, String
            clientStoreFile, String clientStorePass) {
        Set<SenderConfiguration> senderConf;
        if (httpStaticProperties.get(HttpConstants.PROTOCOL).equals(HttpConstants.PROTOCOL_HTTPS)) {
            SenderConfiguration httpsSender = new SenderConfiguration(httpStaticProperties
                    .get(HttpConstants.PORT));
            httpsSender.setTrustStoreFile(clientStoreFile);
            httpsSender.setTrustStorePass(clientStorePass);
            httpsSender.setId(httpStaticProperties.get(HttpConstants.TO));
            httpsSender.setScheme(httpStaticProperties.get(HttpConstants.PROTOCOL));
            senderConf = new HashSet<>(Collections.singletonList(httpsSender));
        } else {
            SenderConfiguration httpSender = new SenderConfiguration(httpStaticProperties.get(HttpConstants.PORT));
            httpSender.setScheme(httpStaticProperties.get(HttpConstants.PROTOCOL));
            senderConf = new HashSet<>(Collections.singletonList(httpSender));
        }
        return senderConf;
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
        String bootstrapBossThreads = sourceConfigReader.readConfig(HttpConstants
                .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        if (!HttpConstants.EMPTY_STRING.equals(bootstrapBossThreads)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE);
            var.setValue(Integer.valueOf(bootstrapBossThreads));
            properties.add(var);
        }
        String bootstrapWorkerThreads = sourceConfigReader.readConfig(HttpConstants
                .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.EMPTY_STRING);
        if (!HttpConstants.EMPTY_STRING.equals(bootstrapWorkerThreads)) {
            var = new TransportProperty();
            var.setName(HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE);
            var.setValue(Integer.valueOf(bootstrapWorkerThreads));
            properties.add(var);
        }
        return new HashSet<>(properties);
    }

    /**
     * Responsible of get the content type of payload.
     *
     * @param mapType     user define map type.
     * @param headersList list of headers defines by user.
     * @return return the exact map type.
     */
    public String getContentType(String mapType, List<Header> headersList) {
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
}
