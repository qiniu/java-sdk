package com.qiniu.processing;

public interface Operation {
    String build();

    boolean onlyPersistent();
}
