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
package io.siddhi.extension.io.http.util;

/**
 * {@code HttpConstant }Http Sink SinkConstants.
 */
public class HttpConstants {

    //source configuration
    //--source transport configuration
    public static final String WORKER_COUNT = "worker.count";
    public static final String DEFAULT_WORKER_COUNT = "1";
    //--source general configuration
    public static final String RECEIVER_URL = "receiver.url";
    public static final String SOURCE_ID = "source.id";
    public static final String SINK_ID = "sink.id";
    public static final String CONNECTION_TIMEOUT = "connection.timeout";
    public static final String IS_AUTH = "basic.auth.enabled";
    public static final String EMPTY_IS_AUTH = "false";
    public static final String MESSAGE_PROCESSOR_ID = "messageProcessorId";
    public static final String MESSAGE_PROCESSOR_ID_VALUE = "Siddhi HTTP-message-processor";
    public static final String LISTENER_PORT = "LISTENER_PORT";
    public static final String ALLOW_STREAMING_RESPONSES = "allow.streaming.responses";
    //Http siddhi sink properties
    public static final String AUTH_USERNAME_PASSWORD_SEPARATOR = ":";
    public static final String METHOD = "method";
    public static final String MESSAGE_ID = "message.id";
    public static final String PUBLISHER_URL = "publisher.url";
    public static final String HEADERS = "headers";
    public static final String RECEIVER_USERNAME = "basic.auth.username";
    public static final String RECEIVER_PASSWORD = "basic.auth.password";
    public static final String MAP_TEXT = "text";
    public static final String MAP_JSON = "json";
    public static final String MAP_XML = "xml";
    public static final String MAP_KEYVALUE = "keyvalue";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DOWNLOAD_ENABLED = "downloading.enabled";
    public static final String DEFAULT_DOWNLOAD_ENABLED_VALUE = "false";
    //Common util values
    public static final String HTTP_METHOD_POST = "POST"; //method name
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
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
    public static final String CREDENTIAL_SEPARATOR = ":";
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

    public static final String DEFAULT_SOURCE_SCHEME = "defaultScheme";
    public static final String DEFAULT_SOURCE_SCHEME_VALUE = "http";
    public static final String DEFAULT_TRACE_LOG_ENABLED = "httpTraceLogEnabled";
    public static final String TRACE_LOG_ENABLED = "trace.log.enabled";
    public static final String DEFAULT_TRACE_LOG_ENABLED_VALUE = "false";
    public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE = "serverBootstrapBossGroupSize";
    public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE = "serverBootstrapWorkerGroupSize";
    public static final String SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE = "serverBootstrapClientGroupSize";
    public static final String CLIENT_BOOTSTRAP_BOSS_GROUP_SIZE = "clientBootstrapBossGroupSize";
    public static final String CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE = "clientBootstrapWorkerGroupSize";
    public static final String CLIENT_BOOTSTRAP_CLIENT_GROUP_SIZE = "clientBootstrapClientGroupSize";
    public static final String DEFAULT_INTERFACE = "0.0.0.0:8280";
    //Http Source & sink variables
    public static final String SOCKET_IDEAL_TIMEOUT = "socket.idle.timeout";
    public static final String SOCKET_IDEAL_TIMEOUT_VALUE = "-1";
    public static final String SSL_VERIFY_CLIENT = "ssl.verify.client";
    public static final String SSL_PROTOCOL = "ssl.protocol";
    public static final String TLS_STORE_TYPE = "tls.store.type";
    @Deprecated
    public static final String SOURCE_PARAMETERS = "parameters";
    public static final String SSS_CONFIGS = "ssl.configurations";
    public static final String REQUEST_SIZE_VALIDATION_CONFIGS = "request.size.validation.configurations";
    public static final String SERVER_BOOTSTRAP_CONFIGS = "server.bootstrap.configurations";
    public static final String CLIENT_CHUNK_DISABLED = "chunk.disabled";

