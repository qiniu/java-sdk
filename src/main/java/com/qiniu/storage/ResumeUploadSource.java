package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

abstract class ResumeUploadSource {

    final String recordKey;
    final int blockSize;
    final String targetRegionId;
    final Configuration.ResumeVersion resumeVersion;

    transient final Configuration config;

    List<Block> blockList;
    // uploadId: 此次文件上传唯一标识 【resume v2 特有】
    String uploadId;
    // expireAt: uploadId 有效期， 单位：秒 【resume v2 特有】
    Long expireAt;

    ResumeUploadSource(Configuration config, String recordKey, String targetRegionId) {
        this.config = config;
        this.blockSize = getBlockSize(config);
        this.recordKey = recordKey;
        this.targetRegionId = targetRegionId;
        this.resumeVersion = config.resumeVersion;
    }

    // 所有块数据是否均已上传
    boolean isAllBlocksUploaded() {
        if (blockList == null || blockList.size() == 0) {
            return true;
        }

        boolean isAllBlockUploaded = true;
        for (ResumeUploadSource.Block block : blockList) {
            if (!block.isUploaded()) {
                isAllBlockUploaded = false;
                break;
            }
        }
        return isAllBlockUploaded;
    }

    ;

    // 获取下一个需要上传的块
    ResumeUploadSource.Block getNextUploadingBlock() throws IOException {
        ResumeUploadSource.Block block = null;
        for (ResumeUploadSource.Block blockP : blockList) {
            if (!blockP.isUploading && !blockP.isUploaded()) {
                block = blockP;
                break;
            }
        }
        return block;
    }

    // 关闭数据流
    abstract void close() throws IOException;

    // 是否为有效源文件
    abstract boolean isValid();

    // 获取资源大小
    abstract long getSize();

    // 获取文件名
    abstract String getFileName();

    boolean recoverFromRecordInfo(ResumeUploadSource source) {
        return false;
    }

    int getBlockSize(Configuration config) {
        if (resumeVersion == Configuration.ResumeVersion.V2) {
            return config.resumeV2BlockSize;
        } else {
            return Constants.BLOCK_SIZE;
        }
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

    static class Block {
        final int index;
        final long offset;
        final Configuration.ResumeVersion resumeVersion;

        int size;

        transient byte[] data;
        transient boolean isUploading;

        // context: 块上传上下文信息 【resume v1 特有】
        String context;
        // etag: 块etag【resume v2 特有】
        String etag;

        Block(Configuration config, long offset, int blockSize, int index) {
            this.resumeVersion = config.resumeVersion;
            this.offset = offset;
            this.size = blockSize;
            this.index = index;
            this.isUploading = false;
            this.etag = null;
            this.context = null;
        }

        boolean isUploaded() {
            boolean isUploaded = false;
            if (resumeVersion == Configuration.ResumeVersion.V1) {
                if (!StringUtils.isNullOrEmpty(context)) {
                    isUploaded = true;
                }
            } else {
                if (!StringUtils.isNullOrEmpty(etag)) {
                    isUploaded = true;
                }
            }
            return isUploaded;
        }
    }
}
