package org.wso2.extension.siddhi.io.http.test.osgi.util;


import com.google.common.io.ByteStreams;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * A util class to be used for tests.
 */
public class ServerUtil {

    public static final String TEST_HOST = "localhost";
    private static final Logger log = LoggerFactory.getLogger(ServerUtil.class);

    ServerUtil() {
    }

    public static String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(
                urlConn.getOutputStream());
        out.write(content);
        out.close();
    }

    static HttpURLConnection request(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.PUT.name())) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty("Connection", "Keep-Alive");
        }
        return urlConn;
    }

    static void setHeader(HttpURLConnection urlConnection, String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    static void handleException(String msg, Exception ex) {
        log.error(msg, ex);
    }

}

