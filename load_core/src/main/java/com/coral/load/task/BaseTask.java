package com.coral.load.task;

import android.text.TextUtils;
import android.util.Log;

import com.coral.load.entity.FileEntity;
import com.coral.load.http.Header;
import com.coral.load.http.HttpMethod;
import com.coral.load.http.LoadRequest;
import com.coral.load.http.LoadResponse;
import com.coral.load.util.AndroidUtil;
import com.coral.load.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xss on 2017/3/21.
 */

public abstract class BaseTask<T> implements Runnable {
    private static final String TAG = BaseTask.class.getSimpleName();

    protected LoadRequest loadRequest;

    public BaseTask(LoadRequest loadRequest) {
        this.loadRequest = loadRequest;
    }

    @Override
    public void run() {
        execute();
    }

    public abstract void execute();

    public abstract void onStart();

    public abstract void onProgress(long total, long current);

    public abstract void onSuccess(T response);

    public abstract void onFailed(int code, String msg);

    public abstract void onFinish();

    protected String getMessage() {
        if (loadRequest.getMethod() == HttpMethod.GET) {
            return "下载失败";
        }
        return "上传失败";
    }

    /**
     * 解析返回结果
     */
    protected Object parseResponse(LoadResponse loadResponse) throws Throwable {
        Log.e(TAG, "--begin parse response--");

        if (loadResponse == null) {
            onFailed(-1, getMessage());
        }

        // post 方法直接返回
        if (loadRequest.getMethod() == HttpMethod.POST) {
            return loadResponse.getBody().string();
        }

        // get 方法返回文件
        return loadFile(loadResponse);
    }

    private FileEntity loadFile(LoadResponse loadResponse) throws Throwable {
        FileEntity entity = new FileEntity();
        InputStream inputStream = loadResponse.getBody().getInputStream();

        String eTag = "";
        Header header = loadResponse.getHeader("Etag");
        if (header != null) {
            eTag = header.value.toString();
        }

        FileOutputStream fileOutputStream;
        // 下载文件（断点续传） / 上传文件
        String saveFilePath = loadRequest.getSaveFilePath();
        File targetFile;
        if (TextUtils.isEmpty(saveFilePath)) {
            targetFile = AndroidUtil.getCacheDir("coral");
        } else {
            targetFile = new File(saveFilePath);
        }

        if (targetFile.isDirectory()) {
            // 防止文件正在写入时, 父文件夹被删除, 继续写入时造成偶现文件节点异常问题.
            IOUtil.deleteFileOrDir(targetFile);
        }

        long current = 0;
        try {
            if (!targetFile.exists()) {
                File dir = targetFile.getParentFile();
                if (dir.exists() || dir.mkdirs()) {
                    targetFile.createNewFile();
                }
            }

            if (loadRequest.isAutoResume()) {
                current = targetFile.length();
                fileOutputStream = new FileOutputStream(targetFile, true);
            } else {
                fileOutputStream = new FileOutputStream(targetFile);
            }
        } catch (IOException e) {
            throw e;
        }

        long total = loadResponse.getBody().contentLength();

        // 返回206 为服务端做 eTag校验，客户端可以同时校验
        if (loadResponse.getCode() == 200 || loadResponse.getCode() == 206) {
            // 检验文件etag，是否是同一个文件
            String originETag = "";
            Header originHeader = loadRequest.getHeader("Etag");
            if (originHeader != null) {
                originETag = originHeader.value.toString();
            }

            // 文件有修改，就重新从头开始下载，不进行断点下载
            if (targetFile.length() != 0 && !isSameFile(originETag, eTag)) {
                IOUtil.deleteFileOrDir(targetFile);
                throw new RuntimeException("need retry");
            }
        }

        byte[] tmp = new byte[10];
        int len;
        while ((len = inputStream.read(tmp)) != -1) {

            // 防止父文件夹被其他进程删除, 继续写入时造成父文件夹变为0字节文件的问题.
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
                throw new IOException("parent be deleted!");
            }

            fileOutputStream.write(tmp, 0, len);
            current += len;

            // 设置下载进度条
            onProgress(total, current);

            // test断点下载，正式要将 buf size 改成 1024
            break;
        }
        fileOutputStream.flush();

        // 关闭 IO流
        IOUtil.closeQuietly(inputStream);
        IOUtil.closeQuietly(fileOutputStream);

        entity.file = targetFile;
        entity.eTag = eTag;

        return entity;
    }

    /**
     * 校验断点续传文件是否一致
     * @param requestETag  断点下载，保存到本地的文件最后修改时间
     * @param responseETag 每次下载，服务器返回的文件最后修改时间
     * @return
     */
    private boolean isSameFile(String requestETag, String responseETag) {
        Log.e("check", ((requestETag == null) ? "null" : requestETag) + ", " + responseETag);
        if (!TextUtils.isEmpty(requestETag) && !requestETag.equals(responseETag)) {
            return false;
        }
        return true;
    }
}
