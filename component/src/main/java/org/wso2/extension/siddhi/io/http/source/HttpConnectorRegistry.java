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
package org.wso2.extension.siddhi.io.http.source;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.http.source.exception.HttpSourceAdaptorRuntimeException;
import org.wso2.extension.siddhi.io.http.source.util.HttpSourceUtil;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfiguration;
import org.wso2.transport.http.netty.config.TransportProperty;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * {@code HttpConnectorRegistry} The code is responsible for maintaining the all active server connectors.
 */
class HttpConnectorRegistry {
    private final Logger log = Logger.getLogger(HttpConnectorRegistry.class);
    private static HttpConnectorRegistry instance = new HttpConnectorRegistry();
    private Map<String, HttpServerConnectorContext> serverConnectorPool = new ConcurrentHashMap<>();
    private Map<String, HttpSourceListener> sourceListenersMap = new ConcurrentHashMap<>();
    private TransportsConfiguration trpConfig;
    private HttpWsConnectorFactory httpConnectorFactory;

    private HttpConnectorRegistry() {
    }

    RequestSizeValidationConfiguration populateRequestSizeValidationConfiguration() {
        return new RequestSizeValidationConfiguration(trpConfig.getTransportProperties());
    }

    void setTrpConfig(String serverBootstrapConfigurationList, String serverHeaderValidation) {
        trpConfig = new TransportsConfiguration();
        Set<TransportProperty> transportProperties = new HashSet<>();
        if (!HttpConstants.EMPTY_STRING.equals(serverBootstrapConfigurationList.trim())) {
            try {
                String[] valueList = serverBootstrapConfigurationList.trim().substring(1,
                        serverBootstrapConfigurationList.length
                                () - 1).split("','");
                Map<String, String> bootstrapValueMap = Arrays.stream(valueList).collect(Collectors.toMap(
                        (valueEntry) -> valueEntry.split(":")[0],
                        (valueEntry) -> valueEntry.split(":")[1]
                        )
                );
                trpConfig.setTransportProperties(HttpSourceUtil.getInstance().populateServerBootstrapConfigurations
                        (bootstrapValueMap, transportProperties));
            } catch (ArrayIndexOutOfBoundsException ex) {
                log.error("Bootstrap configuration is not in expected format please insert them as 'key1:val1'," +
                        "'key2:val2' format");
            }
        }

        if (!HttpConstants.EMPTY_STRING.equals(serverHeaderValidation.trim())) {
            try {
                String[] valueList = serverHeaderValidation.trim().substring(1, serverHeaderValidation.length
                        () - 1).split("','");
                Map<String, String> headerValidationValueMap = Arrays.stream(valueList).collect(Collectors.toMap(
                        (valueEntry) -> valueEntry.split(":")[0],
                        (valueEntry) -> valueEntry.split(":")[1]
                        )
                );
                trpConfig.setTransportProperties(HttpSourceUtil.getInstance().populateHeaderValidationConfigurations
                        (headerValidationValueMap, transportProperties));
            } catch (ArrayIndexOutOfBoundsException ex) {
                log.error("Header validation configuration is not in expected format please insert them as " +
                        "'key1:val1'," +
                        "'key2:val2' format");
            }
        }
    }

