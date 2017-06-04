package org.wso2.siddhi.extension.http.sink.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Server Listener Manger.
 */
public class HttpServerListener implements HttpHandler {
    private AtomicBoolean isEventArraved = new AtomicBoolean(false);
    private StringBuilder strBld;
    private Headers headers;
    private static final Logger logger = Logger.getLogger(HttpServerListener.class);

    HttpServerListener() {
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        // Get the paramString form the request
        String line = "";
        headers = t.getRequestHeaders();
        InputStream is = t.getRequestBody();
        BufferedReader in = new BufferedReader(new InputStreamReader(is)); // initiating
        strBld = new StringBuilder();
        while ((line = in.readLine()) != null) {
            strBld = strBld.append(line + "\n");
            System.out.print(line + "\n");
        }

        logger.info("Event Arrived: " + strBld.toString());
        isEventArraved.set(true);
    }

    public String getData() {
        isEventArraved = new AtomicBoolean(false);
        return strBld.toString();
    }

    public Headers getHeaders() {
        return headers;
    }

    public boolean iaMessageArrive() {
        return isEventArraved.get();
    }

}
