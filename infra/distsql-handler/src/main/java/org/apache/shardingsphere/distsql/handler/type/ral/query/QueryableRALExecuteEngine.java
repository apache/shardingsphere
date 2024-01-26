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

package org.apache.shardingsphere.distsql.handler.type.ral.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.type.ral.query.aware.ConnectionSizeAwareQueryableRALExecutor;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;

/**
 * Queryable RAL execute engine.
 */
@RequiredArgsConstructor
public abstract class QueryableRALExecuteEngine {
    
    private final QueryableRALStatement sqlStatement;
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    @Getter
    private Collection<String> columnNames;
    
    @Getter
    private Collection<LocalDataQueryResultRow> rows;
    
    /**
     * Execute query.
     * 
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void executeQuery() {
        QueryableRALExecutor executor = TypedSPILoader.getService(QueryableRALExecutor.class, sqlStatement.getClass());
        rows = getRows(executor);
        columnNames = executor.getColumnNames();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<LocalDataQueryResultRow> getRows(final QueryableRALExecutor executor) {
        if (executor instanceof DistSQLExecutorDatabaseAware) {
            ((DistSQLExecutorDatabaseAware) executor).setDatabase(getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName)));
        }
        if (executor instanceof ConnectionSizeAwareQueryableRALExecutor) {
            ((ConnectionSizeAwareQueryableRALExecutor) executor).setConnectionSize(getConnectionSize());
        }
        return executor.getRows(sqlStatement, contextManager);
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
    
    protected abstract int getConnectionSize();
}
