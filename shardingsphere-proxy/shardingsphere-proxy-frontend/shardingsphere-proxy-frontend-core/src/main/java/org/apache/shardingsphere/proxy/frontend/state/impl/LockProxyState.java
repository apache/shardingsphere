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
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendException;
import org.apache.shardingsphere.proxy.backend.exception.LockWaitTimeoutException;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateContext;

import java.util.Optional;

/**
 * Lock proxy state.
 */
public final class LockProxyState implements ProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        block(context, databaseProtocolFrontendEngine);
        ProxyStateContext.execute(context, message, databaseProtocolFrontendEngine, backendConnection);
    }
    
    private void block(final ChannelHandlerContext context, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        Long lockTimeoutMilliseconds = ProxyContext.getInstance().getMetaDataContexts().getProps().<Long>getValue(ConfigurationPropertyKey.LOCK_WAIT_TIMEOUT_MILLISECONDS);
        if (!ProxyContext.getInstance().getLock().await(lockTimeoutMilliseconds)) {
            doError(context, databaseProtocolFrontendEngine, new LockWaitTimeoutException(lockTimeoutMilliseconds));
        }
    }
    
    private void doError(final ChannelHandlerContext context, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendException backendException) {
        context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(backendException));
        Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket();
        databasePacket.ifPresent(context::writeAndFlush);
    }
}
