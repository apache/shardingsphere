package io.shardingjdbc.proxy.backend.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class CommandResponsePacketsHandler extends ChannelInboundHandlerAdapter {
    protected abstract void auth(ChannelHandlerContext context, ByteBuf message);
    protected abstract void executeCommandResponsePackets(ChannelHandlerContext context, ByteBuf message);
}
