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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.control.panel.spi.engine.SingletonFacadeEngine;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.engine.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.CommandExecutorSelector;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

/**
 * Frontend channel inbound handler.
 */
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private final BackendConnection backendConnection;
    
    private volatile boolean authorized;
    
    public FrontendChannelInboundHandler(final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        this.databaseProtocolFrontendEngine = databaseProtocolFrontendEngine;
        TransactionType transactionType = TransactionType.valueOf(ProxySchemaContexts.getInstance().getSchemaContexts().getProps().getValue(ConfigurationPropertyKey.PROXY_TRANSACTION_TYPE));
        boolean supportHint = ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED);
        backendConnection = new BackendConnection(transactionType, supportHint);
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        ChannelThreadExecutorGroup.getInstance().register(context.channel().id());
        backendConnection.setConnectionId(databaseProtocolFrontendEngine.getAuthEngine().handshake(context));
        // TODO ref #7013
        SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.gaugeIncrement(MetricsLabelEnum.CHANNEL_COUNT.getName()));
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authorized) {
            authorized = auth(context, (ByteBuf) message);
            return;
        }
        SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.REQUEST_TOTAL.getName()));
        ExecutorService executorService = CommandExecutorSelector.getExecutor(databaseProtocolFrontendEngine.getFrontendContext().isOccupyThreadForPerConnection(), 
                backendConnection.isSupportHint(), backendConnection.getTransactionType(), context.channel().id());
        executorService.execute(new CommandExecutorTask(databaseProtocolFrontendEngine, backendConnection, context, message));
    }
    
    private boolean auth(final ChannelHandlerContext context, final ByteBuf message) {
        try (PacketPayload payload = databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload(message)) {
            AuthenticationResult authResult = databaseProtocolFrontendEngine.getAuthEngine().auth(context, payload, backendConnection);
            if (authResult.isFinished()) {
                backendConnection.setUserName(authResult.getUsername());
                backendConnection.setCurrentSchema(authResult.getDatabase());
            }
            return authResult.isFinished();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            // TODO ref #7014
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
        SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.gaugeDecrement(MetricsLabelEnum.CHANNEL_COUNT.getName()));
        // TODO #7011
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            backendConnection.getResourceSynchronizer().doNotify();
        }
    }
}
