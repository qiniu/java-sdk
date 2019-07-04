package com.qiniu.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UpHostHelper {
    private List<String> lastAccHosts;
    private List<String> lastSrcHosts;

    private long failedPeriod; // 毫秒 1/1000 s
    private Configuration conf;

    private HashMap<String, Long> hostMark = new HashMap();
    private ArrayList<String> lastHosts = new ArrayList();
    private volatile int lastIndex = 0;
    private volatile int mainHostCount = 0;

    UpHostHelper(Configuration conf, int failedPeriodSeconds) {
        this.conf = conf;
        this.failedPeriod = failedPeriodSeconds * 1000L;
    }

    private void initHostMark(List<String> f, List<String> s) {
        mainHostCount = 0;
        if (f.size() > 0) {
            lastHosts.add(f.get(0));
            mainHostCount++;
        }
        if (s.size() > 0) {
            lastHosts.add(s.get(0));
            mainHostCount++;
        }
        // 后面的 host 乱序插入
        int l = Math.min(f.size(), s.size());
        int i = 1;

        ArrayList<Integer> iidx = new ArrayList();
        for (; i < l; i++) {
            iidx.add(i);
        }

        Random r = new Random();
        randomAdd(iidx, r, f, s);
        // f or s ,checked by i < f.size() and i < s.size()
        iidx.clear();
        for (; i < f.size(); i++) {
            iidx.add(i);
        }
        randomAdd(iidx, r, f);

        iidx.clear();
        for (; i < s.size(); i++) {
            iidx.add(i);
        }
        randomAdd(iidx, r, s);
    }

    private void randomAdd(List<Integer> iidx, Random r, List<String> ...list) {
        int size = iidx.size();
        for (int j = 0; j < size; j++) {
            int ji = r.nextInt(iidx.size());
            Integer jv = iidx.remove(ji);
            for (List<String> l : list) {
                lastHosts.add(l.get(jv));
            }
        }
    }



    String upHost(List<String> accHosts, List<String> srcHosts, boolean changeHost) {
        if (lastAccHosts != accHosts || lastSrcHosts != srcHosts) {
            lastAccHosts = accHosts;
            lastSrcHosts = srcHosts;
            synchronized (hostMark) {
                hostMark.clear();
                lastHosts.clear();
                if (conf.useAccUpHost) {
                    initHostMark(lastAccHosts, lastSrcHosts);
                } else {
                    initHostMark(lastSrcHosts, lastAccHosts);
                }
            }
        }

        // acc src 的第一个(upload.qiniup.com, up.qiniup.com)，
        // 作为主上传域名，可以继续使用，不用恢复到 lastHosts.get(0)
        if (!changeHost && lastIndex < mainHostCount) {
            return lastHosts.get(lastIndex);
        }

        if (changeHost) {
            synchronized (hostMark) {
                if (lastIndex >= lastHosts.size()) {
                    lastIndex = 0;
                }
                String host = lastHosts.get(lastIndex);
                hostMark.put(host, System.currentTimeMillis());
                lastIndex++;
            }
        }

        long t = System.currentTimeMillis() - this.failedPeriod;
        // 标记过期后，恢复到 优先使用 第一个 值 //
        int idx = 0;
        for (String h : lastHosts) {
            Long e = hostMark.get(h);
            if (e == null) {
                e = 0L;
            }
            // 不可用标志 已过期 //
            // 优先使用 主 和 未使用的域名 //
            if (e < t && (idx < mainHostCount || idx >= lastIndex)) {
                lastIndex = idx;
                return h;
            }
            idx++;
        }
        // 全部标记为不可用 //
        // 清空所有标记，重新开始 //
        synchronized (hostMark) {
            hostMark.clear();
            lastIndex = 0;
        }
        return lastHosts.get(lastIndex);
    }
}
