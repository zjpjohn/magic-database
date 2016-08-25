package com.magic.bitcask.entity;

public class BitCaskRecord {

	private int crc;

	private int timestamp;

	private short keySize;

	private int valueSize;

	private String key;

	private String value;

	public int getCrc() {
		return crc;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public short getKeySize() {
		return keySize;
	}

	public void setKeySize(short keySize) {
		this.keySize = keySize;
	}

	public int getValueSize() {
		return valueSize;
	}

	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
