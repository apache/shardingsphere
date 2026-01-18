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
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Driver DistSQL executor.
 *
 * <p>
 * Executes DistSQL statements through the ShardingSphere JDBC Driver. Statements whose executors depend on
 * Proxy-only infrastructure are rejected before SPI dispatch; see {@link #UNSUPPORTED_DISTSQL_STATEMENTS}
 * for the current JDBC-unsupported set and {@link #checkSupported(DistSQLStatement)} for the rejection path.
 * </p>
 */
@RequiredArgsConstructor
public final class DriverDistSQLExecutor {
    
    /**
     * DistSQL statements whose executors depend on Proxy-only infrastructure and are therefore rejected by the JDBC Driver.
     *
     * <p>
     * {@link PreviewStatement} is included because its {@code PreviewExecutor} casts the supplied
     * {@code DatabaseConnectionManager} to {@code ProxyDatabaseConnectionManager} and requires the Proxy-side
     * executor statement manager; the JDBC Driver provides neither, so the statement would otherwise fail with
     * {@link ClassCastException} or {@link NullPointerException} at runtime.
     * </p>
     */
    private static final Set<Class<? extends DistSQLStatement>> UNSUPPORTED_DISTSQL_STATEMENTS;
    
    static {
        UNSUPPORTED_DISTSQL_STATEMENTS = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(PreviewStatement.class)));
    }
    
    private final ShardingSphereConnection connection;
    
    /**
     * Execute DistSQL query.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @param statement statement
     * @return result set
     * @throws SQLException SQL exception
     * @throws SQLFeatureNotSupportedException if the statement is not supported in JDBC mode
     */
    public ResultSet executeQuery(final DistSQLStatement sqlStatement, final QueryContext queryContext, final Statement statement) throws SQLException {
        checkSupported(sqlStatement);
        try {
            ContextManager contextManager = connection.getContextManager();
            DistSQLConnectionContext distsqlConnectionContext = createDistSQLConnectionContext(queryContext);
            DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(sqlStatement, connection.getCurrentDatabaseName(), contextManager, distsqlConnectionContext);
            engine.executeQuery();
            return new DistSQLResultSet(engine.getColumnNames(), engine.getRows(), statement);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new SQLFeatureNotSupportedException(String.format("DistSQL statement '%s' is not supported in JDBC mode: %s", sqlStatement.getClass().getSimpleName(), ex.getMessage()), ex);
        }
    }
    
    /**
     * Execute DistSQL update.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @return update count
     * @throws SQLException SQL exception
     * @throws SQLFeatureNotSupportedException if the statement is not supported in JDBC mode
     */
    public int executeUpdate(final DistSQLStatement sqlStatement, final QueryContext queryContext) throws SQLException {
        checkSupported(sqlStatement);
        try {
            ContextManager contextManager = connection.getContextManager();
            DistSQLConnectionContext distsqlConnectionContext = createDistSQLConnectionContext(queryContext);
            DistSQLUpdateExecuteEngine engine = new DistSQLUpdateExecuteEngine(sqlStatement, connection.getCurrentDatabaseName(), contextManager, distsqlConnectionContext);
            engine.executeUpdate();
            return 0;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new SQLFeatureNotSupportedException(String.format("DistSQL statement '%s' is not supported in JDBC mode: %s", sqlStatement.getClass().getSimpleName(), ex.getMessage()), ex);
        }
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
    
    boolean isQueryStatement(final DistSQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement || sqlStatement instanceof RULStatement
                || sqlStatement instanceof QueryableRALStatement;
    }
    
    /**
     * Reject DistSQL statements whose executors depend on Proxy-only infrastructure.
     *
     * @param sqlStatement DistSQL statement to validate
     * @throws SQLFeatureNotSupportedException when the statement belongs to {@link #UNSUPPORTED_DISTSQL_STATEMENTS}
     */
    private void checkSupported(final DistSQLStatement sqlStatement) throws SQLFeatureNotSupportedException {
        if (UNSUPPORTED_DISTSQL_STATEMENTS.contains(sqlStatement.getClass())) {
            throw new SQLFeatureNotSupportedException(String.format(
                    "DistSQL statement '%s' is not supported in ShardingSphere JDBC.", sqlStatement.getClass().getSimpleName()));
        }
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
