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
			super();
		}
		
		@Deprecated
		public Builder(Region originZone) {
			super(originZone);
		}
		
		@Deprecated
        public Builder region(String region) {
        	return (Builder) super.region(region);
        }

		@Deprecated
        public Builder upHttp(String upHttp) {
        	return (Builder) super.accUpHost(upHttp);
        }
        
		@Deprecated
        public Builder upHttps(String upHttps) {
        	return (Builder) super.accUpHost(upHttps);
        }

		@Deprecated
        public Builder upBackupHttp(String upBackupHttp) {
        	return (Builder) super.accUpHost(upBackupHttp);
        }

		@Deprecated
        public Builder upBackupHttps(String upBackupHttps) {
        	return (Builder) super.accUpHost(upBackupHttps);
        }

		@Deprecated
        public Builder iovipHttp(String iovipHttp) {
        	return (Builder) super.iovipHost(iovipHttp);
        }

		@Deprecated
        public Builder iovipHttps(String iovipHttps) {
        	return (Builder) super.iovipHost(iovipHttps);
        }

		@Deprecated
        public Builder rsHttp(String rsHttp) {
        	return (Builder) super.rsHost(rsHttp);
        }

		@Deprecated
        public Builder rsHttps(String rsHttps) {
        	return (Builder) super.rsHost(rsHttps);
        }

		@Deprecated
        public Builder rsfHttp(String rsfHttp) {
        	return (Builder) super.rsfHost(rsfHttp);
        }

		@Deprecated
        public Builder rsfHttps(String rsfHttps) {
        	return (Builder) super.rsfHost(rsfHttps);
        }

		@Deprecated
        public Builder apiHttp(String apiHttp) {
        	return (Builder) super.apiHost(apiHttp);
        }

		@Deprecated
        public Builder apiHttps(String apiHttps) {
        	return (Builder) super.apiHost(apiHttps);
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
		return new Builder(Region.region0()).build();
    }
    
	@Deprecated
    public static Zone huadong() {
        return zone0();
    }

	@Deprecated
    public static Zone qvmZone0() {
		return new Builder(Region.qvmRegion0()).build();
    }

	@Deprecated
    public static Zone qvmHuadong() {
        return qvmZone0();
    }

	@Deprecated
    public static Zone zone1() {
		return new Builder(Region.region1()).build();
    }

	@Deprecated
    public static Zone huabei() {
        return zone1();
    }

	@Deprecated
    public static Zone qvmZone1() {
		return new Builder(Region.qvmRegion1()).build();
    }

	@Deprecated
    public static Zone qvmHuabei() {
        return qvmZone1();
    }

	@Deprecated
    public static Zone zone2() {
		return new Builder(Region.region2()).build();
    }

	@Deprecated
    public static Zone huanan() {
        return zone2();
    }

	@Deprecated
    public static Zone zoneNa0() {
		return new Builder(Region.regionNa0()).build();
    }

	@Deprecated
    public static Zone beimei() {
        return zoneNa0();
    }

	@Deprecated
    public static Zone zoneAs0() {
		return new Builder(Region.regionAs0()).build();
    }

	@Deprecated
    public static Zone xinjiapo() {
        return zoneAs0();
    }

	@Deprecated
    public static Region autoZone() {
        return (Region) Region.autoRegion();
    }
    
}
