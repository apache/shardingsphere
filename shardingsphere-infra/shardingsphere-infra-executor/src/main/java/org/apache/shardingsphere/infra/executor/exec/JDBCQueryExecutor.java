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

package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Execute JDBC query.
 */
public final class JDBCQueryExecutor extends AbstractExecutor {
    
    private final ExecutionUnit executionUnit;
    
    private final RouteContext routeContext;
    
    private final QueryResultMetaData metaData;
    
    private Executor executor;
    
    private Collection<Statement> statements = Lists.newArrayList();
    
    public JDBCQueryExecutor(final ExecutionUnit executionUnit, final RouteContext routeContext, 
                             final ExecContext execContext, final QueryResultMetaData metaData) {
        super(execContext);
        this.executionUnit = executionUnit;
        this.routeContext = routeContext;
        this.metaData = metaData;
    }
    
    @Override
    protected void executeInit() {
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroups = prepareExecutionGroup(Arrays.asList(executionUnit));
            for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups.getInputGroups()) {
                statements.addAll(each.getInputs().stream().map(JDBCExecutionUnit::getStorageResource).collect(Collectors.toList()));
            }
            this.executor = executeQuery(executionGroups);
        } catch (SQLException ex) {
            throw new ShardingSphereException("jdbc executor init failed", ex);
        }
    }
    
    @Override
    protected boolean executeMove() {
        if (executor.moveNext()) {
            replaceCurrent(executor.current());
            return true;
        }
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public void close() {
        if (executor != null) {
            executor.close();
        }
        Collection<SQLException> exceptions = new LinkedList<>();
        statements.forEach(statement -> {
            try {
                statement.close();
            } catch (SQLException ex) {
                exceptions.add(ex);
            }
        });
        statements.clear();
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("");
        exceptions.forEach(ex::setNextException);
        throw new ShardingSphereException(ex);
    }
    
    private Executor executeQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroups) throws SQLException {
        ExecutorEngine executorEngine = new ExecutorEngine(getExecContext().getProps().<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, getExecContext().isHoldTransaction());
        List<QueryResult> results = jdbcExecutor.execute(executionGroups, new JDBCExecutorCallback<QueryResult>(getExecContext().getDatabaseType(), 
                null, true) {
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                ResultSet resultSet;
                if (statement instanceof PreparedStatement) {
                    PreparedStatement pstmt = (PreparedStatement) statement;
                    setParameters(pstmt, getExecContext().getParameters());
                    resultSet = pstmt.executeQuery();
                } else {
                    resultSet = statement.executeQuery(sql);
                }
                return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new JDBCStreamQueryResult(resultSet) : new JDBCMemoryQueryResult(resultSet);
            }
        
            @Override
            protected Optional<QueryResult> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.empty();
            }
        });
        if (results.isEmpty()) {
            return SimpleExecutor.empty(getExecContext(), metaData);
        }
        return wrapQueryResult(getExecContext(), results);
    }
    
    private Executor wrapQueryResult(final ExecContext execContext, final List<QueryResult> queryResults) {
        List<Executor> executors = queryResults.stream().map(queryResult -> new QueryResultExecutor(queryResult, execContext)).collect(Collectors.toList());
        if (executors.size() == 1) {
            return executors.get(0);
        }
        return new MultiExecutor(executors, execContext);
    }
    
    private void setParameters(final PreparedStatement pstmt, final List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            pstmt.setObject(i + 1, parameters.get(i));
        }
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> prepareExecutionGroup(final Collection<ExecutionUnit> executionUnits) throws SQLException {
        int maxConnectionsSizePerQuery = getExecContext().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
    
        String stmtType = JDBCDriverType.PREPARED_STATEMENT;
        if (getExecContext().getParameters().isEmpty()) {
            stmtType = JDBCDriverType.STATEMENT;
        }
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(stmtType,
                maxConnectionsSizePerQuery, getExecContext().getExecutorJDBCManager(), getExecContext().getOption(),
                Collections.singletonList(getExecContext().getShardingRule()));
        return prepareEngine.prepare(routeContext, executionUnits);
    }
    
}
