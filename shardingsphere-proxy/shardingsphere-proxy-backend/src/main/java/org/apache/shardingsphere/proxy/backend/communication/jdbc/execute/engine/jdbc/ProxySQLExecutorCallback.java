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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.jdbc;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.ExecuteQueryResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.update.ExecuteUpdateResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.impl.DefaultSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.query.jdbc.MemoryJDBCQueryResult;
import org.apache.shardingsphere.infra.executor.sql.query.jdbc.StreamJDBCQueryResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.JDBCAccessor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.query.QueryHeaderBuilder;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL executor callback for Proxy.
 */
public final class ProxySQLExecutorCallback extends DefaultSQLExecutorCallback<ExecuteResult> {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final BackendConnection backendConnection;
    
    private final JDBCAccessor accessor;

    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;

    public ProxySQLExecutorCallback(final DatabaseType databaseType, final SQLStatementContext<?> sqlStatementContext, 
                                    final BackendConnection backendConnection, final JDBCAccessor accessor,
                                    final boolean isExceptionThrown, final boolean isReturnGeneratedKeys, final boolean fetchMetaData) {
        super(databaseType, isExceptionThrown);
        this.sqlStatementContext = sqlStatementContext;
        this.backendConnection = backendConnection;
        this.accessor = accessor;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        if (fetchMetaData && !hasMetaData) {
            hasMetaData = true;
            return executeSQL(statement, sql, connectionMode, true);
        }
        return executeSQL(statement, sql, connectionMode, false);
    }
    
    private ExecuteResult executeSQL(final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean withMetadata) throws SQLException {
        backendConnection.add(statement);
        if (accessor.execute(statement, sql, isReturnGeneratedKeys)) {
            ResultSet resultSet = statement.getResultSet();
            backendConnection.add(resultSet);
            return new ExecuteQueryResult(withMetadata ? getQueryHeaders(sqlStatementContext, resultSet.getMetaData()) : null, createQueryResult(resultSet, connectionMode));
        }
        return new ExecuteUpdateResult(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
    }
    
    private List<QueryHeader> getQueryHeaders(final SQLStatementContext<?> sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (isHasSelectExpandProjections()) {
            return getQueryHeaders(((SelectStatementContext) sqlStatementContext).getProjectionsContext(), resultSetMetaData);
        }
        return getQueryHeaders(resultSetMetaData);
    }
    
    private List<QueryHeader> getQueryHeaders(final ProjectionsContext projectionsContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        List<QueryHeader> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= projectionsContext.getExpandProjections().size(); columnIndex++) {
            result.add(QueryHeaderBuilder.build(projectionsContext, resultSetMetaData, ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()), columnIndex));
        }
        return result;
    }
    
    private List<QueryHeader> getQueryHeaders(final ResultSetMetaData resultSetMetaData) throws SQLException {
        List<QueryHeader> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            result.add(QueryHeaderBuilder.build(resultSetMetaData, ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()), columnIndex));
        }
        return result;
    }
    
    private boolean isHasSelectExpandProjections() {
        return sqlStatementContext instanceof SelectStatementContext
                && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) throws SQLException {
        return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamJDBCQueryResult(resultSet) : new MemoryJDBCQueryResult(resultSet);
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
}
