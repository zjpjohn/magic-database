package com.magic.netty;

public class MyTcpConstants {

	/** 检测时间（超过这个时间没有读到数据，则断开连接，秒） */
	public static final int timeForClose = 120;

	/** tcp包内容的长度（字节） */
	public static final int lengthFieldLength = 2;
	public static final int maxFrameLength = Short.MAX_VALUE;

}
