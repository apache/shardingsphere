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

package io.shardingsphere.proxy.backend.common.jdbc.execute;

import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.jdbc.execute.memory.ConnectionStrictlyExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.execute.stream.MemoryStrictlyExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.proxy.backend.common.jdbc.wrapper.PreparedStatementExecutorWrapper;
import io.shardingsphere.proxy.backend.common.jdbc.wrapper.StatementExecutorWrapper;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.PreparedStatementParameter;
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
    
    /**
     * Create instance for text protocol.
     * 
     * @return instance for text protocol
     */
    public static JDBCExecuteEngine createTextProtocolInstance() {
        JDBCExecutorWrapper jdbcExecutorWrapper = new StatementExecutorWrapper();
        return ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode() ? new MemoryStrictlyExecuteEngine(jdbcExecutorWrapper) : new ConnectionStrictlyExecuteEngine(jdbcExecutorWrapper);
    }
    
    /**
     * Create instance for statement protocol.
     *
     * @param preparedStatementParameters parameters of prepared statement
     * @return instance for statement protocol
     */
    public static JDBCExecuteEngine createStatementProtocolInstance(final List<PreparedStatementParameter> preparedStatementParameters) {
        JDBCExecutorWrapper jdbcExecutorWrapper = new PreparedStatementExecutorWrapper(preparedStatementParameters);
        return ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode() ? new MemoryStrictlyExecuteEngine(jdbcExecutorWrapper) : new ConnectionStrictlyExecuteEngine(jdbcExecutorWrapper);
    }
}
