package com.magic.netty.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.netty.request.Request;
import com.magic.netty.request.Response;

import io.netty.channel.ChannelHandlerContext;

public class RequestTask implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static Dispatcher dispatcher;

	private ChannelHandlerContext ctx;

	private Request request;

	public RequestTask(Request request, ChannelHandlerContext ctx) {
		this.request = request;
		this.ctx = ctx;
	}

	@Override
	public void run() {
		Response response = null;
		try {
			response = dispatcher.getResult(request);
			ctx.writeAndFlush(response);
		} catch (Exception e) {
			log.error("request task error", e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("method:{}, key:{}, value:{}. response value:{}", request.getMethod(), request.getKey(),
						request.getValue(), response == null ? "#response is null!#" : response.getValue());
			}
		}
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public static void setDispatcher(Dispatcher dispatcher) {
		RequestTask.dispatcher = dispatcher;
	}

}
