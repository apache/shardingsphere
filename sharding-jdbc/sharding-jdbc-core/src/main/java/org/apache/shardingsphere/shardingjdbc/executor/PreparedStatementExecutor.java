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

package org.apache.shardingsphere.shardingjdbc.executor;

import lombok.Getter;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecutorCallback;
import org.apache.shardingsphere.sharding.execute.sql.execute.result.MemoryQueryResult;
import org.apache.shardingsphere.sharding.execute.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.sharding.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * Prepared statement executor.
 */
public final class PreparedStatementExecutor extends AbstractStatementExecutor {
    
    @Getter
    private final boolean returnGeneratedKeys;
    
    public PreparedStatementExecutor(final int resultSetType, 
                                     final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys, final ShardingConnection shardingConnection) {
        super(true, resultSetType, resultSetConcurrency, resultSetHoldability, shardingConnection);
        this.returnGeneratedKeys = returnGeneratedKeys;
    }
    
    /**
     * Initialize executor.
     *
     * @param executionContext execution context
     * @throws SQLException SQL exception
     */
    public void init(final ExecutionContext executionContext) throws SQLException {
        setSqlStatementContext(executionContext.getSqlStatementContext());
        getInputGroups().addAll(obtainExecuteGroups(executionContext.getExecutionUnits()));
        cacheStatements();
    }
    
    private Collection<InputGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<ExecutionUnit> executionUnits) throws SQLException {
        StatementOption statementOption = returnGeneratedKeys
                ? new StatementOption(true) : new StatementOption(getResultSetType(), getResultSetConcurrency(), getResultSetHoldability());
        return getExecuteGroupEngine().getExecuteUnitGroups(getConnection(), executionUnits, statementOption);
    }
    
    /**
     * Execute query.
     *
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<QueryResult> executeCallback = new SQLExecutorCallback<QueryResult>(getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return getQueryResult(statement, connectionMode);
            }
        };
        return executeCallback(executeCallback);
    }
    
    private QueryResult getQueryResult(final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        ResultSet resultSet = preparedStatement.executeQuery();
        getResultSets().add(resultSet);
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
    }
    
    /**
     * Execute update.
     * 
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Integer> executeCallback = SQLExecuteCallbackFactory.getPreparedUpdateSQLExecuteCallback(getDatabaseType(), isExceptionThrown);
        List<Integer> results = executeCallback(executeCallback);
        if (isAccumulate()) {
            return accumulate(results);
        } else {
            return results.get(0);
        }
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute() throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Boolean> executeCallback = SQLExecuteCallbackFactory.getPreparedSQLExecuteCallback(getDatabaseType(), isExceptionThrown);
        List<Boolean> result = executeCallback(executeCallback);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
}
