package com.magic.netty.executor.factory;

import com.magic.netty.executor.RequestExecutor;
import com.magic.netty.executor.RequestExecutorImpl;

public class ExecutorFactory {

	public static RequestExecutor createRequestExecutorAndInit() {
		RequestExecutor result = new RequestExecutorImpl();
		result.init();
		return result;
	}

}
