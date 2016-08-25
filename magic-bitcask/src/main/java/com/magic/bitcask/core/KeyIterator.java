package com.magic.bitcask.core;

public interface KeyIterator {

	public void each(String key, int timestamp, long position, int size) throws Exception;

}