    public static final String CLIENT_BOOTSTRAP_CONFIGURATION = "client.bootstrap.configurations";
    public static final String PROXY_HOST = "proxy.host";
    public static final String PROXY_PORT = "proxy.port";
    public static final String PROXY_USERNAME = "proxy.username";
    public static final String PROXY_PASSWORD = "proxy.password";
    @Deprecated
    public static final String SINK_PARAMETERS = "parameters";
    public static final String MSG_ID = "msgId";

    //http sink transport properties
    public static final String LATENCY_METRICS_ENABLED = "latency.metrics.enabled";
    public static final String REQUEST_SIZE_VALIDATION = "request.size.validation";
    public static final String REQUEST_SIZE_VALIDATION_MAXIMUM_VALUE = "request.size.validation.maximum.value";
    public static final String REQUEST_SIZE_VALIDATION_REJECT_STATUS_CODE = "request.size.validation.reject.status" +
            ".code";
    public static final String REQUEST_SIZE_VALIDATION_REJECT_MESSAGE = "request.size.validation.reject.message";
    public static final String REQUEST_SIZE_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE = "request.size.validation.reject" +
            ".message.content.type";
    public static final String HEADER_SIZE_VALIDATION = "header.size.validation";
    public static final String HEADER_VALIDATION_MAXIMUM_REQUEST_LINE = "header.validation.maximum.request.line";
    public static final String HEADER_VALIDATION_MAXIMUM_SIZE = "header.validation.maximum.size";
    public static final String HEADER_VALIDATION_MAXIMUM_CHUNK_SIZE = "header.validation.maximum.chunk.size";
    public static final String HEADER_VALIDATION_REJECT_STATUS_CODE = "header.validation.reject.status.code";
    public static final String HEADER_VALIDATION_REJECT_MESSAGE = "header.validation.reject.message";
    public static final String HEADER_VALIDATION_REJECT_MESSAGE_CONTENT_TYPE = "header.validation.reject.message" +
            ".content.type";
    // Not read in the carbon http
    // public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE_PARAM = "server.bootstrap.boss.group.size";
    // public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE_PARAM = "server.bootstrap.worker.group.size";
    // public static final String SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE_PARAM = "server.bootstrap.client.group.size";
    // public static final String SERVER_BOOTSTRAP_SOCKET_REUSE_PARAM = "server.bootstrap.socket.reuse";
    public static final String SERVER_BOOTSTRAP_SOCKET_TIMEOUT_PARAM = "server.bootstrap.socket.timeout";
    public static final String SERVER_BOOTSTRAP_CONNECT_TIMEOUT_PARAM = "server.bootstrap.connect.timeout";
    public static final String SERVER_BOOTSTRAP_NODELAY_PARAM = "server.bootstrap.nodelay";
    public static final String SERVER_BOOTSTRAP_KEEPALIVE_PARAM = "server.bootstrap.keepalive";
    public static final String SERVER_BOOTSTRAP_SENDBUFFERSIZE_PARAM = "server.bootstrap.sendbuffersize";
    public static final String SERVER_BOOTSTRAP_RECIEVEBUFFERSIZE_PARAM = "server.bootstrap.recievebuffersize";
    public static final String SERVER_BOOTSTRAP_SOCKET_BACKLOG_PARAM = "server.bootstrap.socket.backlog";
    //http source transport properties

