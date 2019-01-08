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
		public Builder(Zone originZone) {
			super(originZone);
		}
		
		@Deprecated
        public Builder region(String region) {
        	return (Builder) super.region(region);
        }

		@Deprecated
        public Builder upHttp(String upHttp) {
        	return (Builder) super.upHttp(upHttp);
        }
        
		@Deprecated
        public Builder upHttps(String upHttps) {
        	return (Builder) super.upHttps(upHttps);
        }

		@Deprecated
        public Builder upBackupHttp(String upBackupHttp) {
        	return (Builder) super.upBackupHttp(upBackupHttp);
        }

		@Deprecated
        public Builder upBackupHttps(String upBackupHttps) {
        	return (Builder) super.upBackupHttps(upBackupHttps);
        }

		@Deprecated
        public Builder upIpHttp(String upIpHttp) {
        	return (Builder) super.upHttp(upIpHttp);
        }

		@Deprecated
        public Builder upIpHttps(String upIpHttps) {
        	return (Builder) super.upIpHttps(upIpHttps);
        }

		@Deprecated
        public Builder iovipHttp(String iovipHttp) {
        	return (Builder) super.iovipHttp(iovipHttp);
        }

		@Deprecated
        public Builder iovipHttps(String iovipHttps) {
        	return (Builder) super.iovipHttps(iovipHttps);
        }

		@Deprecated
        public Builder rsHttp(String rsHttp) {
        	return (Builder) super.rsHttp(rsHttp);
        }

		@Deprecated
        public Builder rsHttps(String rsHttps) {
        	return (Builder) super.rsHttps(rsHttps);
        }

		@Deprecated
        public Builder rsfHttp(String rsfHttp) {
        	return (Builder) super.rsfHttp(rsfHttp);
        }

		@Deprecated
        public Builder rsfHttps(String rsfHttps) {
        	return (Builder) super.rsfHttps(rsfHttps);
        }

		@Deprecated
        public Builder apiHttp(String apiHttp) {
        	return (Builder) super.apiHttp(apiHttp);
        }

		@Deprecated
        public Builder apiHttps(String apiHttps) {
        	return (Builder) super.apiHttps(apiHttps);
        }

		@Deprecated
        public Zone autoZone() {
            return AutoZone.instance;
        }
		
		@Deprecated
		public Zone build() {
			return (Zone) super.build();
		}
	}

	@Deprecated
    public static Zone zone0() {
        return (Zone) Region.region0();
    }
    
	@Deprecated
    public static Zone huadong() {
        return zone0();
    }

	@Deprecated
    public static Zone qvmZone0() {
        return (Zone) Region.qvmRegion0();
    }

	@Deprecated
    public static Zone qvmHuadong() {
        return qvmZone0();
    }

	@Deprecated
    public static Zone zone1() {
        return (Zone) Region.region1();
    }

	@Deprecated
    public static Zone huabei() {
        return zone1();
    }

	@Deprecated
    public static Zone qvmZone1() {
        return (Zone) Region.qvmRegion1();
    }

	@Deprecated
    public static Zone qvmHuabei() {
        return qvmZone1();
    }

	@Deprecated
    public static Zone zone2() {
        return (Zone) Region.region2();
    }

	@Deprecated
    public static Zone huanan() {
        return zone2();
    }

	@Deprecated
    public static Zone zoneNa0() {
        return (Zone) Region.regionNa0();
    }

	@Deprecated
    public static Zone beimei() {
        return zoneNa0();
    }

	@Deprecated
    public static Zone zoneAs0() {
        return (Zone) Region.regionAs0();
    }

	@Deprecated
    public static Zone xinjiapo() {
        return zoneAs0();
    }

	@Deprecated
    public static Zone autoZone() {
        return (Zone) Region.autoRegion();
    }
    
}
