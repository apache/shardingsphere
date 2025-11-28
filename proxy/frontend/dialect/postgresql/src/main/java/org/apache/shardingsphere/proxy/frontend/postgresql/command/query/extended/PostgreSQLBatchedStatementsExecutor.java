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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

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
    
    private final JDBCExecutor jdbcExecutor;
    
    private final ConnectionSession connectionSession;
    
    private final MetaDataContexts metaDataContexts;
    
    private final PostgreSQLServerPreparedStatement preparedStatement;
    
    private final Map<ExecutionUnit, List<List<Object>>> executionUnitParams = new HashMap<>();
    
    private final ExecutionContext anyExecutionContext;
    
    private ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
    
    public PostgreSQLBatchedStatementsExecutor(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<List<Object>> parameterSets) {
        jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        this.connectionSession = connectionSession;
        metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        this.preparedStatement = preparedStatement;
        Iterator<List<Object>> parameterSetsIterator = parameterSets.iterator();
        SQLStatementContext sqlStatementContext = null;
        ExecutionContext executionContext = null;
        if (parameterSetsIterator.hasNext()) {
            List<Object> firstGroupOfParam = parameterSetsIterator.next();
            sqlStatementContext = createSQLStatementContext(firstGroupOfParam, preparedStatement.getHintValueContext());
            executionContext = createExecutionContext(createQueryContext(sqlStatementContext, firstGroupOfParam, preparedStatement.getHintValueContext()));
            for (ExecutionUnit each : executionContext.getExecutionUnits()) {
                executionUnitParams.computeIfAbsent(each, unused -> new LinkedList<>()).add(each.getSqlUnit().getParameters());
            }
        }
        anyExecutionContext = executionContext;
        prepareForRestOfParametersSet(parameterSetsIterator, sqlStatementContext, preparedStatement.getHintValueContext());
    }
    
    private SQLStatementContext createSQLStatementContext(final List<Object> params, final HintValueContext hintValueContext) {
        SQLStatementContext result =
                new SQLBindEngine(metaDataContexts.getMetaData(), connectionSession.getCurrentDatabaseName(), hintValueContext).bind(preparedStatement.getSqlStatementContext().getSqlStatement());
        if (result instanceof ParameterAware) {
            ((ParameterAware) result).bindParameters(params);
        }
        return result;
    }
    
    private void prepareForRestOfParametersSet(final Iterator<List<Object>> paramSetsIterator, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext) {
        while (paramSetsIterator.hasNext()) {
            List<Object> eachGroupOfParam = paramSetsIterator.next();
            if (sqlStatementContext instanceof ParameterAware) {
                ((ParameterAware) sqlStatementContext).bindParameters(eachGroupOfParam);
            }
            ExecutionContext eachExecutionContext = createExecutionContext(createQueryContext(sqlStatementContext, eachGroupOfParam, hintValueContext));
            for (ExecutionUnit each : eachExecutionContext.getExecutionUnits()) {
                executionUnitParams.computeIfAbsent(each, unused -> new LinkedList<>()).add(each.getSqlUnit().getParameters());
            }
        }
    }
    
    private QueryContext createQueryContext(final SQLStatementContext sqlStatementContext, final List<Object> params, final HintValueContext hintValueContext) {
        return new QueryContext(sqlStatementContext, preparedStatement.getSql(), params, hintValueContext, connectionSession.getConnectionContext(), metaDataContexts.getMetaData());
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(connectionSession.getCurrentDatabaseName());
        SQLAuditEngine.audit(queryContext, currentDatabase);
        return kernelProcessor.generateExecutionContext(queryContext, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps());
    }
    
    /**
     * Execute batch.
     *
     * @return inserted rows
     * @throws SQLException SQL exception
     */
    public int executeBatch() throws SQLException {
        connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        addBatchedParametersToPreparedStatements();
        return executeBatchedPreparedStatements();
    }
    
    private void addBatchedParametersToPreparedStatements() throws SQLException {
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName()).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery,
                connectionSession.getDatabaseConnectionManager(), (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(false), rules, metaDataContexts.getMetaData());
        executionGroupContext = prepareEngine.prepare(connectionSession.getUsedDatabaseName(), anyExecutionContext, executionUnitParams.keySet(),
                new ExecutionGroupReportContext(connectionSession.getProcessId(), connectionSession.getUsedDatabaseName(), connectionSession.getConnectionContext().getGrantee()));
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit each : eachGroup.getInputs()) {
                prepareJDBCExecutionUnit(each);
            }
        }
    }
    
    private void prepareJDBCExecutionUnit(final JDBCExecutionUnit jdbcExecutionUnit) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) jdbcExecutionUnit.getStorageResource();
        for (List<Object> eachGroupParam : executionUnitParams.getOrDefault(jdbcExecutionUnit.getExecutionUnit(), Collections.emptyList())) {
            ListIterator<Object> params = eachGroupParam.listIterator();
            while (params.hasNext()) {
                int paramIndex = params.nextIndex() + 1;
                Object value = params.next();
                if (value instanceof PostgreSQLTypeUnspecifiedSQLParameter) {
                    value = value.toString();
                }
                preparedStatement.setObject(paramIndex, value);
            }
            preparedStatement.addBatch();
        }
    }
    
    private int executeBatchedPreparedStatements() throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        JDBCExecutorCallback<int[]> callback =
                new BatchedStatementsJDBCExecutorCallback(protocolType, database.getResourceMetaData(), preparedStatement.getSqlStatementContext().getSqlStatement(), isExceptionThrown);
        List<int[]> executeResults = jdbcExecutor.execute(executionGroupContext, callback);
        int result = 0;
        for (int[] eachResult : executeResults) {
            for (int each : eachResult) {
                result += each;
            }
        }
        return result;
    }
    
    private static final class BatchedStatementsJDBCExecutorCallback extends JDBCExecutorCallback<int[]> {
        
        private BatchedStatementsJDBCExecutorCallback(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement,
                                                      final boolean isExceptionThrown) {
            super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
        }
        
        @Override
        protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
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
