package com.magic.bitcask.entity;

import com.magic.bitcask.util.Util;

public class BitCaskOptions {

	public int expiry_secs = 0; // 有效时长（秒）,0表示无限制
	public long max_file_size = 1024 * 1024; /* 1mb file size */
	public boolean read_write = true;
	public int open_timeout_secs = 20;

	public int expiry_time() {
		if (expiry_secs > 0) 
			return Util.tstamp() - expiry_secs;
		else
			return 0;
	}

	public boolean is_read_write() {
		return read_write;
	}
		
	
	
}
