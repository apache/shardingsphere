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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.exception.ExpectedExceptions;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.UserExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Frontend channel inbound handler.
 */
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private final ConnectionSession connectionSession;
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    private final AtomicBoolean authenticated = new AtomicBoolean(false);
    
    public FrontendChannelInboundHandler(final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final Channel channel) {
        this.databaseProtocolFrontendEngine = databaseProtocolFrontendEngine;
        connectionSession = new ConnectionSession(databaseProtocolFrontendEngine.getType(), channel);
        databaseProtocolFrontendEngine.init(connectionSession);
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        int connectionId = databaseProtocolFrontendEngine.getAuthenticationEngine().handshake(context);
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        connectionSession.setConnectionId(connectionId);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authenticated.get()) {
            authenticated.set(authenticate(context, (ByteBuf) message));
            return;
        }
        ProxyStateContext.execute(context, message, databaseProtocolFrontendEngine, connectionSession);
    }
    
    private boolean authenticate(final ChannelHandlerContext context, final ByteBuf message) {
        try {
            AuthenticationResult authResult = databaseProtocolFrontendEngine.getAuthenticationEngine().authenticate(context,
                    databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload(message, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()));
            if (authResult.isFinished()) {
                connectionSession.setGrantee(new Grantee(authResult.getUsername(), authResult.getHostname()));
                connectionSession.setCurrentDatabaseName(authResult.getDatabase());
                connectionSession.setProcessId(processEngine.connect(connectionSession.getUsedDatabaseName(), connectionSession.getConnectionContext().getGrantee()));
            }
            return authResult.isFinished();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            if (ExpectedExceptions.isExpected(ex.getClass())) {
                log.debug("Exception occur: ", ex);
            } else {
                log.error("Exception occur: ", ex);
            }
            context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(ex));
            context.close();
        } finally {
            message.release();
        }
        return false;
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        UserExecutorGroup.getInstance().getExecutorService().execute(this::closeAllResources);
    }
    
    private void closeAllResources() {
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionSession.getConnectionId());
        processCloseExceptions(connectionSession.getDatabaseConnectionManager().closeAllResources());
        Optional.ofNullable(connectionSession.getProcessId()).ifPresent(processEngine::disconnect);
        databaseProtocolFrontendEngine.release(connectionSession);
    }
    
    private void processCloseExceptions(final Collection<SQLException> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("");
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        processException(ex);
    }
    
    private void processException(final Exception cause) {
        if (ExpectedExceptions.isExpected(cause.getClass())) {
            log.debug("Exception occur: ", cause);
        } else {
            log.error("Exception occur: ", cause);
        }
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            connectionSession.getDatabaseConnectionManager().getConnectionResourceLock().doNotify();
        }
    }
}
