package com.magic.bitcask;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.impl.BitCaskFactoryImpl;
import com.magic.bitcask.entity.BitCaskOptions;
import com.magic.netty.NettyServer;
import com.magic.netty.executor.DispatcherImpl;
import com.magic.netty.executor.RequestTask;

public class Main {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		log.info("magic database starting...");

		BitCaskFactory bitCaskFactory = new BitCaskFactoryImpl();
		BitCaskOptions opts = new BitCaskOptions();
		BitCask bitcask = bitCaskFactory.createBitCask(new File("/Users/yinwenhao/workspace/bitcask/"), opts);
		DispatcherImpl dispatcher = new DispatcherImpl();
		dispatcher.setBitcask(bitcask);
		RequestTask.setDispatcher(dispatcher);

		NettyServer server = new NettyServer();
		server.startServer(bitcask);
		log.info("magic database started");
	}
}
