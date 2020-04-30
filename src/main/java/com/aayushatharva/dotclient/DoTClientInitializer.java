package com.aayushatharva.dotclient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class DoTClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public DoTClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslCtx.newHandler(ch.alloc(), "8.8.8.8", 853));

        // and then business logic.
        pipeline.addLast(new DoTClientHandler());
    }
}