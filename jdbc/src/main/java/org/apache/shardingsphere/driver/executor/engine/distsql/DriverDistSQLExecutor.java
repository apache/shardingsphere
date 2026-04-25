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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Driver DistSQL executor.
 */
@RequiredArgsConstructor
public final class DriverDistSQLExecutor {
    
    private final ShardingSphereConnection connection;
    
    /**
     * Execute DistSQL query.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @param statement statement
     * @return result set
     * @throws SQLException SQL exception
     */
    public ResultSet executeQuery(final DistSQLStatement sqlStatement, final QueryContext queryContext, final Statement statement) throws SQLException {
        ContextManager contextManager = connection.getContextManager();
        DistSQLConnectionContext distsqlConnectionContext = createDistSQLConnectionContext(queryContext);
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(sqlStatement, connection.getCurrentDatabaseName(), contextManager, distsqlConnectionContext);
        engine.executeQuery();
        return new DistSQLResultSet(engine.getColumnNames(), engine.getRows(), statement);
    }
    
    /**
     * Execute DistSQL update.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @return update count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final DistSQLStatement sqlStatement, final QueryContext queryContext) throws SQLException {
        ContextManager contextManager = connection.getContextManager();
        DistSQLConnectionContext distsqlConnectionContext = createDistSQLConnectionContext(queryContext);
        DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, connection.getCurrentDatabaseName(), contextManager, distsqlConnectionContext);
        engine.executeUpdate();
        return 0;
    }
    
    /**
     * Execute DistSQL.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @param statement statement
     * @return execute result
     * @throws SQLException SQL exception
     */
    public ExecuteResult execute(final DistSQLStatement sqlStatement, final QueryContext queryContext, final Statement statement) throws SQLException {
        if (isQueryStatement(sqlStatement)) {
            ResultSet resultSet = executeQuery(sqlStatement, queryContext, statement);
            return new ExecuteResult(true, resultSet, 0);
        }
        int updateCount = executeUpdate(sqlStatement, queryContext);
        return new ExecuteResult(false, null, updateCount);
    }
    
    private boolean isQueryStatement(final DistSQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement || sqlStatement instanceof RULStatement
                || sqlStatement instanceof QueryableRALStatement;
    }
    
    private DistSQLConnectionContext createDistSQLConnectionContext(final QueryContext queryContext) {
        return new DistSQLConnectionContext(queryContext, 1,
                connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase(connection.getCurrentDatabaseName()).getProtocolType(),
                connection.getDatabaseConnectionManager(), null);
    }
    
    /**
     * Execute result.
     */
    @Getter
    @RequiredArgsConstructor
    public static final class ExecuteResult {
        
        private final boolean hasResultSet;
        
        private final ResultSet resultSet;
        
        private final int updateCount;
    }
}
