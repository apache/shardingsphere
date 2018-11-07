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

package io.shardingsphere.shardingjdbc.executor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.transaction.TransactionTypeHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
@Getter(AccessLevel.PROTECTED)
public class AbstractStatementExecutor {
    
    private final DatabaseType databaseType;
    
    @Getter
    private final int resultSetType;
    
    @Getter
    private final int resultSetConcurrency;
    
    @Getter
    private final int resultSetHoldability;
    
    private final ShardingConnection connection;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    private final Collection<Connection> connections = new LinkedList<>();
    
    @Getter
    private final List<List<Object>> parameterSets = new LinkedList<>();
    
    @Getter
    private final List<Statement> statements = new LinkedList<>();
    
    @Getter
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<ShardingExecuteGroup<StatementExecuteUnit>> executeGroups = new LinkedList<>();
    
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private SQLType sqlType;
    
    public AbstractStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        this.databaseType = shardingConnection.getShardingContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        int maxConnectionsSizePerQuery = connection.getShardingContext().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ShardingExecuteEngine executeEngine = connection.getShardingContext().getExecuteEngine();
        sqlExecutePrepareTemplate = TransactionType.XA == TransactionTypeHolder.get()
                ? new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery) : new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery, executeEngine);
        sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine);
    }
    
    protected final void cacheStatements() {
        for (ShardingExecuteGroup<StatementExecuteUnit> each : executeGroups) {
            statements.addAll(Lists.transform(each.getInputs(), new Function<StatementExecuteUnit, Statement>() {
                
                @Override
                public Statement apply(final StatementExecuteUnit input) {
                    return input.getStatement();
                }
            }));
            parameterSets.addAll(Lists.transform(each.getInputs(), new Function<StatementExecuteUnit, List<Object>>() {
                
                @Override
                public List<Object> apply(final StatementExecuteUnit input) {
                    return input.getRouteUnit().getSqlUnit().getParameterSets().get(0);
                }
            }));
        }
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return sqlExecuteTemplate.executeGroup((Collection) executeGroups, executeCallback);
    }
    
    /**
     * Clear data.
     *
     * @throws SQLException sql exception
     */
    public void clear() throws SQLException {
        clearStatements();
        statements.clear();
        parameterSets.clear();
        connections.clear();
        resultSets.clear();
        executeGroups.clear();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : getStatements()) {
            each.close();
        }
    }
}
