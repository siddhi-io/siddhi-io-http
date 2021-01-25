/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.http.source;

import io.siddhi.extension.io.http.metrics.EndpointStatus;
import io.siddhi.extension.io.http.metrics.SourceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.PortBindingEventListener;

import java.net.BindException;

/**
 * An implementation of the LifeCycleEventListener. This can be used to listen to the HTTP connector life cycle events.
 *
 * @since 0.94
 */
public class HttpConnectorPortBindingListener implements PortBindingEventListener {

    private static final Logger log = LoggerFactory.getLogger(HttpConnectorPortBindingListener.class);

    private ConnectorStartupSynchronizer connectorStartupSynchronizer;
    private String serverConnectorId;
    private SourceMetrics metrics;

    public HttpConnectorPortBindingListener(ConnectorStartupSynchronizer connectorStartupSynchronizer,
                                            String serverConnectorId, SourceMetrics metrics) {
        this.connectorStartupSynchronizer = connectorStartupSynchronizer;
        this.serverConnectorId = serverConnectorId;
        this.metrics = metrics;
    }

    @Override
    public void onOpen(String serverConnectorId, boolean isHttps) {
        if (metrics != null) {
            metrics.setEndpointStatusMetric(EndpointStatus.ONLINE);
        }

        if (isHttps) {
            log.info("HTTPS source " + serverConnectorId + " has been started");
        } else {
            log.info("HTTP source " + serverConnectorId + " has been started");
        }

        connectorStartupSynchronizer.getCountDownLatch().countDown();
    }

    @Override
    public void onClose(String serverConnectorId, boolean isHttps) {
        if (metrics != null) {
            metrics.setEndpointStatusMetric(EndpointStatus.OFFLINE);
        }

        if (isHttps) {
            log.info("HTTPS source " + serverConnectorId + " has been closed");
        } else {
            log.info("HTTP source " + serverConnectorId + " has been closed");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (metrics != null) {
            metrics.setEndpointStatusMetric(EndpointStatus.OFFLINE);
        }

        log.error("Error in http source ", throwable);

        if (throwable instanceof BindException) {
            connectorStartupSynchronizer.addException(serverConnectorId, (BindException) throwable);
            connectorStartupSynchronizer.getCountDownLatch().countDown();
        }
    }
}
