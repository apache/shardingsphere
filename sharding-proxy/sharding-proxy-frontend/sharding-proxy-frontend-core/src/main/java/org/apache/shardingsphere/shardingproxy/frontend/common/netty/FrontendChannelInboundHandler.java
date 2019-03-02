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

package org.apache.shardingsphere.shardingproxy.frontend.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.spi.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.frontend.common.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.shardingproxy.frontend.common.executor.CommandExecutorSelector;
import org.apache.shardingsphere.shardingproxy.frontend.spi.DatabaseFrontendEngine;
import org.apache.shardingsphere.spi.hook.RootInvokeHook;

/**
 * Frontend channel inbound handler.
 * 
 * @author zhangliang 
 */
@RequiredArgsConstructor
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseFrontendEngine databaseFrontendEngine;
    
    private volatile boolean authorized;
    
    private final BackendConnection backendConnection = new BackendConnection(GlobalContext.getInstance().getTransactionType());
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        ChannelThreadExecutorGroup.getInstance().register(context.channel().id());
        databaseFrontendEngine.handshake(context, backendConnection);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authorized) {
            authorized = databaseFrontendEngine.auth(context, (ByteBuf) message, backendConnection);
        } else {
            CommandExecutorSelector.getExecutor(databaseFrontendEngine.isOccupyThreadForPerConnection(), backendConnection.getTransactionType(), context.channel().id()).execute(new Runnable() {
                
                @Override
                public void run() {
                    RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
                    rootInvokeHook.start();
                    int connectionSize = 0;
                    try (BackendConnection backendConnection = FrontendChannelInboundHandler.this.backendConnection) {
                        databaseFrontendEngine.executeCommand(context, (ByteBuf) message, backendConnection);
                        connectionSize = backendConnection.getConnectionSize();
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        log.error("Exception occur: ", ex);
                    } finally {
                        rootInvokeHook.finish(connectionSize);
                    }
                }
            });
        }
    }
    
    @Override
    @SneakyThrows
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        databaseFrontendEngine.release(backendConnection);
        backendConnection.close(true);
        ChannelThreadExecutorGroup.getInstance().unregister(context.channel().id());
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            backendConnection.getResourceSynchronizer().doNotify();
        }
    }
}
