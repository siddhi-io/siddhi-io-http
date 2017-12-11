/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.http.sink.util;

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
public class HttpsServerListenerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpsServerListenerHandler.class);
    private HttpServerListener serverListener;
    private int port;
    private KeyStore keyStore;
    private  HttpsServer server;
    public HttpsServerListenerHandler(int port) throws KeyStoreException {
        this.serverListener = new HttpServerListener();
        this.port = port;
        keyStore = KeyStore.getInstance("JKS");
    }

    public HttpServerListener getServerListener() {
        return serverListener;
    }

    @Override
    public void run() {
        try {
            char[] passphrase = "wso2carbon".toCharArray();
            keyStore.load(new FileInputStream(System.getProperty("carbon.home") + "/resources/security/wso2carbon.jks"),
                    passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            server = HttpsServer.create(new InetSocketAddress(port), 5);
            server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    SSLContext c = getSSLContext();
                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            server.createContext("/abc", serverListener);
            server.start();
        }  catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm in  while trying to up test https server.", e);
        } catch (CertificateException e) {
            logger.error("Certificate exception in basic authentication", e);
        } catch (KeyStoreException e) {
            logger.error("Keystore exception in  while trying to up test https server." , e);
        } catch (IOException e) {
            logger.error("IOException  while trying to up test https server. ", e);
        } catch (UnrecoverableKeyException e) {
            logger.error("UnrecoverableKeyException while trying to up test https server.", e);
        } catch (KeyManagementException e) {
            logger.error("KeyManagementException while trying to up test https server.", e);
        }
    }
    public void shutdown() {
        if (server != null) {
            server.stop(1);
        }

    }
}
