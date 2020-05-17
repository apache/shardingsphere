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

package org.apache.shardingsphere.proxy.frontend.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.facade.MetricsTrackerFacade;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.context.ShardingSphereProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.CommandExecutorSelector;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;

import java.sql.SQLException;

/**
 * Frontend channel inbound handler.
 */
@RequiredArgsConstructor
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private volatile boolean authorized;
    
    private final BackendConnection backendConnection = new BackendConnection(
            TransactionType.valueOf(ShardingSphereProxyContext.getInstance().getProperties().getValue(ConfigurationPropertyKey.PROXY_TRANSACTION_TYPE)),
            ShardingSphereProxyContext.getInstance().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED));
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        ChannelThreadExecutorGroup.getInstance().register(context.channel().id());
        databaseProtocolFrontendEngine.getAuthEngine().handshake(context, backendConnection);
        MetricsTrackerFacade.getInstance().gaugeInc(MetricsLabelEnum.CHANNEL_COUNT.getName());
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authorized) {
            authorized = auth(context, (ByteBuf) message);
            return;
        }
        MetricsTrackerFacade.getInstance().counterInc(MetricsLabelEnum.REQUEST_TOTAL.getName());
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
    public void channelInactive(final ChannelHandlerContext context) throws SQLException {
        context.fireChannelInactive();
        databaseProtocolFrontendEngine.release(backendConnection);
        backendConnection.close(true);
        ChannelThreadExecutorGroup.getInstance().unregister(context.channel().id());
        MetricsTrackerFacade.getInstance().gaugeDec(MetricsLabelEnum.CHANNEL_COUNT.getName());
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            backendConnection.getResourceSynchronizer().doNotify();
        }
    }
}
