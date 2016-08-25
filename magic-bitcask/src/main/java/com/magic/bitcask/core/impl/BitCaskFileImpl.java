package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.KeyIterator;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.exception.IterException;
import com.magic.bitcask.util.CRC32;
import com.magic.bitcask.util.IO;
import com.magic.bitcask.util.Util;

public class BitCaskFileImpl implements BitCaskFile {

	public enum WriteCheck {
		WRAP, FRESH, OK
	}

	private static final String CHARSET = "UTF-8";
	
	// 4+4+2+4
	private static final int HEADER_SIZE = 14;

	// 4+4+2+8+4
//	private static final int HINT_HEADER_SIZE = 22;

	private FileChannel wch;
	private FileChannel rch;

	private FileChannel wch_hint;

	private AtomicLong write_offset;
	private final File hintFile;
	private final int file_id;

	public BitCaskFileImpl(int file_id, File filename, FileChannel rch) throws IOException {
		this.file_id = file_id;
		this.hintFile = filename;
		this.rch = rch;
	}

	public BitCaskFileImpl(int file_id, File filename, FileChannel wch, FileChannel wch_hint, FileChannel rch)
			throws IOException {
		this.file_id = file_id;
		this.hintFile = filename;
		this.wch = wch;
		this.rch = rch;
		this.wch_hint = wch_hint;
		this.write_offset = new AtomicLong(rch.size());
	}

	public BitCaskFileImpl() {
		this.hintFile = null;
		this.file_id = -1;
	}

	public File getHintFile() {
		return hintFile;
	}

	public String[] read(long offset, int length) throws IOException {

		byte[] header = new byte[HEADER_SIZE];

		ByteBuffer h = ByteBuffer.wrap(header);
		long read = IO.read(rch, h, offset);
		if (read != HEADER_SIZE) {
			throw new IOException("cannot read header @ 0x" + Long.toHexString(offset));
		}

		int crc32 = h.getInt(0);
		/* int tstamp = h.getInt(); */
		int key_len = h.getChar(8);
		int val_len = h.getInt(10);

		int key_val_size = key_len + val_len;

		if (length != (HEADER_SIZE + key_val_size)) {
			throw new IOException("bad entry size");
		}

		byte[] kv = new byte[key_val_size];
		ByteBuffer key_val = ByteBuffer.wrap(kv);

		long kv_pos = offset + HEADER_SIZE;
		read = IO.read(rch, key_val, kv_pos);
		if (read != key_val_size) {
			throw new IOException("cannot read key+value @ 0x" + Long.toHexString(offset));
		}

		CRC32 crc = new CRC32();
		crc.update(header, 4, HEADER_SIZE - 4);
		crc.update(kv);

		if (crc.getValue() != crc32) {
			throw new IOException("Mismatching CRC code");
		}

		String[] result = new String[] { new String(kv, 0, key_len), new String(kv, key_len, val_len) };

		return result;
	}

	public BitCaskKey write(String key, String value) throws IOException {

		int tstamp = Util.tstamp();
		int key_size = key.getBytes().length;
		int value_size = value.getBytes().length;

		ByteBuffer[] vec = file_entry(key, value, tstamp, key_size, value_size);

		int entry_size = HEADER_SIZE + key_size + value_size;
		long entry_pos = write_offset.getAndAdd(entry_size);
		IO.writeFully(wch, vec);

		// ByteBuffer[] hfe = hint_file_entry(tstamp, entry_pos, entry_size,
		// key);
		// IO.writeFully(wch_hint, hfe);

		return new BitCaskKey(file_id, tstamp, entry_pos, entry_size);
	}

	private ByteBuffer[] file_entry(String key, String value, int tstamp, int key_size, int value_size) throws UnsupportedEncodingException {
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer h = ByteBuffer.wrap(header);

		byte[] keyBytes = key.getBytes(CHARSET);
		ByteBuffer k = ByteBuffer.wrap(keyBytes).asReadOnlyBuffer();
		byte[] valueBytes = value.getBytes(CHARSET);
		ByteBuffer v = ByteBuffer.wrap(valueBytes).asReadOnlyBuffer();

		h.putInt(4, tstamp);
		h.putShort(8, (short) key_size);
		h.putInt(10, value_size);

		CRC32 crc = new CRC32();
		crc.update(header, 4, HEADER_SIZE - 4);
		crc.update(key);
		crc.update(value);
		int crc_value = crc.getValue();

		h.putInt(0, crc_value);

		ByteBuffer[] vec = new ByteBuffer[] { h, k, v };
		return vec;
	}

