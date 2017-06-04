package org.wso2.siddhi.extension.http.source.util;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.carbon.kernel.Constants;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;



/**
 * Util class for test cases.
 */
public class HttpTestUtil {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpTestUtil.class);

    public HttpTestUtil() {
    }

    public void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test", "resources");
        System.setProperty(Constants.CARBON_HOME, carbonHome.toString());
        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

    }

    public void httpPublishEventAuthIncorrect(String event, URI baseURI, Boolean auth, String mapping) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = HttpServerUtil.request(baseURI, "/endpoints/RecPro", HttpMethod.POST.name(), true);
            } catch (IOException e) {
                HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                byte[] val = ("admin" + ":" + "admin2").getBytes("UTF-8");
                HttpServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin")
                                .getBytes()));
            }
            HttpServerUtil.setHeader(urlConn, "Content-Type", mapping);
            HttpServerUtil.setHeader(urlConn, "HTTP_METHOD", "POST");
            HttpServerUtil.writeContent(urlConn, event);
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
        } catch (IOException e) {
            HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    public void httpPublishEvent(String event, URI baseURI, String path, Boolean auth, String mapping,
                                 String methodType, int expectedResponceCode) {
        try {
            HttpServerUtil server = new HttpServerUtil();
            HttpURLConnection urlConn = null;
            try {
                urlConn = server.request(baseURI, path, methodType, true);
            } catch (IOException e) {
                HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                byte[] val = ("admin" + ":" + "admin").getBytes("UTF-8");
                HttpServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes()));
            }
            server.setHeader(urlConn, "Content-Type", mapping);
            server.setHeader(urlConn, "HTTP_METHOD", methodType);
            server.writeContent(urlConn, event);
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
        } catch (IOException e) {
            HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    public void httpPublishEmptyPayload(URI baseURI, Boolean auth, String mapping, String methodType) {
        try {
            HttpServerUtil server = new HttpServerUtil();
            HttpURLConnection urlConn = null;
            try {
                urlConn = server.request(baseURI, "/endpoints/RecPro", methodType, true);
            } catch (IOException e) {
                HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                byte[] val = ("admin" + ":" + "admin").getBytes("UTF-8");
                HttpServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").
                                getBytes()));
            }
            server.setHeader(urlConn, "Content-Type", mapping);
            server.setHeader(urlConn, "HTTP_METHOD", methodType);
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    public void httpPublishEventDefault(String event, URI baseURI, Boolean auth, String mapping, String streamName) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = HttpServerUtil.request(baseURI, "/" + streamName, HttpMethod.POST.name(), true);
            } catch (IOException e) {
                HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                HttpServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin")
                                .getBytes()));
            }
            HttpServerUtil.setHeader(urlConn, "Content-Type", mapping);
            HttpServerUtil.setHeader(urlConn, "HTTP_METHOD", "POST");
            HttpServerUtil.writeContent(urlConn, event);
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            HttpServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    public void httpsPublishEvent(String event, String baseURI, Boolean auth, String mapping)
            throws KeyManagementException {
        try {
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("carbon.home") + "/conf/security/" +
                    "client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            char[] passphrase = "wso2carbon".toCharArray(); //password
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(System.getProperty("carbon.home") + "/conf/security/client-trusts" +
                    "tore.jks"), passphrase); //path
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            context.init(null, trustManagers, null);
            SSLSocketFactory sf = context.getSocketFactory();
            URL url = new URL(baseURI);
            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
            httpsCon.setSSLSocketFactory(sf);
            httpsCon.setRequestMethod("POST");
            httpsCon.setRequestProperty("Content-Type", mapping);
            httpsCon.setRequestProperty("HTTP_METHOD", "POST");
            if (auth) {
                httpsCon.setRequestProperty("Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes()));
            }
            httpsCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpsCon.getOutputStream());
            out.write(event);
            out.close();
            logger.info("Event response code " + httpsCon.getResponseCode());
            logger.info("Event response message " + httpsCon.getResponseMessage());
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
            logger.error(e);
        }
    }

}
