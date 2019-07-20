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

import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.StatementExecuteUnit;
import org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.execute.sql.execute.result.MemoryQueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.execute.sql.prepare.SQLExecutePrepareCallback;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * Statement executor.
 * 
 * @author gaohongtao
 * @author caohao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class StatementExecutor extends AbstractStatementExecutor {
    
    public StatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        super(resultSetType, resultSetConcurrency, resultSetHoldability, shardingConnection);
    }
    
    /**
     * Initialize executor.
     *
     * @param routeResult route result
     * @throws SQLException SQL exception
     */
    public void init(final SQLRouteResult routeResult) throws SQLException {
        setOptimizedStatement(routeResult.getOptimizedStatement());
        getExecuteGroups().addAll(obtainExecuteGroups(routeResult.getRouteUnits()));
        cacheStatements();
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<RouteUnit> routeUnits) throws SQLException {
        return getSqlExecutePrepareTemplate().getExecuteUnitGroups(routeUnits, new SQLExecutePrepareCallback() {
            
            @Override
            public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
                return StatementExecutor.super.getConnection().getConnections(connectionMode, dataSourceName, connectionSize);
            }
    
            @SuppressWarnings("MagicConstant")
            @Override
            public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
                return new StatementExecuteUnit(routeUnit, connection.createStatement(getResultSetType(), getResultSetConcurrency(), getResultSetHoldability()), connectionMode);
            }
        });
    }
    
    /**
     * Execute query.
     * 
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecuteCallback<QueryResult> executeCallback = new SQLExecuteCallback<QueryResult>(getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected QueryResult executeSQL(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return getQueryResult(routeUnit, statement, connectionMode);
            }
        };
        return executeCallback(executeCallback);
    }
    
    private QueryResult getQueryResult(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        ResultSet resultSet = statement.executeQuery(routeUnit.getSqlUnit().getSql());
        ShardingRule shardingRule = getConnection().getShardingContext().getShardingRule();
        getResultSets().add(resultSet);
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet, shardingRule) 
                : new MemoryQueryResult(resultSet, shardingRule);
    }
    
    /**
     * Execute update.
     * 
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate() throws SQLException {
        return executeUpdate(new Updater() {
            
            @Override
            public int executeUpdate(final Statement statement, final String sql) throws SQLException {
                return statement.executeUpdate(sql);
            }
        });
    }
    
    /**
     * Execute update with auto generated keys.
     * 
     * @param autoGeneratedKeys auto generated keys' flag
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final int autoGeneratedKeys) throws SQLException {
        return executeUpdate(new Updater() {
            
            @Override
            public int executeUpdate(final Statement statement, final String sql) throws SQLException {
                return statement.executeUpdate(sql, autoGeneratedKeys);
            }
        });
    }
    
    /**
     * Execute update with column indexes.
     *
     * @param columnIndexes column indexes
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final int[] columnIndexes) throws SQLException {
        return executeUpdate(new Updater() {
            
            @Override
            public int executeUpdate(final Statement statement, final String sql) throws SQLException {
                return statement.executeUpdate(sql, columnIndexes);
            }
        });
    }
    
    /**
     * Execute update with column names.
     *
     * @param columnNames column names
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final String[] columnNames) throws SQLException {
        return executeUpdate(new Updater() {
            
            @Override
            public int executeUpdate(final Statement statement, final String sql) throws SQLException {
                return statement.executeUpdate(sql, columnNames);
            }
        });
    }
    
    private int executeUpdate(final Updater updater) throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return updater.executeUpdate(statement, routeUnit.getSqlUnit().getSql());
            }
        };
        List<Integer> results = executeCallback(executeCallback);
        if (isAccumulate()) {
            return accumulate(results);
        } else {
            return null == results.get(0) ? 0 : results.get(0);
        }
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute() throws SQLException {
        return execute(new Executor() {
            
            @Override
            public boolean execute(final Statement statement, final String sql) throws SQLException {
                return statement.execute(sql);
            }
        });
    }
    
    /**
     * Execute SQL with auto generated keys.
     *
     * @param autoGeneratedKeys auto generated keys' flag
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final int autoGeneratedKeys) throws SQLException {
        return execute(new Executor() {
            
            @Override
            public boolean execute(final Statement statement, final String sql) throws SQLException {
                return statement.execute(sql, autoGeneratedKeys);
            }
        });
    }
    
    /**
     * Execute SQL with column indexes.
     *
     * @param columnIndexes column indexes
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final int[] columnIndexes) throws SQLException {
        return execute(new Executor() {
            
            @Override
            public boolean execute(final Statement statement, final String sql) throws SQLException {
                return statement.execute(sql, columnIndexes);
            }
        });
    }
    
    /**
     * Execute SQL with column names.
     *
     * @param columnNames column names
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final String[] columnNames) throws SQLException {
        return execute(new Executor() {
            
            @Override
            public boolean execute(final Statement statement, final String sql) throws SQLException {
                return statement.execute(sql, columnNames);
            }
        });
    }
    
    private boolean execute(final Executor executor) throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecuteCallback<Boolean> executeCallback = new SQLExecuteCallback<Boolean>(getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return executor.execute(statement, routeUnit.getSqlUnit().getSql());
            }
        };
        List<Boolean> result = executeCallback(executeCallback);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    private interface Updater {
        
        int executeUpdate(Statement statement, String sql) throws SQLException;
    }
    
    private interface Executor {
        
        boolean execute(Statement statement, String sql) throws SQLException;
    }
}

