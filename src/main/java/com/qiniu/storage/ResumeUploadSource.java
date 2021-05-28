package com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.util.StringUtils;

import java.io.IOException;
import java.util.*;

abstract class ResumeUploadSource {

    final String recordKey;
    final int blockSize;
    final Configuration.ResumableUploadAPIVersion resumableUploadAPIVersion;

    transient Configuration config;

    List<Block> blockList;
    // uploadId: 此次文件上传唯一标识 【resume v2 特有】
    String uploadId;
    // expireAt: uploadId 有效期， 单位：秒 【resume v2 特有】
    Long expireAt;

    ResumeUploadSource() {
        this.blockSize = 0;
        this.recordKey = null;
        this.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V1;
    }

    ResumeUploadSource(Configuration config, String recordKey) {
        this.config = config;
        this.blockSize = getBlockSize(config);
        this.recordKey = recordKey;
        this.resumableUploadAPIVersion = config.resumableUploadAPIVersion;
    }

    // 所有块数据是否 正在上传 或者 已上传，为 true 则说明没有需要上传的数据块
    boolean isAllBlocksUploadingOrUploaded() {
        if (blockList == null || blockList.size() == 0) {
            return true;
        }

        boolean isAllBlockUploadingOrUploaded = true;
        for (ResumeUploadSource.Block block : blockList) {
            if (!block.isUploading && !block.isUploaded()) {
                isAllBlockUploadingOrUploaded = false;
                break;
            }
        }
        return isAllBlockUploadingOrUploaded;
    }

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

    boolean couldReload() {
        return false;
    }

    boolean reload() {
        return false;
    }

    void clearState() {
        for (ResumeUploadSource.Block block : blockList) {
            block.clearState();
        }
        uploadId = null;
        expireAt = null;
    }

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

    // 是否有已上传的数据
    boolean hasUploadData() {
        if (blockList == null || blockList.size() == 0) {
            return false;
        }

        boolean hasUploadData = false;
        for (ResumeUploadSource.Block block : blockList) {
            if (block.isUploaded()) {
                hasUploadData = true;
                break;
            }
        }
        return hasUploadData;
    }

    boolean recoverFromRecordInfo(ResumeUploadSource source) {
        return false;
    }

    int getBlockSize(Configuration config) {
        if (resumableUploadAPIVersion == Configuration.ResumableUploadAPIVersion.V2) {
            return config.resumableUploadAPIV2BlockSize;
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
    List<Map<String, Object>> getPartInfo() {

        // 排序
        Collections.sort(blockList, new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return o1.index - o2.index; // small enough and both greater than 0 //
            }
        });

        List<Map<String, Object>> partInfo = new ArrayList<>();
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            Map<String, Object> part = new HashMap<>();
            if (block.etag != null) {
                part.put("partNumber", block.index + 1);
                part.put("etag", block.etag);
            }
            partInfo.add(part);
        }
        return partInfo;
    }

    static class Block {
        final int index;
        final long offset;
        final Configuration.ResumableUploadAPIVersion resumableUploadAPIVersion;

        int size;

        transient byte[] data;
        transient boolean isUploading;

        // context: 块上传上下文信息 【resume v1 特有】
        String context;
        // etag: 块etag【resume v2 特有】
        String etag;

        Block() {
            this.offset = 0;
            this.index = 0;
            this.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V1;
        }

        Block(Configuration config, long offset, int blockSize, int index) {
            this.resumableUploadAPIVersion = config.resumableUploadAPIVersion;
            this.offset = offset;
            this.size = blockSize;
            this.index = index;
            this.clearState();
        }

        boolean isUploaded() {
            boolean isUploaded = false;
            if (resumableUploadAPIVersion == Configuration.ResumableUploadAPIVersion.V1) {
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

        void clearState() {
            this.isUploading = false;
            this.etag = null;
            this.context = null;
            this.data = null;
        }
    }
}
