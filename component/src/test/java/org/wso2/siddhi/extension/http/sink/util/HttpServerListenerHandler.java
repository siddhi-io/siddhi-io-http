package org.wso2.siddhi.extension.http.sink.util;

import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *Http test sever listener.
 */
public class HttpServerListenerHandler implements Runnable {
    public HttpServerListener getServerListner() {
        return sl;
    }
    private static final Logger logger = Logger.getLogger(HttpServerListenerHandler.class);
    private HttpServerListener sl;
    private HttpServer server;
    private int port;

    public HttpServerListenerHandler(int port, String expectedContentType) {

        this.sl = new HttpServerListener();
        this.port = port;
        run();
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 5);
            server.createContext("/", sl);
            server.start();
        } catch (IOException e) {
          logger.error("Error in creating test server.");
        }

    }

    public void shutdown() {
        if (server != null) {
            server.stop(1);
        }

    }


}
