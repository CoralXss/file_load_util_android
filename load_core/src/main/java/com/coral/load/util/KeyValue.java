package com.coral.load.util;

/**
 * Created by xss on 2017/3/20.
 */

public class KeyValue {

    public final String key;
    public final Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getValueString() {
        return value == null ? "" : value.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        KeyValue keyValue = (KeyValue) obj;
        // 键值是否一致
        return keyValue == null ? keyValue.key == null : key.equals(keyValue.key);

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "KeyValue { key = " + key + ", value = " + value + " }";
    }
}
