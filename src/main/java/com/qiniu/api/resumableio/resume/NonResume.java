package com.qiniu.api.resumableio.resume;

public class NonResume extends BaseResume {


	public NonResume(int blockCount, String key) {
		super(blockCount, key);
	}

	@Override
	public void load() {
		
	}

	@Override
	public void save() {
		
	}

	@Override
	public void clean() {
		
	}

}
