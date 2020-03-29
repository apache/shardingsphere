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
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.masterslave.route.engine.MasterSlaveRouteDecorator;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.MasterSlaveConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.MasterSlaveRuntimeContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.pluggble.prepare.SimpleQueryPrepareEngine;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Statement that support master-slave.
 */
@Getter
public final class MasterSlaveStatement extends AbstractStatementAdapter {
    
    private final MasterSlaveConnection connection;
    
    @Getter(AccessLevel.NONE)
    private final DataNodeRouter dataNodeRouter;
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final Collection<Statement> routedStatements = new LinkedList<>();
    
    public MasterSlaveStatement(final MasterSlaveConnection connection) {
        this(connection, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public MasterSlaveStatement(final MasterSlaveConnection connection, final int resultSetType, final int resultSetConcurrency) {
        this(connection, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public MasterSlaveStatement(final MasterSlaveConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        super(Statement.class);
        this.connection = connection;
        dataNodeRouter = new DataNodeRouter(connection.getRuntimeContext().getMetaData(), connection.getRuntimeContext().getProperties(), connection.getRuntimeContext().getSqlParserEngine());
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        clearPrevious();
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        SimpleQueryPrepareEngine prepareEngine = new SimpleQueryPrepareEngine(
                Collections.singletonList(runtimeContext.getRule()), runtimeContext.getProperties(), runtimeContext.getMetaData(), runtimeContext.getSqlParserEngine());
        ExecutionContext executionContext = prepareEngine.prepare(sql, Collections.emptyList());
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        Preconditions.checkState(1 == executionContext.getExecutionUnits().size(), "Cannot support executeQuery for DML or DDL");
        Statement statement = connection.getConnection(executionUnit.getDataSourceName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        routedStatements.add(statement);
        return statement.executeQuery(executionUnit.getSqlUnit().getSql());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        clearPrevious();
        int result = 0;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result += statement.executeUpdate(sql);
        }
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        clearPrevious();
        int result = 0;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result += statement.executeUpdate(sql, autoGeneratedKeys);
        }
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        clearPrevious();
        int result = 0;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result += statement.executeUpdate(sql, columnIndexes);
        }
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        clearPrevious();
        int result = 0;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result += statement.executeUpdate(sql, columnNames);
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        clearPrevious();
        boolean result = false;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result = statement.execute(sql);
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        clearPrevious();
        boolean result = false;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result = statement.execute(sql, autoGeneratedKeys);
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        clearPrevious();
        boolean result = false;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result = statement.execute(sql, columnIndexes);
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        clearPrevious();
        boolean result = false;
        MasterSlaveRuntimeContext runtimeContext = connection.getRuntimeContext();
        RouteContext routeContext = dataNodeRouter.route(sql, Collections.emptyList(), false);
        routeContext = new MasterSlaveRouteDecorator().decorate(routeContext, runtimeContext.getMetaData(), runtimeContext.getRule(), runtimeContext.getProperties());
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Statement statement = connection.getConnection(each.getDataSourceMapper().getActualName()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            routedStatements.add(statement);
            result = statement.execute(sql, columnNames);
        }
        return result;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Preconditions.checkState(1 == routedStatements.size());
        return routedStatements.iterator().next().getGeneratedKeys();
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        Preconditions.checkState(1 == routedStatements.size());
        return routedStatements.iterator().next().getResultSet();
    }
    
    private void clearPrevious() throws SQLException {
        for (Statement each : routedStatements) {
            each.close();
        }
        routedStatements.clear();
    }
    
    @Override
    public boolean isAccumulate() {
        return false;
    }
}