    public static final String CLIENT_BOOTSTRAP_NODELAY = "client.bootstrap.nodelay";
    public static final String CLIENT_BOOTSTRAP_KEEPALIVE = "client.bootstrap.keepalive";
    public static final String CLIENT_BOOTSTRAP_SENDBUFFERSIZE = "client.bootstrap.sendbuffersize";
    public static final String CLIENT_BOOTSTRAP_RECIEVEBUFFERSIZE = "client.bootstrap.recievebuffersize";
    public static final String CLIENT_BOOTSTRAP_CONNECT_TIMEOUT = "client.bootstrap.connect.timeout";
    public static final String CLIENT_BOOTSTRAP_SOCKET_REUSE = "client.bootstrap.socket.reuse";
    public static final String CLIENT_BOOTSTRAP_SOCKET_TIMEOUT = "client.bootstrap.socket.timeout";
    public static final String HTTP_TRACE_LOG_ENABLED = "httpTraceLogEnabled";
    public static final String LOG_TRACE_ENABLE_DEFAULT_VALUE = "false";
    public static final String DEFAULT_ENCODE_PAYLOAD_VALUE = "false";
    public static final String PARAMETER_SEPARATOR = "','";
    public static final String VALUE_SEPARATOR = ":";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String FILE_URL = "file.url";
    public static final String DOWNLOAD_PATH = "download.path";
    public static final String IS_DOWNLOADABLE_CONTENT = "__is_downloadable_content";
    // HTTP codes for response source
    public static final String HTTP_STATUS_CODE = "http.status.code";
    public static final String DEFAULT_HTTP_SUCCESS_CODE = "200";
    // HTTP Default ports if the port is not present in the given URL
    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;
    //getting refreshtoken and new access token
    public static final String PATH = "path";
    public static final String TOKEN_URL = "token.url";
    public static final String CONSUMER_SECRET = "consumer.secret";
    public static final String CONSUMER_KEY = "consumer.key";
    public static final String GRANT_TYPE = "grant_type";
    public static final String GRANT_PASSWORD = "password";
    public static final String GRANT_REFRESHTOKEN = "refresh_token";
    public static final String GRANT_CLIENTTOKEN = "client_credentials";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final int SUCCESS_CODE = 200;
    public static final int MULTIPLE_CHOICES = 300;
    public static final int CLIENT_REQUEST_TIMEOUT = 408;
    public static final int AUTHENTICATION_FAIL_CODE = 401;
    public static final int PERSISTENT_ACCESS_FAIL_CODE = 400;
    public static final int INTERNAL_SERVER_FAIL_CODE = 500;
    public static final String RECEIVER_OAUTH_USERNAME = "oauth.username";
    public static final String RECEIVER_OAUTH_PASSWORD = "oauth.password";
    public static final String RECEIVER_REFRESH_TOKEN = "refresh.token";
    public static final String BASIC_AUTH = "basic.auth";
    public static final String NO_AUTH = "no.auth";
    public static final String OAUTH = "oauth";
    public static final int MAXIMUM_TRY_COUNT = 2;
    public static final int MINIMUM_TRY_COUNT = 1;
    public static final String NEW_LINE = "\n";
    public static final String BEARER = "Bearer ";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String BLOCKING_IO = "blocking.io";

    //pool configurations
    public static final String MAX_ACTIVE_CONNECTIONS_PER_POOL = "max.pool.active.connections";
    public static final String DEFAULT_MAX_ACTIVE_CONNECTIONS_PER_POOL = "-1"; // unlimited
    public static final String MIN_IDLE_CONNECTIONS_PER_POOL = "min.pool.idle.connections";
    public static final String DEFAULT_MIN_IDLE_CONNECTIONS_PER_POOL = "0";
    public static final String MAX_IDLE_CONNECTIONS_PER_POOL = "max.pool.idle.connections";
    public static final String DEFAULT_MAX_IDLE_CONNECTIONS_PER_POOL = "100";
    public static final String MIN_EVICTABLE_IDLE_TIME = "min.evictable.idle.time";
    public static final String DEFAULT_MIN_EVICTABLE_IDLE_TIME = "300000";
    public static final String TIME_BETWEEN_EVICTION_RUNS = "time.between.eviction.runs";
    public static final String DEFAULT_TIME_BETWEEN_EVICTION_RUNS = "30000";
    public static final String TEST_ON_BORROW = "test.on.borrow";
    public static final String DEFAULT_TEST_ON_BORROW = "true";
    public static final String TEST_WHILE_IDLE = "test.while.idle";
    public static final String DEFAULT_TEST_WHILE_IDLE = "true";
    public static final String EXHAUSTED_ACTION = "exhausted.action";
    public static final String DEFAULT_EXHAUSTED_ACTION = "1"; // block when exhausted
    public static final String MAX_WAIT_TIME = "max.wait.time";
    public static final String DEFAULT_MAX_WAIT_TIME = "60000";

    public static final String HOSTNAME_VERIFICATION_ENABLED = "hostname.verification.enabled";
    public static final String SSL_VERIFICATION_DISABLED = "ssl.verification.disabled";
}
