package com.magic.bitcask.core;

import java.io.IOException;

public interface BitCask {

	public void set(String key, String value) throws IOException;

	public String get(String key) throws IOException;

}
