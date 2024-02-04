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

package org.apache.shardingsphere.distsql.handler.type.query;

import lombok.Getter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorAwareSetter;
import org.apache.shardingsphere.distsql.handler.type.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * DistSQL query execute engine.
 */
public abstract class DistSQLQueryExecuteEngine {
    
    private final DistSQLStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final String databaseName;
    
    @Getter
    private Collection<String> columnNames;
    
    @Getter
    private Collection<LocalDataQueryResultRow> rows;
    
    public DistSQLQueryExecuteEngine(final DistSQLStatement sqlStatement, final String currentDatabaseName, final ContextManager contextManager) {
        this.sqlStatement = sqlStatement;
        this.contextManager = contextManager;
        databaseName = DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName);
    }
    
    /**
     * Execute query.
     *
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public void executeQuery() throws SQLException {
        DistSQLQueryExecutor<DistSQLStatement> executor = TypedSPILoader.getService(DistSQLQueryExecutor.class, sqlStatement.getClass());
        columnNames = executor.getColumnNames();
        try {
            new DistSQLExecutorAwareSetter(executor).set(contextManager, null == databaseName ? null : getDatabase(databaseName), getDistSQLConnectionContext());
        } catch (final UnsupportedSQLOperationException ignored) {
            rows = Collections.emptyList();
            return;
        }
        rows = executor.getRows(sqlStatement, contextManager);
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
    
    protected abstract DistSQLConnectionContext getDistSQLConnectionContext();
}
