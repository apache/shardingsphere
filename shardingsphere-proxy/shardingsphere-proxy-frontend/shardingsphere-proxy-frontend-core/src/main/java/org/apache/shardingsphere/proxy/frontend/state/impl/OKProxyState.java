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
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.UserExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.concurrent.ExecutorService;

/**
 * OK proxy state.
 */
public final class OKProxyState implements ProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        CommandExecutorTask commandExecutorTask = new CommandExecutorTask(databaseProtocolFrontendEngine, backendConnection, context, message);
        ExecutorService executorService;
        if (requireOccupyThreadForConnection(backendConnection)) {
            executorService = ConnectionThreadExecutorGroup.getInstance().get(backendConnection.getConnectionId());
        } else if (isPreferNettyEventLoop()) {
            executorService = context.executor();
        } else if (databaseProtocolFrontendEngine.getFrontendContext().isRequiredSameThreadForConnection()) {
            executorService = ConnectionThreadExecutorGroup.getInstance().get(backendConnection.getConnectionId());
        } else {
            executorService = UserExecutorGroup.getInstance().getExecutorService();
        }
        executorService.execute(commandExecutorTask);
    }
    
    private boolean requireOccupyThreadForConnection(final BackendConnection backendConnection) {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)
                || TransactionType.isDistributedTransaction(backendConnection.getTransactionStatus().getTransactionType());
    }
    
    private boolean isPreferNettyEventLoop() {
        switch (ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)) {
            case "OLTP":
                return true;
            case "OLAP":
                return false;
            default:
                throw new IllegalArgumentException("The property proxy-backend-executor-suitable must be 'OLAP' or 'OLTP'");
        }
    }
}
