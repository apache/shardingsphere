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

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecutorCallback;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.sharding.execute.sql.prepare.SQLExecuteGroupEngine;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.impl.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Abstract statement executor.
 */
@Getter
public abstract class AbstractStatementExecutor {
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final ShardingConnection connection;
    
    private final DatabaseType databaseType;
    
    private final List<List<Object>> parameterSets;
    
    private final List<Statement> statements;
    
    private final List<ResultSet> resultSets;
    
    private final Collection<InputGroup<StatementExecuteUnit>> inputGroups;
    
    private final SQLExecuteGroupEngine executeGroupEngine;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    private final RuleSchemaMetaDataLoader metaDataLoader;
    
    @Setter
    private SQLStatementContext sqlStatementContext;
    
    public AbstractStatementExecutor(final boolean isPreparedStatement, 
                                     final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        this.databaseType = shardingConnection.getRuntimeContext().getDatabaseType();
        parameterSets = new LinkedList<>();
        statements = new LinkedList<>();
        resultSets = new CopyOnWriteArrayList<>();
        inputGroups = new LinkedList<>();
        int maxConnectionsSizePerQuery = connection.getRuntimeContext().getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        executeGroupEngine = new SQLExecuteGroupEngine(isPreparedStatement, maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(connection.getRuntimeContext().getExecutorKernel(), connection.isHoldTransaction());
        metaDataLoader = new RuleSchemaMetaDataLoader(connection.getRuntimeContext().getRule().toRules());
    }
    
    protected final void cacheStatements() {
        for (InputGroup<StatementExecuteUnit> each : inputGroups) {
            statements.addAll(each.getInputs().stream().map(StatementExecuteUnit::getStatement).collect(Collectors.toList()));
            parameterSets.addAll(each.getInputs().stream().map(input -> input.getExecutionUnit().getSqlUnit().getParameters()).collect(Collectors.toList()));
        }
    }
    
    /**
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     * 
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     * 
     * @param executeCallback execute callback
     * @param <T> class type of return value 
     * @return result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    protected final <T> List<T> executeCallback(final SQLExecutorCallback<T> executeCallback) throws SQLException {
        List<T> result = sqlExecuteTemplate.execute((Collection) inputGroups, executeCallback);
        refreshMetaDataIfNeeded(connection.getRuntimeContext(), sqlStatementContext);
        return result;
    }
    
    /**
     * is accumulate.
     * 
     * @return accumulate or not
     */
    public final boolean isAccumulate() {
        return !connection.getRuntimeContext().getRule().isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    /**
     * Clear data.
     *
     * @throws SQLException SQL exception
     */
    public void clear() throws SQLException {
        clearStatements();
        statements.clear();
        parameterSets.clear();
        resultSets.clear();
        inputGroups.clear();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : getStatements()) {
            each.close();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void refreshMetaDataIfNeeded(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            refreshStrategy.get().refreshMetaData(runtimeContext.getMetaData(), sqlStatementContext, 
                tableName -> metaDataLoader.load(databaseType, connection.getDataSourceMap(), tableName, connection.getRuntimeContext().getProperties()));
        }
    }
}
