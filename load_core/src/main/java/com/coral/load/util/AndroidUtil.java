package com.coral.load.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by xss on 2017/3/24.
 */

public class AndroidUtil {

    public static Boolean existsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getCacheDir(String dirName) {
        File result;
        if (existsSdcard()) {
            result = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/" + dirName);

            if (result.exists() || result.mkdirs()) {
                return result;
            }
        }
        return null;
    }

    public static File getCacheDir(Context context, String dirName) {
        File result;
        if (existsSdcard()) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) {
                result = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + context.getPackageName() + "/cache/" + dirName);
            } else {
                result = new File(cacheDir, dirName);
            }
        } else {
            result = new File(context.getCacheDir(), dirName);
        }
        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }
}
