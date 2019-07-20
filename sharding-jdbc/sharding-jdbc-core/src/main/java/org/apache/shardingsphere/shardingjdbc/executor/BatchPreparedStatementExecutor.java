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

package org.apache.shardingsphere.shardingjdbc.executor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.StatementExecuteUnit;
import org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.execute.sql.prepare.SQLExecutePrepareCallback;
import org.apache.shardingsphere.core.route.BatchRouteUnit;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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
     * @param routeResult route result
     * @throws SQLException SQL exception
     */
    public void init(final SQLRouteResult routeResult) throws SQLException {
        setOptimizedStatement(routeResult.getOptimizedStatement());
        getExecuteGroups().addAll(obtainExecuteGroups(routeUnits));
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<BatchRouteUnit> routeUnits) throws SQLException {
        return getSqlExecutePrepareTemplate().getExecuteUnitGroups(Lists.transform(new ArrayList<>(routeUnits), new Function<BatchRouteUnit, RouteUnit>() {
    
            @Override
            public RouteUnit apply(final BatchRouteUnit input) {
                return input.getRouteUnit();
            }
        }), new SQLExecutePrepareCallback() {
            
            @Override
            public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
                return BatchPreparedStatementExecutor.super.getConnection().getConnections(connectionMode, dataSourceName, connectionSize);
            }
            
            @Override
            public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
                return new StatementExecuteUnit(routeUnit, createPreparedStatement(connection, routeUnit.getSqlUnit().getSql()), connectionMode);
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
     * @param routeResult route result
     */
    public void addBatchForRouteUnits(final SQLRouteResult routeResult) {
        handleOldRouteUnits(createBatchRouteUnits(routeResult.getRouteUnits()));
        handleNewRouteUnits(createBatchRouteUnits(routeResult.getRouteUnits()));
        batchCount++;
    }
    
    private Collection<BatchRouteUnit> createBatchRouteUnits(final Collection<RouteUnit> routeUnits) {
        Collection<BatchRouteUnit> result = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            result.add(new BatchRouteUnit(each));
        }
        return result;
    }
    
    private void handleOldRouteUnits(final Collection<BatchRouteUnit> newRouteUnits) {
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
        oldBatchRouteUnit.getRouteUnit().getSqlUnit().getParameters().addAll(newBatchRouteUnit.getRouteUnit().getSqlUnit().getParameters());
        oldBatchRouteUnit.mapAddBatchCount(batchCount);
    }
    
    private void handleNewRouteUnits(final Collection<BatchRouteUnit> newRouteUnits) {
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
            protected int[] executeSQL(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
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
        for (ShardingExecuteGroup<StatementExecuteUnit> each : getExecuteGroups()) {
            for (StatementExecuteUnit eachUnit : each.getInputs()) {
                Map<Integer, Integer> jdbcAndActualAddBatchCallTimesMap = null;
                for (BatchRouteUnit eachRouteUnit : routeUnits) {
                    if (eachRouteUnit.getRouteUnit().equals(eachUnit.getRouteUnit())) {
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
        for (ShardingExecuteGroup<StatementExecuteUnit> each : getExecuteGroups()) {
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
        for (ShardingExecuteGroup<StatementExecuteUnit> each : getExecuteGroups()) {
            Optional<StatementExecuteUnit> target = getStatementExecuteUnit(statement, each);
            if (target.isPresent()) {
                result = getParameterSets(target.get());
                break;
            }
        }
        return result;
    }
    
    private Optional<StatementExecuteUnit> getStatementExecuteUnit(final Statement statement, final ShardingExecuteGroup<StatementExecuteUnit> executeGroup) {
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
                return input.getRouteUnit().equals(executeUnit.getRouteUnit());
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


