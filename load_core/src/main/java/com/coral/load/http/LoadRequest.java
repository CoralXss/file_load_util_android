package com.coral.load.http;


import com.coral.load.callback.HttpRetryHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xss on 2017/3/19.
 * desc: 上传/下载配置，
 * 1. 是否支持断点续传、是否需要展示下载进度条、下载路径(无为默认路径，待处理);  （OK）
 * 2. 是否支持 https;  (应是默认支持)
 * 3. 是否直接多文件上传；
 * 4. 是否支持带参文件上传/下载.  (ok了，但是一个字节一个字节的读到os中，上传很慢，40byte的就会花费 30s)
 * 5. 上传大文件：使用 HttpURLConnection上传大文件，有致命的问题，因为手机分配给 app的内存很小，会发生内存溢出,
 *          解决方式：使用 Socket模拟 POST 进行 HTTP 上传大文件  /  分块传输，不写内存，而是写缓存数据，可测试几百Mb的文件
 * 6. 点击 取消/暂停下载/上传 策略.
 * 7. 如何处理请求成功后，上传文件 和 下载文件不同的回调? ok
 */

public class LoadRequest {
    private final String mUrl;

    private final HttpMethod mMethod;

    private final List<Header> mHeaders;

    private final RequestBody mRequestBody;

    private final String mSaveFilePath;

    private final boolean mIsAutoResume;

    private HttpRetryHandler httpRetryHandler;

    private int maxRetryCount = 2;

    private LoadRequest(Builder builder) {
        this.mUrl = builder.mUrl;
        this.mMethod = builder.mMethod;
        this.mHeaders = builder.mHeaders;
        this.mRequestBody = builder.mRequestBody;
        this.mSaveFilePath = builder.mSaveFilePath;
        this.mIsAutoResume = builder.mIsAutoResume;
        if (httpRetryHandler == null) {
            httpRetryHandler = new HttpRetryHandler();
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public HttpMethod getMethod() {
        return mMethod;
    }

    public List<Header> getHeaders() {
        return mHeaders;
    }

    public Header getHeader(String key) {
        if (mHeaders != null) {
            for (Header header : mHeaders) {
                if (key.equals(header.key)) {
                    return header;
                }
            }
        }
        return null;
    }

    public RequestBody getRequestBody() {
        return mRequestBody;
    }

    public String getSaveFilePath() {
        return mSaveFilePath;
    }

    public boolean isAutoResume() {
        return mIsAutoResume;
    }

    public HttpRetryHandler getHttpRetryHandler() {
        return httpRetryHandler;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public static final class Builder {
        private String mUrl;

        private HttpMethod mMethod;

        private List<Header> mHeaders;

        private RequestBody mRequestBody;

        private String mSaveFilePath;

        private boolean mIsAutoResume;

        public Builder url(String url) {
            this.mUrl = url;
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            this.mMethod = method;
            return this;
        }

        public Builder addHeaders(String key, String value) {
            if (mHeaders == null) {
                mHeaders = new ArrayList<>();
            }
            Header header = new Header(key, value, true);
            mHeaders.add(header);
            return this;
        }

        public Builder setRequestBody(RequestBody requestBody) {
            this.mRequestBody = requestBody;
            return this;
        }

        public Builder setSaveFilePath(String filePath) {
            this.mSaveFilePath = filePath;
            return this;
        }

        public Builder setIsAutoResume(boolean isAutoResume) {
            this.mIsAutoResume = isAutoResume;
            return this;
        }

        public LoadRequest build() {
            return new LoadRequest(this);
        }
    }


}
