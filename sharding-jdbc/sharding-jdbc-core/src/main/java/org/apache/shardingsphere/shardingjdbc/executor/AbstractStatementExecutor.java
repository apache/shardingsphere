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
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializerEntry;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.metadata.decorator.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.sharding.execute.metadata.loader.ShardingTableMetaDataLoader;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.sharding.execute.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.metadata.JDBCDataSourceMapConnectionManager;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializer;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.engine.ExecutorEngine;
import org.apache.shardingsphere.underlying.executor.engine.InputGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract statement executor.
 *
 * @author panjuan
 * @author maxiaoguang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractStatementExecutor {
    
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
    @Setter
    private SQLStatementContext sqlStatementContext;
    
    @Getter
    private final List<List<Object>> parameterSets = new LinkedList<>();
    
    @Getter
    private final List<Statement> statements = new LinkedList<>();
    
    @Getter
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<InputGroup<StatementExecuteUnit>> inputGroups = new LinkedList<>();
    
    public AbstractStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        this.databaseType = shardingConnection.getRuntimeContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        int maxConnectionsSizePerQuery = connection.getRuntimeContext().getProperties().<Integer>getValue(PropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ExecutorEngine executorEngine = connection.getRuntimeContext().getExecutorEngine();
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(executorEngine, connection.isHoldTransaction());
    }
    
    protected final void cacheStatements() {
        for (InputGroup<StatementExecuteUnit> each : inputGroups) {
            statements.addAll(Lists.transform(each.getInputs(), new Function<StatementExecuteUnit, Statement>() {
                
                @Override
                public Statement apply(final StatementExecuteUnit input) {
                    return input.getStatement();
                }
            }));
            parameterSets.addAll(Lists.transform(each.getInputs(), new Function<StatementExecuteUnit, List<Object>>() {
                
                @Override
                public List<Object> apply(final StatementExecuteUnit input) {
                    return input.getExecutionUnit().getSqlUnit().getParameters();
                }
            }));
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
    protected final <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
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
        connections.clear();
        resultSets.clear();
        inputGroups.clear();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : getStatements()) {
            each.close();
        }
    }
    
    private void refreshMetaDataIfNeeded(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        if (sqlStatementContext.getSqlStatement() instanceof CreateTableStatement) {
            refreshTableMetaDataForCreateTable(runtimeContext, sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof AlterTableStatement) {
            refreshTableMetaDataForAlterTable(runtimeContext, sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof DropTableStatement) {
            refreshTableMetaDataForDropTable(runtimeContext, sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof CreateIndexStatement) {
            refreshTableMetaDataForCreateIndex(runtimeContext, sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof DropIndexStatement) {
            refreshTableMetaDataForDropIndex(runtimeContext, sqlStatementContext);
        }
    }
    
    private void refreshTableMetaDataForCreateTable(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        runtimeContext.getMetaData().getTables().put(tableName, createTableMetaDataInitializerEntry().init(tableName));
    }
    
    private void refreshTableMetaDataForAlterTable(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        runtimeContext.getMetaData().getTables().put(tableName, createTableMetaDataInitializerEntry().init(tableName));
    }
    
    private void refreshTableMetaDataForDropTable(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            runtimeContext.getMetaData().getTables().remove(each);
        }
    }
    
    private void refreshTableMetaDataForCreateIndex(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) {
        CreateIndexStatement createIndexStatement = (CreateIndexStatement) sqlStatementContext.getSqlStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        runtimeContext.getMetaData().getTables().get(sqlStatementContext.getTablesContext().getSingleTableName()).getIndexes().add(createIndexStatement.getIndex().getName());
    }
    
    private void refreshTableMetaDataForDropIndex(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) sqlStatementContext.getSqlStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        TableMetaData tableMetaData = runtimeContext.getMetaData().getTables().get(sqlStatementContext.getTablesContext().getSingleTableName());
        if (!sqlStatementContext.getTablesContext().isEmpty()) {
            tableMetaData.getIndexes().removeAll(indexNames);
        }
        for (String each : indexNames) {
            Optional<String> logicTableName = findLogicTableName(runtimeContext.getMetaData().getTables(), each);
            if (logicTableName.isPresent()) {
                tableMetaData.getIndexes().remove(each);
            }
        }
    }
    
    private Collection<String> getIndexNames(final DropIndexStatement dropIndexStatement) {
        Collection<String> result = new LinkedList<>();
        for (IndexSegment each : dropIndexStatement.getIndexes()) {
            result.add(each.getName());
        }
        return result;
    }
    
    private Optional<String> findLogicTableName(final TableMetas tableMetas, final String logicIndexName) {
        for (String each : tableMetas.getAllTableNames()) {
            if (tableMetas.get(each).containsIndex(logicIndexName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private TableMetaDataInitializerEntry createTableMetaDataInitializerEntry() {
        ShardingRule shardingRule = connection.getRuntimeContext().getRule();
        ShardingSphereProperties properties = connection.getRuntimeContext().getProperties();
        Map<BaseRule, TableMetaDataInitializer> tableMetaDataInitializes = new HashMap<>(2, 1);
        tableMetaDataInitializes.put(shardingRule, new ShardingTableMetaDataLoader(connection.getRuntimeContext().getMetaData().getDataSources(), connection.getRuntimeContext().getExecutorEngine(), 
                new JDBCDataSourceMapConnectionManager(connection.getDataSourceMap()),
                properties.<Integer>getValue(PropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY), properties.<Boolean>getValue(PropertiesConstant.CHECK_TABLE_METADATA_ENABLED)));
        if (!shardingRule.getEncryptRule().getEncryptTableNames().isEmpty()) {
            tableMetaDataInitializes.put(shardingRule.getEncryptRule(), new EncryptTableMetaDataDecorator());
        }
        return new TableMetaDataInitializerEntry(tableMetaDataInitializes);
    }
}
