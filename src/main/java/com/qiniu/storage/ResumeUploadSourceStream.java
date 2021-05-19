package com.qiniu.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class ResumeUploadSourceStream extends ResumeUploadSource {

    private long readOffset = 0;
    private boolean isAllDataRead;
    private final InputStream inputStream;
    private final String fileName;

    ResumeUploadSourceStream(InputStream inputStream, Configuration config, String recordKey, String fileName) {
        super(config, recordKey);
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.blockList = new LinkedList<>();
    }

    @Override
    boolean isAllBlocksUploadingOrUploaded() {
        if (!isAllDataRead) {
            return false;
        }
        return super.isAllBlocksUploadingOrUploaded();
    }

    @Override
    boolean isAllBlocksUploaded() {
        if (!isAllDataRead) {
            return false;
        }
        return super.isAllBlocksUploaded();
    }

    @Override
    Block getNextUploadingBlock() throws IOException {
        ResumeUploadSource.Block block = super.getNextUploadingBlock();
        if (block != null) {
            return block;
        }

        block = new Block(config, readOffset, getBlockSize(config), blockList.size());
        block.data = getBlockData(block);
        if (block.size == 0) {
            return null;
        }

        blockList.add(block);
        return block;
    }

    @Override
    void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Override
    boolean isValid() {
        return inputStream != null;
    }

    /// 只有数据流读取结束才有效，即 isAllBlocksUploaded() 为 true 时值有效
    @Override
    long getSize() {
        return readOffset;
    }

    @Override
    String getFileName() {
        return fileName;
    }

    private byte[] getBlockData(ResumeUploadSource.Block block) throws IOException {
        if (block.data != null) {
            return block.data;
        }

        byte[] buffer = null;
        synchronized (this) {
            while (true) {
                if (readOffset == block.offset) {
                    int readSize = 0;
                    buffer = new byte[block.size];
                    while (readSize < block.size) {
                        int ret = inputStream.read(buffer, readSize, block.size - readSize);
                        if (ret < 0) {
                            isAllDataRead = true;
                            break;
                        }
                        readSize += ret;
                    }
                    block.data = buffer;
                    block.size = readSize;
                    readOffset += readSize;
                    break;
                } else if (readOffset < block.offset) {
                    readOffset += inputStream.skip(block.offset - readOffset);
                } else {
                    throw new IOException("read block data error");
                }
            }
        }
        return buffer;
    }
}