	// private ByteBuffer[] hint_file_entry(int tstamp, long entry_offset, int
	// entry_size, String key) throws UnsupportedEncodingException {
	// byte[] header = new byte[HINT_HEADER_SIZE];
	// ByteBuffer h = ByteBuffer.wrap(header);
	// h.putInt(4, tstamp);
	// byte[] keyBytes = key.getBytes(CHARSET);
	// h.putShort(8, (short) keyBytes.length);
	// h.putLong(10, entry_offset);
	// h.putInt(18, entry_size);
	//
	// CRC32 crc = new CRC32();
	// crc.update(header, 4, HINT_HEADER_SIZE - 4);
	// crc.update(key);
	// int crc_value = crc.getValue();
	//
	// h.putInt(0, crc_value);
	//
	// return new ByteBuffer[] { h, ByteBuffer.wrap(keyBytes).asReadOnlyBuffer()
	// };
	// }

	/**
	 * open existing bitcask file in given directory
	 * 
	 * @throws IOException
	 */
	// public static BitCaskFileImpl open(File dirname, int tstamp) throws
	// IOException {
	//
	// File filename = mk_filename(dirname, tstamp);
	// return open(filename);
	// }

	// public static BitCaskFileImpl open(File filename) throws IOException {
	//
	// int tstamp = BitCaskFileImpl.tstamp(filename);
	//
	// FileChannel rch = new RandomAccessFile(filename, "r").getChannel();
	//
	// return new BitCaskFileImpl(tstamp, filename, null, null, rch);
	// }

	/** return true if this bitcask file has a hint file */
	public boolean hasHintfile() {
		return hint_filename(hintFile).canRead();
	}

	/** Close for writing */
	public synchronized void closeForWriting() throws IOException {
		if (wch != null) {
			wch.close();
			wch = null;
		}
		if (wch_hint != null) {
			wch_hint.close();
			wch = null;
		}
	}

	/** Close for reading and writing */
	public synchronized void close() throws IOException {
		closeForWriting();
		if (rch != null) {
			rch.close();
		}
	}

	/**
	 * Given a directory and a timestamp, construct a data file name.
	 */
	// public static File mk_filename(File dirname, int tstamp) {
	// return new File(dirname, "" + tstamp + ".bitcask.data");
	// }

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

	public WriteCheck checkWrite(String key, String value, long maxFileSize) {
		if (file_id == -1)
			return WriteCheck.FRESH;

		int size = HEADER_SIZE + key.getBytes().length + value.getBytes().length;

		if (write_offset.get() + size > maxFileSize) {
			return WriteCheck.WRAP;
		} else {
			return WriteCheck.OK;
		}
	}

	public static int tstamp(File file) {
		String name = file.getName();
		int idx = name.indexOf('.');
		int val = Integer.parseInt(name.substring(0, idx));
		return val;
	}

	@Override
	public void doForEachKey(KeyIterator iter) throws Exception {
		byte[] header = new byte[HEADER_SIZE];
		long pos = 0;
		while (true) {
			ByteBuffer h = ByteBuffer.wrap(header);
			long read = IO.read(rch, h, pos);
			if (read == 0) {
				return;
			}
			if (read != HEADER_SIZE) {
				throw new IterException();
			}

			h.rewind();
			h.getInt(); // skip crc32
			int tstamp = h.getInt();
			int key_len = h.getChar();
			int val_len = h.getInt();

			byte[] k = new byte[key_len];
			ByteBuffer key = ByteBuffer.wrap(k);

			read = IO.read(rch, key, pos + HEADER_SIZE);
			if (read != key_len) {
				throw new IterException();
			}

			int entry_size = HEADER_SIZE + key_len + val_len;
			
			key.rewind();
			String keyString = Charset.forName(CHARSET).decode(key.asReadOnlyBuffer()).toString();
			iter.each(keyString, tstamp, pos, entry_size);
			pos += entry_size;
		}
	}

}
