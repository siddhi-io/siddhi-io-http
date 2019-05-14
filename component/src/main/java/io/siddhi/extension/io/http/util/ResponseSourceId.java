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
package io.siddhi.extension.io.http.util;

/**
 * Class for holding sinkid and the status code for a response source
 */
public class ResponseSourceId {
    private String sinkId;
    private String httpCode;

    public ResponseSourceId(String sinkId, String httpCode) {
        this.sinkId = sinkId;
        this.httpCode = httpCode;
    }

    public String getSinkId() {
        return sinkId;
    }

    public String getHttpCode() {
        return httpCode;
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
