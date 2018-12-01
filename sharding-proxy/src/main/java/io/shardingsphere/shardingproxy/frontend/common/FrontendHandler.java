/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.frontend.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.common.executor.ChannelThreadExecutorGroup;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Frontend handler.
 * 
 * @author zhangliang 
 */
public abstract class FrontendHandler extends ChannelInboundHandlerAdapter {
    
    private volatile boolean authorized;
    
    @Getter
    private volatile BackendConnection backendConnection = new BackendConnection(GlobalRegistry.getInstance().getTransactionType());

    @Override
    public final void channelActive(final ChannelHandlerContext context) {
        ChannelThreadExecutorGroup.getInstance().register(context.channel().id());
        handshake(context);
    }
    
    protected abstract void handshake(ChannelHandlerContext context);
    
    @Override
    public final void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authorized) {
            auth(context, (ByteBuf) message);
            authorized = true;
        } else {
            executeCommand(context, (ByteBuf) message);
        }
    }
    
    protected abstract void auth(ChannelHandlerContext context, ByteBuf message);
    
    protected abstract void executeCommand(ChannelHandlerContext context, ByteBuf message);
    
    @Override
    @SneakyThrows
    public final void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        backendConnection.close(true);
        ChannelThreadExecutorGroup.getInstance().unregister(context.channel().id());
    }
}
