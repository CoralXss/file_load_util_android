package com.coral.load.callback;

import android.util.Log;

import com.coral.load.http.HttpMethod;
import com.coral.load.http.LoadRequest;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * Created by xss on 2017/3/24.
 */

public class HttpRetryHandler {
    private static final String TAG = HttpRetryHandler.class.getSimpleName();

    protected int maxRetryCount = 2;

    protected static HashSet<Class<?>> blackList = new HashSet<Class<?>>();

    static {
//        blackList.add(Callback.CancelledException.class);
        blackList.add(MalformedURLException.class);
        blackList.add(URISyntaxException.class);
        blackList.add(NoRouteToHostException.class);
        blackList.add(PortUnreachableException.class);
        blackList.add(ProtocolException.class);
        blackList.add(NullPointerException.class);
        blackList.add(FileNotFoundException.class);
        blackList.add(JSONException.class);
        blackList.add(UnknownHostException.class);
        blackList.add(IllegalArgumentException.class);
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public boolean canRetry(LoadRequest request, Throwable ex, int count) {

        Log.e("Retry", (ex != null) ? ex.toString() : "error");

        if (count > maxRetryCount) {
            Log.w(TAG, "The Max Retry times has been reached!");
            return false;
        }

        if (!HttpMethod.permitsRetry(request.getMethod())) {
            Log.e(TAG, request.toString());
            Log.e(TAG, "The Request Method can not be retried.");
            return false;
        }

        // 当抛出 RuntimeException need retry才进行重连
        if (ex != null && blackList.contains(ex.getClass())) {
            Log.w(TAG, request.toString());
            Log.w(TAG, "The Exception can not be retried.");
            return false;
        }

        return true;
    }
}
