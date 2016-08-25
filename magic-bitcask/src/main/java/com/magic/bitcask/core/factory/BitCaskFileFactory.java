package com.magic.bitcask.core.factory;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.core.BitCaskFile;

public interface BitCaskFileFactory {

	public BitCaskFile openBitCaskFile(File dirname) throws IOException;

	public BitCaskFile createBitCaskFile(File dirname) throws IOException;

	public BitCaskFile createBitCaskFile(File dirname, int tstamp) throws IOException;

}
