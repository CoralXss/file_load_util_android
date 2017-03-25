package com.coral.load.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xss on 2017/3/21.
 */

public class LoadResponse {

    private final int code;
    private final String message;
    private final List<Header> headers;
    private final ResponseBody body;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public ResponseBody getBody() {
        return body;
    }

    public Header getHeader(String key) {
        if (headers != null) {
            for (Header header : headers) {
                if (key.equals(header.key)) {
                    return header;
                }
            }
        }
        return null;
    }

    public LoadResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public static final class Builder {
        private int code;
        private String message;
        private List<Header> headers;
        private ResponseBody body;

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setHeaders(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeaders(String key, String value) {
            if (headers == null) {
                headers = new ArrayList<>();
            }
            headers.add(new Header(key, value, true));
            return this;
        }

        public Builder setResponseBody(ResponseBody body) {
            this.body = body;
            return this;
        }

        public LoadResponse build() {
            return new LoadResponse(this);
        }
    }
}
