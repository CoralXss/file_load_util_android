package com.coral.load.http;


import com.coral.load.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xss on 2017/3/21.
 */

public abstract class ResponseBody {

    public abstract MediaType contentType();

    public abstract long contentLength();

    public abstract InputStream getInputStream();

    public static ResponseBody create(final MediaType contentType, final long contentLength, final InputStream is) {
        if (is == null) {
            throw new NullPointerException("content == null");
        }

        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public InputStream getInputStream() {
                return is;
            }
        };
    }

    public final byte[] bytes() throws IOException {
        InputStream is = getInputStream();

        return IOUtil.readBytes(is);
    }

    public final String string() throws IOException {
        return new String(bytes(), "UTF-8");
    }

}
