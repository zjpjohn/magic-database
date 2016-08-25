package com.magic.bitcask.core.factory.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.core.impl.BitCaskFileImpl;
import com.magic.bitcask.util.Util;

public class BitCaskFileFactoryImpl implements BitCaskFileFactory {

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile openBitCaskFile(File fileName) throws IOException {
		ensureFile(fileName);

		FileChannel rch = new RandomAccessFile(fileName, "r").getChannel();

		return new BitCaskFileImpl(Util.tstamp(fileName), fileName, rch);
	}

	@Override
	public BitCaskFile createBitCaskFile(File dirname) throws IOException {
		return createBitCaskFile(dirname, Util.tstamp());
	}

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile createBitCaskFile(File dirname, int tstamp) throws IOException {
		ensuredir(dirname);

		boolean created = false;

		File filename = null;
		while (!created) {
			filename = mk_filename(dirname, tstamp);
			created = filename.createNewFile();
			if (!created) {
				tstamp += 1;
			}
		}

		FileChannel wch = new FileOutputStream(filename, true).getChannel();
		FileChannel wch_hint = new FileOutputStream(hint_filename(filename), true).getChannel();

		FileChannel rch = new RandomAccessFile(filename, "r").getChannel();

		return new BitCaskFileImpl(tstamp, filename, wch, wch_hint, rch);
	}

	/**
	 * Given a directory and a timestamp, construct a data file name.
	 */
	static File mk_filename(File dirname, int tstamp) {
		return new File(dirname, "" + tstamp + ".bitcask.data");
	}

	/**
	 * Given the name of the data file (filename), construct the name of the
	 * corresponding hint file.
	 */
	private static File hint_filename(File filename) {
		File parent = filename.getParentFile();
		String name = filename.getName();

		if (name.endsWith(".data")) {
			return new File(parent, name.substring(0, name.length() - 5) + ".hint");
		} else {
			return new File(parent, name + ".hint");
		}
	}

	/** Create-if-not-exists for directory or fail */
	private void ensuredir(File dirname) {
		if (dirname.exists() && dirname.isDirectory())
			return;
		if (!dirname.mkdirs())
			throw new RuntimeException("cannot create " + dirname);
	}

	/** Create-if-not-exists for directory or fail */
	private void ensureFile(File fileName) {
		if (fileName.exists() && fileName.isFile()) {
			return;
		} else {
			throw new RuntimeException("cannot read " + fileName);
		}
	}

}
