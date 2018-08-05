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
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngineFactory;
import io.shardingsphere.proxy.backend.netty.NettyBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Backend handler factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendHandlerFactory {
    
    private static final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    /**
     * Create new instance of text protocol backend handler.
     * 
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newTextProtocolInstance(
            final int connectionId, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        return RULE_REGISTRY.getBackendNIOConfig().isUseNIO()
                ? new NettyBackendHandler(connectionId, sequenceId, sql, databaseType) : new JDBCBackendHandler(sql, JDBCExecuteEngineFactory.createTextProtocolInstance(backendConnection));
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
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newBinaryProtocolInstance(
            final int connectionId, final int sequenceId, final String sql, final List<Object> parameters, final BackendConnection backendConnection, final DatabaseType databaseType) {
        return RULE_REGISTRY.getBackendNIOConfig().isUseNIO() ? new NettyBackendHandler(connectionId, sequenceId, sql, databaseType)
                : new JDBCBackendHandler(sql, JDBCExecuteEngineFactory.createBinaryProtocolInstance(parameters, backendConnection));
    }
}
