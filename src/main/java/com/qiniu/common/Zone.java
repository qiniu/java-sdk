package com.qiniu.common;

/**
 * 建议用Region
 */
@Deprecated
public class Zone extends Region {

    @Deprecated
    public static class Builder extends Region.Builder {

        @Deprecated
        public Builder() {
        }

        @Deprecated
        public Builder(Region originZone) {
            super(originZone);
        }

        @Override
        protected void init() {
            region = new Zone();
        }

        @Deprecated
        public Region autoZone() {
            return super.autoRegion();
        }

        @Deprecated
        public Zone build() {
            return (Zone) super.build();
        }
    }

    @Deprecated
    public static Zone zone0() {
        return zone(Region.region0());
    }

    @Deprecated
    public static Zone huadong() {
        return zone0();
    }

    @Deprecated
    public static Zone qvmZone0() {
        return zone(Region.qvmRegion0());
    }

    @Deprecated
    public static Zone qvmHuadong() {
        return qvmZone0();
    }

    @Deprecated
    public static Zone zone1() {
        return zone(Region.region1());
    }

    @Deprecated
    public static Zone huabei() {
        return zone1();
    }

    @Deprecated
    public static Zone qvmZone1() {
        return zone(Region.qvmRegion1());
    }

    @Deprecated
    public static Zone qvmHuabei() {
        return qvmZone1();
    }

    @Deprecated
    public static Zone zone2() {
        return zone(Region.region2());
    }

    @Deprecated
    public static Zone huanan() {
        return zone2();
    }

    @Deprecated
    public static Zone zoneNa0() {
        return zone(Region.regionNa0());
    }

    @Deprecated
    public static Zone beimei() {
        return zoneNa0();
    }

    @Deprecated
    public static Zone zoneAs0() {
        return zone(Region.regionAs0());
    }

    @Deprecated
    public static Zone xinjiapo() {
        return zoneAs0();
    }

    @Deprecated
    public static Region autoZone() {
        return Region.autoRegion();
    }


    private static Zone zone(Region region) {
        return new Builder(region).build();
    }

}
