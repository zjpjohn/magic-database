package com.magic.bitcask.core.factory;

import java.io.File;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.entity.BitCaskOptions;

public interface BitCaskFactory {

	public BitCask createBitCask(File dirname, BitCaskOptions opts) throws Exception;

}
