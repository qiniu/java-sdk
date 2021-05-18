package com.qiniu.storage;

import com.qiniu.common.QiniuException;

import java.util.ArrayList;
import java.util.List;

public class RegionGroup extends Region {

    private Region currentRegion = null;
    private int currentRegionIndex = 0;
    private final List<Region> regionList = new ArrayList<>();


    public boolean addRegion(Region region) {
        if (region == null) {
            return false;
        }

        regionList.add(region);

        if (currentRegion == null) {
            updateCurrentRegion();
        }

        return true;
    }

    String getRegion(RegionReqInfo regionReqInfo) {
        if (currentRegion == null) {
            return "";
        } else {
            return currentRegion.getRegion(regionReqInfo);
        }
    }

    List<String> getSrcUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getSrcUpHost(regionReqInfo);
        }
    }

    List<String> getAccUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getAccUpHost(regionReqInfo);
        }
    }

    String getIovipHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getIovipHost(regionReqInfo);
        }
    }

    String getRsHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getRsHost(regionReqInfo);
        }
    }

    String getRsfHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getRsfHost(regionReqInfo);
        }
    }

    String getApiHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getApiHost(regionReqInfo);
        }
    }

    Region getCurrentRegion(RegionReqInfo regionReqInfo) {
        if (currentRegion == null) {
            return null;
        } else if (currentRegion instanceof AutoRegion || currentRegion instanceof RegionGroup) {
            return currentRegion.getCurrentRegion(regionReqInfo);
        } else {
            return currentRegion;
        }
    }

    private void updateCurrentRegion() {
        if (regionList.size() == 0) {
            return;
        }

        if (currentRegionIndex < regionList.size()) {
            currentRegion = regionList.get(currentRegionIndex);
        }
    }
}
