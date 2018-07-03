package org.wso2.extension.siddhi.io.http.util;

/**
 * Class for holding sinkid and the status code for a response source
 */
public class ResponseSourceId {
    String sinkId;
    String httpCode;

    public ResponseSourceId(String sinkId, String httpCode) {
        this.sinkId = sinkId;
        this.httpCode = httpCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResponseSourceId that = (ResponseSourceId) o;

        if (sinkId != null ? !sinkId.equals(that.sinkId) : that.sinkId != null) {
            return false;
        }
        return httpCode != null ? httpCode.matches(that.httpCode) : that.httpCode == null;
    }

    @Override
    public int hashCode() {
        int result = sinkId != null ? sinkId.hashCode() : 0;
        result = 31 * result + (httpCode != null ? httpCode.hashCode() : 0);
        return result;
    }
}
