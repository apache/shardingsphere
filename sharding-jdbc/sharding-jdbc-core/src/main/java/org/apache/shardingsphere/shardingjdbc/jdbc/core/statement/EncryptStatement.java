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

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.EncryptRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.EncryptResultSet;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationStatement;
import org.apache.shardingsphere.sql.parser.relation.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.engine.impl.DefaultSQLRewriteEngine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encrypt statement.
 *
 * @author panjuan
 */
@Slf4j
public final class EncryptStatement extends AbstractUnsupportedOperationStatement {
    
    @Getter
    private final EncryptConnection connection;
    
    private final Statement statement;
    
    private final EncryptRuntimeContext runtimeContext;
    
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
    
    @SuppressWarnings("unchecked")
    private String getRewriteSQL(final String sql) {
        SQLStatement sqlStatement = runtimeContext.getParseEngine().parse(sql, false);
        RelationMetas relationMetas = getRelationMetas(runtimeContext.getMetaData().getTables());
        sqlStatementContext = SQLStatementContextFactory.newInstance(relationMetas, sql, Collections.emptyList(), sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteEntry(runtimeContext.getMetaData(), 
                runtimeContext.getProperties()).createSQLRewriteContext(sql, Collections.emptyList(), sqlStatementContext, createSQLRewriteContextDecorator(runtimeContext.getRule()));
        String result = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext).getSql();
        showSQL(result);
        return result;
    }
    
    private RelationMetas getRelationMetas(final TableMetas tableMetas) {
        Map<String, RelationMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size());
        for (String each : tableMetas.getAllTableNames()) {
            TableMetaData tableMetaData = tableMetas.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
    
    private Map<BaseRule, SQLRewriteContextDecorator> createSQLRewriteContextDecorator(final EncryptRule encryptRule) {
        Map<BaseRule, SQLRewriteContextDecorator> result = new HashMap<>(1, 1);
        result.put(encryptRule, new EncryptSQLRewriteContextDecorator());
        return result;
    }
    
    private void showSQL(final String sql) {
        boolean showSQL = runtimeContext.getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW);
        if (showSQL) {
            log.info("Rule Type: encrypt");
            log.info("SQL: {}", sql);
        }
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
