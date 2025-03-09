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
package io.siddhi.extension.io.http.source;

import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.table.Table;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import io.siddhi.extension.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import io.siddhi.extension.io.http.source.util.HttpSourceUtil;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.InboundMsgSizeValidationConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportProperty;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static io.siddhi.extension.io.http.util.HttpConstants.PARAMETER_SEPARATOR;
import static io.siddhi.extension.io.http.util.HttpIoUtil.populateParameterMap;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
public class HttpConnectorRegistry {
    private static HttpConnectorRegistry instance = new HttpConnectorRegistry();
    private static final Logger log = LogManager.getLogger(HttpConnectorRegistry.class);
    protected TransportsConfiguration trpConfig;
    protected DefaultHttpWsConnectorFactory httpConnectorFactory;
    private Map<String, HttpServerConnectorContext> serverConnectorPool = new ConcurrentHashMap<>();
    private Map<String, HttpSourceListener> sourceListenersMap = new ConcurrentHashMap<>();

    protected HttpConnectorRegistry() {
    }

    /**
     * Get HttpConnectorRegistry instance.
     *
     * @return HttpConnectorRegistry instance
     */
    static HttpConnectorRegistry getInstance() {
        return instance;
    }

    private static String getSeverConnectorKey(String listenerUrl) {
        URL aURL;
        try {
            aURL = new URL(listenerUrl);
        } catch (MalformedURLException e) {
            throw new SiddhiAppCreationException("Server connector is not in a proper format ", e);
        }
        return aURL.getHost() + ":" + String.valueOf(aURL.getPort());
    }

    InboundMsgSizeValidationConfig populateRequestSizeValidationConfiguration() {
        return new InboundMsgSizeValidationConfig();
    }

