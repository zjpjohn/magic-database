package com.magic.bitcask;

import java.util.zip.CRC32;

public class TestCRC32 {
	
	public static void main(String[] args) {  
        String uri = "D:\\ETFMY100PHFUNDBulletin20130325.txt";
        long start = System.currentTimeMillis();
        for (int i=0; i<1; i++) {
	        CRC32 crc = new CRC32();
	        crc.update(uri.getBytes());
	        crc.getValue();
	        System.out.println((int)crc.getValue());
        }
        System.out.println(System.currentTimeMillis()-start);
//        System.out.println(crc.getValue());
        start = System.currentTimeMillis();
        for (int i=0; i<1; i++) {
	        com.magic.bitcask.util.CRC32 crc2 = new com.magic.bitcask.util.CRC32();
	        crc2.update(uri.getBytes());
	        crc2.getValue();
	        System.out.println(crc2.getValue());
        }
        System.out.println(System.currentTimeMillis()-start);
//        System.out.println(crc2.getValue());
        start = System.currentTimeMillis();
        for (int i=0; i<1000000; i++) {
	        CRC32 crc = new CRC32();
	        crc.update(uri.getBytes());
	        crc.getValue();
        }
        System.out.println(System.currentTimeMillis()-start);
    }

}
