/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.routing.BatchRouteUnit;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public final class BatchPreparedStatementExecutor {
    
    private final DatabaseType dbType;
    
    private SQLType sqlType;
    
    private int batchCount;
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final boolean returnGeneratedKeys;
    
    private final ShardingConnection connection;
    
    private final Collection<BatchRouteUnit> routeUnits = new LinkedList<>();
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    @Getter
    private final List<ResultSet> resultSets = new LinkedList<>();
    
    private final Collection<Connection> connections = new LinkedList<>();
    
    private final Collection<ShardingExecuteGroup<SQLExecuteUnit>> executeGroups = new LinkedList<>();
    
    public BatchPreparedStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys,
                                          final ShardingConnection shardingConnection) {
        this.dbType = shardingConnection.getShardingDataSource().getShardingContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.returnGeneratedKeys = returnGeneratedKeys;
        this.connection = shardingConnection;
        sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
    }
    
    /**
     * Init executor.
     *
     * @exception SQLException sql exception
     */
    public void init() throws SQLException {
        executeGroups.addAll(obtainExecuteGroups(routeUnits));
    }
    
    private Collection<ShardingExecuteGroup<SQLExecuteUnit>> obtainExecuteGroups(final Collection<BatchRouteUnit> routeUnits) throws SQLException {
        return sqlExecutePrepareTemplate.getExecuteUnitGroups(Lists.transform(new ArrayList<>(routeUnits), new Function<BatchRouteUnit, RouteUnit>() {
    
            @Override
            public RouteUnit apply(final BatchRouteUnit input) {
                return input.getRouteUnit();
            }
        }), new SQLExecutePrepareCallback() {
            
            @Override
            public Connection getConnection(final String dataSourceName) throws SQLException {
                Connection conn = connection.getNewConnection(dataSourceName);
                connections.add(conn);
                return conn;
            }
            
            @Override
            public SQLExecuteUnit createSQLExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
                PreparedStatement preparedStatement = createPreparedStatement(connection, routeUnit.getSqlUnit().getSql());
                return new StatementExecuteUnit(routeUnit, preparedStatement, connectionMode);
            }
        });
    }
    
    private PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        return returnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    /**
     * Add batch for route units.
     *
     * @param batchCount batch count
     * @param routeResult route result
     * @throws SQLException sql exception
     */
    public void addBatchForRouteUnits(final int batchCount, final SQLRouteResult routeResult) throws SQLException {
        this.batchCount = batchCount;
        sqlType = routeResult.getSqlStatement().getType();
        handleOldRouteUnits(createBatchRouteUnits(routeResult.getRouteUnits()));
        handleNewRouteUnits(createBatchRouteUnits(routeResult.getRouteUnits()));
    }
    
    private Collection<BatchRouteUnit> createBatchRouteUnits(final Collection<RouteUnit> routeUnits) {
        Collection<BatchRouteUnit> result = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            result.add(new BatchRouteUnit(each));
        }
        return result;
    }
    
    private void handleOldRouteUnits(final Collection<BatchRouteUnit> oldRouteUnits) {
        oldRouteUnits.retainAll(routeUnits);
        for (final BatchRouteUnit each : oldRouteUnits) {
            reviseBatchRouteUnit(each);
        }
    }
    
    private void reviseBatchRouteUnit(final BatchRouteUnit batchRouteUnit) {
        Optional<BatchRouteUnit> batchRouteUnitOptional = Iterators.tryFind(routeUnits.iterator(), new Predicate<BatchRouteUnit>() {
            
            @Override
            public boolean apply(final BatchRouteUnit input) {
                return input.equals(batchRouteUnit);
            }
        });
        if (batchRouteUnitOptional.isPresent()) {
            batchRouteUnitOptional.get().getRouteUnit().getSqlUnit().getParameterSets().add(batchRouteUnit.getRouteUnit().getSqlUnit().getParameterSets().get(0));
            batchRouteUnitOptional.get().mapAddBatchCount(batchCount);
        }
    }
    
    private void handleNewRouteUnits(final Collection<BatchRouteUnit> newRouteUnits) throws SQLException {
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
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<int[]> callback = new SQLExecuteCallback<int[]>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected int[] executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
                return sqlExecuteUnit.getStatement().executeBatch();
            }
        };
        return accumulate(executeCallback(callback));
    }
    
    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[batchCount];
        int count = 0;
        for (BatchRouteUnit each : routeUnits) {
            for (Entry<Integer, Integer> entry : each.getJdbcAndActualAddBatchCallTimesMap().entrySet()) {
                int value = null == results.get(count) ? 0 : results.get(count)[entry.getValue()];
                if (DatabaseType.Oracle == dbType) {
                    result[entry.getKey()] = value;
                } else {
                    result[entry.getKey()] += value;
                }
            }
            count++;
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return sqlExecuteTemplate.executeGroup((Collection) executeGroups, executeCallback);
    }
    
    /**
     * Get statements.
     *
     * @return statements
     */
    public List<Statement> getStatements() {
        List<Statement> result = new LinkedList<>();
        for (ShardingExecuteGroup<SQLExecuteUnit> each : executeGroups) {
            result.addAll(Lists.transform(each.getInputs(), new Function<SQLExecuteUnit, Statement>() {
                
                @Override
                public Statement apply(final SQLExecuteUnit input) {
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
        Optional<SQLExecuteUnit> target = Optional.absent();
        List<List<Object>> result = new LinkedList<>();
        for (ShardingExecuteGroup<SQLExecuteUnit> each : executeGroups) {
            target = Iterators.tryFind(each.getInputs().iterator(), new Predicate<SQLExecuteUnit>() {
                @Override
                public boolean apply(final SQLExecuteUnit input) {
                    return input.getStatement().equals(statement);
                }
            });
        }
        if (target.isPresent()) {
            result.addAll(target.get().getRouteUnit().getSqlUnit().getParameterSets());
        }
        return result;
    }
    
    /**
     * Clear data.
     *
     * @throws SQLException sql exception
     */
    public void clear() throws SQLException {
        for (Statement each : getStatements()) {
            each.close();
        }
        routeUnits.clear();
        resultSets.clear();
        executeGroups.clear();
    }
}


