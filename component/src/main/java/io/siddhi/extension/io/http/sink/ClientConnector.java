/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.extension.io.http.sink;

import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Map;

/**
 * Class to have the client connector and related properties
 */
public class ClientConnector {

    private String publisherURL;
    private Map<String, String> httpURLProperties;
    private HttpClientConnector httpClientConnector;

    public ClientConnector(String publisherURL, Map<String, String> httpURLProperties,
                           HttpClientConnector httpClientConnector) {
        this.publisherURL = publisherURL;
        this.httpURLProperties = httpURLProperties;
        this.httpClientConnector = httpClientConnector;
    }

    public String getPublisherURL() {
        return publisherURL;
    }

    public HttpResponseFuture send(HttpCarbonMessage cMessage) {
        return httpClientConnector.send(cMessage);
    }

    public Map<String, String> getHttpURLProperties() {
        return httpURLProperties;
    }

}
