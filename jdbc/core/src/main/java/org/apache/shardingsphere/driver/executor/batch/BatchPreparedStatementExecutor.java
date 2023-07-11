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

package org.apache.shardingsphere.driver.executor.batch;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Prepared statement executor to process add batch.
 */
public final class BatchPreparedStatementExecutor {
    
    private final MetaDataContexts metaDataContexts;
    
    private final JDBCExecutor jdbcExecutor;
    
    private ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
    
    @Getter
    private final Collection<BatchExecutionUnit> batchExecutionUnits;
    
    private int batchCount;
    
    private final String databaseName;
    
    public BatchPreparedStatementExecutor(final MetaDataContexts metaDataContexts, final JDBCExecutor jdbcExecutor, final String databaseName) {
        this.databaseName = databaseName;
        this.metaDataContexts = metaDataContexts;
        this.jdbcExecutor = jdbcExecutor;
        executionGroupContext = new ExecutionGroupContext<>(new LinkedList<>(), new ExecutionGroupReportContext(databaseName));
        batchExecutionUnits = new LinkedList<>();
    }
    
    /**
     * Initialize executor.
     *
     * @param executionGroupContext execution group context
     */
    public void init(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext) {
        this.executionGroupContext = executionGroupContext;
    }
    
    /**
     * Add batch for execution units.
     *
     * @param executionUnits execution units
     */
    public void addBatchForExecutionUnits(final Collection<ExecutionUnit> executionUnits) {
        Collection<BatchExecutionUnit> batchExecutionUnits = createBatchExecutionUnits(executionUnits);
        handleOldBatchExecutionUnits(batchExecutionUnits);
        handleNewBatchExecutionUnits(batchExecutionUnits);
        batchCount++;
    }
    
    private Collection<BatchExecutionUnit> createBatchExecutionUnits(final Collection<ExecutionUnit> executionUnits) {
        List<BatchExecutionUnit> result = new ArrayList<>(executionUnits.size());
        for (ExecutionUnit each : executionUnits) {
            BatchExecutionUnit batchExecutionUnit = new BatchExecutionUnit(each);
            result.add(batchExecutionUnit);
        }
        return result;
    }
    
    private void handleOldBatchExecutionUnits(final Collection<BatchExecutionUnit> newExecutionUnits) {
        newExecutionUnits.forEach(this::reviseBatchExecutionUnits);
    }
    
    private void reviseBatchExecutionUnits(final BatchExecutionUnit batchExecutionUnit) {
        for (BatchExecutionUnit each : batchExecutionUnits) {
            if (each.equals(batchExecutionUnit)) {
                reviseBatchExecutionUnit(each, batchExecutionUnit);
            }
        }
    }
    
    private void reviseBatchExecutionUnit(final BatchExecutionUnit oldBatchExecutionUnit, final BatchExecutionUnit newBatchExecutionUnit) {
        oldBatchExecutionUnit.getExecutionUnit().getSqlUnit().getParameters().addAll(newBatchExecutionUnit.getExecutionUnit().getSqlUnit().getParameters());
        oldBatchExecutionUnit.mapAddBatchCount(batchCount);
    }
    
    private void handleNewBatchExecutionUnits(final Collection<BatchExecutionUnit> newExecutionUnits) {
        newExecutionUnits.removeAll(batchExecutionUnits);
        for (BatchExecutionUnit each : newExecutionUnits) {
            each.mapAddBatchCount(batchCount);
        }
        batchExecutionUnits.addAll(newExecutionUnits);
    }
    
    /**
     * Execute batch.
     *
     * @param sqlStatementContext SQL statement context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public int[] executeBatch(final SQLStatementContext sqlStatementContext) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        JDBCExecutorCallback<int[]> callback = new JDBCExecutorCallback<int[]>(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatementContext.getSqlStatement(), isExceptionThrown) {
            
            @Override
            protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return statement.executeBatch();
            }
            
            @SuppressWarnings("OptionalContainsCollection")
            @Override
            protected Optional<int[]> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
        List<int[]> results = jdbcExecutor.execute(executionGroupContext, callback);
        if (results.isEmpty()) {
            return new int[0];
        }
        return isNeedAccumulate(sqlStatementContext) ? accumulate(results) : results.get(0);
    }
    
    private boolean isNeedAccumulate(final SQLStatementContext sqlStatementContext) {
        for (ShardingSphereRule each : metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    private int[] accumulate(final List<int[]> executeResults) {
        int[] result = new int[batchCount];
        int count = 0;
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit eachUnit : each.getInputs()) {
                accumulate(executeResults.get(count), result, eachUnit);
                count++;
            }
        }
        return result;
    }
    
    private void accumulate(final int[] executeResult, final int[] addBatchCounts, final JDBCExecutionUnit executionUnit) {
        for (Entry<Integer, Integer> entry : getJDBCAndActualAddBatchCallTimesMap(executionUnit).entrySet()) {
            int value = null == executeResult ? 0 : executeResult[entry.getValue()];
            addBatchCounts[entry.getKey()] += value;
        }
    }
    
    private Map<Integer, Integer> getJDBCAndActualAddBatchCallTimesMap(final JDBCExecutionUnit executionUnit) {
        for (BatchExecutionUnit each : batchExecutionUnits) {
            if (isSameDataSourceAndSQL(each, executionUnit)) {
                return each.getJdbcAndActualAddBatchCallTimesMap();
            }
        }
        return Collections.emptyMap();
    }
    
    private boolean isSameDataSourceAndSQL(final BatchExecutionUnit batchExecutionUnit, final JDBCExecutionUnit jdbcExecutionUnit) {
        return batchExecutionUnit.getExecutionUnit().getDataSourceName().equals(jdbcExecutionUnit.getExecutionUnit().getDataSourceName())
                && batchExecutionUnit.getExecutionUnit().getSqlUnit().getSql().equals(jdbcExecutionUnit.getExecutionUnit().getSqlUnit().getSql());
    }
    
    /**
     * Get statements.
     *
     * @return statements
     */
    public List<Statement> getStatements() {
        List<Statement> result = new LinkedList<>();
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit eachUnit : eachGroup.getInputs()) {
                Statement storageResource = eachUnit.getStorageResource();
                result.add(storageResource);
            }
        }
        return result;
    }
    
    /**
     * Get parameter sets.
     *
     * @param statement statement
     * @return parameter sets
     */
    public List<List<Object>> getParameterSet(final Statement statement) {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            Optional<JDBCExecutionUnit> result = findJDBCExecutionUnit(statement, each);
            if (result.isPresent()) {
                return getParameterSets(result.get());
            }
        }
        return Collections.emptyList();
    }
    
    private Optional<JDBCExecutionUnit> findJDBCExecutionUnit(final Statement statement, final ExecutionGroup<JDBCExecutionUnit> executionGroup) {
        for (JDBCExecutionUnit each : executionGroup.getInputs()) {
            if (each.getStorageResource().equals(statement)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private List<List<Object>> getParameterSets(final JDBCExecutionUnit executionUnit) {
        for (BatchExecutionUnit each : batchExecutionUnits) {
            if (isSameDataSourceAndSQL(each, executionUnit)) {
                return each.getParameterSets();
            }
        }
        throw new IllegalStateException();
    }
    
    /**
     * Clear.
     */
    public void clear() {
        getStatements().clear();
        executionGroupContext.getInputGroups().clear();
        batchCount = 0;
        batchExecutionUnits.clear();
    }
}
