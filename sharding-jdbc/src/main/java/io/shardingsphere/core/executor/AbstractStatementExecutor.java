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
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.routing.BatchRouteUnit;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract statement executor.
 *
 * @author panjuan
 */
public abstract class AbstractStatementExecutor {
    
    private final DatabaseType databaseType;
    
    private SQLType sqlType;
    
    private int batchCount;
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final boolean returnGeneratedKeys;
    
    private final ShardingConnection connection;
    
    private final Collection<BatchRouteUnit> routeUnits = new LinkedList<>();
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    @Getter
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<Connection> connections = new LinkedList<>();
    
    private final Collection<ShardingExecuteGroup<StatementExecuteUnit>> executeGroups = new LinkedList<>();
    
    public AbstractStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys,
                                     final ShardingConnection shardingConnection) {
        this.databaseType = shardingConnection.getShardingDataSource().getShardingContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.returnGeneratedKeys = returnGeneratedKeys;
        this.connection = shardingConnection;
        sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
    }
    
    /**
     * Initialize executor.
     *
     * @exception SQLException sql exception
     */
    public void init() throws SQLException {
        executeGroups.addAll(obtainExecuteGroups(routeUnits));
    }
    
    protected abstract Collection<ShardingExecuteGroup<StatementExecuteUnit>> obtainExecuteGroups(final Collection<BatchRouteUnit> routeUnits) throws SQLException;
    
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
        for (ShardingExecuteGroup<StatementExecuteUnit> each : executeGroups) {
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
        Optional<StatementExecuteUnit> target;
        List<List<Object>> result = new LinkedList<>();
        for (ShardingExecuteGroup<StatementExecuteUnit> each : executeGroups) {
            target = Iterators.tryFind(each.getInputs().iterator(), new Predicate<StatementExecuteUnit>() {
                @Override
                public boolean apply(final StatementExecuteUnit input) {
                    return input.getStatement().equals(statement);
                }
            });
            if (target.isPresent()) {
                result.addAll(target.get().getRouteUnit().getSqlUnit().getParameterSets());
                break;
            }
        }
        return result;
    }
    
    /**
     * Clear data.
     *
     * @throws SQLException sql exception
     */
    public void clear() throws SQLException {
        clearStatements();
        clearConnections();
        batchCount = 0;
        connections.clear();
        routeUnits.clear();
        resultSets.clear();
        executeGroups.clear();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : getStatements()) {
            each.close();
        }
    }
    
    private void clearConnections() {
        for (Connection each : connections) {
            connection.release(each);
        }
    }
}


