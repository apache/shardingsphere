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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Prepared statement executor.
 * 
 * @author zhangliang
 * @author caohao
 * @author maxiaoguang
 * @author panjuan
 */
public final class PreparedStatementExecutor extends AbstractStatementExecutor {
    
    private SQLType sqlType;
    
    private final boolean returnGeneratedKeys;
    
    @Getter
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    @Getter
    private final List<PreparedStatement> statements = new LinkedList<>();
    
    @Getter
    private final List<List<Object>> parameterSets = new LinkedList<>();
    
    public PreparedStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys, final ShardingConnection shardingConnection) {
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
        sqlType = routeResult.getSqlStatement().getType();
        getExecuteGroups().addAll(obtainExecuteGroups(routeResult.getRouteUnits()));
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<RouteUnit> routeUnits) throws SQLException {
        return getSqlExecutePrepareTemplate().getExecuteUnitGroups(routeUnits, new SQLExecutePrepareCallback() {
            
            @Override
            public Connection getConnection(final String dataSourceName) throws SQLException {
                Connection conn = PreparedStatementExecutor.super.getConnection().getNewConnection(dataSourceName);
                getConnections().add(conn);
                return conn;
            }
            
            @Override
            public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
                PreparedStatement preparedStatement = createPreparedStatement(connection, routeUnit.getSqlUnit().getSql());
                statements.add(preparedStatement);
                parameterSets.add(routeUnit.getSqlUnit().getParameterSets().get(0));
                return new StatementExecuteUnit(routeUnit, preparedStatement, connectionMode);
            }
        });
    }
    
    private PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        return returnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql, getResultSetType(), getResultSetConcurrency(), getResultSetHoldability());
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
        SQLExecuteCallback<QueryResult> executeCallback = new SQLExecuteCallback<QueryResult>(getDatabaseType(), sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected QueryResult executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return getQueryResult(statementExecuteUnit);
            }
        };
        return executeCallback(executeCallback);
    }
    
    private QueryResult getQueryResult(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statementExecuteUnit.getStatement();
        ResultSet resultSet = preparedStatement.executeQuery();
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
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(getDatabaseType(), sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).executeUpdate();
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
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Boolean> executeCallback = new SQLExecuteCallback<Boolean>(databaseType, sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).execute();
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
}
