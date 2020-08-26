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

import io.netty.handler.codec.http.HttpHeaderValues;
import io.siddhi.extension.io.http.util.HttpConstants;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code HttpRequest} Handle the HTTP request for invalid access token.
 */
public class HttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    private HttpRequest() {
    }

    public static List<String> getResponse(String tokenUrl, String encodedAuth, String payload,
                                           OkHttpClient client) {
        ArrayList<String> responses = new ArrayList<>();

        MediaType mediaType = MediaType.parse(String.valueOf(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED));
        RequestBody requestBody = RequestBody.create(mediaType, payload);
        Request request = new Request.Builder()
                .url(tokenUrl)
                .method(HttpConstants.HTTP_METHOD_POST, requestBody)
                .addHeader(HttpConstants.HTTP_CONTENT_TYPE,
                        String.valueOf(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED))
                .addHeader(HttpConstants.AUTHORIZATION_HEADER, encodedAuth)
                .build();

        try {
            Response response = client.newCall(request).execute();
            responses.add(String.valueOf(response.code()));
            responses.add(response.body().string());
        }  catch (NullPointerException e) {
            LOG.error(String.format("Error occurred while reading the response message. %s", e.getMessage()), e);
        } catch (IOException e) {
            LOG.error(String.format("Error occurred while sending the token request. %s", e.getMessage()), e);
        }
        return responses;
    }
}
