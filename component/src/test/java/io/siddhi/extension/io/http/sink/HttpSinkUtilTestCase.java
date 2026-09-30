/*
 *  Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.siddhi.extension.io.http.sink;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.siddhi.extension.io.http.sink.util.HttpSinkUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;

import java.util.List;

/**
 * Tests header parsing in {@link HttpSinkUtil}.
 */
public class HttpSinkUtilTestCase {

    @Test
    public void testWhitespaceAroundHeaderNameAndValueIsTrimmed() {
        List<Header> headers = HttpSinkUtil.getHeaders(
                "'Authorization: Bearer xxxxx',' Content-Type :text/plain ','country:sl'");

        Assert.assertEquals(headers.size(), 3);
        Assert.assertEquals(headers.get(0).getName(), "Authorization");
        Assert.assertEquals(headers.get(0).getValue(), "Bearer xxxxx");
        Assert.assertEquals(headers.get(1).getName(), "Content-Type");
        Assert.assertEquals(headers.get(1).getValue(), "text/plain");
        Assert.assertEquals(headers.get(2).getName(), "country");
        Assert.assertEquals(headers.get(2).getValue(), "sl");

        DefaultHttpHeaders nettyHeaders = new DefaultHttpHeaders(true);
        for (Header header : headers) {
            nettyHeaders.add(header.getName(), header.getValue());
        }
        Assert.assertEquals(nettyHeaders.get("Authorization"), "Bearer xxxxx");
    }

    @Test
    public void testColonInHeaderValueIsKept() {
        List<Header> headers = HttpSinkUtil.getHeaders("'Referer: http://localhost:8005/abc'");

        Assert.assertEquals(headers.get(0).getName(), "Referer");
        Assert.assertEquals(headers.get(0).getValue(), "http://localhost:8005/abc");
    }
}
