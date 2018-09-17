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

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Statement executor.
 * 
 * @author gaohongtao
 * @author caohao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
public final class StatementExecutor {
    
    private final DatabaseType databaseType;
    
    @Getter(AccessLevel.NONE)
    private SQLType sqlType;
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final ShardingConnection connection;
    
    @Getter(AccessLevel.NONE)
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    @Getter(AccessLevel.NONE)
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    private final List<Statement> statements = new LinkedList<>();
    
    private final List<List<Object>> parameterSets = new LinkedList<>();
    
    private final Collection<Connection> connections = new LinkedList<>();
    
    @Getter(AccessLevel.NONE)
    private final Collection<ShardingExecuteGroup<StatementExecuteUnit>> executeGroups = new LinkedList<>();
    
    public StatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        this.databaseType = shardingConnection.getShardingDataSource().getShardingContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
    }
    
    /**
     * Initialize executor.
     *
     * @param routeResult route result
     * @throws SQLException SQL exception
     */
    public void init(final SQLRouteResult routeResult) throws SQLException {
        sqlType = routeResult.getSqlStatement().getType();
        executeGroups.addAll(obtainExecuteGroups(routeResult.getRouteUnits()));
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<RouteUnit> routeUnits) throws SQLException {
        return sqlExecutePrepareTemplate.getExecuteUnitGroups(routeUnits, new SQLExecutePrepareCallback() {
            
            @Override
            public Connection getConnection(final String dataSourceName) throws SQLException {
                Connection conn = connection.getNewConnection(dataSourceName);
                connections.add(conn);
                return conn;
            }
            
            @Override
            public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
                Statement statement = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
                statements.add(statement);
                parameterSets.add(routeUnit.getSqlUnit().getParameterSets().get(0));
                return new StatementExecuteUnit(routeUnit, statement, connectionMode);
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
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<QueryResult> executeCallback = new SQLExecuteCallback<QueryResult>(databaseType, sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected QueryResult executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return getQueryResult(statementExecuteUnit);
            }
        };
        return executeCallback(executeCallback);
    }
    
    private QueryResult getQueryResult(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
        ResultSet resultSet = statementExecuteUnit.getStatement().executeQuery(statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
        resultSets.add(resultSet);
        return ConnectionMode.MEMORY_STRICTLY == statementExecuteUnit.getConnectionMode() ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
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
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(databaseType, sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return updater.executeUpdate(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
        List<Integer> results = executeCallback(executeCallback);
        return accumulate(results);
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
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Boolean> executeCallback = new SQLExecuteCallback<Boolean>(databaseType, sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return executor.execute(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
        List<Boolean> result = executeCallback(executeCallback);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return sqlExecuteTemplate.executeGroup((Collection) executeGroups, executeCallback);
    }
    
    /**
     * Clear data.
     *
     * @throws SQLException sql exception
     */
    public void clear() throws SQLException {
        clearStatements();
        clearConnections();
        connections.clear();
        resultSets.clear();
        statements.clear();
        parameterSets.clear();
        executeGroups.clear();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
    }
    
    private void clearConnections() {
        for (Connection each : connections) {
            connection.release(each);
        }
    }
    
    private interface Updater {
        
        int executeUpdate(Statement statement, String sql) throws SQLException;
    }
    
    private interface Executor {
        
        boolean execute(Statement statement, String sql) throws SQLException;
    }
}

