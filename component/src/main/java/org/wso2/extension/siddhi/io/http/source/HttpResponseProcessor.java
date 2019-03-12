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
package org.wso2.extension.siddhi.io.http.source;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.extension.siddhi.io.http.util.HttpConstants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * Handles sending data to source listener.
 */
public class HttpResponseProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponseProcessor.class);
    private HttpCarbonMessage carbonMessage;
    private SourceEventListener sourceEventListener;
    private String sinkId;
    private String[] trpProperties;
    boolean shouldAllowStreamingResponses;

    private String filePath;

    HttpResponseProcessor(HttpCarbonMessage cMessage, SourceEventListener sourceEventListener, boolean
            shouldAllowStreamingResponses, String sinkId, String[] trpProperties) {
        this.carbonMessage = cMessage;
        this.sourceEventListener = sourceEventListener;
        this.sinkId = sinkId;
        this.trpProperties = trpProperties;
        this.shouldAllowStreamingResponses = shouldAllowStreamingResponses;
    }

    @Override
    public void run() {
        int code = carbonMessage.getNettyHttpResponse().status().code() / 100;
        boolean isDownloadableContent = (boolean) (carbonMessage.getProperty(HttpConstants.IS_DOWNLOADABLE_CONTENT));

        if (isDownloadableContent && code == 2) {
            filePath = carbonMessage.getProperty(HttpConstants.DOWNLOAD_PATH).toString();
            String fileName = writeToTile(carbonMessage);
            if (fileName != null) {
                sourceEventListener.onEvent(fileName, trpProperties);
            }
        } else {
            HttpContent content;
            if (!shouldAllowStreamingResponses) {
                BufferedReader buf = new BufferedReader(
                        new InputStreamReader(
                                new HttpMessageDataStreamer(carbonMessage).getInputStream(), Charset.defaultCharset()));
                try {
                    String payload = buf.lines().collect(Collectors.joining("\n"));
                    if (!payload.equals(HttpConstants.EMPTY_STRING)) {
                        sourceEventListener.onEvent(payload, trpProperties);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Submitted Event :" + payload);
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Empty payload event, hence dropping the event chunk at source " + sinkId);
                        }
                    }
                } finally {
                    try {
                        buf.close();
                    } catch (IOException e) {
                        logger.error("Error occurred when closing the byte buffer in source " + sinkId, e);
                    }
                }
            } else {
                do {
                    content = carbonMessage.getHttpContent();
                    if (content != null) {
                        String payload = content.content().toString(Charset.defaultCharset());
                        if (!payload.equals(HttpConstants.EMPTY_STRING)) {
                            sourceEventListener.onEvent(payload, trpProperties);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Submitted Event :" + payload);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Empty payload event, hence dropping the event chunk in the source " +
                                        "with sink.id : " + sinkId);
                            }
                        }
                    }
                } while (!(content instanceof LastHttpContent));
            }
        }
    }

    private String writeToTile(HttpCarbonMessage carbonMessage) {
        File file = new File(filePath);
        try (InputStream inputStream = new HttpMessageDataStreamer(carbonMessage).getInputStream();
             OutputStream outputStream = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            return filePath;
        } catch (FileNotFoundException e) {
            logger.error("Given path to download the file : '" + filePath + "' cannot be found.", e);
        } catch (IOException e) {
            logger.error("Error occurred during writing the file to '" + filePath + "' due to " + e.getMessage(), e);
        }
        return null;
    }
}
