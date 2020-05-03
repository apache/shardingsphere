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
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractMasterSlavePreparedStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.MasterSlaveConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * PreparedStatement that support master-slave.
 */
@Getter
public final class MasterSlavePreparedStatement extends AbstractMasterSlavePreparedStatementAdapter {
    
    private final MasterSlaveConnection connection;
    
    private final SQLStatement sqlStatement;
    
    @Getter
    private final ParameterMetaData parameterMetaData;
    
    private final Collection<PreparedStatement> routedStatements = new LinkedList<>();
    
    public MasterSlavePreparedStatement(final MasterSlaveConnection connection, final String sql) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public MasterSlavePreparedStatement(final MasterSlaveConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public MasterSlavePreparedStatement(
            final MasterSlaveConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.connection = connection;
        sqlStatement = connection.getRuntimeContext().getSqlParserEngine().parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        RuntimeContext runtimeContext = connection.getRuntimeContext();
        Collection<ShardingSphereRule> rules = runtimeContext.getRules();
        RouteContext routeContext = new DataNodeRouter(runtimeContext.getMetaData(), runtimeContext.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        ExecutionContext executionContext = new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(runtimeContext.getMetaData(), sqlRewriteResult));
        if (runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            PreparedStatement preparedStatement = connection.getConnection(
                    each.getDataSourceName()).prepareStatement(each.getSqlUnit().getSql(), resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(preparedStatement);
        }
    }
    
    public MasterSlavePreparedStatement(final MasterSlaveConnection connection, final String sql, final int autoGeneratedKeys) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.connection = connection;
        sqlStatement = connection.getRuntimeContext().getSqlParserEngine().parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        RuntimeContext runtimeContext = connection.getRuntimeContext();
        Collection<ShardingSphereRule> rules = runtimeContext.getRules();
        RouteContext routeContext = new DataNodeRouter(runtimeContext.getMetaData(), runtimeContext.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        ExecutionContext executionContext = new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(runtimeContext.getMetaData(), sqlRewriteResult));
        if (runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            PreparedStatement preparedStatement = connection.getConnection(each.getDataSourceName()).prepareStatement(each.getSqlUnit().getSql(), autoGeneratedKeys);
            routedStatements.add(preparedStatement);
        }
    }
    
    public MasterSlavePreparedStatement(final MasterSlaveConnection connection, final String sql, final int[] columnIndexes) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.connection = connection;
        sqlStatement = connection.getRuntimeContext().getSqlParserEngine().parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        RuntimeContext runtimeContext = connection.getRuntimeContext();
        Collection<ShardingSphereRule> rules = runtimeContext.getRules();
        RouteContext routeContext = new DataNodeRouter(runtimeContext.getMetaData(), runtimeContext.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        ExecutionContext executionContext = new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(runtimeContext.getMetaData(), sqlRewriteResult));
        if (runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            PreparedStatement preparedStatement = connection.getConnection(each.getDataSourceName()).prepareStatement(each.getSqlUnit().getSql(), columnIndexes);
            routedStatements.add(preparedStatement);
        }
    }
    
    public MasterSlavePreparedStatement(final MasterSlaveConnection connection, final String sql, final String[] columnNames) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.connection = connection;
        sqlStatement = connection.getRuntimeContext().getSqlParserEngine().parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        RuntimeContext runtimeContext = connection.getRuntimeContext();
        Collection<ShardingSphereRule> rules = runtimeContext.getRules();
        RouteContext routeContext = new DataNodeRouter(runtimeContext.getMetaData(), runtimeContext.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        ExecutionContext executionContext = new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(runtimeContext.getMetaData(), sqlRewriteResult));
        if (runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            PreparedStatement preparedStatement = connection.getConnection(each.getDataSourceName()).prepareStatement(each.getSqlUnit().getSql(), columnNames);
            routedStatements.add(preparedStatement);
        }
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support executeQuery for DDL");
        return routedStatements.iterator().next().executeQuery();
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        int result = 0;
        for (PreparedStatement each : routedStatements) {
            result += each.executeUpdate();
        }
        return result;
    }
    
    @Override
    public boolean execute() throws SQLException {
        boolean result = false;
        for (PreparedStatement each : routedStatements) {
            result = each.execute();
        }
        return result;
    }
    
    @Override
    public void clearBatch() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support clearBatch for DDL");
        routedStatements.iterator().next().clearBatch();
    }
    
    @Override
    public void addBatch() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support addBatch for DDL");
        routedStatements.iterator().next().addBatch();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support executeBatch for DDL");
        return routedStatements.iterator().next().executeBatch();
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support getResultSet for DDL");
        return routedStatements.iterator().next().getResultSet();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Preconditions.checkArgument(1 == routedStatements.size(), "Cannot support getGeneratedKeys for DDL");
        return routedStatements.iterator().next().getGeneratedKeys();
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return routedStatements.iterator().next().getResultSetHoldability();
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return routedStatements.iterator().next().getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        return routedStatements.iterator().next().getResultSetType();
    }
    
    @Override
    public boolean isAccumulate() {
        return false;
    }
}
