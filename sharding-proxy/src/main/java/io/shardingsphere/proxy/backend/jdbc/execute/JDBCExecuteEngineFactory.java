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

package io.shardingsphere.proxy.backend.jdbc.execute;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.memory.ConnectionStrictlyExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.execute.stream.MemoryStrictlyExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.proxy.backend.jdbc.wrapper.PreparedStatementExecutorWrapper;
import io.shardingsphere.proxy.backend.jdbc.wrapper.StatementExecutorWrapper;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JDBC execute engine factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCExecuteEngineFactory {
    
    private static final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    /**
     * Create instance for text protocol.
     * 
     * @param backendConnection backend connection
     * @return instance for text protocol
     */
    public static JDBCExecuteEngine createTextProtocolInstance(final BackendConnection backendConnection) {
        JDBCExecutorWrapper jdbcExecutorWrapper = new StatementExecutorWrapper();
        return ConnectionMode.MEMORY_STRICTLY == RULE_REGISTRY.getConnectionMode()
                ? new MemoryStrictlyExecuteEngine(backendConnection, jdbcExecutorWrapper) : new ConnectionStrictlyExecuteEngine(backendConnection, jdbcExecutorWrapper);
    }
    
    /**
     * Create instance for binary protocol.
     *
     * @param parameters parameters of prepared statement
     * @param backendConnection backend connection
     * @return instance for binary protocol
     */
    public static JDBCExecuteEngine createBinaryProtocolInstance(final List<Object> parameters, final BackendConnection backendConnection) {
        JDBCExecutorWrapper jdbcExecutorWrapper = new PreparedStatementExecutorWrapper(parameters);
        return ConnectionMode.MEMORY_STRICTLY == RULE_REGISTRY.getConnectionMode()
                ? new MemoryStrictlyExecuteEngine(backendConnection, jdbcExecutorWrapper) : new ConnectionStrictlyExecuteEngine(backendConnection, jdbcExecutorWrapper);
    }
}
