/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.extension.io.http.sink.updatetoken;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.siddhi.extension.io.http.util.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * {@code HttpRequest} Handle the HTTP request for invalid access token.
 */
public class HttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    public static ArrayList<String> sendPostRequest(HttpClientConnector httpClientConnector, String serverScheme,
                                                    String serverHost, int serverPort, String serverPath,
                                                    String payload, Map<String, String> headers) {
        ArrayList<String> responses = new ArrayList<>();

        HttpCarbonMessage msg = createHttpPostReq(serverScheme, serverHost, serverPort, serverPath, payload);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            msg.setHeader(entry.getKey(), entry.getValue());
        }
        CountDownLatch latch = new CountDownLatch(1);
        DefaultListener listener = new DefaultListener(latch, HttpConstants.OAUTH);
        HttpResponseFuture responseFuture = httpClientConnector.send(msg);
        responseFuture.setHttpConnectorListener(listener);
        try {
            boolean latchCount = latch.await(30, TimeUnit.SECONDS);
            if (!latchCount) {
                LOG.debug("Time out due to getting new access token. ");
            }
        } catch (InterruptedException e) {
            LOG.debug("Time out due to getting new access token. " + e);
        }
        HttpCarbonMessage response = listener.getHttpResponseMessage();
        String statusCode = Integer.toString(response.getNettyHttpResponse().status().code());
        responses.add(statusCode);
        InputStream httpMessageDataStreamer = new HttpMessageDataStreamer(response).getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(httpMessageDataStreamer, Charset.defaultCharset());
        try (BufferedReader buffer = new BufferedReader(inputStreamReader)) {
            String responsePayload = buffer.lines().collect(Collectors.joining("\n"));
            responses.add(responsePayload);
        } catch (IOException e) {
            LOG.debug("There was an error in reading the file while generating new access token. " + e);
        }
        return responses;
    }

    private static HttpCarbonMessage createHttpPostReq(String serverScheme, String serverHost, int serverPort,
                                                       String serverPath, String payload) {
        HttpCarbonMessage httpPostRequest = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, serverPath));
        httpPostRequest.setProperty(Constants.PROTOCOL, serverScheme);
        httpPostRequest.setProperty(Constants.HTTP_HOST, serverHost);
        httpPostRequest.setProperty(Constants.HTTP_PORT, serverPort);
        httpPostRequest.setProperty(Constants.TO, serverPath);
        httpPostRequest.setProperty(Constants.HTTP_METHOD, Constants.HTTP_POST_METHOD);
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload.getBytes(Charset.forName("UTF-8")));
        httpPostRequest.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        return httpPostRequest;
    }
}
