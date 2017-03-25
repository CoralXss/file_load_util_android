package com.coral.load;

import com.coral.load.callback.LoadCallback;
import com.coral.load.executor.ThreadExecutor;
import com.coral.load.http.LoadRequest;
import com.coral.load.task.LoadTask;

/**
 * Created by xss on 2017/3/19.
 */

public class HttpManager {

    private static final Object lock = new Object();
    private static volatile HttpManager instance;

    private HttpManager() {
    }

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    /**
     * 下载
     * @param entity
     * @param callback
     */
    public void get(LoadRequest entity, LoadCallback callback) {
        request(entity, callback);
    }

    /**
     * 上传
     * @param entity
     * @param callback
     */
    public void post(LoadRequest entity, LoadCallback callback) {
        request(entity, callback);
    }

    private void request(LoadRequest entity, LoadCallback callback) {
        // onStart()方法直接放在主线程执行，若放在子线程执行，则通过 handler来回调，太耗时间，没多大必要
        callback.onStart();
        LoadTask task = new LoadTask(entity, callback);
        new ThreadExecutor().execute(task);
    }

}
