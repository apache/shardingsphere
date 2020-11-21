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

package org.apache.shardingsphere.infra.executor.sql.group.driver.jdbc;

import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.jdbc.connection.JDBCExecutorManager;
import org.apache.shardingsphere.infra.executor.sql.group.driver.DriverExecutionGroupEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Execution group engine for prepared statement.
 */
public final class PreparedStatementExecutionGroupEngine extends DriverExecutionGroupEngine<JDBCExecutionUnit, JDBCExecutorManager, Connection, StatementOption> {
    
    public PreparedStatementExecutionGroupEngine(final int maxConnectionsSizePerQuery,
                                                 final JDBCExecutorManager executionConnection, final StatementOption option, final Collection<ShardingSphereRule> rules) {
        super(maxConnectionsSizePerQuery, executionConnection, option, rules);
    }
    
    @Override
    protected JDBCExecutionUnit createDriverSQLExecutionUnit(final ExecutionUnit executionUnit, final JDBCExecutorManager executorManager, final Connection connection,
                                                             final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        PreparedStatement preparedStatement = createPreparedStatement(
                executionUnit.getSqlUnit().getSql(), executionUnit.getSqlUnit().getParameters(), executorManager, connection, connectionMode, option);
        return new JDBCExecutionUnit(executionUnit, connectionMode, preparedStatement);
    }
    
    private PreparedStatement createPreparedStatement(final String sql, final List<Object> parameters, final JDBCExecutorManager executionConnection, final Connection connection,
                                                      final ConnectionMode connectionMode, final StatementOption statementOption) throws SQLException {
        return (PreparedStatement) executionConnection.createStorageResource(sql, parameters, connection, connectionMode, statementOption);
    }
}
