package com.qiniu.processing;

public interface Operation {
    String toString();

    boolean onlyPersistent();
}
