package com.magic.bitcask.core;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.core.impl.BitCaskFileImpl.WriteCheck;
import com.magic.bitcask.entity.BitCaskKey;

public interface BitCaskFile {

	/**
	 * 读数据
	 * 
	 * @param offset
	 * @param length
	 * @return [key, value]
	 * @throws IOException
	 */
	public String[] read(long offset, int length) throws IOException;

	public BitCaskKey write(String key, String value) throws IOException;

	public File getHintFile();

	public WriteCheck checkWrite(String key, String value, long maxFileSize);

	public void closeForWriting() throws IOException;

	public void doForEachKey(KeyIterator iter) throws Exception;

}
