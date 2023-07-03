package com.qiniu.storage;

import com.qiniu.util.Timestamp;
import com.qiniu.util.UrlUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class HostProvider {

    public static HostProvider ArrayProvider(String... hosts) {
        if (hosts == null || hosts.length == 0) {
            return null;
        }
        return new ArrayProvider(hosts);
    }

    /**
     * 域名获取方法
     **/
    abstract String provider();

    /**
     * 冻结域名
     *
     * @param host           将要被冻结域名
     * @param freezeDuration 冻结时间，单位：毫秒
     **/
    abstract void freezeHost(String host, int freezeDuration);

    /***
     * 域名是否有效
     *
     * @param host 域名
     * @return boolean
     **/
    abstract boolean isHostValid(String host);


    private static final class ArrayProvider extends HostProvider {

        private int nextIndex = 0;
        private final String[] values;
        private final Map<String, Value> items = new HashMap<>();

        private ArrayProvider(String... values) {
            super();
            this.values = values;

            if (this.values == null) {
                return;
            }

            for (int i = 0; i < this.values.length; i++) {
                String value = UrlUtils.removeHostScheme(this.values[i]);
                this.values[i] = value;
                this.items.put(value, new Value(value));
            }
        }

        @Override
        public String provider() {
            if (values == null || values.length == 0) {
                return "";
            }

            int s = Math.max(nextIndex, 0);
            int l = values.length;
            for (int i = s; i < (s + l); i++) {
                String key = values[i % l];
                Value v = items.get(key);
                if (v != null && v.isValid()) {
                    nextIndex = (i + 1) % l;
                    return v.value;
                }
            }
            return null;
        }

        @Override
        public void freezeHost(String host, int freezeDuration) {
            Value v = items.get(host);
            if (v == null) {
                return;
            }

            v.freeze(freezeDuration);
        }

        @Override
        boolean isHostValid(String host) {
            Value v = items.get(host);
            if (v == null) {
                return true;
            }
            return v.isValid();
        }

        private static final class Value {
            private final String value;
            private long validAfterTime;

            private Value(String value) {
                this.value = value;
            }

            private void freeze(int freezeDuration) {
                validAfterTime = freezeDuration + Timestamp.second();
            }

            private boolean isValid() {
                return Timestamp.second() >= validAfterTime;
            }
        }
    }
}
