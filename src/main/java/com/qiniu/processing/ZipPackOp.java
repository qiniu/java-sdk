package com.qiniu.processing;

import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

/**
 * Created by bailong on 15/2/25.
 */
public final class ZipPackOp implements Operation {
    private StringMap args;

    public ZipPackOp(StringMap args) {
        this.args = args;
    }

    public ZipPackOp() {
        this(new StringMap());
    }

    public ZipPackOp append(String url) {
        return append(url, "");
    }

    public ZipPackOp append(String url, String alias) {
        args.put(url, alias);
        return this;
    }

    @Override
    public String build() {
        if (args.size() == 0) {
            throw new IllegalStateException("zip list must have at least one part.");
        }
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
