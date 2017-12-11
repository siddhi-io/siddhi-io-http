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
 * {@code HttpConstant }Http Sink SinkConstants.
 */
public class HttpConstants {

    private HttpConstants() {
    }

    //source configuration
    //--source transport configuration
    public static final String WORKER_COUNT = "worker.count";
    public static final String DEFAULT_WORKER_COUNT = "1";

    //--source general configuration
    public static final String RECEIVER_URL = "receiver.url";
    public static final String IS_AUTH = "basic.auth.enabled";
    public static final String EMPTY_IS_AUTH = "false";
    public static final String MESSAGE_PROCESSOR_ID = "messageProcessorId";
    public static final String MESSAGE_PROCESSOR_ID_VALUE = "Siddhi HTTP-message-processor";
    public static final String LISTENER_PORT = "LISTENER_PORT";

    //Http siddhi sink properties
    public static final String AUTH_USERNAME_PASSWORD_SEPARATOR = ":";
    public static final String METHOD = "method";
    public static final String PUBLISHER_URL = "publisher.url";
    public static final String HEADERS = "headers";
    public static final String RECEIVER_USERNAME = "basic.auth.username";
    public static final String RECEIVER_PASSWORD = "basic.auth.password";
    public static final String MAP_TEXT = "text";
    public static final String MAP_JSON = "json";
    public static final String MAP_XML = "xml";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_ENCODING = "UTF-8";

    //Common util values
    public static final String HTTP_METHOD_POST = "POST"; //method name
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String DEFAULT_METHOD = HTTP_METHOD_POST;
    public static final String DEFAULT_HEADER = null;
    public static final String PROTOCOL_ID = "http";
    public static final String HEADER_SPLITTER_REGEX = "','";
    public static final String HEADER_NAME_VALUE_SPLITTER = ":";
    public static final String HTTP_SINK_ID = "http sink";
    public static final String METHOD_DEFAULT = "POST";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String SCHEME = "SCHEME";
    public static final String PORT = "PORT";
    public static final String HOST = "HOST";
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_METHOD = "Basic ";
    public static final String EMPTY_STRING = "";
    public static final String TO = "TO";
    public static final String DEFAULT_HOST_VALUE = "0.0.0.0";
    public static final String PROTOCOL_HOST_SEPARATOR = "://";
    public static final String PORT_HOST_SEPARATOR = ":";
    public static final String PORT_CONTEXT_SEPARATOR = "/";
    public static final String PORT_CONTEXT_KEY_SEPARATOR = "-";

    //GlobaleConfigurations
    public static final String HTTP_PORT = "defaultHttpPort";
    public static final String HTTP_PORT_VALUE = "8280";
    public static final String HTTPS_PORT = "defaultHttpsPort";
    public static final String HTTPS_PORT_VALUE = "8243";
    public static final String DEFAULT_HOST = "defaultHost";
    public static final String CLIENT_TRUSTSTORE_PATH = "trustStoreLocation";
    public static final String CLIENT_TRUSTSTORE_PATH_PARAM = "https.truststore.file";
    public static final String CLIENT_TRUSTSTORE_PATH_VALUE = "${carbon.home}/resources/security/client-truststore.jks";
    public static final String CLIENT_TRUSTSTORE_PASSWORD = "trustStorePassword";
    public static final String CLIENT_TRUSTSTORE_PASSWORD_PARAM = "https.truststore.password";
    public static final String CLIENT_TRUSTSTORE_PASSWORD_VALUE = "wso2carbon";
    public static final String KEYSTORE_FILE = "keyStoreLocation";
    public static final String KEYSTORE_FILE_VALUE = "${carbon.home}/resources/security/wso2carbon.jks";
    public static final String KEYSTORE_PASSWORD = "keyStorePassword";
    public static final String KEYSTORE_PASSWORD_VALUE = "wso2carbon";
    public static final String CERT_PASSWORD = "certPassword";
    public static final String CERT_PASSWORD_VALUE = "wso2carbon";
    public static final String DEFAULT_SOURCE_SCHEME = "defaultScheme";
    public static final String DEFAULT_SOURCE_SCHEME_VALUE = "http";
    public static final String DEFAULT_TRACE_LOG_ENABLED = "httpTraceLogEnabled";
    public static final String TRACE_LOG_ENABLED = "trace.log.enabled";
    public static final String DEFAULT_TRACE_LOG_ENABLED_VALUE = "false";

    public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE = "serverBootstrapBossGroupSize";
    public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE_VALUE = "";
    public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE = "serverBootstrapWorkerGroupSize";
    public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE_VALUE = "";
    public static final String CLIENT_BOOTSTRAP_BOSS_GROUP_SIZE = "clientBootstrapBossGroupSize";
    public static final String CLIENT_BOOTSTRAP_BOSS_GROUP_SIZE_VALUE = "";
    public static final String CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE = "clientBootstrapWorkerGroupSize";
    public static final String CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE_VALUE = "";
    public static final String DEFAULT_INTERFACE = "0.0.0.0:9090";

    //Http Source & sink variables
    public static final String SOCKET_IDEAL_TIMEOUT = "socket.idle.timeout";
    public static final String SSL_VERIFY_CLIENT = "ssl.verify.client";
    public static final String SSL_PROTOCOL = "ssl.protocol";
    public static final String TLS_STORE_TYPE = "tls.store.type";
    public static final String SOURCE_PARAMETERS = "parameters";
    public static final String REQUEST_SIZE_VALIDATION_CONFIG = "request.size.validation.configurations";
    public static final String SERVER_BOOTSTRAP_CONFIGURATION = "server.bootstrap.configurations";

    public static final String CLIENT_CHUNK_ENABLED = "chunk.disabled";
    public static final String CLIENT_FOLLOW_REDIRECT = "follow.redirect";
    public static final String CLIENT_MAX_REDIRECT_COUNT = "max.redirect.count";
    public static final String CLIENT_BOOTSTRAP_CONFIGURATION = "client.bootstrap.configurations";
    public static final String CLIENT_POOL_CONFIGURATION = "client.threadpool.configurations";
    public static final String PROXY_HOST = "proxy.host";
    public static final String PROXY_PORT = "proxy.port";
    public static final String PROXY_USERNAME = "proxy.username";
    public static final String PROXY_PASSWORD = "proxy.password";
    public static final String SINK_PARAMETERS = "parameters";
}
