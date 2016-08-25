package com.magic.bitcask;

import java.io.File;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.impl.BitCaskFactoryImpl;
import com.magic.bitcask.entity.BitCaskOptions;

public class TestBitCask {
	
	public static void main(String[] args) throws Exception {
		BitCaskFactory bitCaskFactory = new BitCaskFactoryImpl();
		BitCaskOptions opts = new BitCaskOptions();
		BitCask bitcask = bitCaskFactory.createBitCask(new File("/Users/yinwenhao/workspace/bitcask/"), opts);
//		bitcask.put("aaa", "12345");
		System.out.println(bitcask.get("aaa"));
		bitcask.set("aaa", "123456");
		System.out.println(bitcask.get("aaa"));
	}

}
