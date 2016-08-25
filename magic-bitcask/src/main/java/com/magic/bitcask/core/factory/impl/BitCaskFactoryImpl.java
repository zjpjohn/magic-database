package com.magic.bitcask.core.factory.impl;

import java.io.File;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.core.KeyIterator;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.core.factory.BitCaskKeyManagerFactory;
import com.magic.bitcask.core.impl.BitCaskFileImpl;
import com.magic.bitcask.core.impl.BitCaskImpl;
import com.magic.bitcask.core.impl.BitCaskLock;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.entity.BitCaskOptions;
import com.magic.bitcask.enums.Type;
import com.magic.bitcask.util.Util;

public class BitCaskFactoryImpl implements BitCaskFactory {

	public static final BitCaskFileImpl FRESH_FILE = new BitCaskFileImpl();

	private BitCaskKeyManagerFactory keyManagerFactory = new BitCaskKeyManagerFactoryImpl();

	private BitCaskFileFactory fileFactory = new BitCaskFileFactoryImpl();

	private static final String DATABASE_DIR = "bitcaskdb";

	@Override
	public BitCask createBitCask(File dirname, BitCaskOptions opts) throws Exception {
		BitCaskImpl result = new BitCaskImpl();

		File dbDir = new File(dirname, DATABASE_DIR);
		ensuredir(dbDir);

		if (opts.is_read_write()) {
			BitCaskLock.deleteStaleLock(Type.WRITE, dbDir);
			result.setWriteFile(FRESH_FILE);
		}

		result.setDirname(dbDir);

		BitCaskKeydir keyManager = keyManagerFactory.createBitCaskKeyManager(dbDir, opts.open_timeout_secs);
		result.setKeyManager(keyManager);
		if (!keyManager.isReady()) {
			File[] files = result.readableFiles();
			scanKeyFiles(result, files, keyManager);
			keyManager.markReady();
		}

		result.setMaxFileSize(opts.max_file_size);
		result.setDirname(dbDir);
		result.setOptions(opts);
		result.setBitCaskFileFactory(new BitCaskFileFactoryImpl());
		return result;
	}

	/** Create-if-not-exists for directory or fail */
	private void ensuredir(File dirname) {
		if (dirname.exists() && dirname.isDirectory()) {
			return;
		}
		if (!dirname.mkdirs()) {
			throw new RuntimeException("cannot create " + dirname);
		}
	}

	private void scanKeyFiles(BitCaskImpl result, File[] files, final BitCaskKeydir keydir) throws Exception {
		for (File f : files) {
			BitCaskFile bcFile = fileFactory.openBitCaskFile(f);
			bcFile.doForEachKey(new KeyIterator() {

				@Override
				public void each(String key, int timestamp, long position, int size) throws Exception {
					BitCaskKey bck = new BitCaskKey(Util.tstamp(f), timestamp, position, size);
					keydir.put(key, bck);
				}
			});
			result.addReadFile(bcFile);
		}
	}

}
