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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Batched statements executor for Firebird.
 */
public final class FirebirdBatchedStatementsExecutor {
    
    private static final int NOT_EXECUTED = Integer.MIN_VALUE;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final JDBCExecutor jdbcExecutor;
    
    private final ConnectionSession connectionSession;
    
    private final MetaDataContexts metaDataContexts;
    
    private final FirebirdServerPreparedStatement preparedStatement;
    
    private final Map<ExecutionUnit, List<List<Object>>> executionUnitParams = new LinkedHashMap<>();
    
    private final Map<ExecutionUnit, List<Integer>> executionUnitBatchMessageIndexes = new LinkedHashMap<>();
    
    private final Map<PreparedStatement, List<List<Object>>> pendingBatchParams = new IdentityHashMap<>();
    
    private final int batchMessageCount;
    
    private final boolean multiError;
    
    private final ExecutionContext anyExecutionContext;
    
    private ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
    
    public FirebirdBatchedStatementsExecutor(final ConnectionSession connectionSession, final FirebirdServerPreparedStatement preparedStatement, final List<List<Object>> parameterSets,
                                             final boolean multiError) {
        jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        this.multiError = multiError;
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
        prepareBatchedStatements();
        List<BatchExecution> batchExecutions;
        List<BatchExecutionUnitResult> executeResults;
        try {
            batchExecutions = createBatchExecutionPlan();
            executeResults = executeBatchedPreparedStatements(batchExecutions);
        } finally {
            closePreparedStatements();
        }
        return createBatchCompletion(batchExecutions, executeResults);
    }
    
