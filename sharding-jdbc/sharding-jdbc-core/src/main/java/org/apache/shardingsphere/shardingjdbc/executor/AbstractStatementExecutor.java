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
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.executor.ShardingExecuteEngine;
import org.apache.shardingsphere.core.executor.ShardingExecuteGroup;
import org.apache.shardingsphere.core.executor.StatementExecuteUnit;
import org.apache.shardingsphere.core.executor.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.metadata.JDBCTableMetaDataConnectionManager;

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
    private SQLStatement sqlStatement;
    
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
        refreshShardingMetaDataIfNeeded(connection.getShardingContext(), sqlStatement);
        return result;
    }
    
    protected boolean isAccumulate() {
        return !connection.getShardingContext().getShardingRule().isAllBroadcastTables(sqlStatement.getTables().getTableNames());
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
    
    private void refreshShardingMetaDataIfNeeded(final ShardingContext shardingContext, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            refreshTableMetaData(shardingContext, (CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            refreshTableMetaData(shardingContext, (AlterTableStatement) sqlStatement);
        } else if (sqlStatement instanceof DropTableStatement) {
            refreshTableMetaData(shardingContext, (DropTableStatement) sqlStatement);
        }
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final CreateTableStatement createTableStatement) {
        String tableName = createTableStatement.getTables().getSingleTableName();
        shardingContext.getMetaData().getTable().put(tableName, getTableMetaDataInitializer().load(tableName, shardingContext.getShardingRule()));
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final AlterTableStatement alterTableStatement) {
        String tableName = alterTableStatement.getTables().getSingleTableName();
        shardingContext.getMetaData().getTable().put(tableName, getTableMetaDataInitializer().load(tableName, shardingContext.getShardingRule()));
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final DropTableStatement dropTableStatement) {
        for (String each : dropTableStatement.getTables().getTableNames()) {
            shardingContext.getMetaData().getTable().remove(each);
        }
    }
    
    private TableMetaDataInitializer getTableMetaDataInitializer() {
        ShardingProperties shardingProperties = connection.getShardingContext().getShardingProperties();
        return new TableMetaDataInitializer(connection.getShardingContext().getMetaData().getDataSource(), 
                connection.getShardingContext().getExecuteEngine(), new JDBCTableMetaDataConnectionManager(connection.getDataSourceMap()),
                shardingProperties.<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
    }
}
