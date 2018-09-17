/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.wrapper.PreparedStatementExecutorWrapper;
import io.shardingsphere.proxy.backend.jdbc.wrapper.StatementExecutorWrapper;
import io.shardingsphere.proxy.backend.netty.NettyBackendHandler;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Backend handler factory.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendHandlerFactory {
    
    private static final ProxyContext PROXY_CONTEXT = ProxyContext.getInstance();
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @param frontendHandler frontend handler
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newTextProtocolInstance(
            final int connectionId, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType, final FrontendHandler frontendHandler) {
        RuleRegistry ruleRegistry = PROXY_CONTEXT.getRuleRegistry(frontendHandler.getCurrentSchema());
        return PROXY_CONTEXT.isUseNIO()
                ? new NettyBackendHandler(frontendHandler, ruleRegistry, connectionId, sequenceId, sql, databaseType)
                : new JDBCBackendHandler(
                        frontendHandler, ruleRegistry, sql, new JDBCExecuteEngine(backendConnection, new StatementExecutorWrapper(ruleRegistry)));
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @param databaseType database type
     * @param frontendHandler frontend handler
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newBinaryProtocolInstance(
            final int connectionId, final int sequenceId, final String sql, final List<Object> parameters, final BackendConnection backendConnection,
            final DatabaseType databaseType, final FrontendHandler frontendHandler) {
        RuleRegistry ruleRegistry = PROXY_CONTEXT.getRuleRegistry(frontendHandler.getCurrentSchema());
        return PROXY_CONTEXT.isUseNIO() ? new NettyBackendHandler(frontendHandler, ruleRegistry, connectionId, sequenceId, sql, databaseType)
                : new JDBCBackendHandler(frontendHandler, ruleRegistry, sql, new JDBCExecuteEngine(backendConnection, new PreparedStatementExecutorWrapper(ruleRegistry, parameters)));
    }
}

