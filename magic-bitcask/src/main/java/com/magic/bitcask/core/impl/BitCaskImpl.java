package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.entity.BitCaskOptions;
import com.magic.bitcask.enums.Type;
import com.magic.bitcask.util.Util;

public class BitCaskImpl implements BitCask {

	private static final String TOMBSTONE = "bitcask_tombstone";

	private File dirname;

	private BitCaskFile writeFile;

	private BitCaskLock writeLock;

	private Map<File, BitCaskFile> readFiles = new HashMap<File, BitCaskFile>();

	private long maxFileSize;

	private BitCaskKeydir keyManager;

	private BitCaskFileFactory bitCaskFileFactory;

	private BitCaskOptions options;

	@Override
	public void set(String key, String value) throws IOException {
		if (writeFile == null) {
			throw new IOException("read only");
		}

		switch (writeFile.checkWrite(key, value, maxFileSize)) {
		case WRAP: {
			writeFile.closeForWriting();
			BitCaskFile last_write_file = writeFile;
			BitCaskFile nwf = bitCaskFileFactory.createBitCaskFile(dirname);
			writeLock.write_activefile(nwf);

			writeFile = nwf;
			readFiles.put(last_write_file.getHintFile(), last_write_file);
			break;
		}

		case FRESH:
		// time to start our first write file
		{
			BitCaskLock wl = BitCaskLock.acquire(Type.WRITE, dirname);
			BitCaskFile nwf = bitCaskFileFactory.createBitCaskFile(dirname);
			wl.write_activefile(nwf);

			this.writeLock = wl;
			this.writeFile = nwf;
			readFiles.put(writeFile.getHintFile(), writeFile);
			break;
		}

		case OK:
			// we're good to go
		}

		BitCaskKey entry = writeFile.write(key, value);
		keyManager.put(key, entry);
	}

	@Override
	public String get(String key) throws IOException {
		return get(key, 2);
	}

	private String get(String key, int try_num) throws IOException {
		BitCaskKey entry = keyManager.get(key);
		if (entry == null) {
			return null;
		}

		if (entry.getTimestamp() < options.expiry_time()) {
			return null;
		}

		BitCaskFile file_state = get_filestate(entry.getFileId());
		/** merging deleted file between keydir.get and here */
		if (file_state == null) {
			Thread.yield();
			return get(key, try_num - 1);
		}

		String[] kv = file_state.read(entry.getPosition(), entry.getSize());

		if (kv[1].equals(TOMBSTONE)) {
			return null;
		} else {
			return kv[1];
		}
	}

	public void addReadFile(BitCaskFile readFile) {
		readFiles.put(readFile.getHintFile(), readFile);
	}

	private BitCaskFile get_filestate(int fileId) throws IOException {
		File fname = Util.mkFilename(dirname, fileId);
		BitCaskFile f = readFiles.get(fname);
		if (f != null) {
			return f;
		}

		f = bitCaskFileFactory.openBitCaskFile(fname);
		readFiles.put(fname, f);

		return f;
	}

	public File[] readableFiles() {

		final File writing_file = BitCaskLock.readActivefile(Type.WRITE, dirname);
		final File merging_file = BitCaskLock.readActivefile(Type.MERGE, dirname);

		return listDataFiles(writing_file, merging_file);
	}

	private static Pattern DATA_FILE = Pattern.compile("[0-9]+.bitcask.data");

	private File[] listDataFiles(final File writing_file, final File merging_file) {
		File[] files = dirname.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f == writing_file || f == merging_file)
					return false;

				return DATA_FILE.matcher(f.getName()).matches();
			}
		});

		Arrays.sort(files, 0, files.length, REVERSE_DATA_FILE_COMPARATOR);

		return files;
	}

	private static final Comparator<? super File> REVERSE_DATA_FILE_COMPARATOR = new Comparator<File>() {

		@Override
		public int compare(File file0, File file1) {
			int i0 = BitCaskFileImpl.tstamp(file0);
			int i1 = BitCaskFileImpl.tstamp(file1);

			if (i0 < i1)
				return 1;
			if (i0 == i1)
				return 0;

			return -1;
		}
	};

	public File getDirname() {
		return dirname;
	}

	public void setDirname(File dirname) {
		this.dirname = dirname;
	}

	public BitCaskFile getWriteFile() {
		return writeFile;
	}

	public void setWriteFile(BitCaskFile writeFile) {
		this.writeFile = writeFile;
	}

	public BitCaskLock getWriteLock() {
		return writeLock;
	}

	public void setWriteLock(BitCaskLock writeLock) {
		this.writeLock = writeLock;
	}

	public Map<File, BitCaskFile> getReadFiles() {
		return readFiles;
	}

	public void setReadFiles(Map<File, BitCaskFile> readFiles) {
		this.readFiles = readFiles;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public BitCaskKeydir getKeyManager() {
		return keyManager;
	}

	public void setKeyManager(BitCaskKeydir keyManager) {
		this.keyManager = keyManager;
	}

	public BitCaskFileFactory getBitCaskFileFactory() {
		return bitCaskFileFactory;
	}

	public void setBitCaskFileFactory(BitCaskFileFactory bitCaskFileFactory) {
		this.bitCaskFileFactory = bitCaskFileFactory;
	}

	public BitCaskOptions getOptions() {
		return options;
	}

	public void setOptions(BitCaskOptions options) {
		this.options = options;
	}

}
