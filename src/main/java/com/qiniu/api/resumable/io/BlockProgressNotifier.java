package com.qiniu.api.resumable.io;

/**
 * Notifies the progress of a block upload.
 */
public interface BlockProgressNotifier {
	void notify(int blockIndex, BlockProgress progress);
}
