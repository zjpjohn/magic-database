package com.magic.bitcask.util;

import java.io.File;

public class Util {

	public static int tstamp(File file) {
		String name = file.getName();
		int idx = name.indexOf('.');
		int val = Integer.parseInt(name.substring(0, idx));
		return val;
	}

	/** in bitcask, timestamp is the #seconds in the system */
	public static int tstamp() {
		return (int) (System.currentTimeMillis() / 1000L);
	}

	/** Create-if-not-exists for directory or fail */
	public static void ensuredir(File dirname) {
		if (dirname.exists() && dirname.isDirectory())
			return;
		if (!dirname.mkdirs())
			throw new RuntimeException("cannot create " + dirname);
	}

	/**
	 * Given a directory and a timestamp, construct a data file name.
	 */
	public static File mkFilename(File dirname, int tstamp) {
		return new File(dirname, "" + tstamp + ".bitcask.data");
	}

}
