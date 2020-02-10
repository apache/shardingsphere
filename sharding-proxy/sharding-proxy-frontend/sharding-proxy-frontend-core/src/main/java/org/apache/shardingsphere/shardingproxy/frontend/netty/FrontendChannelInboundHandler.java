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

package org.apache.shardingsphere.shardingproxy.frontend.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.shardingproxy.frontend.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.shardingproxy.frontend.executor.CommandExecutorSelector;
import org.apache.shardingsphere.shardingproxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.shardingproxy.transport.payload.PacketPayload;
import org.apache.shardingsphere.transaction.core.TransactionType;

/**
 * Frontend channel inbound handler.
 * 
 * @author zhangliang
 * @author liya
 */
@RequiredArgsConstructor
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private volatile boolean authorized;
    
    private final BackendConnection backendConnection = new BackendConnection(
            TransactionType.valueOf(ShardingProxyContext.getInstance().getProperties().<String>getValue(PropertiesConstant.PROXY_TRANSACTION_TYPE)),
            ShardingProxyContext.getInstance().getProperties().<Boolean>getValue(PropertiesConstant.PROXY_HINT_ENABLED));
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        ChannelThreadExecutorGroup.getInstance().register(context.channel().id());
        databaseProtocolFrontendEngine.getAuthEngine().handshake(context, backendConnection);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authorized) {
            authorized = auth(context, (ByteBuf) message);
            return;
        }
        CommandExecutorSelector.getExecutor(databaseProtocolFrontendEngine.getFrontendContext().isOccupyThreadForPerConnection(), backendConnection.isSupportHint(),
                backendConnection.getTransactionType(), context.channel().id()).execute(new CommandExecutorTask(databaseProtocolFrontendEngine, backendConnection, context, message));
    }
    
    private boolean auth(final ChannelHandlerContext context, final ByteBuf message) {
        try (PacketPayload payload = databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload(message)) {
            return databaseProtocolFrontendEngine.getAuthEngine().auth(context, payload, backendConnection);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Exception occur: ", ex);
            context.write(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(ex));
        }
        return false;
    }
    
    @Override
    @SneakyThrows
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        databaseProtocolFrontendEngine.release(backendConnection);
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
