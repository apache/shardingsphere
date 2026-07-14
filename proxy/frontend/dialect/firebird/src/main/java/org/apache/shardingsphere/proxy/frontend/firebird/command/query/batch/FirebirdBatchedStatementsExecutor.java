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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

/**
 * Batched statements executor for Firebird.
 */
public final class FirebirdBatchedStatementsExecutor {
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final JDBCExecutor jdbcExecutor;
    
    private final ConnectionSession connectionSession;
    
    private final MetaDataContexts metaDataContexts;
    
    private final FirebirdServerPreparedStatement preparedStatement;
    
    private final Map<ExecutionUnit, List<List<Object>>> executionUnitParams = new LinkedHashMap<>();
    
    private final Map<ExecutionUnit, List<Integer>> executionUnitBatchMessageIndexes = new LinkedHashMap<>();
    
    private final int batchMessageCount;
    
    private final ExecutionContext anyExecutionContext;
    
    private ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
    
    public FirebirdBatchedStatementsExecutor(final ConnectionSession connectionSession, final FirebirdServerPreparedStatement preparedStatement, final List<List<Object>> parameterSets) {
        jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        this.connectionSession = connectionSession;
        metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        this.preparedStatement = preparedStatement;
        batchMessageCount = parameterSets.size();
        Iterator<List<Object>> parameterSetsIterator = parameterSets.iterator();
        SQLStatementContext sqlStatementContext = null;
        ExecutionContext executionContext = null;
        int batchMessageIndex = 0;
        if (parameterSetsIterator.hasNext()) {
            List<Object> firstGroupOfParam = parameterSetsIterator.next();
            sqlStatementContext = createSQLStatementContext(firstGroupOfParam, preparedStatement.getHintValueContext());
            executionContext = createExecutionContext(createQueryContext(sqlStatementContext, firstGroupOfParam, preparedStatement.getHintValueContext()));
            addExecutionUnitParams(executionContext, batchMessageIndex++);
        }
        anyExecutionContext = executionContext;
        prepareForRestOfParametersSet(parameterSetsIterator, sqlStatementContext, preparedStatement.getHintValueContext(), batchMessageIndex);
    }
    
    private SQLStatementContext createSQLStatementContext(final List<Object> params, final HintValueContext hintValueContext) {
        SQLStatementContext result = new SQLBindEngine(
                metaDataContexts.getMetaData(), connectionSession.getCurrentDatabaseName(), hintValueContext).bind(preparedStatement.getSqlStatementContext().getSqlStatement());
        if (result instanceof ParameterAware) {
            ((ParameterAware) result).bindParameters(params);
        }
        return result;
    }
    
    private void prepareForRestOfParametersSet(final Iterator<List<Object>> paramSetsIterator, final SQLStatementContext sqlStatementContext,
                                               final HintValueContext hintValueContext, final int firstBatchMessageIndex) {
        int batchMessageIndex = firstBatchMessageIndex;
        while (paramSetsIterator.hasNext()) {
            List<Object> eachGroupOfParam = paramSetsIterator.next();
            if (sqlStatementContext instanceof ParameterAware) {
                ((ParameterAware) sqlStatementContext).bindParameters(eachGroupOfParam);
            }
            ExecutionContext eachExecutionContext = createExecutionContext(createQueryContext(sqlStatementContext, eachGroupOfParam, hintValueContext));
            addExecutionUnitParams(eachExecutionContext, batchMessageIndex++);
        }
    }
    
