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
package io.siddhi.extension.io.http.sink.util;

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.http.sink.exception.HttpSinkAdaptorRuntimeException;
import io.siddhi.extension.io.http.util.HttpConstants;
import io.siddhi.extension.io.http.util.TrpPropertyTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.ProxyServerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_CONNECT_TIMEOUT;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_KEEPALIVE;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_NODELAY;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_RECIEVEBUFFERSIZE;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SENDBUFFERSIZE;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SOCKET_REUSE;
import static io.siddhi.extension.io.http.util.HttpConstants.CLIENT_BOOTSTRAP_SOCKET_TIMEOUT;
import static io.siddhi.extension.io.http.util.HttpConstants.EMPTY_STRING;
import static io.siddhi.extension.io.http.util.HttpConstants.HTTP_TRACE_LOG_ENABLED;
import static io.siddhi.extension.io.http.util.HttpConstants.LOG_TRACE_ENABLE_DEFAULT_VALUE;
import static io.siddhi.extension.io.http.util.HttpConstants.PARAMETER_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpIoUtil.populateParameterMap;
import static org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants.REQUEST_URL;

/**
 * {@code HttpSinkUtil } responsible of the all configuration reading and input formatting of http transport.
 */
public class HttpSinkUtil {
    private static final Logger log = LogManager.getLogger(HttpSinkUtil.class);

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
            String path = url.getPath();
            httpStaticProperties.put(Constants.PROTOCOL, protocol);
            httpStaticProperties.put(Constants.HTTP_HOST, url.getHost());
            httpStaticProperties.put(HttpConstants.PATH, path);
            int port;
            if (Constants.HTTPS_SCHEME.equalsIgnoreCase(protocol)) {
                port = url.getPort() != -1 ? url.getPort() : HttpConstants.DEFAULT_HTTPS_PORT;
            } else {
                port = url.getPort() != -1 ? url.getPort() : HttpConstants.DEFAULT_HTTP_PORT;
            }
            httpStaticProperties.put(Constants.HTTP_PORT, Integer.toString(port));
            httpStaticProperties.put(REQUEST_URL, url.toString());
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
     * @param sinkConfigReader configuration reader for sink.
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
     * @param sinkConfigReader configuration reader for sink.
     * @return default trust password.
     */
    public static String trustStorePassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.CLIENT_TRUSTSTORE_PASSWORD,
                HttpConstants.CLIENT_TRUSTSTORE_PASSWORD_VALUE);
    }

    /**
     * user can give custom key store file if user did not give then custom then system read the default values which is
     * in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return key store file path.
     */
    public static String keyStorePath(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.KEYSTORE_FILE, HttpConstants.KEYSTORE_FILE_VALUE);
    }

    /**
     * user can give custom keystore password,, if user did not give then custom then system read the default values
     * which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return key store password.
     */
    public static String keyStorePassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.KEYSTORE_PASSWORD, HttpConstants.KEYSTORE_PASSWORD_VALUE);
    }

    /**
     * user can give custom key store key password if user did not give then custom then system read the default values
     * which is in the deployment yaml.
     *
     * @param sinkConfigReader configuration reader for sink.
     * @return key store key password.
     */
    public static String keyPassword(ConfigReader sinkConfigReader) {
        return sinkConfigReader.readConfig(HttpConstants.KEYSTORE_KEY_PASSWORD,
                HttpConstants.KEYSTORE_KEY_PASSWORD_VALUE);
    }

    /**
     * Method is responsible for set sender configuration values .
     *
     * @param httpStaticProperties the map that url details.
     * @param clientStoreFile      the client trust store file path.
     * @param clientStorePass      the client trust store pass path.
     * @param configReader         configuration reader.
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
     * @param clientBootstrapConfigurationList client bootstrap configuration list.
     * @return return the set of config transportation configuration.
     */
    public static Map<String, Object> populateTransportConfiguration(String clientBootstrapConfigurationList) {
        Map<String, Object> properties = new HashMap<>();
        //populate bootstrap configurations
        if (!HttpConstants.EMPTY_STRING.equals(clientBootstrapConfigurationList.trim())) {
            String[] valueList = clientBootstrapConfigurationList.trim()
                    .substring(1, clientBootstrapConfigurationList.length() - 1)
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
                if (HttpConstants.HTTP_CONTENT_TYPE.equalsIgnoreCase(h.getName())) {
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

            case HttpConstants.MAP_KEYVALUE:
                return HttpConstants.APPLICATION_URL_ENCODED;

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
                    log.error("Transport property:'{}' is no defined.", tryMap.get(key).name());
                    break;
            }
        });
        return properties;
    }

    /**
     * This map contains the properties other than String.
     *
     * @return
     */
    private static Map<String, TrpPropertyTypes> trpPropertyTypeMap() {
        Map<String, TrpPropertyTypes> trpPropertyTypes = new HashMap<>();
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_NODELAY, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_KEEPALIVE, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SENDBUFFERSIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_RECIEVEBUFFERSIZE, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_CONNECT_TIMEOUT, TrpPropertyTypes.INTEGER);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SOCKET_REUSE, TrpPropertyTypes.BOOLEAN);
        trpPropertyTypes.put(CLIENT_BOOTSTRAP_SOCKET_TIMEOUT, TrpPropertyTypes.INTEGER);
        return trpPropertyTypes;
    }

    public static PoolConfiguration createPoolConfigurations(OptionHolder optionHolder) {
        int executorServiceThreads = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.EXECUTOR_SERVICE_THREAD_COUNT, HttpConstants.DEFAULT_EXECUTOR_SERVICE_THREAD_COUNT));
        int maxIdlePerPool = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.MAX_IDLE_CONNECTIONS_PER_POOL, HttpConstants.DEFAULT_MAX_IDLE_CONNECTIONS_PER_POOL));
        int minIdlePerPool = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.MIN_IDLE_CONNECTIONS_PER_POOL, HttpConstants.DEFAULT_MIN_IDLE_CONNECTIONS_PER_POOL));
        int maxActivePerPool = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.MAX_ACTIVE_CONNECTIONS_PER_POOL, HttpConstants.DEFAULT_MAX_ACTIVE_CONNECTIONS_PER_POOL));
        boolean testOnBorrow = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(HttpConstants.TEST_ON_BORROW,
                HttpConstants.DEFAULT_TEST_ON_BORROW));
        boolean testWhileIdle = Boolean.parseBoolean(
                optionHolder.validateAndGetStaticValue(HttpConstants.TEST_WHILE_IDLE,
                        HttpConstants.DEFAULT_TEST_WHILE_IDLE));
        long timeBetweenEvictionRuns = Long.parseLong(optionHolder.validateAndGetStaticValue(
                HttpConstants.TIME_BETWEEN_EVICTION_RUNS, HttpConstants.DEFAULT_TIME_BETWEEN_EVICTION_RUNS));
        long minEvictableIdleTime = Long.parseLong(optionHolder.validateAndGetStaticValue(
                HttpConstants.MIN_EVICTABLE_IDLE_TIME, HttpConstants.DEFAULT_MIN_EVICTABLE_IDLE_TIME));
        byte exhaustedAction = (byte) Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.EXHAUSTED_ACTION, HttpConstants.DEFAULT_EXHAUSTED_ACTION));
        int maxWaitTime = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                HttpConstants.MAX_WAIT_TIME, HttpConstants.DEFAULT_MAX_WAIT_TIME));
        PoolConfiguration connectionPoolConfiguration = new PoolConfiguration();
        connectionPoolConfiguration.setExecutorServiceThreads(executorServiceThreads);
        connectionPoolConfiguration.setMaxActivePerPool(maxActivePerPool);
        connectionPoolConfiguration.setMinIdlePerPool(minIdlePerPool);
        connectionPoolConfiguration.setMaxIdlePerPool(maxIdlePerPool);
        connectionPoolConfiguration.setTestOnBorrow(testOnBorrow);
        connectionPoolConfiguration.setTestWhileIdle(testWhileIdle);
        connectionPoolConfiguration.setTimeBetweenEvictionRuns(timeBetweenEvictionRuns);
        connectionPoolConfiguration.setMinEvictableIdleTime(minEvictableIdleTime);
        connectionPoolConfiguration.setExhaustedAction(exhaustedAction);
        connectionPoolConfiguration.setMaxWaitTime(maxWaitTime);
        return connectionPoolConfiguration;
    }

    public static DefaultHttpWsConnectorFactory createConnectorFactory(ConfigReader configReader) {
        //read trp globe configuration
        String bootstrapWorker = configReader
                .readConfig(HttpConstants.CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE, EMPTY_STRING);
        String bootstrapBoss = configReader.readConfig(HttpConstants.CLIENT_BOOTSTRAP_BOSS_GROUP_SIZE, EMPTY_STRING);
        String bootstrapClient = configReader.readConfig(HttpConstants.CLIENT_BOOTSTRAP_CLIENT_GROUP_SIZE,
                EMPTY_STRING);
        //if bootstrap configurations are given then pass it if not let take default value of transport
        if (!EMPTY_STRING.equals(bootstrapBoss) && !EMPTY_STRING.equals(bootstrapWorker)) {
            if (!EMPTY_STRING.equals(bootstrapClient)) {
                return new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss),
                        Integer.parseInt(bootstrapWorker), Integer.parseInt(bootstrapClient));
            } else {
                return new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss),
                        Integer.parseInt(bootstrapWorker), Integer.parseInt(bootstrapWorker));
            }
        }
        return new DefaultHttpWsConnectorFactory();
    }

    public static ProxyServerConfiguration createProxyServerConfiguration(OptionHolder optionHolder, String streamID,
                                                                          String appName) {
        String proxyHost = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_HOST, EMPTY_STRING);
        String proxyPort = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_PORT, EMPTY_STRING);
        String proxyUsername = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_USERNAME,
                EMPTY_STRING);
        String proxyPassword = optionHolder.validateAndGetStaticValue(HttpConstants.PROXY_PASSWORD,
                EMPTY_STRING);
        //if proxy username and password not equal to null then create proxy configurations
        if (!EMPTY_STRING.equals(proxyHost) && !EMPTY_STRING.equals(proxyPort)) {
            try {
                ProxyServerConfiguration proxyServerConfiguration = new ProxyServerConfiguration(proxyHost, Integer
                        .parseInt(proxyPort));
                if (!EMPTY_STRING.equals(proxyPassword) && !EMPTY_STRING.equals
                        (proxyUsername)) {
                    proxyServerConfiguration.setProxyPassword(proxyPassword);
                    proxyServerConfiguration.setProxyUsername(proxyUsername);
                }
                return proxyServerConfiguration;
            } catch (UnknownHostException e) {
                log.error("Proxy url of sink defined in '{}' of Siddhi App '{}' is invalid.", streamID, appName, e);
            }
        }
        return null;
    }

    /**
     * This method return byte size for a given string.
     *
     * @param body http request body
     * @return byte size of the body
     */
    public static long getByteSize(String body) {
        return body.getBytes(Charset.defaultCharset()).length;
    }
}
