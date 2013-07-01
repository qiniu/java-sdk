package com.qiniu.testing;

import java.io.File;
import java.io.RandomAccessFile;

public class FileUtils {

	/**
	 * Make a fixed size file specified by the parameter fsize.
	 * @param  fsize 
	 * 		   the test file's size
	 * @return  A RandomAccessFile object
	 * @throws  Exception if any exception occurs
	 */
	public static RandomAccessFile makeFixedSizeTestFile(int fsize)
			throws Exception {

		File file = new File("temp");
		// to confirm that the file will be deleted, when the jvm exit.
		// not a good, maybe.
		file.deleteOnExit(); 
		RandomAccessFile testFile = new RandomAccessFile(file, "rw");

		for (int i = 0; i < fsize; i++) {
			testFile.writeByte('a' ^ i);
		}
		
		// set the read pointer points to the beginning of the file.
		testFile.seek(0);	
		return testFile;
	}

}