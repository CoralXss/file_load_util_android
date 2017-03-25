package com.coral.load.http;


import com.coral.load.util.KeyValue;

import java.util.List;

/**
 * Created by xss on 2017/3/20.
 */

public class Header extends KeyValue {

    public final boolean setHeader;

    private List<KeyValue> headers;

    public Header(String key, Object value) {
        super(key, value);
        this.setHeader = false;
    }

    public Header(String key, Object value, boolean setHeader) {
        super(key, value);
        this.setHeader = setHeader;
    }

}
