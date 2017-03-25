package com.coral.load.http;

import android.util.Log;

import com.coral.load.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by xss on 2017/3/20.
 */

public abstract class RequestBody {

    /** Returns the Content-Type header for this body. */
    public abstract MediaType getContentType();

    /**
     * Returns the number of bytes that will be written to {@code out} in a call to {@link #writeTo},
     * or -1 if that count is unknown.
     */
    public long getContentLength() throws IOException {
        return -1;
    }

    /** Writes the content of this request to {@code out}. */
    public abstract void writeTo(OutputStream os) throws IOException;

    public static RequestBody create(MediaType contentType, String content) {
        Charset charset = Charset.forName("UTF-8");
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Charset.forName("UTF-8");
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    public static RequestBody create(final MediaType contentType, final byte[] content) {
        if (content == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public MediaType getContentType() {
                return contentType;
            }

            @Override
            public long getContentLength() throws IOException {
                return content.length;
            }

            @Override
            public void writeTo(OutputStream os) throws IOException {
                os.write(content);
            }
        };
    }

    /**
     * Returns a new request body that transmits the content of {@code file}.
     * @param mediaType
     * @param file
     * @return
     */
    public static RequestBody create(final MediaType mediaType, final File file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }

        return new RequestBody() {
            @Override
            public MediaType getContentType() {
                return mediaType;
            }

            @Override
            public long getContentLength() throws IOException {
                return file.length();
            }

            @Override
            public void writeTo(OutputStream os) throws IOException {
                // TODO: 2017/3/20  此处可添加上传进度条

                Log.e("test", "begin file body");

                // 将文件内容写到流中
                IOUtil.writeAll(file, os);

                Log.e("test", "end file body");
            }
        };
    }



}
