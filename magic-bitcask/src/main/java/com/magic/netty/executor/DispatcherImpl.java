package com.magic.netty.executor;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.exception.WrongMethodException;
import com.magic.netty.request.Request;
import com.magic.netty.request.Response;

public class DispatcherImpl implements Dispatcher {

	private BitCask bitcask;

	@Override
	public Response getResult(Request request) throws Exception {
		String method = request.getMethod().toLowerCase();
		Response response = new Response();
		response.setKey(request.getKey());
		switch (method) {
		case "get":
			response.setValue(bitcask.get(request.getKey()));
			break;
		case "set":
			bitcask.set(request.getKey(), request.getValue());
			response.setValue(request.getValue());
			break;
		default:
			throw new WrongMethodException();
		}
		return response;
	}

	public BitCask getBitcask() {
		return bitcask;
	}

	public void setBitcask(BitCask bitcask) {
		this.bitcask = bitcask;
	}

}
