/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxypg.frontend.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.frontend.common.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.shardingproxypg.runtime.GlobalRegistry;

/**
 * Frontend handler.
 * 
 * @author zhangliang 
 */
public abstract class FrontendHandler extends ChannelInboundHandlerAdapter {
    
    @Setter
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
            authorized = true;
            auth(context, (ByteBuf) message);
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
