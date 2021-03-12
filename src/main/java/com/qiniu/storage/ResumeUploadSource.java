package com.qiniu.storage;

import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ResumeUploadSource {

    final long size;
    final int blockSize;
    final String fileName;
    final String sourceId;
    final String targetRegionId;
    final List<Block> blockList;
    final Configuration.ResumeVersion resumeVersion;

    transient final UploadSource source;
    transient final Configuration config;

    // source 读取的 offset, 只能增大不能减小
    long readOffset;

    // uploadId: 此次文件上传唯一标识 【resume v2 特有】
    String uploadId;
    // expireAt: uploadId 有效期， 单位：秒 【resume v2 特有】
    Long expireAt;

    ResumeUploadSource(UploadSource source, Configuration config, String targetRegionId) {
        this.source = source;
        this.sourceId = source.getID();
        this.fileName = source.getFileName();
        this.size = source.getSize();
        this.config = config;
        this.blockSize = config.resumeV2BlockSize;
        this.targetRegionId = targetRegionId;
        this.blockList = createBlockList(config, source.getSize(), blockSize);
        this.resumeVersion = config.resumeVersion;
    }

    private boolean isSameResource(ResumeUploadSource source) {
        return source != null &&
                source.sourceId != null && source.sourceId.equals(sourceId) &&
                source.size == size && source.blockSize == blockSize &&
                source.fileName != null && source.fileName.equals(fileName) &&
                source.targetRegionId != null && source.targetRegionId.equals(targetRegionId) &&
                source.resumeVersion == resumeVersion;
    }

    boolean copyResourceUploadStateWhenValidAndSame(ResumeUploadSource source) {
        // resume v2 比服务有效期少 2 天
        if (!StringUtils.isNullOrEmpty(source.uploadId) && source.expireAt - 1000 * 3600 * 24 * 2 < System.currentTimeMillis()) {
            return false;
        }

        if (!isSameResource(source)) {
            return false;
        }

        uploadId = source.uploadId;
        expireAt = source.expireAt;
        if (blockList != null && blockList.size() > 0 &&
                source.blockList != null && source.blockList.size() > 0 &&
                source.blockList.size() == blockList.size()) {
            for (int i = 0; i < blockList.size(); i++) {
                Block block = blockList.get(i);
                Block blockCopy = source.blockList.get(i);
                block.state = blockCopy.state;
                block.context = blockCopy.context;
                block.etag = blockCopy.etag;
            }
        }
        return true;
    }

    boolean isAllBlocksUploaded() {
        boolean isAllBlockUploaded = true;
        for (Block block : blockList) {
            if (block.state != Block.UploadState.Uploaded) {
                isAllBlockUploaded = false;
                break;
            }
        }
        return isAllBlockUploaded;
    }

    byte[] getBlockData(Block block) throws IOException {
        if (block.data != null) {
            return block.data;
        }

        byte[] buffer = null;
        synchronized (this) {
            while (true) {
                if (readOffset == block.offset) {
                    int readSize = 0;
                    buffer = new byte[block.size];
                    while (readSize != block.size) {
                        readSize += source.readData(buffer, readSize, block.size);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    block.data = buffer;
                    readOffset += block.size;
                    break;
                } else if (readOffset < block.offset) {
                    readOffset += source.skip(block.offset - readOffset);
                } else {
                    throw new IOException("read block data error");
                }
            }
        }
        return buffer;
    }

    synchronized Block getNextUploadingBlock() {
        Block block = null;
        for (Block blockP : blockList) {
            if (blockP.state == Block.UploadState.Waiting) {
                blockP.state = Block.UploadState.Uploading;
                block = blockP;
                break;
            }
        }
        return block;
    }

    // 分片 V1 make file 使用
    String[] getAllBlockContextList() {
        String[] contextList = new String[blockList.size()];
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            if (block.context != null) {
                contextList[i] = block.context;
            }
        }
        return contextList;
    }

    // 分片 V2 complete upload 使用
    List<StringMap> getPartInfo() {

        // 排序
        Collections.sort(blockList, new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return o1.index - o2.index; // small enough and both greater than 0 //
            }
        });

        List<StringMap> partInfo = new ArrayList<>();
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            StringMap part = new StringMap();
            if (block.etag != null) {
                part.put("partNumber", block.index);
                part.put("etag", block.etag);
            }
            partInfo.add(part);
        }
        return partInfo;
    }

    private List<Block> createBlockList(Configuration config, long fileSize, int blockSize) {
        long offset = 0;
        int blockIndex = 1;
        List<Block> blockList = new ArrayList<>();
        while (offset < fileSize) {
            int lastSize = (int) (fileSize - offset);
            int blockSizeP = Math.min(lastSize, blockSize);
            Block block = new Block(config, offset, blockSizeP, blockIndex);
            blockList.add(block);
            offset += blockSizeP;
            blockIndex += 1;
        }
        return blockList;
    }

    static class Block {
        final int index;
        final long offset;
        final int size;

        private UploadState state;
        private transient final Configuration config;
        private transient byte[] data;

        // context: 块上传上下文信息 【resume v1 特有】
        String context;
        // etag: 块etag【resume v2 特有】
        String etag;

        private Block(Configuration config, long offset, int blockSize, int index) {
            this.config = config;
            this.offset = offset;
            this.size = blockSize;
            this.index = index;
            this.state = UploadState.Waiting;
            this.etag = null;
            this.context = null;
        }

        void updateState() {
            if (config.resumeVersion == Configuration.ResumeVersion.V1) {
                if (StringUtils.isNullOrEmpty(context)) {
                    state = UploadState.Waiting;
                } else {
                    state = UploadState.Uploaded;
                    data = null;
                }
            } else {
                if (StringUtils.isNullOrEmpty(etag)) {
                    state = UploadState.Waiting;
                } else {
                    state = UploadState.Uploaded;
                    data = null;
                }
            }
        }

        enum UploadState {
            Waiting,
            Uploading,
            Uploaded,
        }
    }
}