    private void prepareBatchedStatements() throws SQLException {
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName()).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery,
                connectionSession.getDatabaseConnectionManager(), (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(false), rules, metaDataContexts.getMetaData());
        executionGroupContext = prepareEngine.prepare(connectionSession.getUsedDatabaseName(), anyExecutionContext, executionUnitParams.keySet(),
                new ExecutionGroupReportContext(connectionSession.getProcessId(), connectionSession.getUsedDatabaseName(), connectionSession.getConnectionContext().getGrantee()));
    }
    
    private void addPendingBatch(final BatchExecution batchExecution) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) batchExecution.executionUnit.getStorageResource();
        List<List<Object>> params = getBatchParams(batchExecution);
        pendingBatchParams.put(preparedStatement, params);
        addBatchedParameters(preparedStatement, params);
    }
    
    private void addBatchedParameters(final PreparedStatement preparedStatement, final List<List<Object>> params) throws SQLException {
        for (List<Object> eachGroupParam : params) {
            ListIterator<Object> eachParams = eachGroupParam.listIterator();
            while (eachParams.hasNext()) {
                int paramIndex = eachParams.nextIndex() + 1;
                Object value = eachParams.next();
                preparedStatement.setObject(paramIndex, value);
            }
            preparedStatement.addBatch();
        }
    }
    
    private List<List<Object>> getBatchParams(final BatchExecution batchExecution) {
        return executionUnitParams.getOrDefault(batchExecution.executionUnit.getExecutionUnit(), Collections.<List<Object>>emptyList())
                .subList(batchExecution.fromOffset, batchExecution.toOffset);
    }
    
    private List<Integer> getBatchMessageIndexes(final BatchExecution batchExecution) {
        return executionUnitBatchMessageIndexes.getOrDefault(batchExecution.executionUnit.getExecutionUnit(), Collections.<Integer>emptyList())
                .subList(batchExecution.fromOffset, batchExecution.toOffset);
    }
    
    private void closePreparedStatements() throws SQLException {
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit each : eachGroup.getInputs()) {
                each.getStorageResource().close();
            }
        }
    }
    
    private List<BatchExecutionUnitResult> executeBatchedPreparedStatements(final List<BatchExecution> batchExecutions) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        JDBCExecutorCallback<BatchExecutionUnitResult> callback =
                new BatchedStatementsJDBCExecutorCallback(protocolType, database.getResourceMetaData(), preparedStatement.getSqlStatementContext().getSqlStatement(), isExceptionThrown);
        return isHaltedOnFirstFailure() ? executeInBatchMessageOrder(batchExecutions, callback) : jdbcExecutor.execute(executionGroupContext, callback);
    }
    
    private List<BatchExecutionUnitResult> executeInBatchMessageOrder(final List<BatchExecution> batchExecutions,
                                                                      final JDBCExecutorCallback<BatchExecutionUnitResult> callback) throws SQLException {
        List<BatchExecutionUnitResult> result = new LinkedList<>();
        for (BatchExecution each : batchExecutions) {
            addPendingBatch(each);
            List<BatchExecutionUnitResult> executeResults = jdbcExecutor.execute(createSingleUnitExecutionGroupContext(each.executionUnit), callback);
            result.addAll(executeResults);
            if (executeResults.stream().anyMatch(eachResult -> !eachResult.failures.isEmpty())) {
                break;
            }
        }
        return result;
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createSingleUnitExecutionGroupContext(final JDBCExecutionUnit executionUnit) {
        return new ExecutionGroupContext<>(Collections.singleton(new ExecutionGroup<>(Collections.singletonList(executionUnit))), executionGroupContext.getReportContext());
    }
    
    private FirebirdBatchCompletion createBatchCompletion(final List<BatchExecution> batchExecutions, final List<BatchExecutionUnitResult> executeResults) throws SQLException {
        int[] messageUpdateCounts = new int[batchMessageCount];
        Arrays.fill(messageUpdateCounts, NOT_EXECUTED);
        NavigableMap<Integer, SQLException> failedMessages = new TreeMap<>();
        Iterator<BatchExecution> executions = batchExecutions.iterator();
        Iterator<BatchExecutionUnitResult> results = executeResults.iterator();
        while (executions.hasNext() && results.hasNext()) {
            List<Integer> batchMessageIndexes = getBatchMessageIndexes(executions.next());
            BatchExecutionUnitResult eachResult = results.next();
            mergeUpdateCounts(messageUpdateCounts, batchMessageIndexes, eachResult.updateCounts);
            markFailedMessages(messageUpdateCounts, failedMessages, batchMessageIndexes, eachResult.failures);
        }
        int processedMessageCount = getProcessedMessageCount(messageUpdateCounts, failedMessages);
        return new FirebirdBatchCompletion(processedMessageCount, toFirebirdUpdateCounts(messageUpdateCounts, processedMessageCount), createFailures(failedMessages, processedMessageCount));
    }
    
    private void markFailedMessages(final int[] messageUpdateCounts, final Map<Integer, SQLException> failedMessages,
                                    final List<Integer> batchMessageIndexes, final Map<Integer, SQLException> failedOffsets) {
        for (Entry<Integer, SQLException> each : failedOffsets.entrySet()) {
            markFailedMessage(messageUpdateCounts, failedMessages, getBatchMessageIndex(batchMessageIndexes, each.getKey()), each.getValue());
        }
    }
    
    private int getBatchMessageIndex(final List<Integer> batchMessageIndexes, final int offset) {
        if (batchMessageIndexes.isEmpty()) {
            return 0;
        }
        return offset < batchMessageIndexes.size() ? batchMessageIndexes.get(offset) : batchMessageIndexes.get(batchMessageIndexes.size() - 1);
    }
    
    private void markFailedMessage(final int[] messageUpdateCounts, final Map<Integer, SQLException> failedMessages, final int messageIndex, final SQLException failureCause) {
        messageUpdateCounts[messageIndex] = Statement.EXECUTE_FAILED;
        failedMessages.putIfAbsent(messageIndex, failureCause);
    }
    
    private int getProcessedMessageCount(final int[] messageUpdateCounts, final NavigableMap<Integer, SQLException> failedMessages) throws SQLException {
        int notExecutedMessageIndex = batchMessageCount;
        for (int i = 0; i < batchMessageCount; i++) {
            if (NOT_EXECUTED == messageUpdateCounts[i]) {
                notExecutedMessageIndex = i;
                break;
            }
        }
        if (failedMessages.isEmpty()) {
            return notExecutedMessageIndex;
        }
        Entry<Integer, SQLException> firstFailedMessage = failedMessages.firstEntry();
        if (notExecutedMessageIndex <= firstFailedMessage.getKey()) {
            throw firstFailedMessage.getValue();
        }
        return multiError ? notExecutedMessageIndex : Math.min(notExecutedMessageIndex, firstFailedMessage.getKey() + 1);
    }
    
    private Collection<FirebirdBatchCompletion.Failure> createFailures(final Map<Integer, SQLException> failedMessages, final int processedMessageCount) {
        Collection<FirebirdBatchCompletion.Failure> result = new LinkedList<>();
        for (Entry<Integer, SQLException> each : failedMessages.entrySet()) {
            if (each.getKey() < processedMessageCount) {
                result.add(new FirebirdBatchCompletion.Failure(each.getKey(), each.getValue()));
            }
        }
        return result;
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
    
    private List<BatchExecution> createBatchExecutionPlan() throws SQLException {
        List<JDBCExecutionUnit> executionUnits = new LinkedList<>();
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            executionUnits.addAll(eachGroup.getInputs());
        }
        if (!isHaltedOnFirstFailure()) {
            return createWholeUnitBatchExecutions(executionUnits);
        }
        return createBatchExecutionsInMessageOrder(executionUnits);
    }
    
    private List<BatchExecution> createWholeUnitBatchExecutions(final Collection<JDBCExecutionUnit> executionUnits) throws SQLException {
        List<BatchExecution> result = new ArrayList<>(executionUnits.size());
        for (JDBCExecutionUnit each : executionUnits) {
            BatchExecution batchExecution = new BatchExecution(each, 0, executionUnitParams.getOrDefault(each.getExecutionUnit(), Collections.emptyList()).size(), false);
            addPendingBatch(batchExecution);
            result.add(batchExecution);
        }
        return result;
    }
    
    private List<BatchExecution> createBatchExecutionsInMessageOrder(final Collection<JDBCExecutionUnit> executionUnits) {
        Map<Integer, Collection<JDBCExecutionUnit>> unitsByBatchMessageIndex = new TreeMap<>();
        for (JDBCExecutionUnit each : executionUnits) {
            for (int eachMessageIndex : executionUnitBatchMessageIndexes.getOrDefault(each.getExecutionUnit(), Collections.<Integer>emptyList())) {
                unitsByBatchMessageIndex.computeIfAbsent(eachMessageIndex, key -> new LinkedList<>()).add(each);
            }
        }
        List<BatchExecution> result = new ArrayList<>(batchMessageCount);
        Map<JDBCExecutionUnit, Integer> consumedOffsets = new IdentityHashMap<>();
        for (Entry<Integer, Collection<JDBCExecutionUnit>> entry : unitsByBatchMessageIndex.entrySet()) {
            boolean isSharedMessage = 1 < entry.getValue().size();
            for (JDBCExecutionUnit each : entry.getValue()) {
                appendBatchExecution(result, each, consumedOffsets, isSharedMessage);
            }
        }
        return result;
    }
    
    private void appendBatchExecution(final List<BatchExecution> batchExecutions, final JDBCExecutionUnit executionUnit,
                                      final Map<JDBCExecutionUnit, Integer> consumedOffsets, final boolean isSharedMessage) {
        int offset = consumedOffsets.merge(executionUnit, 1, Integer::sum) - 1;
        BatchExecution last = batchExecutions.isEmpty() ? null : batchExecutions.get(batchExecutions.size() - 1);
        if (null != last && !last.sharedMessage && !isSharedMessage && last.executionUnit == executionUnit) {
            last.toOffset = offset + 1;
            return;
        }
        batchExecutions.add(new BatchExecution(executionUnit, offset, offset + 1, isSharedMessage));
    }
    
    private boolean isHaltedOnFirstFailure() {
        return !multiError && 1 < executionUnitParams.size();
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
        if (NOT_EXECUTED == current) {
            return updateCount;
        }
        if (Statement.EXECUTE_FAILED == updateCount || Statement.EXECUTE_FAILED == current) {
            return Statement.EXECUTE_FAILED;
        }
        if (Statement.SUCCESS_NO_INFO == updateCount || Statement.SUCCESS_NO_INFO == current) {
            return Statement.SUCCESS_NO_INFO;
        }
        return current + updateCount;
    }
    
    private final class BatchedStatementsJDBCExecutorCallback extends JDBCExecutorCallback<BatchExecutionUnitResult> {
        
        private BatchedStatementsJDBCExecutorCallback(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement,
                                                      final boolean isExceptionThrown) {
            super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
        }
        
        @Override
        protected BatchExecutionUnitResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
            return executeBatchedStatement((PreparedStatement) statement);
        }
        
        private BatchExecutionUnitResult executeBatchedStatement(final PreparedStatement statement) throws SQLException {
            List<List<Object>> params = pendingBatchParams.getOrDefault(statement, Collections.emptyList());
            List<Integer> updateCounts = new LinkedList<>();
            Map<Integer, SQLException> failures = new TreeMap<>();
            while (true) {
                int executedCount = updateCounts.size();
                try {
                    addAll(updateCounts, statement.executeBatch());
                    return createResult(updateCounts, failures);
                } catch (final SQLException ex) {
                    int[] executedUpdateCounts = getExecutedUpdateCounts(ex);
                    addAll(updateCounts, executedUpdateCounts);
                    registerFailures(failures, ex, updateCounts, executedCount, params.size() - executedCount > executedUpdateCounts.length);
                }
                if (!multiError || updateCounts.size() >= params.size()) {
                    return createResult(updateCounts, failures);
                }
                statement.clearBatch();
                addBatchedParameters(statement, params.subList(updateCounts.size(), params.size()));
            }
        }
        
        private int[] getExecutedUpdateCounts(final SQLException ex) {
            if (!(ex instanceof BatchUpdateException)) {
                return new int[0];
            }
            int[] result = ((BatchUpdateException) ex).getUpdateCounts();
            return null == result ? new int[0] : result;
        }
        
        private void registerFailures(final Map<Integer, SQLException> failures, final SQLException ex, final List<Integer> updateCounts,
                                      final int executedCount, final boolean isHaltedBeforeRemainingMessages) {
            List<Integer> failedOffsets = new LinkedList<>();
            for (int i = executedCount; i < updateCounts.size(); i++) {
                if (Statement.EXECUTE_FAILED == updateCounts.get(i)) {
                    failedOffsets.add(i);
                }
            }
            if (isHaltedBeforeRemainingMessages) {
                failedOffsets.add(updateCounts.size());
                updateCounts.add(Statement.EXECUTE_FAILED);
            }
            if (failedOffsets.isEmpty()) {
                failedOffsets.add(Math.max(executedCount, updateCounts.size() - 1));
            }
            Iterator<SQLException> causes = getFailureCauses(ex, failedOffsets.size()).iterator();
            for (int each : failedOffsets) {
                failures.putIfAbsent(each, causes.next());
            }
        }
        
        private Collection<SQLException> getFailureCauses(final SQLException ex, final int failedCount) {
            Collection<SQLException> chainedCauses = new LinkedList<>();
            for (SQLException each = ex.getNextException(); null != each; each = each.getNextException()) {
                chainedCauses.add(each);
            }
            if (chainedCauses.size() == failedCount) {
                return chainedCauses;
            }
            return Collections.nCopies(failedCount, ex);
        }
        
        private void addAll(final List<Integer> updateCounts, final int[] executedUpdateCounts) {
            for (int each : executedUpdateCounts) {
                updateCounts.add(each);
            }
        }
        
        private BatchExecutionUnitResult createResult(final List<Integer> updateCounts, final Map<Integer, SQLException> failures) {
            int[] result = new int[updateCounts.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = updateCounts.get(i);
            }
            return new BatchExecutionUnitResult(result, failures);
        }
        
        @Override
        protected Optional<BatchExecutionUnitResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
            return Optional.empty();
        }
    }
    
    private static final class BatchExecution {
        
        private final JDBCExecutionUnit executionUnit;
        
        private final int fromOffset;
        
        private int toOffset;
        
        private final boolean sharedMessage;
        
        private BatchExecution(final JDBCExecutionUnit executionUnit, final int fromOffset, final int toOffset, final boolean sharedMessage) {
            this.executionUnit = executionUnit;
            this.fromOffset = fromOffset;
            this.toOffset = toOffset;
            this.sharedMessage = sharedMessage;
        }
    }
    
    @RequiredArgsConstructor
    private static final class BatchExecutionUnitResult {
        
        private final int[] updateCounts;
        
        private final Map<Integer, SQLException> failures;
    }
}
