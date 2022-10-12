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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

/**
 * Batched statements executor for PostgreSQL.
 */
public final class PostgreSQLBatchedStatementsExecutor {
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final JDBCExecutor jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), false);
    
    private final ConnectionSession connectionSession;
    
    private final MetaDataContexts metaDataContexts;
    
    private final PostgreSQLPreparedStatement preparedStatement;
    
    private final Map<ExecutionUnit, List<List<Object>>> executionUnitParameters = new HashMap<>();
    
    private final ExecutionContext anyExecutionContext;
    
    private ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
    
    public PostgreSQLBatchedStatementsExecutor(final ConnectionSession connectionSession, final PostgreSQLPreparedStatement preparedStatement, final List<List<Object>> parameterSets) {
        this.connectionSession = connectionSession;
        metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        this.preparedStatement = preparedStatement;
        Iterator<List<Object>> parameterSetsIterator = parameterSets.iterator();
        SQLStatementContext<?> sqlStatementContext = null;
        ExecutionContext executionContext = null;
        if (parameterSetsIterator.hasNext()) {
            List<Object> firstGroupOfParameter = parameterSetsIterator.next();
            sqlStatementContext = createSQLStatementContext(firstGroupOfParameter);
            executionContext = createExecutionContext(createQueryContext(sqlStatementContext, firstGroupOfParameter));
            for (ExecutionUnit each : executionContext.getExecutionUnits()) {
                executionUnitParameters.computeIfAbsent(each, unused -> new LinkedList<>()).add(each.getSqlUnit().getParameters());
            }
        }
        anyExecutionContext = executionContext;
        prepareForRestOfParametersSet(parameterSetsIterator, sqlStatementContext);
    }
    
    private SQLStatementContext<?> createSQLStatementContext(final List<Object> parameters) {
        return SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData().getDatabases(), parameters, preparedStatement.getSqlStatement(), connectionSession.getDatabaseName());
    }
    
    private void prepareForRestOfParametersSet(final Iterator<List<Object>> parameterSetsIterator, final SQLStatementContext<?> sqlStatementContext) {
        while (parameterSetsIterator.hasNext()) {
            List<Object> eachGroupOfParameter = parameterSetsIterator.next();
            if (sqlStatementContext instanceof ParameterAware) {
                ((ParameterAware) sqlStatementContext).setUpParameters(eachGroupOfParameter);
            }
            ExecutionContext eachExecutionContext = createExecutionContext(createQueryContext(sqlStatementContext, eachGroupOfParameter));
            for (ExecutionUnit each : eachExecutionContext.getExecutionUnits()) {
                executionUnitParameters.computeIfAbsent(each, unused -> new LinkedList<>()).add(each.getSqlUnit().getParameters());
            }
        }
    }
    
    private QueryContext createQueryContext(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters) {
        return new QueryContext(sqlStatementContext, preparedStatement.getSql(), parameters);
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        SQLCheckEngine.check(queryContext.getSqlStatementContext(), queryContext.getParameters(),
                metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getRuleMetaData().getRules(),
                connectionSession.getDatabaseName(), metaDataContexts.getMetaData().getDatabases(), null);
        return kernelProcessor.generateExecutionContext(queryContext, metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()),
                metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps(), connectionSession.getConnectionContext());
    }
    
    /**
     * Execute batch.
     *
     * @return inserted rows
     * @throws SQLException SQL exception
     */
    public int executeBatch() throws SQLException {
        connectionSession.getBackendConnection().handleAutoCommit();
        addBatchedParametersToPreparedStatements();
        return executeBatchedPreparedStatements();
    }
    
    private void addBatchedParametersToPreparedStatements() throws SQLException {
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = connectionSession.getTransactionStatus().isInConnectionHeldTransaction()
                ? 1
                : metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT,
                maxConnectionsSizePerQuery,
                (JDBCBackendConnection) connectionSession.getBackendConnection(), (JDBCBackendStatement) connectionSession.getStatementManager(),
                new StatementOption(false), rules, connectionSession.getDatabaseType());
        executionGroupContext = prepareEngine.prepare(anyExecutionContext.getRouteContext(), executionUnitParameters.keySet());
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit each : eachGroup.getInputs()) {
                prepareJDBCExecutionUnit(each);
            }
        }
    }
    
    private void prepareJDBCExecutionUnit(final JDBCExecutionUnit jdbcExecutionUnit) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) jdbcExecutionUnit.getStorageResource();
        for (List<Object> eachGroupParameter : executionUnitParameters.getOrDefault(jdbcExecutionUnit.getExecutionUnit(), Collections.emptyList())) {
            ListIterator<Object> parametersIterator = eachGroupParameter.listIterator();
            while (parametersIterator.hasNext()) {
                int parameterIndex = parametersIterator.nextIndex() + 1;
                Object value = parametersIterator.next();
                if (value instanceof PostgreSQLTypeUnspecifiedSQLParameter) {
                    value = value.toString();
                }
                preparedStatement.setObject(parameterIndex, value);
            }
            preparedStatement.addBatch();
        }
    }
    
    private int executeBatchedPreparedStatements() throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName());
        DatabaseType databaseType = database.getResourceMetaData().getDatabaseType();
        DatabaseType protocolType = database.getProtocolType();
        JDBCExecutorCallback<int[]> callback = new BatchedStatementsJDBCExecutorCallback(protocolType, databaseType, preparedStatement.getSqlStatement(), isExceptionThrown);
        List<int[]> executeResults = jdbcExecutor.execute(executionGroupContext, callback);
        int result = 0;
        for (int[] eachResult : executeResults) {
            for (int each : eachResult) {
                result += each;
            }
        }
        return result;
    }
    
    private static class BatchedStatementsJDBCExecutorCallback extends JDBCExecutorCallback<int[]> {
        
        BatchedStatementsJDBCExecutorCallback(final DatabaseType protocolType, final DatabaseType databaseType, final SQLStatement sqlStatement, final boolean isExceptionThrown) {
            super(protocolType, databaseType, sqlStatement, isExceptionThrown, ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext());
        }
        
        @Override
        protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
            try {
                return statement.executeBatch();
            } finally {
                statement.close();
            }
        }
        
        @SuppressWarnings("OptionalContainsCollection")
        @Override
        protected Optional<int[]> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
            return Optional.empty();
        }
    }
}
