package com.aayushatharva.dotclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.xbill.DNS.Message;

public class DoTClientHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        ByteBuf dataLengthBuf = byteBuf.readBytes(2);
        byte[] dataLengthByte = new byte[dataLengthBuf.readableBytes()];
        dataLengthBuf.readBytes(dataLengthByte);
        int length = ((dataLengthByte[0] & 0xFF) << 8) + (dataLengthByte[1] & 0xFF);
        ByteBuf dnsBuf = byteBuf.readBytes(length);
        byte[] pck = new byte[dnsBuf.readableBytes()];
        dnsBuf.readBytes(pck);


        System.out.println("--------------------------RESPONSE--------------------------");
        System.out.println(new Message(pck));
        System.out.println("------------------------------------------------------------");
    }
}
