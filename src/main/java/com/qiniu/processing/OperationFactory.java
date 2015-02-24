package com.qiniu.processing;

import com.qiniu.storage.BucketManager;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

public final class OperationFactory {
    private OperationFactory() {
    }

    public static Operation normal(String cmd, Object mode, StringMap args) {
        return new Normal(cmd, mode, args);
    }

    public static Operation normal(String cmd) {
        return normal(cmd, null, null);
    }

    public static Operation saveAs(String bucket, String key) {
        return new Save(bucket, key);
    }

    public static Operation mkzip(StringMap args) {
        return new Mkzip(args);
    }

    private static class Normal implements Operation {
        private String cmd;
        private Object mode;
        private StringMap args;

        private Normal(String cmd, Object mode, StringMap args) {
            this.cmd = cmd;
            this.mode = mode;
            this.args = args;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder(cmd);
            if (mode != null) {
                b.append("/");
                b.append(mode);
            }
            if (args != null) {
                args.iterate(new StringMap.Do() {
                    @Override
                    public void deal(String key, Object value) {
                        b.append("/");
                        b.append(key);
                        b.append("/");
                        b.append(value);
                    }
                });
            }

            return b.toString();
        }

        @Override
        public boolean onlyPersistent() {
            return false;
        }
    }

    private static class Save implements Operation {
        private String bucket;
        private String key;

        private Save(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }

        @Override
        public String toString() {
            return "saveas/" + BucketManager.entry(bucket, key);
        }

        @Override
        public boolean onlyPersistent() {
            return false;
        }
    }

    private static class Mkzip implements Operation {
        private StringMap args;

        private Mkzip(StringMap args) {
            this.args = args;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder("mkzip");
            args.iterate(new StringMap.Do() {
                @Override
                public void deal(String key, Object value) {
                    b.append("/url/");
                    b.append(UrlSafeBase64.encodeToString(key));
                    String val = (String) value;
                    if (!StringUtils.isEmpty(val)) {
                        b.append("/alias/");
                        b.append(UrlSafeBase64.encodeToString(val));
                    }
                }
            });
            return b.toString();
        }

        @Override
        public boolean onlyPersistent() {
            return true;
        }
    }
}
