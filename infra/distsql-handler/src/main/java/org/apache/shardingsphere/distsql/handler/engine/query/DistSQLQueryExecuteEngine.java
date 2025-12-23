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

package org.apache.shardingsphere.distsql.handler.engine.query;

import lombok.Getter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorAwareSetter;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * DistSQL query execute engine.
 */
public final class DistSQLQueryExecuteEngine {
    
    private final DistSQLStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final DistSQLConnectionContext distsqlConnectionContext;
    
    private final String databaseName;
    
    @Getter
    private Collection<String> columnNames;
    
    @Getter
    private Collection<LocalDataQueryResultRow> rows;
    
    public DistSQLQueryExecuteEngine(final DistSQLStatement sqlStatement, final String currentDatabaseName,
                                     final ContextManager contextManager, final DistSQLConnectionContext distsqlConnectionContext) {
        this.sqlStatement = sqlStatement;
        this.contextManager = contextManager;
        this.distsqlConnectionContext = distsqlConnectionContext;
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
        try {
            new DistSQLExecutorAwareSetter(executor).set(contextManager, null == databaseName ? null : contextManager.getDatabase(databaseName), distsqlConnectionContext, null, sqlStatement);
        } catch (final UnsupportedSQLOperationException ignored) {
            columnNames = executor.getColumnNames(sqlStatement);
            rows = Collections.emptyList();
            return;
        }
        columnNames = executor.getColumnNames(sqlStatement);
        rows = executor.getRows(sqlStatement, contextManager);
    }
}