    /**
     * Set transport properties.
     *
     * @param serverBootstrapConfigurationList bootstrap configurations.
     * @param serverHeaderValidation           header validation configurations.
     */
    void setTransportConfig(String serverBootstrapConfigurationList, String serverHeaderValidation) {
        trpConfig = new TransportsConfiguration();
        Set<TransportProperty> transportProperties = new HashSet<>();
        if (!HttpConstants.EMPTY_STRING.equals(serverBootstrapConfigurationList.trim())) {
            String[] valueList = serverBootstrapConfigurationList.trim()
                    .substring(1, serverBootstrapConfigurationList.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            trpConfig.setTransportProperties(HttpSourceUtil.populateBootstrapConfigurations
                    (populateParameterMap(valueList), transportProperties));
        }
        if (!HttpConstants.EMPTY_STRING.equals(serverHeaderValidation.trim())) {
            String[] valueList = serverHeaderValidation.trim()
                    .substring(1, serverHeaderValidation.length() - 1)
                    .split(PARAMETER_SEPARATOR);
            trpConfig.setTransportProperties(HttpSourceUtil.populateTransportProperties
                    (populateParameterMap(valueList), transportProperties));
        }
    }

    /**
     * Get the source listener map.
     *
     * @return the source listener map
     */
    Map<String, HttpSourceListener> getSourceListenersMap() {
        return this.sourceListenersMap;
    }

    /**
     * Register new source listener.
     *
     * @param sourceEventListener the source event listener.
     * @param listenerUrl         the listener url.
     * @param workerThread        the worker thread count of siddhi level thread pool executor.
     * @param isAuth              the authentication is required for source listener.
     * @param siddhiAppName       the Siddhi application name
     */
    void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl,
                                int workerThread, Boolean isAuth, String[] requestedTransportPropertyNames,
                                String siddhiAppName, SourceMetrics metrics) {
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener,
                        requestedTransportPropertyNames, siddhiAppName, metrics));
        if (httpSourceListener != null) {
            if (metrics != null) {
                metrics.getTotalHttpErrorsMetric().inc();
            }
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    /**
     * Register new source listener.
     *
     * @param sourceEventListener the source event listener.
     * @param listenerUrl         the listener url.
     * @param workerThread        the worker thread count of siddhi level thread pool executor.
     * @param isAuth              the authentication is required for source listener.
     * @param siddhiAppName       the Siddhi application name
     * @param metrics             Source metrics object
     * @param table               Subscription data table
     * @param hubId               webhub ID
     * @param siddhiAppContext    siddhiAppContext
     */
    void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl, int workerThread,
                                Boolean isAuth, String[] requestedTransportPropertyNames,
                                String siddhiAppName, SourceMetrics metrics, Table table, String hubId,
                                SiddhiAppContext siddhiAppContext, List<String> topics) {
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener,
                        requestedTransportPropertyNames, siddhiAppName, metrics, table, hubId, siddhiAppContext,
                        topics));
        if (httpSourceListener != null) {
            if (metrics != null) {
                metrics.getTotalHttpErrorsMetric().inc();
            }
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param listenerUrl   the listener url
     * @param siddhiAppName siddhi app name
     * @param metrics source metrics
     */
    protected void unregisterSourceListener(String listenerUrl, String siddhiAppName, SourceMetrics metrics) {
        String key = HttpSourceUtil.getSourceListenerKey(listenerUrl, metrics);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.get(key);
        if (httpSourceListener != null && httpSourceListener.getSiddhiAppName().equals(siddhiAppName)) {
            sourceListenersMap.remove(key);
            httpSourceListener.disconnect();
        }
    }

    /**
     * Initialize and start the server connector factory. This should be created at once for siddhi.
     *
     * @param sourceConfigReader the siddhi source config reader.
     */
    protected synchronized void initBootstrapConfigIfFirst(ConfigReader sourceConfigReader) {
        // to make sure it will create only once
        if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory == null)) {
            String bootstrapWorker = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            String bootstrapBoss = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            String bootstrapClient = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_CLIENT_GROUP_SIZE, HttpConstants.EMPTY_STRING);
            if (!HttpConstants.EMPTY_STRING.equals(bootstrapBoss) && !HttpConstants.EMPTY_STRING.equals
                    (bootstrapWorker)) {
                if (!HttpConstants.EMPTY_STRING.equals(bootstrapClient)) {
                    httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss), Integer
                            .parseInt(bootstrapWorker), Integer.parseInt(bootstrapClient));
                } else {
                    httpConnectorFactory = new DefaultHttpWsConnectorFactory(Integer.parseInt(bootstrapBoss), Integer
                            .parseInt(bootstrapWorker), Integer.parseInt(bootstrapWorker));
                }
            } else {
                httpConnectorFactory = new DefaultHttpWsConnectorFactory();
            }
        }
    }

    /**
     * Stop server connector controller.
     */
    protected void clearBootstrapConfigIfLast() {
        synchronized (this) {
            if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory != null)) {
                httpConnectorFactory.shutdownNow();
                this.httpConnectorFactory = null;
            }
        }
    }

    /**
     * Create http server connector for given listener configurations.
     *
     * @param listenerConfig listener configurations.
     */
    void createHttpServerConnector(ListenerConfiguration listenerConfig, SourceMetrics metrics) {
        synchronized (this) {
            String listenerInterface = listenerConfig.getHost() + ":" + listenerConfig.getPort();
            HttpServerConnectorContext httpServerConnectorContext =
                    serverConnectorPool.get(listenerInterface);
            if (httpServerConnectorContext != null) {
                if (checkForConflicts(listenerConfig, httpServerConnectorContext)) {
                    if (metrics != null) {
                        metrics.getTotalHttpErrorsMetric().inc();
                    }

                    throw new HttpSourceAdaptorRuntimeException("Conflicting configuration detected for listener " +
                            "configuration id " + listenerConfig.getId());
                }
                httpServerConnectorContext.incrementReferenceCount();
                return;
            }
            ServerBootstrapConfiguration serverBootstrapConfiguration = HttpConnectorUtil
                    .getServerBootstrapConfiguration(trpConfig.getTransportProperties());
            ServerConnector serverConnector =
                    httpConnectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfig);
            httpServerConnectorContext = new HttpServerConnectorContext(serverConnector, listenerConfig);
            serverConnectorPool.put(serverConnector.getConnectorID(), httpServerConnectorContext);
            httpServerConnectorContext.incrementReferenceCount();
            this.registerServerConnector(serverConnector, listenerConfig, metrics);
        }
    }

    /**
     * Register new server connector.
     *
     * @param serverConnector server connector.
     * @param listenerConfig  listener configuration.
     */
    void registerServerConnector(ServerConnector serverConnector, ListenerConfiguration listenerConfig,
                                 SourceMetrics metrics) {
        ServerConnectorFuture connectorFuture = serverConnector.start();
        ConnectorStartupSynchronizer startupSyncer =
                new ConnectorStartupSynchronizer(new CountDownLatch(1));
        setConnectorListeners(connectorFuture, serverConnector.getConnectorID(), startupSyncer, metrics);
        try {
            // Wait for all the connectors to start
            startupSyncer.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new HttpSourceAdaptorRuntimeException("Error in starting HTTP server connector for server: " +
                    listenerConfig.getHost() + ":" + listenerConfig.getPort(), e);
        }
        validateConnectorStartup(startupSyncer);
    }

    Map<String, HttpServerConnectorContext> getServerConnectorPool() {
        return serverConnectorPool;
    }

    /**
     * Register the new server connector.
     *
     * @param listenerUrl the listener url
     */
    boolean unregisterServerConnector(String listenerUrl) {
        String port = HttpSourceUtil.getPort(listenerUrl);
        synchronized (this) {
            HttpServerConnectorContext context = serverConnectorPool.get(getSeverConnectorKey(listenerUrl));
            if (context != null) {
                if (context.getReferenceCount() == 1) {
                    serverConnectorPool.remove(getSeverConnectorKey(listenerUrl));
                    log.info("Server connector for port '{}' has successfully shutdown.", port);
                    context.decrementReferenceCount();
                    return context.getServerConnector().stop();
                }
                context.decrementReferenceCount();
            }
            return false;
        }
    }

    /**
     * This method wil check that if there is already registered server connectors which may be http but if it have
     * jks security setup then it can be use as https transport as well
     * listener configuration.
     *
     * @param listenerConfiguration server listener configuration.
     * @param context               server connector context handler
     * @return conflict exits or not.
     */
    private boolean checkForConflicts(ListenerConfiguration listenerConfiguration,
                                      HttpServerConnectorContext context) {
        if (context == null) {
            return false;
        }
        if (listenerConfiguration.getScheme().equalsIgnoreCase("https")) {
            ListenerConfiguration config = context.getListenerConfiguration();
            if (config.getScheme().equalsIgnoreCase("https")) {
                if (!listenerConfiguration.getKeyStoreFile().equals(config.getKeyStoreFile())
                        || !listenerConfiguration.getKeyStorePass().equals(config.getKeyStorePass())) {
                    log.info("There is already registered https server connector for same host:port which has " +
                            " conflicting configurations.");
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    protected void setConnectorListeners(ServerConnectorFuture connectorFuture, String serverConnectorId,
                                         ConnectorStartupSynchronizer startupSyncer, SourceMetrics metrics) {
        connectorFuture.setHttpConnectorListener(new HTTPConnectorListener());
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId, metrics));
    }

    private void validateConnectorStartup(ConnectorStartupSynchronizer startupSyncer) {
        int noOfExceptions = startupSyncer.getExceptions().size();
        if (noOfExceptions <= 0) {
            return;
        }

        startupSyncer.getExceptions().forEach((connectorId, e) -> {
            log.error("siddhi: {}: [{}]", e.getMessage(), connectorId, e);
        });

        if (noOfExceptions == 1) {
            // If the no. of exceptions is equal to one there is an error has occured.
            throw new HttpSourceAdaptorRuntimeException("failed to start the server connectors");
        }
    }

    /**
     * The server connector context.
     */
    private static class HttpServerConnectorContext {
        private ServerConnector serverConnector;
        private ListenerConfiguration listenerConfiguration;
        private int referenceCount = 0;

        public HttpServerConnectorContext(ServerConnector
                                                  serverConnector, ListenerConfiguration listenerConfiguration) {
            this.serverConnector = serverConnector;
            this.listenerConfiguration = listenerConfiguration;
        }

        public void incrementReferenceCount() {
            this.referenceCount++;
        }

        public void decrementReferenceCount() {
            this.referenceCount--;
        }

        public ServerConnector getServerConnector() {
            return this.serverConnector;
        }

        public ListenerConfiguration getListenerConfiguration() {
            return this.listenerConfiguration;
        }

        public int getReferenceCount() {
            return this.referenceCount;
        }
    }
}
