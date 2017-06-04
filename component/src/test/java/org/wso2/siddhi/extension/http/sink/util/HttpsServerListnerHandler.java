package org.wso2.siddhi.extension.http.sink.util;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/**
 * Https test sever listener.
 */
public class HttpsServerListnerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpsServerListnerHandler.class);
    private HttpServerListener sl;
    private int port;
    private KeyStore ks;
    public HttpsServerListnerHandler(int port, String expectedContentType) throws KeyStoreException {
        this.sl = new HttpServerListener();
        this.port = port;
        ks = KeyStore.getInstance("JKS");
        run();
    }

    public HttpServerListener getServerListner() {
        return sl;
    }

    @Override
    public void run() {

        HttpsServer server;
        try {
            char[] passphrase = "wso2carbon".toCharArray();
            ks.load(new FileInputStream(System.getProperty("carbon.home") + "/conf/security/wso2carbon.jks"),
                    passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            server = HttpsServer.create(new InetSocketAddress(port), 5);
            server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                    // statement above could throw IAE if any params invalid.

                }
            });
            server.createContext("/", sl);
            server.start();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                KeyStoreException | KeyManagementException e) {
            logger.error("Error in creating test server ", e);
        }
    }
}