    private void addExecutionUnitParams(final ExecutionContext executionContext, final int batchMessageIndex) {
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            List<List<Object>> params = executionUnitParams.get(each);
            if (null == params) {
                params = new LinkedList<>();
                executionUnitParams.put(each, params);
            }
            params.add(each.getSqlUnit().getParameters());
            List<Integer> batchMessageIndexes = executionUnitBatchMessageIndexes.get(each);
            if (null == batchMessageIndexes) {
                batchMessageIndexes = new LinkedList<>();
                executionUnitBatchMessageIndexes.put(each, batchMessageIndexes);
            }
            batchMessageIndexes.add(batchMessageIndex);
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
     * @return batch completion in the Firebird domain
     * @throws SQLException SQL exception
     */
    public FirebirdBatchCompletion executeBatch() throws SQLException {
        connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        addBatchedParametersToPreparedStatements();
        return createBatchCompletion(executeBatchedPreparedStatements());
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
                preparedStatement.setObject(paramIndex, value);
            }
            preparedStatement.addBatch();
        }
    }
    
    private List<BatchExecutionUnitResult> executeBatchedPreparedStatements() throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        JDBCExecutorCallback<BatchExecutionUnitResult> callback =
                new BatchedStatementsJDBCExecutorCallback(protocolType, database.getResourceMetaData(), preparedStatement.getSqlStatementContext().getSqlStatement(), isExceptionThrown);
        return jdbcExecutor.execute(executionGroupContext, callback);
    }
    
    private FirebirdBatchCompletion createBatchCompletion(final List<BatchExecutionUnitResult> executeResults) {
        int[] messageUpdateCounts = new int[batchMessageCount];
        int failedMessageIndex = -1;
        SQLException failureCause = null;
        Iterator<ExecutionUnit> executionUnits = getExecutionUnitsInExecutionOrder().iterator();
        Iterator<BatchExecutionUnitResult> results = executeResults.iterator();
        while (executionUnits.hasNext() && results.hasNext()) {
            ExecutionUnit executionUnit = executionUnits.next();
            BatchExecutionUnitResult eachResult = results.next();
            List<Integer> batchMessageIndexes = executionUnitBatchMessageIndexes.getOrDefault(executionUnit, Collections.emptyList());
            mergeUpdateCounts(messageUpdateCounts, batchMessageIndexes, eachResult.updateCounts);
            if (null != eachResult.failure) {
                int eachFailedMessageIndex = getFailedMessageIndex(eachResult.updateCounts, batchMessageIndexes);
                if (-1 == failedMessageIndex || eachFailedMessageIndex < failedMessageIndex) {
                    failedMessageIndex = eachFailedMessageIndex;
                    failureCause = eachResult.failure;
                }
            }
        }
        return -1 == failedMessageIndex
                ? new FirebirdBatchCompletion(batchMessageCount, toFirebirdUpdateCounts(messageUpdateCounts, batchMessageCount))
                : createFailedBatchCompletion(messageUpdateCounts, failedMessageIndex, failureCause);
    }
    
    private FirebirdBatchCompletion createFailedBatchCompletion(final int[] messageUpdateCounts, final int failedMessageIndex, final SQLException failureCause) {
        int[] processedUpdateCounts = toFirebirdUpdateCounts(messageUpdateCounts, failedMessageIndex + 1);
        processedUpdateCounts[failedMessageIndex] = FirebirdBatchCompletion.EXECUTE_FAILED;
        return new FirebirdBatchCompletion(failedMessageIndex + 1, processedUpdateCounts, new FirebirdBatchCompletion.Failure(failedMessageIndex, failureCause));
    }
    
    /**
     * Get the original client index of the failed batch message within one execution unit.
     *
     * @param updateCounts unit-local update counts carried by the batch failure
     * @param batchMessageIndexes original client indexes of the unit's batched messages
     * @return zero-based original client index of the failed message
     */
    private int getFailedMessageIndex(final int[] updateCounts, final List<Integer> batchMessageIndexes) {
        if (batchMessageIndexes.isEmpty()) {
            return 0;
        }
        int failedOffset = updateCounts.length;
        for (int i = 0; i < updateCounts.length; i++) {
            if (Statement.EXECUTE_FAILED == updateCounts[i]) {
                failedOffset = i;
                break;
            }
        }
        return batchMessageIndexes.get(Math.min(failedOffset, batchMessageIndexes.size() - 1));
    }
    
    private int[] toFirebirdUpdateCounts(final int[] messageUpdateCounts, final int processedMessageCount) {
        int[] result = Arrays.copyOf(messageUpdateCounts, processedMessageCount);
        for (int i = 0; i < result.length; i++) {
            if (Statement.EXECUTE_FAILED == result[i]) {
                result[i] = FirebirdBatchCompletion.EXECUTE_FAILED;
            }
        }
        return result;
    }
    
    private List<ExecutionUnit> getExecutionUnitsInExecutionOrder() {
        List<ExecutionUnit> result = new LinkedList<>();
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit each : eachGroup.getInputs()) {
                result.add(each.getExecutionUnit());
            }
        }
        return result;
    }
    
    private void mergeUpdateCounts(final int[] result, final List<Integer> batchMessageIndexes, final int[] updateCounts) {
        int countSize = Math.min(batchMessageIndexes.size(), updateCounts.length);
        for (int i = 0; i < countSize; i++) {
            int updateCount = updateCounts[i];
            int batchMessageIndex = batchMessageIndexes.get(i);
            result[batchMessageIndex] = mergeUpdateCount(result[batchMessageIndex], updateCount);
        }
    }
    
    private int mergeUpdateCount(final int current, final int updateCount) {
        if (Statement.EXECUTE_FAILED == updateCount || Statement.EXECUTE_FAILED == current) {
            return Statement.EXECUTE_FAILED;
        }
        if (Statement.SUCCESS_NO_INFO == updateCount || Statement.SUCCESS_NO_INFO == current) {
            return Statement.SUCCESS_NO_INFO;
        }
        return current + updateCount;
    }
    
    private static final class BatchedStatementsJDBCExecutorCallback extends JDBCExecutorCallback<BatchExecutionUnitResult> {
        
        private BatchedStatementsJDBCExecutorCallback(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement,
                                                      final boolean isExceptionThrown) {
            super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
        }
        
        @Override
        protected BatchExecutionUnitResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
            try {
                return new BatchExecutionUnitResult(statement.executeBatch(), null);
            } catch (final BatchUpdateException ex) {
                return new BatchExecutionUnitResult(null == ex.getUpdateCounts() ? new int[0] : ex.getUpdateCounts(), ex);
            } finally {
                statement.close();
            }
        }
        
        @Override
        protected Optional<BatchExecutionUnitResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
            return Optional.empty();
        }
    }
    
    @RequiredArgsConstructor
    private static final class BatchExecutionUnitResult {
        
        private final int[] updateCounts;
        
        private final SQLException failure;
    }
}
