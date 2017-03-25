package com.coral.load.callback;


/**
 * Created by xss on 2017/3/19.
 */

public interface LoadCallback<T> {

    void onStart();

    /**
     * 响应进度更新
     * @param total
     * @param current
     */
    void onProgress(long total, long current);

    void onSuccess(T result);

    void onFailed(int code, String msg);

    void onFinish();
}
