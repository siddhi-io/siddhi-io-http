package org.wso2.extension.siddhi.io.http.test.osgi.source.util;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.carbon.kernel.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Util class for test cases.
 */
public class TestUtil {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestUtil.class);

    public TestUtil() {
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
                urlConn = ServerUtil.request(baseURI, "/endpoints/RecPro", HttpMethod.POST.name(), true);
            } catch (IOException e) {
                ServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                ServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin2")
                                .getBytes()));
            }
            ServerUtil.setHeader(urlConn, "Content-Type", mapping);
            ServerUtil.setHeader(urlConn, "HTTP_METHOD", "POST");
            ServerUtil.writeContent(urlConn, event);
            assert urlConn != null;
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            ServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    public void httpPublishEvent(String event, URI baseURI, String path, Boolean auth, String mapping,
                                 String methodType) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = ServerUtil.request(baseURI, path, methodType, true);
            } catch (IOException e) {
                ServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                byte[] val = ("admin" + ":" + "admin").getBytes("UTF-8");
                ServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes()));
            }
            ServerUtil.setHeader(urlConn, "Content-Type", mapping);
            ServerUtil.setHeader(urlConn, "HTTP_METHOD", methodType);
            ServerUtil.writeContent(urlConn, event);
            assert urlConn != null;
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            ServerUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }
}
