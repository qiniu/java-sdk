package com.qiniu.storage;

import com.qiniu.util.Timestamp;

import java.util.HashMap;
import java.util.Map;

public abstract class HostProvider {

    public static HostProvider ArrayProvider(String... hosts) {
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


    private static class ArrayProvider extends HostProvider {

        private int nextIndex = 0;
        private final String[] values;
        private final Map<String, Value> items = new HashMap<>();

        private ArrayProvider(String... values) {
            super();
            this.values = values;

            if (values == null) {
                return;
            }

            for (String value : values) {
                this.items.put(value, new Value(value));
            }
        }

        @Override
        public String provider() {
            int s = nextIndex;
            int l = values.length;
            for (int i = s; i < (s + l); i++) {
                String key = values[i % l];
                Value v = items.get(key);
                if (v.isValid()) {
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

        private static class Value {
            private final String value;
            private long deadline;

            private Value(String value) {
                this.value = value;
            }

            private void freeze(int freezeDuration) {
                deadline = freezeDuration + Timestamp.second();
            }

            private boolean isValid() {
                return Timestamp.second() < deadline;
            }
        }
    }
}
