package com.magic.netty.executor;

public interface RequestExecutor {

	public void execute(Runnable task);

	public void destroy() throws Exception;

	public void init();
}
