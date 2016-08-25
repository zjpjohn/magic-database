package com.magic.netty;

import com.magic.bitcask.core.BitCask;
import com.magic.netty.handler.DecodeHandler;
import com.magic.netty.handler.EncodeHandler;
import com.magic.netty.handler.MyCloseHandler;
import com.magic.netty.handler.TcpServerHandler;
import com.magic.netty.request.Request;
import com.magic.netty.serial.InputFactory;
import com.magic.netty.serial.OutputFactory;
import com.magic.netty.serial.impl.JsonSerialFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServer {

	private InputFactory input = new JsonSerialFactory();

	private OutputFactory output = new JsonSerialFactory();

	private int bossNum = 1;

	private int workerNum = 0;

	public void startServer(BitCask bitcask) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(bossNum);
		EventLoopGroup workerGroup = new NioEventLoopGroup(workerNum);

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast("idleStateHandler", new IdleStateHandler(ServcerTcpConstants.timeForClose, 0, 0));
							p.addLast("closeHandler", new MyCloseHandler());
							p.addLast("lengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(
									ServcerTcpConstants.maxFrameLength, 0, ServcerTcpConstants.lengthFieldLength));
							p.addLast("lengthFieldPrepender",
									new LengthFieldPrepender(ServcerTcpConstants.lengthFieldLength));
							p.addLast("encoder", new EncodeHandler(output));
							p.addLast("decoder", new DecodeHandler(input, Request.class));
							p.addLast("actionHandler", new TcpServerHandler());
						}
					});

			// 使用对象池
			b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);// 关键是这句

			// Bind and start to accept incoming connections.
			b.bind(7865).sync().channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public InputFactory getInput() {
		return input;
	}

	public void setInput(InputFactory input) {
		this.input = input;
	}

	public OutputFactory getOutput() {
		return output;
	}

	public void setOutput(OutputFactory output) {
		this.output = output;
	}

	public int getBossNum() {
		return bossNum;
	}

	public void setBossNum(int bossNum) {
		this.bossNum = bossNum;
	}

	public int getWorkerNum() {
		return workerNum;
	}

	public void setWorkerNum(int workerNum) {
		this.workerNum = workerNum;
	}

}
