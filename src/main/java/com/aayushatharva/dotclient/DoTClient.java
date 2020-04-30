package com.aayushatharva.dotclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.xbill.DNS.*;

import java.util.Arrays;

public class DoTClient {

    public static void main(String[] args) throws Exception {

        OpenSsl.ensureAvailability();

        SslContext sslCtx = SslContextBuilder.forClient()
                .protocols("TLSv1.3", "TLSv1.2")
                .ciphers(Arrays.asList(
                        "TLS_AES_256_GCM_SHA384",
                        "TLS_AES_128_GCM_SHA256",
                        "TLS_CHACHA20_POLY1305_SHA256",
                        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"))
                .sslProvider(SslProvider.OPENSSL)
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

        System.out.println("--------------------------REQUEST--------------------------");
        System.out.println(message);
        System.out.println("-----------------------------------------------------------");

        channel.writeAndFlush(Unpooled.wrappedBuffer(message.toWire())).sync();
    }
}
