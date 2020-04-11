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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.execute.sql.prepare.SQLExecuteGroupCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.underlying.common.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL execute group callback for Sharding-Proxy.
 */
@RequiredArgsConstructor
public final class ProxyJDBCExecuteGroupCallback implements SQLExecuteGroupCallback {
    
    private static final Integer MYSQL_MEMORY_FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private static final int POSTGRESQL_MEMORY_FETCH_ONE_ROW_A_TIME = 1;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    @Override
    public Statement createStatement(final Connection connection, final ExecutionUnit executionUnit, final ConnectionMode connectionMode, final StatementOption statementOption) throws SQLException {
        Statement result = jdbcExecutorWrapper.createStatement(connection, executionUnit.getSqlUnit(), statementOption.isReturnGeneratedKeys());
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    private void setFetchSize(final Statement statement) throws SQLException {
        if (LogicSchemas.getInstance().getDatabaseType() instanceof MySQLDatabaseType) {
            statement.setFetchSize(MYSQL_MEMORY_FETCH_ONE_ROW_A_TIME);
        } else if (LogicSchemas.getInstance().getDatabaseType() instanceof PostgreSQLDatabaseType) {
            statement.setFetchSize(POSTGRESQL_MEMORY_FETCH_ONE_ROW_A_TIME);
        }
    }
}