    /**
     * Get HttpConnectorRegistry instance.
     *
     * @return HttpConnectorRegistry instance
     */
    static HttpConnectorRegistry getInstance() {
        return instance;
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
     */
    void registerSourceListener(SourceEventListener sourceEventListener, String listenerUrl, int
            workerThread, Boolean isAuth, String[] requestedTransportPropertyNames) {
        String listenerKey = HttpSourceUtil.getSourceListenerKey(listenerUrl);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.putIfAbsent(listenerKey,
                new HttpSourceListener(workerThread, listenerUrl, isAuth, sourceEventListener
                        , requestedTransportPropertyNames));
        if (httpSourceListener != null) {
            throw new SiddhiAppCreationException("Listener URL " + listenerUrl + " already connected");
        }
    }

    /**
     * Unregister the source listener.
     *
     * @param listenerUrl the listener url
     */
    void unregisterSourceListener(String listenerUrl) {
        String key = HttpSourceUtil.getSourceListenerKey(listenerUrl);
        HttpSourceListener httpSourceListener = this.sourceListenersMap.remove(key);
        if (httpSourceListener != null) {
            httpSourceListener.disconnect();
        }
    }

    /**
     * Initialize and start the server connector factory. This should be created at once for siddhi.
     *
     * @param sourceConfigReader the siddhi source config reader.
     */
    synchronized void initBootstrapConfigIfFirst(ConfigReader sourceConfigReader) {
        // to make sure it will create only once
        if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory == null)) {
            String bootstrapWorker = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_WORKER_GROUP_SIZE, HttpConstants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE_VALUE);
            String bootstrapBoss = sourceConfigReader.readConfig(HttpConstants
                    .SERVER_BOOTSTRAP_BOSS_GROUP_SIZE, HttpConstants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE_VALUE);
            if (!HttpConstants.EMPTY_STRING.equals(bootstrapBoss) && !HttpConstants.EMPTY_STRING.equals
                    (bootstrapWorker)) {
                httpConnectorFactory = new HttpWsConnectorFactoryImpl(Integer.parseInt(bootstrapBoss), Integer
                        .parseInt(bootstrapWorker));
            } else {
                httpConnectorFactory = new HttpWsConnectorFactoryImpl();
            }
        }
    }

    /**
     * Stop server connector controller.
     */
    void clearBootstrapConfigIfLast() {
        synchronized (this) {
            if ((this.sourceListenersMap.isEmpty()) && (httpConnectorFactory != null)) {
                this.httpConnectorFactory = null;
            }
        }
    }


    void createHttpServerConnector(ListenerConfiguration listenerConfig) {
        synchronized (this) {
            String listenerInterface = listenerConfig.getHost() + ":" + listenerConfig.getPort();
            HttpServerConnectorContext httpServerConnectorContext =
                    serverConnectorPool.get(listenerInterface);
            if (httpServerConnectorContext != null) {
                if (checkForConflicts(listenerConfig, httpServerConnectorContext)) {
                    throw new HttpSourceAdaptorRuntimeException("Conflicting configuration detected for listener " +
                            "configuration id " + listenerConfig.getId());
                }
                httpServerConnectorContext.incrementReferenceCount();
                return;
            }
            ServerBootstrapConfiguration serverBootstrapConfiguration = HTTPConnectorUtil
                    .getServerBootstrapConfiguration(trpConfig.getTransportProperties());
            ServerConnector serverConnector =
                    httpConnectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfig);
            httpServerConnectorContext = new HttpServerConnectorContext(serverConnector, listenerConfig);
            serverConnectorPool.put(serverConnector.getConnectorID(), httpServerConnectorContext);
            httpServerConnectorContext.incrementReferenceCount();
            this.registerServerConnector(serverConnector, listenerConfig);
        }
    }

    void registerServerConnector(org.wso2.transport.http.netty.contract.ServerConnector
                                         serverConnector, ListenerConfiguration listenerConfig) {
        ServerConnectorFuture connectorFuture = serverConnector.start();
        ConnectorStartupSynchronizer startupSyncer =
                new ConnectorStartupSynchronizer(new CountDownLatch(1));
        setConnectorListeners(connectorFuture, serverConnector.getConnectorID(), startupSyncer);
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
                    if (ConnectionManager.getInstance() != null) {
                        ConnectionManager.getInstance().getTargetChannelPool().clear();
                    }
                    log.info("Server connector for port '" + port + "' has successfully shutdown.");
                    context.decrementReferenceCount();
                    return context.getServerConnector().stop();
                }
                context.decrementReferenceCount();
            }
            return false;
        }
    }

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

    /**
     * This method wil check that if there is already registered server connectors which may be http but if it have
     * jks security setup then it can be use as https transport as well
     * listener configuration
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
                        || !listenerConfiguration.getKeyStorePass().equals(config.getKeyStorePass())
                        || !listenerConfiguration.getCertPass().equals(config.getCertPass())) {
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

    private void setConnectorListeners(ServerConnectorFuture connectorFuture, String serverConnectorId,
                                       ConnectorStartupSynchronizer startupSyncer) {
        connectorFuture.setHttpConnectorListener(new HTTPConnectorListener());
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId));
    }

    private void validateConnectorStartup(ConnectorStartupSynchronizer startupSyncer) {
        int noOfExceptions = startupSyncer.getExceptions().size();
        if (noOfExceptions <= 0) {
            return;
        }

        startupSyncer.getExceptions().forEach((connectorId, e) -> {
            log.error("siddhi: " + e.getMessage() + ": [" + connectorId + "]");
        });

        if (noOfExceptions == 1) {
            // If the no. of exceptions is equal to the no. of connectors to be started, then none of the
            // connectors have started properly and we can terminate the runtime
            throw new HttpSourceAdaptorRuntimeException("failed to start the server connectors");
        }
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
}
