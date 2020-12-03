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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendException;
import org.apache.shardingsphere.proxy.backend.exception.CircuitBreakException;
import org.apache.shardingsphere.proxy.backend.exception.LockWaitTimeoutException;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.CommandExecutorSelector;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Lock proxy state.
 */
@Slf4j
public final class LockProxyState implements ProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        block(context, databaseProtocolFrontendEngine);
        if (StateContext.getCurrentState() == StateType.OK) {
            doExecute(context, message, databaseProtocolFrontendEngine, backendConnection);
        } else if (StateContext.getCurrentState() == StateType.CIRCUIT_BREAK) {
            doError(context, databaseProtocolFrontendEngine, new CircuitBreakException());
        }
    }
    
    private void block(final ChannelHandlerContext context, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        if (!LockContext.await()) {
            doError(context, databaseProtocolFrontendEngine, new LockWaitTimeoutException());
            return;
        }
    }
    
    private void doExecute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        boolean supportHint = ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED);
        boolean isOccupyThreadForPerConnection = databaseProtocolFrontendEngine.getFrontendContext().isOccupyThreadForPerConnection();
        ExecutorService executorService = CommandExecutorSelector.getExecutorService(
                isOccupyThreadForPerConnection, supportHint, backendConnection.getTransactionStatus().getTransactionType(), context.channel().id());
        Runnable commandExecutorTask = new CommandExecutorTask(databaseProtocolFrontendEngine, backendConnection, context, message);
        executorService.execute(commandExecutorTask);
    }
    
    private void doError(final ChannelHandlerContext context, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendException backendException) {
        context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(backendException));
        Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket();
        databasePacket.ifPresent(context::writeAndFlush);
    }
}
