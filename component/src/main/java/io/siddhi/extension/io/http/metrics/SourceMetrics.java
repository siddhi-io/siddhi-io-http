/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.siddhi.extension.io.http.metrics;

import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.si.metrics.core.internal.MetricsDataHolder;

/**
 * Class to publish source metrics.
 */
public class SourceMetrics extends Metrics {
    private final String url;

    public SourceMetrics(String siddhiAppName, String streamName, String url) {
        super(siddhiAppName, streamName);
        this.url = url;
    }

    // To count the total reads from siddhi app level
    public Counter getTotalReadsMetric() {
        return MetricsDataHolder.getInstance().getMetricService()
                .counter(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Total.Reads.%s",
                        siddhiAppName, "http"), Level.INFO);
    }

    // To count the total HTTP reads
    public Counter getTotalHttpReadsMetric() {
        return MetricsDataHolder.getInstance().getMetricService()
                .counter(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Http.Source.Total.Reads.%s.%s",
                        siddhiAppName, streamName, url), Level.INFO);
    }

    // To count the total HTTP errors
    public Counter getTotalHttpErrorsMetric() {
        return MetricsDataHolder.getInstance().getMetricService()
                .counter(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Http.Source.Total.Errors.%s.%s",
                        siddhiAppName, streamName, url), Level.INFO);
    }

    // To count the request size
    public Counter getRequestSizeMetric() {
        return MetricsDataHolder.getInstance().getMetricService()
                .counter(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Http.Source.Total.Request.Size.%s.%s",
                        siddhiAppName, streamName, url), Level.INFO);
    }

    // To set the online/offline status
    public void setEndpointStatusMetric(EndpointStatus status) {
        MetricsDataHolder.getInstance().getMetricService()
                .gauge(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Http.Source.Endpoint.Status.%s.%s",
                        siddhiAppName, streamName, url), Level.INFO, status::ordinal);
    }

    // To set the last event time
    public void setLastEventTime(long lastEventTime) {
        MetricsDataHolder.getInstance().getMetricService()
                .gauge(String.format("io.siddhi.SiddhiApps.%s.Siddhi.Http.Source.Last.Event.Time.%s.%s",
                        siddhiAppName, streamName, url), Level.INFO, () -> lastEventTime);
    }
}
