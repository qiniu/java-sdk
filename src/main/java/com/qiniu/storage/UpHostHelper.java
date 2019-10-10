package com.qiniu.storage;

import com.qiniu.common.QiniuException;

import java.util.*;

class UpHostHelper {
    private long failedPeriodMillis; // 毫秒 1/1000 s
    private Configuration conf;

    private LinkedHashMap<String, RegionUpHost> regionHostsLRU =
            new LinkedHashMap<String, RegionUpHost>(1, 1.0f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, RegionUpHost> eldest) {
                    return this.size() > 6;
                }
            };


    UpHostHelper(Configuration conf, int failedPeriodSeconds) {
        this.conf = conf;
        this.failedPeriodMillis = failedPeriodSeconds * 1000L;
    }


    String upHost(Region region, String upToken, String lastUsedHost, boolean changeHost, boolean mustReturnUpHost)
            throws QiniuException {
        RegionReqInfo regionReqInfo = new RegionReqInfo(upToken);

        // auto region may failed here.
        List<String> accHosts;
        List<String> srcHosts;
        try {
            accHosts = region.getAccUpHost(regionReqInfo);
            srcHosts = region.getSrcUpHost(regionReqInfo);
        } catch (QiniuException e) { // it will success soon.
            if (mustReturnUpHost && conf.useDefaultUpHostIfNone) {
                return failedUpHost("failed_get_region");
            } else {
                throw e;
            }
        }

        String regionKey = region.getRegion(regionReqInfo) + "_&//=_" + getRegionKey(srcHosts, accHosts);
        RegionUpHost regionHost = regionHostsLRU.get(regionKey);
        if (regionHost == null) {
            regionHost = new RegionUpHost();
            regionHostsLRU.put(regionKey, regionHost);
        }

        String host = regionHost.upHost(accHosts, srcHosts, lastUsedHost, changeHost);
        return host;
    }

    private String failedUpHost(String regionKey) {
        List<String> srcHosts;
        List<String> accHosts;

        RegionUpHost regionHost = regionHostsLRU.get(regionKey);
        if (regionHost == null) {
            regionHost = new RegionUpHost();
            regionHostsLRU.put(regionKey, regionHost); // small ,do not need remove it.
        }
        if (regionHost.lastSrcHosts == null) {
            accHosts = new ArrayList<String>() {
                {
                    this.add("upload.qiniup.com");
                    this.add("upload-z1.qiniup.com");
                    this.add("upload-z2.qiniup.com");
                    this.add("upload-na0.qiniup.com");
                    this.add("upload-as0.qiniup.com");
                }
            };
            Collections.shuffle(accHosts); // ?
            srcHosts = new ArrayList<>();
        } else {
            srcHosts = regionHost.lastSrcHosts;
            accHosts = regionHost.lastAccHosts;
        }
        String host = regionHost.upHost(accHosts, srcHosts, null, false);
        return host;
    }


    String getRegionKey(List<String> srcHosts, List<String> accHosts) {
        String host = null;
        String lhost = null;
        for (String a : srcHosts) {
            if (host == null) {
                host = a;
                lhost = a;
            }
            if (a.length() < host.length()) {
                host = a;
            }
            if (a.length() > lhost.length()) {
                lhost = a;
            }
        }
        if (host != null) {
            return host + ";+=-_" + lhost;
        }

        for (String a : accHosts) {
            if (host == null) {
                host = a;
                lhost = a;
            }
            if (a.length() < host.length()) {
                host = a;
            }
            if (a.length() > lhost.length()) {
                lhost = a;
            }
        }
        return host + ";+=-_" + lhost;
    }


    class RegionUpHost {
        List<String> lastAccHosts;
        List<String> lastSrcHosts;

        HashMap<String, Long> hostMark = new HashMap<>();
        ArrayList<String> lastHosts;

        volatile int mainHostCount = 0;
        volatile String lastHost;

        private void initHostMark(List<String> f, List<String> s) {
            ArrayList<String> _lastHosts = new ArrayList<>();
            int _mainHostCount = 0;

            if (f.size() > 0) {
                _lastHosts.add(f.get(0));
                _mainHostCount++;
            }
            if (s.size() > 0) {
                _lastHosts.add(s.get(0));
                _mainHostCount++;
            }
            // 后面的 host 乱序插入
            int l = Math.min(f.size(), s.size());
            int i = 1;

            Random r = new Random();
            ArrayList<Integer> iidx = new ArrayList<>();

            for (; i < l; i++) {
                iidx.add(i);
            }
            randomAdd(_lastHosts, iidx, r, f, s);

            // f or s ,checked by i < f.size() and i < s.size()
            iidx.clear();
            for (; i < f.size(); i++) {
                iidx.add(i);
            }
            randomAdd(_lastHosts, iidx, r, f, null);

            iidx.clear();
            for (; i < s.size(); i++) {
                iidx.add(i);
            }
            randomAdd(_lastHosts, iidx, r, s, null);

            lastHosts = _lastHosts;
            mainHostCount = _mainHostCount;
        }

        private void randomAdd(ArrayList<String> _lastHosts, ArrayList<Integer> iidx, Random r,
                               List<String> list1, List<String> list2) {
            int size = iidx.size();
            for (int j = 0; j < size; j++) {
                int ji = r.nextInt(iidx.size());
                Integer jv = iidx.remove(ji);
                if (list1 != null) {
                    _lastHosts.add(list1.get(jv));
                }
                if (list2 != null) {
                    _lastHosts.add(list2.get(jv));
                }
            }
        }

        String upHost(List<String> accHosts, List<String> srcHosts, String lastUsedHost, boolean changeHost) {
            if (lastAccHosts != accHosts || lastSrcHosts != srcHosts) {
                lastAccHosts = accHosts;
                lastSrcHosts = srcHosts;
                synchronized (hostMark) {
                    hostMark.clear();
                    if (conf.accUpHostFirst) {
                        initHostMark(lastAccHosts, lastSrcHosts);
                    } else {
                        initHostMark(lastSrcHosts, lastAccHosts);
                    }
                    lastHost = lastHosts.get(0); // at last one domain
                }
            }

            long t = System.currentTimeMillis() - failedPeriodMillis;

            // acc src 的第一个，比如(upload.qiniup.com, up.qiniup.com), //
            // 作为主上传域名，可以继续使用，不用恢复到 lastHosts.get(0) //
            // 优先处理 //
            if (!changeHost) {
                Long el0 = hostMark.get(lastHost);
                if (el0 == null) {
                    el0 = 0L;
                }
                if (el0 < t) {
                    int lastUsedIdx = lastHosts.indexOf(lastHost);
                    if (lastUsedIdx > -1 && lastUsedIdx < mainHostCount) {
                        return lastHost;
                    }
                }
                if (lastUsedHost != null && !lastHost.equals(lastUsedHost)) {
                    Long el = hostMark.get(lastUsedHost);
                    if (el == null) {
                        el = 0L;
                    }
                    if (el < t) {
                        int lastUsedIdx = lastHosts.indexOf(lastUsedHost);
                        if (lastUsedIdx > -1 && lastUsedIdx < mainHostCount) {
                            lastHost = lastUsedHost;
                            return lastUsedHost;
                        }
                    }
                }
            }

            if (changeHost) {
                synchronized (hostMark) {
                    // uploader 使用的的 host 与 本地记录的 host 不一样，直接返回本地记录的 host，这也是新的 host //
                    // 减少 多个同时失败，大量同时更改 host，导致 将 host 该回到 老的无效 host //
                    String host = lastUsedHost != null ? lastUsedHost : lastHost;
                    if (!lastHost.equals(host)) {
                        Long el = hostMark.get(lastHost);
                        if (el == null) {
                            el = 0L;
                        }
                        if (el < t) {
                            return lastHost;
                        }
                    }
                    // 标记当前 host 不可用 //
                    hostMark.put(host, System.currentTimeMillis());
                }
            }


            // 下方 未加锁，仍有可能 重复更改 host ，加 return lastHost; 可能会减少这种几率 //
            String _lastHost = lastHost;

            // 标记过期后，恢复到 优先使用 第一个 值 //
            for (String h : lastHosts) {
                Long e = hostMark.get(h);
                if (e == null) {
                    e = 0L;
                }
                // 不可用标志已过期，即域名可以使用 //
                if (e < t) {
                    lastHost = h;
                    return h;
                } else {
                    // 单线程，下面代码没什么意义. //
                    // 其它线程调用了 upHost(true)，可能改变 lastHost 状态，提前返回，不用进入 "全部标记为不可用" //
                    if (_lastHost != lastHost) {
                        return lastHost;
                    }
                }
            }

            // 全部标记为不可用 //
            // 清空所有标记，重新开始 //
            synchronized (hostMark) {
                hostMark.clear();
                lastHost = lastHosts.get(0); // must be in synchronized .  lastHosts.clear() //
            }
            return lastHost;
        }
    }
}
