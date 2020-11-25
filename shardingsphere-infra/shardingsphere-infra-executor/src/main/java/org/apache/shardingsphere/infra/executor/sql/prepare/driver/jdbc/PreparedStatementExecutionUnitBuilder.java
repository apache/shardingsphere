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

package org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC prepared statement execution unit builder.
 */
public final class PreparedStatementExecutionUnitBuilder implements JDBCExecutionUnitBuilder {
    
    @Override
    public JDBCExecutionUnit build(final ExecutionUnit executionUnit, final ExecutorJDBCManager executorManager,
                                   final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        PreparedStatement preparedStatement = createPreparedStatement(
                executionUnit.getSqlUnit().getSql(), executionUnit.getSqlUnit().getParameters(), executorManager, connection, connectionMode, option);
        return new JDBCExecutionUnit(executionUnit, connectionMode, preparedStatement);
    }
    
    private PreparedStatement createPreparedStatement(final String sql, final List<Object> parameters, final ExecutorJDBCManager executorJDBCManager, final Connection connection,
                                                      final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return (PreparedStatement) executorJDBCManager.createStorageResource(sql, parameters, connection, connectionMode, option);
    }
    
    @Override
    public String getType() {
        return JDBCExecutionUnitBuilderType.PREPARED_STATEMENT;
    }
}
