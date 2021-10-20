package org.apache.shardingsphere.proxy.frontend.netty;

import org.apache.shardingsphere.proxy.frontend.connection.ConnectionLimitContext;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrontendChannelLimitInboundHandler extends ChannelDuplexHandler {
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (ConnectionLimitContext.INSTANCE.connect()) {
            ctx.fireChannelActive();
        } else {
            log.info("Close channel {}, The server connections greater than {}", ctx.channel().remoteAddress(), ConnectionLimitContext.INSTANCE.getConnectionLimit());
            ctx.close();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        ConnectionLimitContext.INSTANCE.disconnect();
    }
}
