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
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.StatementExecuteUnit;
import org.apache.shardingsphere.core.execute.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.core.execute.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import org.apache.shardingsphere.spi.database.DatabaseType;

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
 * @author maxiaoguang
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
    @Setter
    private OptimizedStatement optimizedStatement;
    
    @Getter
    private final List<List<Object>> parameterSets = new LinkedList<>();
    
    @Getter
    private final List<Statement> statements = new LinkedList<>();
    
    @Getter
    private final List<ResultSet> resultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<ShardingExecuteGroup<StatementExecuteUnit>> executeGroups = new LinkedList<>();
    
    public AbstractStatementExecutor(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final ShardingConnection shardingConnection) {
        this.databaseType = shardingConnection.getRuntimeContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        int maxConnectionsSizePerQuery = connection.getRuntimeContext().getProps().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ShardingExecuteEngine executeEngine = connection.getRuntimeContext().getExecuteEngine();
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine, connection.isHoldTransaction());
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
                    return input.getRouteUnit().getSqlUnit().getParameters();
                }
            }));
        }
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        List<T> result = sqlExecuteTemplate.executeGroup((Collection) executeGroups, executeCallback);
        refreshMetaDataIfNeeded(connection.getRuntimeContext(), optimizedStatement);
        return result;
    }
    
    protected final boolean isAccumulate() {
        return !connection.getRuntimeContext().getRule().isAllBroadcastTables(optimizedStatement.getTables().getTableNames());
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
    
    private void refreshMetaDataIfNeeded(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) throws SQLException {
        if (null == optimizedStatement) {
            return;
        }
        if (optimizedStatement.getSQLStatement() instanceof CreateTableStatement) {
            refreshTableMetaDataForCreateTable(runtimeContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof AlterTableStatement) {
            refreshTableMetaDataForAlterTable(runtimeContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropTableStatement) {
            refreshTableMetaDataForDropTable(runtimeContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof CreateIndexStatement) {
            refreshTableMetaDataForCreateIndex(runtimeContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropIndexStatement) {
            refreshTableMetaDataForDropIndex(runtimeContext, optimizedStatement);
        }
    }
    
    private void refreshTableMetaDataForCreateTable(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) throws SQLException {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        runtimeContext.getMetaData().getTables().put(tableName, getTableMetaDataInitializer().load(tableName, runtimeContext.getRule()));
    }
    
    private void refreshTableMetaDataForAlterTable(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) throws SQLException {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        runtimeContext.getMetaData().getTables().put(tableName, getTableMetaDataInitializer().load(tableName, runtimeContext.getRule()));
    }
    
    private void refreshTableMetaDataForDropTable(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) {
        for (String each : optimizedStatement.getTables().getTableNames()) {
            runtimeContext.getMetaData().getTables().remove(each);
        }
    }
    
    private void refreshTableMetaDataForCreateIndex(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) {
        CreateIndexStatement createIndexStatement = (CreateIndexStatement) optimizedStatement.getSQLStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        runtimeContext.getMetaData().getTables().get(optimizedStatement.getTables().getSingleTableName()).getIndexes().add(createIndexStatement.getIndex().getName());
    }
    
    private void refreshTableMetaDataForDropIndex(final ShardingRuntimeContext runtimeContext, final OptimizedStatement optimizedStatement) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) optimizedStatement.getSQLStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        TableMetaData tableMetaData = runtimeContext.getMetaData().getTables().get(optimizedStatement.getTables().getSingleTableName());
        if (!optimizedStatement.getTables().isEmpty()) {
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
    
    private TableMetaDataInitializer getTableMetaDataInitializer() {
        ShardingProperties shardingProperties = connection.getRuntimeContext().getProps();
        return new TableMetaDataInitializer(connection.getRuntimeContext().getMetaData().getDataSources(), 
                connection.getRuntimeContext().getExecuteEngine(), new JDBCTableMetaDataConnectionManager(connection.getDataSourceMap()),
                shardingProperties.<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
    }
}
