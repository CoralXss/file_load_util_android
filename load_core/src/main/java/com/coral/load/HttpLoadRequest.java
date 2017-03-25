package com.coral.load;

import android.os.Build;
import android.text.TextUtils;

import com.coral.load.http.Header;
import com.coral.load.http.HttpMethod;
import com.coral.load.http.LoadRequest;
import com.coral.load.http.LoadResponse;
import com.coral.load.http.MediaType;
import com.coral.load.http.RequestBody;
import com.coral.load.http.ResponseBody;
import com.coral.load.util.AndroidUtil;
import com.coral.load.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xss on 2017/3/19.
 */

public class HttpLoadRequest {
    private static final String TAG = HttpLoadRequest.class.getSimpleName();

    private static final int CHUNK_LENGTH = 1024 * 1024;

    private LoadRequest loadRequest;

    private HttpURLConnection connection = null;
    // 用来存储下载的字节流
    private InputStream inputStream = null;
    private int responseCode = 0;

    private String contentType;
    private int contentLength;

    private List<Header> responseHeaders;

    public HttpLoadRequest(LoadRequest params) {
        this.loadRequest = params;
    }

    public void sendRequest() throws Throwable { // 查看 HttpRequest类

        try {
            URL url = new URL(loadRequest.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // set method
            HttpMethod method = loadRequest.getMethod();
            if (TextUtils.isEmpty(method.toString())) {
                method = HttpMethod.GET;
            }

            // try to fix bug: accidental EOFException before API 19
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                connection.setRequestProperty("Connection", "close");
            }

            // add headers
            addHeaders(connection);

            // write body
            if (HttpMethod.permitsRequestBody(method)) {
                RequestBody requestBody = loadRequest.getRequestBody();

                // 如果设置了 请求体，添加请求体
                if (requestBody != null) {
                    String contentType = requestBody.getContentType().toString();
                    long contentLength = requestBody.getContentLength();

                    if (!TextUtils.isEmpty(contentType)) {
                        connection.setRequestProperty("Content-Type", contentType);
                    }

                    /**
                     * 设置 chunked/FixedLength，使输出流不在本地进行缓存，而是使用固定大小的数组来缓存输出流，等缓存满时自动调用基础流输出。
                     * 这个主要用在无法确定流的具体长度又不想在本地进行缓存时用到，上传大数据流用分块传输可有效解决OOM问题
                     * 注：此种模式 和 Content-Length 不能一同设置，否则服务器500错误
                     */
                    if (contentLength < 0) {
                        // 未知输出流的长度
                        connection.setChunkedStreamingMode(CHUNK_LENGTH);
                    } else {
                        if (contentLength < Integer.MAX_VALUE) {
                            connection.setFixedLengthStreamingMode((int) contentLength);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            // 已知输出流的长度
                            connection.setFixedLengthStreamingMode(contentLength);
                        } else {
                            connection.setChunkedStreamingMode(CHUNK_LENGTH);
                        }
                    }
                    // 如果是 GET 方法，此处设置 doOutput true，就是变成 post方法
                    connection.setDoOutput(true);

                    /**
                     * 当写大数据流到服务器时会出现 OOM，主要原因是，URLConnection的输出流首先是在本地内存进行缓存，
                     *  然后再一次性输出，这样当输出流一大时，就会出现内存不够用的情况。
                     *  在调用 getInputStream()后才一次性写入网络流中。
                     *
                     *  通过设置 ChunkedStreamMode 模式，可先发送请求头，在 os.close()时服务器会收到完整的请求
                     */
                    requestBody.writeTo(connection.getOutputStream());
                }
            } else {
                if (loadRequest.isAutoResume()) {  // 支持断点续传下载
                    String savedPath = loadRequest.getSaveFilePath();
                    File targetFile;
                    if (TextUtils.isEmpty(savedPath)) {
                        targetFile = AndroidUtil.getCacheDir("coral");
                    } else {
                        targetFile = new File(savedPath);
                    }
                    // 设置断点续传：请求头中方 "Range"
                    if (targetFile != null && targetFile.exists()) {
                        connection.addRequestProperty("Range", "bytes=" + targetFile.length());
                    } else {
                        connection.addRequestProperty("Range", "bytes=0-");
                    }
                }
            }

            // get response code
            responseCode = connection.getResponseCode();
            contentLength = connection.getContentLength();
            contentType = connection.getContentType();
            responseHeaders = getHeaders(connection);

            // get response
            inputStream = responseCode > 400 ? connection.getErrorStream() : connection.getInputStream();

            // 一次请求执行完成后，connection=null被释放了  ?????

        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    private void addHeaders(HttpURLConnection connection) {
        List<Header> headers = loadRequest.getHeaders();
        if (headers != null) {
            for (Header header : headers) {
                String key = header.key;
                String value = header.getValueString();

                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    connection.setRequestProperty(key, value);
                }
            }
        }
    }

    public void close() throws IOException {
        if (inputStream != null) {
            IOUtil.closeQuietly(inputStream);
            inputStream = null;
        }
        if (connection != null) {
            connection.disconnect();
        }
    }

    public List<Header> getHeaders(HttpURLConnection connection) throws IOException {
        List<Header> headers = new ArrayList<>();
        if (connection != null) {
            headers.add(new Header("Content-Length", connection.getHeaderField("Content-Length")));
            headers.add(new Header("Content-Type", connection.getHeaderField("Content-Type")));
            headers.add(new Header("Accept-Ranges", connection.getHeaderField("Accept-Ranges")));
            headers.add(new Header("Etag", connection.getHeaderField("Etag")));
        }
        return headers;
    }

    public List<Header> getHeaders() {
        return responseHeaders;
    }

    public String getContentType() {
        return TextUtils.isEmpty(contentType) ? "" : contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * 发送请求，获取响应结果并构建 Response响应对象
     *
     * @return
     * @throws Throwable
     */
    public LoadResponse loadResult() throws Throwable {
        sendRequest();

        LoadResponse response = new LoadResponse.Builder()
                .code(getResponseCode())
                .setResponseBody(ResponseBody.create(MediaType.parse(getContentType()), getContentLength(), getInputStream()))
                .setHeaders(getHeaders())
                .build();

        return response;
    }

}
