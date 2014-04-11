package com.qiniu.api.resumableio.resume;

public abstract class BaseResume implements Resumable {

	protected int blockCount;
	protected String key;
	protected Block[] blocks;
	
	public BaseResume(int blockCount, String key){
		this.blockCount = blockCount;
		this.key = key;
		initBlocks();
	}
	
	private void initBlocks(){
		blocks = new Block[blockCount];
	}
	
	@Override
	public int blockCount() {
		return blockCount;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public boolean isDone() {
		return this.blockCount == doneCount();
	}

	@Override
	public boolean isBlockDone(int idx) {
		return blocks[idx] != null;
	}
	
	@Override
	public int doneCount(){
		int count = 0;
		for(Block b : blocks){
			if(b != null){
				count ++;
			}
		}
		return count;
	}
	
	@Override
	public Block getBlock(int idx) {
		return blocks[idx];
	}

	@Override
	public String getCtx(int idx) {
		Block block = blocks[idx];
		if(block != null){
			return block.getCtx();
		}else{
			return null;
		}
	}

	@Override
	public void set(Block block){
		blocks[block.getIdx()] = block;
	}
	
	@Override
	public void drop(Block block){
		blocks[block.getIdx()] = null;
	}


}
