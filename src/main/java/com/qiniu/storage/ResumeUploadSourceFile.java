package com.qiniu.storage;

import com.qiniu.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;

class ResumeUploadSourceFile extends ResumeUploadSource {

    private final long size;
    private final String fileName;
    private final transient File file;
    private transient RandomAccessFile randomAccessFile;

    ResumeUploadSourceFile(File file, Configuration config, String recordKey) {
        super(config, recordKey);
        this.file = file;
        this.fileName = file.getName();
        this.size = file.length();
        createBlockList(config, size, blockSize);
    }

    private void createBlockList(Configuration config, long fileSize, int blockSize) {
        blockList = new ArrayList<>();
        long offset = 0;
        int blockIndex = 0;
        while (offset < fileSize) {
            int lastSize = (int) (fileSize - offset);
            int blockSizeP = Math.min(lastSize, blockSize);
            Block block = new Block(config, offset, blockSizeP, blockIndex);
            blockList.add(block);
            offset += blockSizeP;
            blockIndex += 1;
        }
    }

    @Override
    boolean isValid() {
        return file != null && file.canRead();
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    String getFileName() {
        return fileName;
    }

    @Override
    boolean couldReload() {
        return true;
    }

    @Override
    boolean reload() {
        return true;
    }

    @Override
    ResumeUploadSource.Block getNextUploadingBlock() throws IOException {
        ResumeUploadSource.Block block = super.getNextUploadingBlock();
        if (block != null && block.data == null) {
            setBlockData(block);
        }
        return block;
    }

    private void setBlockData(ResumeUploadSource.Block block) throws IOException {

        int readSize = 0;
        byte[] buffer = new byte[block.size];

        RandomAccessFile randomAccessFile = getRandomAccessFile();
        randomAccessFile.seek(block.offset);

        while (readSize != block.size) {
            int ret = randomAccessFile.read(buffer, readSize, block.size - readSize);
            if (ret < 0) {
                break;
            }
            readSize += ret;
        }

        if (readSize < block.size) {
            throw new IOException("read file data error");
        }

        block.data = buffer;
    }

    private RandomAccessFile getRandomAccessFile() throws IOException {
        if (randomAccessFile == null && file != null) {
            randomAccessFile = new RandomAccessFile(file, "r");
        }
        return randomAccessFile;
    }

    @Override
    boolean recoverFromRecordInfo(ResumeUploadSource source) {
        if (!isSameResource(source)) {
            return false;
        }

        boolean needRecovered = true;
        if (source.resumableUploadAPIVersion == Configuration.ResumableUploadAPIVersion.V2) {
            if (StringUtils.isNullOrEmpty(source.uploadId)) {
                return false;
            }
            // 服务端是 7 天，此处有效期少 1 天，为 6 天
            long currentTimestamp = new Date().getTime() / 1000;
            long expireAtTimestamp = source.expireAt - 24 * 3600;
            needRecovered = expireAtTimestamp > currentTimestamp;
        }

        if (needRecovered) {
            uploadId = source.uploadId;
            expireAt = source.expireAt;
            blockList = source.blockList;
        }
        return needRecovered;
    }

    private boolean isSameResource(ResumeUploadSource source) {
        if (!(source instanceof ResumeUploadSourceFile)) {
            return false;
        }

        ResumeUploadSourceFile sourceFile = (ResumeUploadSourceFile) source;
        if (sourceFile.recordKey == null || !sourceFile.recordKey.equals(recordKey)) {
            return false;
        }

        if (sourceFile.size != size || sourceFile.blockSize != blockSize) {
            return false;
        }

        if (sourceFile.blockList == null || sourceFile.blockList.size() == 0) {
            return false;
        }

        if (sourceFile.getFileName() == null || !sourceFile.getFileName().equals(getFileName())) {
            return false;
        }

        return sourceFile.resumableUploadAPIVersion == resumableUploadAPIVersion;
    }

    @Override
    void close() throws IOException {
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}
