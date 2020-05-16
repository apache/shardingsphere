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

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteUpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSphereSchemas;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.executor.impl.DefaultSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.queryresult.MemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.queryresult.StreamQueryResult;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL executor callback for Proxy.
 */
public final class ProxySQLExecutorCallback extends DefaultSQLExecutorCallback<ExecuteResponse> {
    
    static {
        ShardingSphereServiceLoader.register(RuleProxySQLExecutorCallback.class);
    }
    
    private final SQLStatementContext sqlStatementContext;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;

    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;

    public ProxySQLExecutorCallback(final SQLStatementContext sqlStatementContext, final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper,
                                    final boolean isExceptionThrown, final boolean isReturnGeneratedKeys, final boolean fetchMetaData) {
        super(ShardingSphereSchemas.getInstance().getDatabaseType(), isExceptionThrown);
        this.sqlStatementContext = sqlStatementContext;
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResponse executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        boolean withMetaData = false;
        if (fetchMetaData && !hasMetaData) {
            hasMetaData = true;
            withMetaData = true;
        }
        return executeSQL(statement, sql, connectionMode, withMetaData);
    }
    
    private ExecuteResponse executeSQL(final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean withMetadata) throws SQLException {
        backendConnection.add(statement);
        if (jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            ResultSet resultSet = statement.getResultSet();
            backendConnection.add(resultSet);
            return new ExecuteQueryResponse(withMetadata ? getQueryHeaders(sqlStatementContext, resultSet.getMetaData()) : null, createQueryResult(resultSet, connectionMode));
        }
        return new ExecuteUpdateResponse(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
    }
    
    private List<QueryHeader> getQueryHeaders(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return getQueryHeaders(((SelectStatementContext) sqlStatementContext).getProjectionsContext(), resultSetMetaData);
        }
        return getQueryHeaders(resultSetMetaData);
    }
    
    private List<QueryHeader> getQueryHeaders(final ProjectionsContext projectionsContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        List<QueryHeader> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= projectionsContext.getExpandProjections().size(); columnIndex++) {
            result.add(new QueryHeader(projectionsContext, resultSetMetaData, backendConnection.getSchema(), columnIndex));
        }
        return result;
    }
    
    private List<QueryHeader> getQueryHeaders(final ResultSetMetaData resultSetMetaData) throws SQLException {
        List<QueryHeader> result = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            result.add(new QueryHeader(resultSetMetaData, backendConnection.getSchema(), columnIndex));
        }
        return result;
    }
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) throws SQLException {
        return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
}
