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
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateContext;
import org.apache.shardingsphere.readwritesplitting.route.impl.PrimaryVisitedManager;
import org.apache.shardingsphere.transaction.core.TransactionType;

/**
 * Frontend channel inbound handler.
 */
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private final BackendConnection backendConnection;
    
    private volatile boolean authenticated;
    
    public FrontendChannelInboundHandler(final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        this.databaseProtocolFrontendEngine = databaseProtocolFrontendEngine;
        TransactionType transactionType = TransactionType.valueOf(ProxyContext.getInstance().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.PROXY_TRANSACTION_TYPE));
        backendConnection = new BackendConnection(transactionType);
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        int connectionId = databaseProtocolFrontendEngine.getAuthenticationEngine().handshake(context);
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        backendConnection.setConnectionId(connectionId);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authenticated) {
            authenticated = authenticate(context, (ByteBuf) message);
            return;
        }
        ProxyStateContext.execute(context, message, databaseProtocolFrontendEngine, backendConnection);
    }
    
    private boolean authenticate(final ChannelHandlerContext context, final ByteBuf message) {
        try (PacketPayload payload = databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload(message)) {
            AuthenticationResult authResult = databaseProtocolFrontendEngine.getAuthenticationEngine().authenticate(context, payload);
            if (authResult.isFinished()) {
                backendConnection.setGrantee(new Grantee(authResult.getUsername(), authResult.getHostname()));
                backendConnection.setCurrentSchema(authResult.getDatabase());
            }
            return authResult.isFinished();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Exception occur: ", ex);
            context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(ex, backendConnection));
            context.close();
        }
        return false;
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        closeAllResources();
    }
    
    private void closeAllResources() {
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(backendConnection.getConnectionId());
        PrimaryVisitedManager.clear();
        backendConnection.closeResultSets();
        backendConnection.closeStatements();
        backendConnection.closeConnections(true);
        backendConnection.closeFederateExecutor();
        databaseProtocolFrontendEngine.release(backendConnection);
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            backendConnection.getResourceLock().doNotify();
        }
    }
}
