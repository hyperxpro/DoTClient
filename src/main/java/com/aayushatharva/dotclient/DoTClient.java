package com.aayushatharva.dotclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.xbill.DNS.*;

public class DoTClient {

    public static void main(String[] args) throws Exception {

        SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channelFactory(() -> {
                    if (Epoll.isAvailable()) {
                        return new EpollSocketChannel();
                    } else {
                        return new NioSocketChannel();
                    }
                })
                .handler(new DoTClientInitializer(sslCtx));

        Channel channel = bootstrap.connect("8.8.8.8", 853).sync().channel();

        Message message = Message.newQuery(Record.newRecord(Name.fromString("www.google.com."), Type.A, DClass.IN));

        byte[] data = new byte[2];
        data[1] = (byte) (message.toWire().length & 0xFF);
        data[0] = (byte) ((message.toWire().length >> 8) & 0xFF);
        channel.write(Unpooled.wrappedBuffer(data));

        ChannelFuture lastWriteFuture = channel.writeAndFlush(Unpooled.wrappedBuffer(message.toWire()));

        System.out.println("--------------------------REQUEST--------------------------");
        System.out.println(message);
        System.out.println("-----------------------------------------------------------");

        if (lastWriteFuture != null) {
            lastWriteFuture.sync();
        }
    }
}
