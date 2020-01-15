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
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.prepare.SQLExecutePrepareCallback;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.underlying.common.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.dialect.PostgreSQLDatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * SQL execute prepare callback for Sharding-Proxy.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ProxyJDBCExecutePrepareCallback implements SQLExecutePrepareCallback {
    
    private static final Integer MYSQL_MEMORY_FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private static final int POSTGRESQL_MEMORY_FETCH_ONE_ROW_A_TIME = 1;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final boolean isReturnGeneratedKeys;
    
    @Override
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        return backendConnection.getConnections(connectionMode, dataSourceName, connectionSize);
    }
    
    @Override
    public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final ExecutionUnit executionUnit, final ConnectionMode connectionMode) throws SQLException {
        Statement statement = jdbcExecutorWrapper.createStatement(connection, executionUnit.getSqlUnit(), isReturnGeneratedKeys);
        if (connectionMode.equals(ConnectionMode.MEMORY_STRICTLY)) {
            if (LogicSchemas.getInstance().getDatabaseType() instanceof MySQLDatabaseType) {
                statement.setFetchSize(MYSQL_MEMORY_FETCH_ONE_ROW_A_TIME);
            } else if (LogicSchemas.getInstance().getDatabaseType() instanceof PostgreSQLDatabaseType) {
                statement.setFetchSize(POSTGRESQL_MEMORY_FETCH_ONE_ROW_A_TIME);
            }
        }
        return new StatementExecuteUnit(executionUnit, statement, connectionMode);
    }
}
