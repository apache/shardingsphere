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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.sharding.execute.context.ShardingExecutionContext;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.sharding.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.sharding.execute.sql.prepare.SQLExecutePrepareCallback;
import org.apache.shardingsphere.shardingjdbc.executor.AbstractStatementExecutor;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.engine.InputGroup;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepared statement executor to process add batch.
 * 
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class BatchPreparedStatementExecutor extends AbstractStatementExecutor {
    
    private final Collection<BatchRouteUnit> routeUnits = new LinkedList<>();
    
    @Getter
    private final boolean returnGeneratedKeys;
    
    private int batchCount;
    
    public BatchPreparedStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys,
                                          final ShardingConnection shardingConnection) {
        super(resultSetType, resultSetConcurrency, resultSetHoldability, shardingConnection);
        this.returnGeneratedKeys = returnGeneratedKeys;
    }
    
    /**
     * Initialize executor.
     *
     * @param sqlStatementContext SQL statement context
     * @throws SQLException SQL exception
     */
    public void init(final SQLStatementContext sqlStatementContext) throws SQLException {
        setSqlStatementContext(sqlStatementContext);
        getInputGroups().addAll(obtainExecuteGroups(routeUnits));
    }
    
    private Collection<InputGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<BatchRouteUnit> batchRouteUnits) throws SQLException {
        return getSqlExecutePrepareTemplate().getExecuteUnitGroups(Lists.transform(new ArrayList<>(batchRouteUnits), new Function<BatchRouteUnit, ExecutionUnit>() {
    
            @Override
            public ExecutionUnit apply(final BatchRouteUnit input) {
                return input.getExecutionUnit();
            }
        }), new SQLExecutePrepareCallback() {
            
            @Override
            public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
                return BatchPreparedStatementExecutor.super.getConnection().getConnections(connectionMode, dataSourceName, connectionSize);
            }
            
            @Override
            public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final ExecutionUnit executionUnit, final ConnectionMode connectionMode) throws SQLException {
                return new StatementExecuteUnit(executionUnit, createPreparedStatement(connection, executionUnit.getSqlUnit().getSql()), connectionMode);
            }
        });
    }
    
    @SuppressWarnings("MagicConstant")
    private PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        return returnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : connection.prepareStatement(sql, getResultSetType(), getResultSetConcurrency(), getResultSetHoldability());
    }
    
    /**
     * Add batch for route units.
     *
     * @param shardingExecutionContext sharding execution context
     */
    public void addBatchForRouteUnits(final ShardingExecutionContext shardingExecutionContext) {
        handleOldBatchRouteUnits(createBatchRouteUnits(shardingExecutionContext.getExecutionUnits()));
        handleNewBatchRouteUnits(createBatchRouteUnits(shardingExecutionContext.getExecutionUnits()));
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
        for (final BatchRouteUnit each : newRouteUnits) {
            Optional<BatchRouteUnit> batchRouteUnitOptional = Iterators.tryFind(routeUnits.iterator(), new Predicate<BatchRouteUnit>() {
                
                @Override
                public boolean apply(final BatchRouteUnit input) {
                    return input.equals(each);
                }
            });
            if (batchRouteUnitOptional.isPresent()) {
                reviseBatchRouteUnit(batchRouteUnitOptional.get(), each);
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
     * @return execute results
     * @throws SQLException SQL exception
     */
    public int[] executeBatch() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecuteCallback<int[]> callback = new SQLExecuteCallback<int[]>(getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return statement.executeBatch();
            }
        };
        List<int[]> results = executeCallback(callback);
        if (isAccumulate()) {
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
                    if (eachRouteUnit.getExecutionUnit().getDataSourceName().equals(eachUnit.getExecutionUnit().getDataSourceName())
                            && eachRouteUnit.getExecutionUnit().getSqlUnit().getSql().equals(eachUnit.getExecutionUnit().getSqlUnit().getSql())) {
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
    
    /**
     * Get statements.
     *
     * @return statements
     */
    @Override
    public List<Statement> getStatements() {
        List<Statement> result = new LinkedList<>();
        for (InputGroup<StatementExecuteUnit> each : getInputGroups()) {
            result.addAll(Lists.transform(each.getInputs(), new Function<StatementExecuteUnit, Statement>() {
                
                @Override
                public Statement apply(final StatementExecuteUnit input) {
                    return input.getStatement();
                }
            }));
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
        return Iterators.tryFind(executeGroup.getInputs().iterator(), new Predicate<StatementExecuteUnit>() {
            
            @Override
            public boolean apply(final StatementExecuteUnit input) {
                return input.getStatement().equals(statement);
                }
        });
    }
    
    private List<List<Object>> getParameterSets(final StatementExecuteUnit executeUnit) {
        List<List<Object>> result;
        result = Collections2.filter(routeUnits, new Predicate<BatchRouteUnit>() {

            @Override
            public boolean apply(final BatchRouteUnit input) {
                return input.getExecutionUnit().getDataSourceName().equals(executeUnit.getExecutionUnit().getDataSourceName())
                        && input.getExecutionUnit().getSqlUnit().getSql().equals(executeUnit.getExecutionUnit().getSqlUnit().getSql());
            }
        }).iterator().next().getParameterSets();
        return result;
    }
    
    @Override
    public void clear() throws SQLException {
        super.clear();
        batchCount = 0;
        routeUnits.clear();
    }
}


