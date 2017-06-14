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
package org.wso2.extension.siddhi.io.http.util;

/**
 * {@code HttpConstant }Http Sink Constants.
 */
public class HttpConstants {

    private HttpConstants() {
    }

    //Thread pool parameters
    public static final String WORKER_COUNT = "worker.count";
    public static final String DEFAULT_WORKER_COUNT = "1";
    public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE = "server.bootstrap.boss.group.size";
    public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE = "server.bootstrap.worker.group.size";
    public static final String LISTENER_PORT = "LISTENER_PORT";
    //Netty parameter
    public static final String LATENCY_METRICS = "latency.metrics.enabled";
    public static final String LATENCY_METRICS_VALUE = "true";
    public static final String SERVER_BOOTSTRAP_SOCKET_TIMEOUT = "server.bootstrap.socket.timeout";
    public static final String SERVER_BOOTSTRAP_SOCKET_TIMEOUT_VALUE = "15";
    public static final String CLIENT_BOOTSTRAP_SOCKET_TIMEOUT = "client.bootstrap.socket.timeout";
    public static final String CLIENT_BOOTSTRAP_SOCKET_TIMEOUT_VALUE = "15";

    //util values
    public static final String PROTOCOL_ID = "http";
    public static final String HEADER_SPLITER = "#";
    public static final String HEADER_NAME_VALUE_SPLITER = ":";
    public static final String HTTP_SINK_ID = "http sink";
    public static final String METHOD_DEFAULT = "POST";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String PORT = "PORT";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_METHOD = "Basic ";
    public static final String DEFAULT_CONTEXT_VALUE = "";
    public static final String HOST = "HOST";
    public static final String EMPTY_STRING = "";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String TO = "TO";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String DEFAULT_HOST_VALUE = "0.0.0.0";
    public static final String DEFAULT_PROTOCOL_VALUE = "http";
    public static final String PROTOCOL_HOST_SEPARATOR = "://";
    public static final String PORT_HOST_SEPARATOR = ":";
    public static final String PORT_CONTEXT_SEPARATOR = "/";
    public static final String PORT_CONTEXT_KEY_SEPARATOR = "-";

    //Http siddhi sink properties
    public static final String AUTH_USERNAME_PASSWORD_SEPARATOR = ":";
    public static final String METHOD = "method";
    public static final String PUBLISHER_URL = "publisher.url";
    public static final String HEADERS = "headers";
    public static final String RECEIVER_USERNAME = "basic.auth.username";
    public static final String RECEIVER_PASSWORD = "basic.auth.password";
    public static final String CLIENT_TRUSTSTORE_PATH = "client.truststore.path";
    public static final String CLIENT_TRUSTSTORE_PASSWORD = "client.truststore.password";
    public static final String MAP_TEXT = "text";
    public static final String MAP_JSON = "json";
    public static final String MAP_XML = "xml";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String DEFAULT_ENCODING = "UTF-8";

    //https parameters
    public static final String KEYSTORE_FILE = "https.keystore.file";
    public static final String KEYSTORE_FILE_VALUE = "${carbon.home}/resources/security/wso2carbon.jks";
    public static final String KEYSTORE_PASSWORD = "https.keystore.password";
    public static final String KEYSTORE_PASSWORD_VALUE = "wso2carbon";
    public static final String CERT_PASSWORD = "https.cert.password";
    public static final String CERT_PASSWORD_VALUE = "wso2carbon";
    public static final String DEFAULT_PORT = "default.port";
    public static final String DEFAULT_PORT_VALUE = "9763";
    public static final String DEFAULT_HOST = "default.host";
    public static final String DEFAULT_PROTOCOL = "default.protocol";
    public static final String TRUSTSTORE_FILE = "https.truststore.file";
    public static final String TRUSTSTORE_FILE_VALUE = "${carbon.home}/resources/security/client-truststore.jks";
    public static final String TRUSTSTORE_PASSWORD = "https.truststore.password";
    public static final String TRUSTSTORE_PASSWORD_VALUE = "wso2carbon";

    //Http siddhi source properties
    public static final String RECEIVER_URL = "receiver.url";
    public static final String ISAUTH = "basic.auth.enabled";
    public static final String EMPTY_ISAUTH = "false";
    public static final String ATHORIZATION_HEADER = "Authorization";
    public static final String CARBON_SECURITY_CONFIGURATION = "CarbonSecurityConfig";
    public static final String MESSAGEPROCESSOR_ID = "Siddhi HTTP-message-processor";
}
