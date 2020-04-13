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

package org.apache.shardingsphere.shardingjdbc.executor.batch;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecutorCallback;
import org.apache.shardingsphere.sharding.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.shardingjdbc.executor.AbstractStatementExecutor;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.executor.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.group.PreparedStatementExecuteGroupEngine;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

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
import java.util.stream.Collectors;

/**
 * Prepared statement executor to process add batch.
 */
public final class BatchPreparedStatementExecutor extends AbstractStatementExecutor {
    
    private final PreparedStatementExecuteGroupEngine executeGroupEngine;
    
    private final Collection<BatchRouteUnit> routeUnits = new LinkedList<>();
    
    private int batchCount;
    
    public BatchPreparedStatementExecutor(final ShardingConnection shardingConnection, final boolean serial) {
        super(shardingConnection, serial);
        int maxConnectionsSizePerQuery = shardingConnection.getRuntimeContext().getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        executeGroupEngine = new PreparedStatementExecuteGroupEngine(maxConnectionsSizePerQuery);
    }
    
    @Override
    public void init(final ExecutionContext executionContext, final StatementOption statementOption) throws SQLException {
        getInputGroups().addAll(generateExecuteGroups(routeUnits, statementOption));
    }
    
    private Collection<InputGroup<StatementExecuteUnit>> generateExecuteGroups(final Collection<BatchRouteUnit> batchRouteUnits, final StatementOption statementOption) throws SQLException {
        return executeGroupEngine.generate(new ArrayList<>(batchRouteUnits).stream().map(BatchRouteUnit::getExecutionUnit).collect(Collectors.toList()), getConnection(), statementOption);
    }
    
    /**
     * Add batch for route units.
     *
     * @param executionContext execution context
     */
    public void addBatchForRouteUnits(final ExecutionContext executionContext) {
        handleOldBatchRouteUnits(createBatchRouteUnits(executionContext.getExecutionUnits()));
        handleNewBatchRouteUnits(createBatchRouteUnits(executionContext.getExecutionUnits()));
        batchCount++;
    }
    
    private Collection<BatchRouteUnit> createBatchRouteUnits(final Collection<ExecutionUnit> executionUnits) {
        Collection<BatchRouteUnit> result = new LinkedList<>();
        for (ExecutionUnit each : executionUnits) {
            result.add(new BatchRouteUnit(each));
        }
        return result;
    }
    
    private void handleOldBatchRouteUnits(final Collection<BatchRouteUnit> newRouteUnits) {
        for (BatchRouteUnit each : newRouteUnits) {
            for (BatchRouteUnit unit : routeUnits) {
                if (unit.equals(each)) {
                    reviseBatchRouteUnit(unit, each);
                }
            }
        }
    }
    
    private void reviseBatchRouteUnit(final BatchRouteUnit oldBatchRouteUnit, final BatchRouteUnit newBatchRouteUnit) {
        oldBatchRouteUnit.getExecutionUnit().getSqlUnit().getParameters().addAll(newBatchRouteUnit.getExecutionUnit().getSqlUnit().getParameters());
        oldBatchRouteUnit.mapAddBatchCount(batchCount);
    }
    
    private void handleNewBatchRouteUnits(final Collection<BatchRouteUnit> newRouteUnits) {
        newRouteUnits.removeAll(routeUnits);
        for (BatchRouteUnit each : newRouteUnits) {
            each.mapAddBatchCount(batchCount);
        }
        routeUnits.addAll(newRouteUnits);
    }
    
    /**
     * Execute batch.
     * 
     * @param sqlStatementContext SQL statement context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public int[] executeBatch(final SQLStatementContext sqlStatementContext) throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<int[]> callback = new SQLExecutorCallback<int[]>(getConnection().getRuntimeContext().getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return statement.executeBatch();
            }
        };
        List<int[]> results = executeCallback(callback);
        if (isAccumulate(sqlStatementContext)) {
            return accumulate(results);
        } else {
            return results.get(0);
        }
    }
    
    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[batchCount];
        int count = 0;
        for (InputGroup<StatementExecuteUnit> each : getInputGroups()) {
            for (StatementExecuteUnit eachUnit : each.getInputs()) {
                Map<Integer, Integer> jdbcAndActualAddBatchCallTimesMap = Collections.emptyMap();
                for (BatchRouteUnit eachRouteUnit : routeUnits) {
                    if (isSameDataSourceAndSQL(eachRouteUnit, eachUnit)) {
                        jdbcAndActualAddBatchCallTimesMap = eachRouteUnit.getJdbcAndActualAddBatchCallTimesMap();
                        break;
                    }
                }
                for (Entry<Integer, Integer> entry : jdbcAndActualAddBatchCallTimesMap.entrySet()) {
                    int value = null == results.get(count) ? 0 : results.get(count)[entry.getValue()];
                    result[entry.getKey()] += value;
                }
                count++;
            }
        }
        return result;
    }
    
    private boolean isSameDataSourceAndSQL(final BatchRouteUnit batchRouteUnit, final StatementExecuteUnit statementExecuteUnit) {
        return batchRouteUnit.getExecutionUnit().getDataSourceName().equals(statementExecuteUnit.getExecutionUnit().getDataSourceName())
                && batchRouteUnit.getExecutionUnit().getSqlUnit().getSql().equals(statementExecuteUnit.getExecutionUnit().getSqlUnit().getSql());
    }
    
    /**
     * Get statements.
     *
     * @return statements
     */
    @Override
    public List<Statement> getStatements() {
        List<Statement> result = new LinkedList<>();
        for (InputGroup<StatementExecuteUnit> each : getInputGroups()) {
            result.addAll(each.getInputs().stream().map(StatementExecuteUnit::getStatement).collect(Collectors.toList()));
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
        List<List<Object>> result = new LinkedList<>();
        for (InputGroup<StatementExecuteUnit> each : getInputGroups()) {
            Optional<StatementExecuteUnit> target = getStatementExecuteUnit(statement, each);
            if (target.isPresent()) {
                result = getParameterSets(target.get());
                break;
            }
        }
        return result;
    }
    
    private Optional<StatementExecuteUnit> getStatementExecuteUnit(final Statement statement, final InputGroup<StatementExecuteUnit> executeGroup) {
        for (StatementExecuteUnit each : executeGroup.getInputs()) {
            if (each.getStatement().equals(statement)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private List<List<Object>> getParameterSets(final StatementExecuteUnit executeUnit) {
        Optional<BatchRouteUnit> batchRouteUnit = routeUnits.stream().filter(routeUnit -> isSameDataSourceAndSQL(routeUnit, executeUnit)).findFirst();
        Preconditions.checkState(batchRouteUnit.isPresent());
        return batchRouteUnit.get().getParameterSets();
    }
    
    @Override
    public void clear() throws SQLException {
        super.clear();
        batchCount = 0;
        routeUnits.clear();
    }
}


