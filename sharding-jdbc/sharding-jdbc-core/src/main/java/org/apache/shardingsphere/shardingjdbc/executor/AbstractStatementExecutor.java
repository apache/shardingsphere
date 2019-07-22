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
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
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
        this.databaseType = shardingConnection.getShardingContext().getDatabaseType();
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = shardingConnection;
        int maxConnectionsSizePerQuery = connection.getShardingContext().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ShardingExecuteEngine executeEngine = connection.getShardingContext().getExecuteEngine();
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine, connection.isSerialExecute());
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
        refreshShardingMetaDataIfNeeded(connection.getShardingContext(), optimizedStatement);
        return result;
    }
    
    protected final boolean isAccumulate() {
        return !connection.getShardingContext().getShardingRule().isAllBroadcastTables(optimizedStatement.getTables().getTableNames());
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
    
    private void refreshShardingMetaDataIfNeeded(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        if (null == optimizedStatement) {
            return;
        }
        if (optimizedStatement.getSQLStatement() instanceof CreateTableStatement) {
            refreshTableMetaDataForCreateTable(shardingContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof AlterTableStatement) {
            refreshTableMetaDataForAlterTable(shardingContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropTableStatement) {
            refreshTableMetaDataForDropTable(shardingContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof CreateIndexStatement) {
            refreshTableMetaDataForCreateIndex(shardingContext, optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropIndexStatement) {
            refreshTableMetaDataForDropIndex(shardingContext, optimizedStatement);
        }
    }
    
    private void refreshTableMetaDataForCreateTable(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        shardingContext.getMetaData().getTable().put(tableName, getTableMetaDataInitializer().load(tableName, shardingContext.getShardingRule()));
    }
    
    private void refreshTableMetaDataForAlterTable(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        shardingContext.getMetaData().getTable().put(tableName, getTableMetaDataInitializer().load(tableName, shardingContext.getShardingRule()));
    }
    
    private void refreshTableMetaDataForDropTable(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        for (String each : optimizedStatement.getTables().getTableNames()) {
            shardingContext.getMetaData().getTable().remove(each);
        }
    }
    
    private void refreshTableMetaDataForCreateIndex(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        CreateIndexStatement createIndexStatement = (CreateIndexStatement) optimizedStatement.getSQLStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        shardingContext.getMetaData().getTable().get(optimizedStatement.getTables().getSingleTableName()).getLogicIndexes().add(createIndexStatement.getIndex().getName());
    }
    
    private void refreshTableMetaDataForDropIndex(final ShardingContext shardingContext, final OptimizedStatement optimizedStatement) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) optimizedStatement.getSQLStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        if (!optimizedStatement.getTables().isEmpty()) {
            shardingContext.getMetaData().getTable().get(optimizedStatement.getTables().getSingleTableName()).getLogicIndexes().removeAll(indexNames);
        }
        for (String each : indexNames) {
            Optional<String> logicTableName = shardingContext.getMetaData().getTable().getLogicTableName(each);
            if (logicTableName.isPresent()) {
                shardingContext.getMetaData().getTable().get(logicTableName.get()).getLogicIndexes().remove(each);
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
    
    private TableMetaDataInitializer getTableMetaDataInitializer() {
        ShardingProperties shardingProperties = connection.getShardingContext().getShardingProperties();
        return new TableMetaDataInitializer(connection.getShardingContext().getMetaData().getDataSource(), 
                connection.getShardingContext().getExecuteEngine(), new JDBCTableMetaDataConnectionManager(connection.getDataSourceMap()),
                shardingProperties.<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
    }
}
