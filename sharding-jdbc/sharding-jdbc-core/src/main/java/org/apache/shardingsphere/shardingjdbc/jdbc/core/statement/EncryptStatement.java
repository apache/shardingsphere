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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.EncryptResultSet;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationStatement;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

/**
 * Encrypt statement.
 */
public final class EncryptStatement extends AbstractUnsupportedOperationStatement {
    
    @Getter
    private final EncryptConnection connection;
    
    private final Statement statement;
    
    private final RuntimeContext runtimeContext;
    
    private SQLStatementContext sqlStatementContext;
    
    private EncryptResultSet resultSet;
    
    public EncryptStatement(final EncryptConnection connection) throws SQLException {
        this(connection, connection.getConnection().createStatement());
    }
    
    public EncryptStatement(final EncryptConnection connection, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, connection.getConnection().createStatement(resultSetType, resultSetConcurrency));
    }
    
    public EncryptStatement(final EncryptConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this(connection, connection.getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }
    
    private EncryptStatement(final EncryptConnection connection, final Statement statement) {
        this.connection = connection;
        this.statement = statement;
        runtimeContext = connection.getRuntimeContext();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        ResultSet resultSet = statement.executeQuery(getRewriteSQL(sql));
        this.resultSet = new EncryptResultSet(runtimeContext, sqlStatementContext, this, resultSet);
        return this.resultSet;
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    private String getRewriteSQL(final String sql) {
        Collection<BaseRule> rules = runtimeContext.getRules();
        RouteContext routeContext = new DataNodeRouter(runtimeContext.getMetaData(), runtimeContext.getProperties(),
                rules).route(runtimeContext.getSqlParserEngine().parse(sql, false), sql, Collections.emptyList());
        sqlStatementContext = routeContext.getSqlStatementContext();
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        ExecutionContext executionContext = new ExecutionContext(sqlStatementContext, ExecutionContextBuilder.build(runtimeContext.getMetaData(), sqlRewriteResult));
        Preconditions.checkArgument(1 == executionContext.getExecutionUnits().size());
        if (runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
        return executionContext.getExecutionUnits().iterator().next().getSqlUnit().getSql();
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return statement.executeUpdate(getRewriteSQL(sql));
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        return statement.executeUpdate(getRewriteSQL(sql), autoGeneratedKeys);
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return statement.executeUpdate(getRewriteSQL(sql), columnIndexes);
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return statement.executeUpdate(getRewriteSQL(sql), columnNames);
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        boolean result = statement.execute(getRewriteSQL(sql));
        this.resultSet = createEncryptResultSet(statement);
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        boolean result = statement.execute(getRewriteSQL(sql), autoGeneratedKeys);
        this.resultSet = createEncryptResultSet(statement);
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        boolean result = statement.execute(getRewriteSQL(sql), columnIndexes);
        this.resultSet = createEncryptResultSet(statement);
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        boolean result = statement.execute(getRewriteSQL(sql), columnNames);
        this.resultSet = createEncryptResultSet(statement);
        return result;
    }
    
    private EncryptResultSet createEncryptResultSet(final Statement statement) throws SQLException {
        return null == statement.getResultSet() ? null : new EncryptResultSet(runtimeContext, sqlStatementContext, this, statement.getResultSet());
    }
    
    @Override
    public void close() throws SQLException {
        statement.close();
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }
    
    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        statement.setMaxFieldSize(max);
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }
    
    @Override
    public void setMaxRows(final int max) throws SQLException {
        statement.setMaxRows(max);
    }
    
    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        statement.setEscapeProcessing(enable);
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }
    
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        statement.setQueryTimeout(seconds);
    }
    
    @Override
    public void cancel() throws SQLException {
        statement.cancel();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }
    
    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        return statement.getMoreResults(current);
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        statement.setFetchSize(rows);
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return statement.isClosed();
    }
    
    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        statement.setPoolable(poolable);
    }
    
    @Override
    public boolean isPoolable() throws SQLException {
        return statement.isPoolable();
    }
}
