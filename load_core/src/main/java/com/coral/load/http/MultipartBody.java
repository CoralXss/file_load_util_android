package com.coral.load.http;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by xss on 2017/3/20.
 */

public class MultipartBody extends RequestBody {

    public static final MediaType FORM = MediaType.parse("multipart/form-data");

    private final String boundary;
    private final MediaType originalType;
    private final MediaType contentType;
    private final List<Part> parts;

    private long contentLength = -1L;

    MultipartBody(String boundary, MediaType type, List<Part> parts) {
        this.boundary = boundary;
        this.originalType = type;
        // connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        this.contentType = MediaType.parse(type + "; boundary=" + boundary);
        this.parts = parts;
    }


    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public long getContentLength() throws IOException {
        return contentLength; // 要计算
    }

    private static byte[] BOUNDARY_PREFIX_BYTES = "--------7da3d81520810".getBytes();
    private static byte[] END_BYTES = "\r\n".getBytes();
    private static byte[] TWO_DASHES_BYTES = "--".getBytes();
    private static final byte[] COLONSPACE = {':', ' '};

    @Override
    public void writeTo(OutputStream os) throws IOException {
        Log.e("multi", "--begin write");
        for (Part part: parts) {
            Header header = part.getHeader();  // Content-Disposition 部分
            RequestBody body = part.getBody();      // 参数 key对应的 value值

            writeLine(os, TWO_DASHES_BYTES, boundary.getBytes());

            if (header != null) {
                writeLine(os, header.key.getBytes(), COLONSPACE, header.getValueString().getBytes());
                os.write(END_BYTES);
                body.writeTo(os);
                os.write(END_BYTES);
            }
        }
        writeLine(os, TWO_DASHES_BYTES, boundary.getBytes(), TWO_DASHES_BYTES);

        writeLine(os, String.format("Content-Length: %d", countBytes()).getBytes());
        writeLine(os, String.format("Content-Type: %s", getContentType()).getBytes());

        Log.e("multi", "--begin end");
    }

    private long countBytes() throws IOException {
        long countByte = 0l;

        for (Part part: parts) {
            Header header = part.getHeader();  // Content-Disposition 部分
            RequestBody body = part.getBody();      // 参数 key对应的 value值

            countByte += TWO_DASHES_BYTES.length + boundary.getBytes().length + END_BYTES.length;

            if (header != null) {
                countByte += header.key.getBytes().length + COLONSPACE.length +
                        header.getValueString().getBytes().length +  END_BYTES.length +
                        END_BYTES.length + body.getContentLength() + END_BYTES.length;
            }
        }
        countByte += TWO_DASHES_BYTES.length + boundary.getBytes().length + TWO_DASHES_BYTES.length + END_BYTES.length;

        return countByte;
    }

    private void writeLine(OutputStream out, byte[]... bs) throws IOException {
        if (bs != null) {
            for (byte[] b : bs) {
                out.write(b);
            }
        }
        out.write(END_BYTES);
    }

    public static final class Part {

        private Header header;
        private RequestBody body;
        private boolean isFile;

        private Part(Header header, RequestBody body, boolean isFile) {
            this.header = header;
            this.body = body;
            this.isFile = isFile;
        }

        public Header getHeader() {
            return header;
        }

        public RequestBody getBody() {
            return body;
        }

        public boolean isFile() {
            return isFile;
        }

        private static Part create(Header header, RequestBody body, boolean isFile) {
            if (body == null) {
                throw new NullPointerException("body == null");
            }
            if (header != null && "Content-Type".equals(header.getValueString())) {
                throw new IllegalArgumentException("Unexpected header: Content-Type");
            }
            if (header != null && "Content-Length".equals(header.getValueString())) {
                throw new IllegalArgumentException("Unexpected header: Content-Length");
            }
            return new Part(header, body, isFile);
        }

        /**
         * 普通参数
         * @param name
         * @param value
         * @return
         */
        public static Part createFormData(String name, String value) {
            return createFormData(name, null, RequestBody.create(null, value));
        }

        /**
         * 创建 Content-Disposition
         * @param name
         * @param fileName
         * @param body
         * @return
         */
        public static Part createFormData(String name, String fileName, RequestBody body) {
            if (name == null) {
                throw new NullPointerException("name = null");
            }
            StringBuilder contentDisposition = new StringBuilder();
            contentDisposition.append("form-data; name=\"" + name + "\"");

            if (!TextUtils.isEmpty(fileName)) {
                contentDisposition.append("; filename=\"" + fileName + "\"");
            }

            Header header = new Header("Content-Disposition", contentDisposition.toString(), true);

            return create(header, body, TextUtils.isEmpty(fileName) ? false : true);
        }
    }


    public static final class Builder {

        private final String boundary;

        private MediaType type = FORM;

        private final List<Part> parts = new ArrayList<>();

        public Builder() {
            this(UUID.randomUUID().toString());
        }

        public Builder(String boundary) {
            this.boundary = boundary;
        }

        public Builder setType(MediaType type) {
            if (type == null) {
                throw new NullPointerException("type == null");
            }
            if (!type.type().equals("multipart")) {
                throw new IllegalArgumentException("multipart != " + type);
            }
            this.type = type;
            return this;
        }

        /** Add a form data part to the body. */
        public Builder addFormDataPart(String name, String value) {
            return addPart(Part.createFormData(name, value));
        }

        /** Add a form data part to the body. */
        public Builder addFormDataPart(String name, String filename, RequestBody body) {
            return addPart(Part.createFormData(name, filename, body));
        }

        /** Add a part to the body. */
        public Builder addPart(Part part) {
            if (part == null) throw new NullPointerException("part == null");
            parts.add(part);
            return this;
        }

        public MultipartBody build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("Multipart body must have at least one part.");
            }
            return new MultipartBody(boundary, type, parts);
        }

    }

}
