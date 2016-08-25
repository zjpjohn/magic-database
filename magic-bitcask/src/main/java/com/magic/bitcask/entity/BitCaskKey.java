package com.magic.bitcask.entity;

public class BitCaskKey {

	private int fileId;

	private int timestamp;

	private long position;

	private int size;

	public BitCaskKey(int fileId, int timestamp, long position, int size) {
		this.fileId = fileId;
		this.timestamp = timestamp;
		this.position = position;
		this.size = size;
	}

	public boolean is_newer_than(BitCaskKey old) {
		return old.getTimestamp() < timestamp || (old.getTimestamp() == timestamp
				&& (old.getFileId() < fileId || (old.getFileId() == fileId && old.getPosition() < position)));
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
