package com.coral.load.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xss on 2017/3/23.
 */

public class LoadSharedPrefUtil {

    private static final String SP_FILE_NAME = "sp_file_file_load";

    private static final String KEY_SP_ETAG = "key_sp_etag";

    private static LoadSharedPrefUtil instance_;

    private Context context;

    private LoadSharedPrefUtil(Context context) {
        this.context = context;
    }

    public static LoadSharedPrefUtil getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (instance_ == null || instance_.context != applicationContext) {
            instance_ = new LoadSharedPrefUtil(applicationContext);
        }
        return instance_;
    }

    protected String getSpFileName() {
        return SP_FILE_NAME;
    }

    protected SharedPreferences.Editor getEdit() {
        return getSp().edit();
    }

    protected SharedPreferences getSp() {
        SharedPreferences sp = context.getSharedPreferences(getSpFileName(), Context.MODE_PRIVATE);
        return sp;
    }

    public void setETag(String filePath, String eTag) {
        getEdit().putString(filePath, eTag).commit();
    }

    public String getETag(String filePath) {
        return getSp().getString(filePath, "");
    }

}
