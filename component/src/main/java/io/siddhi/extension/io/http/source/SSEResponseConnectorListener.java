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

package io.siddhi.extension.io.http.source;

import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.extension.io.http.metrics.SourceMetrics;

/**
 * Extended HttpCallResponseConnectorListener for HttpSSESource, where shouldAllowStreamingResponses is true.
 */
public class SSEResponseConnectorListener extends HttpCallResponseConnectorListener {
    public SSEResponseConnectorListener(int numberOfThreads, SourceEventListener sourceEventListener,
                                        String streamId, String[] trpPropertyNames, String siddhiAppName,
                                        SourceMetrics metrics) {
        super(numberOfThreads, sourceEventListener, true, streamId,
                trpPropertyNames, siddhiAppName, metrics);
    }
}
