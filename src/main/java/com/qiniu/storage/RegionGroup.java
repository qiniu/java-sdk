package com.qiniu.storage;

import com.qiniu.common.QiniuException;

import java.util.ArrayList;
import java.util.List;

public class RegionGroup extends Region implements Cloneable {

    private Region currentRegion = null;
    private int currentRegionIndex = 0;
    private final List<Region> regionList = new ArrayList<>();


    public synchronized boolean addRegion(Region region) {
        if (region == null) {
            return false;
        }

        regionList.add(region);

        if (currentRegion == null) {
            updateCurrentRegion();
        }

        return true;
    }

    @Override
    synchronized boolean switchRegion(RegionReqInfo regionReqInfo) {
        if (currentRegion != null && currentRegion.isValid() && currentRegion.switchRegion(regionReqInfo)) {
            return true;
        }

        if ((currentRegionIndex + 1) < regionList.size()) {
            currentRegionIndex += 1;
            updateCurrentRegion();
            return true;
        } else {
            return false;
        }
    }

    @Override
    String getRegion(RegionReqInfo regionReqInfo) {
        if (currentRegion == null) {
            return "";
        } else {
            return currentRegion.getRegion(regionReqInfo);
        }
    }

    @Override
    List<String> getSrcUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getSrcUpHost(regionReqInfo);
        }
    }

    @Override
    List<String> getAccUpHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getAccUpHost(regionReqInfo);
        }
    }

    @Override
    String getIovipHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getIovipHost(regionReqInfo);
        }
    }

    @Override
    String getIoSrcHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getIoSrcHost(regionReqInfo);
        }
    }

    @Override
    String getRsHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getRsHost(regionReqInfo);
        }
    }

    @Override
    String getRsfHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getRsfHost(regionReqInfo);
        }
    }

    @Override
    String getApiHost(RegionReqInfo regionReqInfo) throws QiniuException {
        if (currentRegion == null) {
            return null;
        } else {
            return currentRegion.getApiHost(regionReqInfo);
        }
    }

    @Override
    Region getCurrentRegion(RegionReqInfo regionReqInfo) {
        if (currentRegion == null) {
            return null;
        } else if (currentRegion instanceof AutoRegion || currentRegion instanceof RegionGroup) {
            return currentRegion.getCurrentRegion(regionReqInfo);
        } else {
            return currentRegion;
        }
    }

    @Override
    boolean isValid() {
        if (regionList.size() == 0) {
            return false;
        }

        boolean valid = true;
        for (Region region : regionList) {
            if (region == null || !region.isValid()) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private void updateCurrentRegion() {
        if (regionList.size() == 0) {
            return;
        }

        if (currentRegionIndex < regionList.size()) {
            currentRegion = regionList.get(currentRegionIndex);
        }
    }

    @Override
    public synchronized Object clone() {
        RegionGroup region = new RegionGroup();
        for (Region subRegion : regionList) {
            if (subRegion != null) {
                subRegion = (Region) subRegion.clone();
                region.addRegion(subRegion);
            }
        }
        region.currentRegionIndex = currentRegionIndex;
        region.updateCurrentRegion();
        return region;
    }
}
