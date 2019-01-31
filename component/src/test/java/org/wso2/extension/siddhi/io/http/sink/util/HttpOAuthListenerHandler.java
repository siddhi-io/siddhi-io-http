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
 * Http test sever listener.
 */
public class HttpOAuthListenerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpServerListenerHandler.class);
    private HttpOAuthListener httpOAuthListener;
    private HttpOAuthEndpointListener httpOAuthEndpointListener;

    private HttpsServer server1;
    private HttpsServer server2;
    private int port1;
    private int port2;
    private KeyStore keyStore;

    public HttpOAuthListenerHandler(int port1, int port2) throws KeyStoreException {
        this.httpOAuthListener = new HttpOAuthListener();
        this.httpOAuthEndpointListener = new HttpOAuthEndpointListener();
        this.port1 = port1;
        this.port2 = port2;
        keyStore = KeyStore.getInstance("JKS");
    }

    public HttpOAuthListener getServerListener() {
        return httpOAuthListener;
    }

    @Override
    public void run() {
        try {
            char[] passphrase = "wso2carbon".toCharArray();
            keyStore.load(new FileInputStream(System.getProperty("carbon.home") +
                            "/resources/security/wso2carbon.jks"), passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            server1 = HttpsServer.create(new InetSocketAddress(port1), 5);
            server2 = HttpsServer.create(new InetSocketAddress(port2), 5);

            server1.setHttpsConfigurator(new HttpsConfigurator(ssl) {
                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    SSLContext c = getSSLContext();
                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            server2.setHttpsConfigurator(new HttpsConfigurator(ssl) {
                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    SSLContext c = getSSLContext();
                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            server1.createContext("/token", httpOAuthListener);
            server1.start();
            server2.createContext("/abc", httpOAuthEndpointListener);
            server2.start();
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm in  while trying to up test https server.", e);
        } catch (CertificateException e) {
            logger.error("Certificate exception in basic authentication", e);
        } catch (KeyStoreException e) {
            logger.error("Keystore exception in  while trying to up test https server.", e);
        } catch (IOException e) {
            logger.error("IOException  while trying to up test https server. ", e);
        } catch (UnrecoverableKeyException e) {
            logger.error("UnrecoverableKeyException while trying to up test https server.", e);
        } catch (KeyManagementException e) {
            logger.error("KeyManagementException while trying to up test https server.", e);
        }
    }

    public void shutdown() {
        if (server1 != null) {
            logger.info("Shutting down server 1");
            server1.stop(1);
        }
        if (server2 != null) {
            logger.info("Shutting down server 2");
            server2.stop(1);
        }
    }
    public HttpOAuthEndpointListener getHttpOAuthListener() {
        return httpOAuthEndpointListener;
    }
}

