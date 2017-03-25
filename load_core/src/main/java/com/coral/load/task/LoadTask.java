package com.coral.load.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.coral.load.HttpLoadRequest;
import com.coral.load.callback.HttpRetryHandler;
import com.coral.load.callback.LoadCallback;
import com.coral.load.http.LoadRequest;
import com.coral.load.http.LoadResponse;

/**
 * Created by xss on 2017/3/19.
 */

public class LoadTask extends BaseTask {

    private LoadCallback loadCallback;

    private static LoadHandler loadHandler = new LoadHandler();

    private Object result;

    public LoadTask(LoadRequest loadRequest, LoadCallback loadCallback) {
        super(loadRequest);
        this.loadCallback = loadCallback;
    }

    private Object getResult() {
        return result;
    }

    @Override
    public void execute() {
        // retry 初始化
        boolean retry = true;

        int retryCount = 0;
        Throwable exception = null;
        HttpRetryHandler retryHandler = loadRequest.getHttpRetryHandler();
        if (retryHandler == null) {
            retryHandler = new HttpRetryHandler();
        }
        retryHandler.setMaxRetryCount(loadRequest.getMaxRetryCount());

        while (retry) {
            retry = false;

            try {
                // 创建请求对象
                HttpLoadRequest request = new HttpLoadRequest(loadRequest);

                // 请求前，先关闭上次请求
                request.close();

                // 返回请求结果
                LoadResponse response = request.loadResult();
                int respCode = response.getCode();

                if (respCode == 200 || respCode == 206) {
                    result = parseResponse(response);
                    onSuccess(result);
                } else {
                    onFailed(respCode, getMessage());
                }

            } catch (Throwable throwable) {
                retry = retryHandler.canRetry(loadRequest, exception, ++retryCount);
                throwable.printStackTrace();
                if (!retry) {
                    onFailed(-1, getMessage());
                }
            } finally {
                if (!retry) {
                    onFinish();
                }
            }
        }
    }

    @Override
    public void onStart() {
        // do nothing, for  LoadCallback.onStart() executed in MainThread
    }

    @Override
    public void onProgress(long total, long current) {
        loadHandler.obtainMessage(MSG_WHAT_ON_PROGRESS, new HandleObject(this, total, current))
                   .sendToTarget();
    }

    @Override
    public void onSuccess(Object response) {
        loadHandler.obtainMessage(MSG_WHAT_ON_SUCCESS, new HandleObject(this, response))
                .sendToTarget();
    }

    @Override
    public void onFailed(int code, String msg) {
        loadHandler.obtainMessage(MSG_WHAT_ON_FAILED, new HandleObject(this, code, msg))
                .sendToTarget();
    }

    @Override
    public void onFinish() {
        loadHandler.obtainMessage(MSG_WHAT_ON_FINISH, this)
                .sendToTarget();
    }

    private static final int MSG_WHAT_ON_START = 0x0001;
    private static final int MSG_WHAT_ON_PROGRESS = 0x0002;
    private static final int MSG_WHAT_ON_SUCCESS = 0x0003;
    private static final int MSG_WHAT_ON_FAILED = 0x0004;
    private static final int MSG_WHAT_ON_FINISH = 0x0005;

    private static class HandleObject {
        final LoadTask task;
        final Object[] args;

        public HandleObject(LoadTask task, Object... args) {
            this.task = task;
            this.args = args;
        }
    }

    public static class LoadHandler extends Handler {

        private LoadHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj == null) {
                throw new IllegalArgumentException("msg must not be null");
            }

            LoadTask task;
            Object[] args = null;
            if (msg.obj instanceof LoadTask) {
                task = (LoadTask) msg.obj;
            } else {
                HandleObject handleObject = (HandleObject) msg.obj;
                task = handleObject.task;
                args = handleObject.args;
            }

            if (msg.what == MSG_WHAT_ON_START) {
                task.loadCallback.onStart();
            } else if (msg.what == MSG_WHAT_ON_PROGRESS) {
                task.loadCallback.onProgress(Long.parseLong(String.valueOf(args[0])), Long.parseLong(String.valueOf(args[1])));
            } else if (msg.what == MSG_WHAT_ON_SUCCESS) {
                task.loadCallback.onSuccess(task.getResult());
            } else if (msg.what == MSG_WHAT_ON_FAILED) {
                task.loadCallback.onFailed(Integer.parseInt(String.valueOf(args[0])), args[1].toString());
            } else if (msg.what == MSG_WHAT_ON_FINISH) {
                task.loadCallback.onFinish();
            }
        }
    }

}
