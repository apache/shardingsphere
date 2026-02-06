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

package org.apache.shardingsphere.driver.executor.engine.facade;

import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteExecutor;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteQueryExecutor;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteUpdateExecutor;
import org.apache.shardingsphere.driver.executor.engine.transaction.DriverTransactionSQLStatementExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Driver executor facade.
 */
public final class DriverExecutorFacade implements AutoCloseable {
    
    private final ShardingSphereConnection connection;
    
    private final StatementOption statementOption;
    
    private final StatementManager statementManager;
    
    private final JDBCDriverType jdbcDriverType;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private final DriverExecuteQueryExecutor queryExecutor;
    
    private final DriverExecuteUpdateExecutor updateExecutor;
    
    private final DriverExecuteExecutor executeExecutor;
    
    public DriverExecutorFacade(final ShardingSphereConnection connection, final StatementOption statementOption, final StatementManager statementManager, final JDBCDriverType jdbcDriverType,
                                final ShardingSphereDatabase currentDatabase) {
        this.connection = connection;
        this.statementOption = statementOption;
        this.statementManager = statementManager;
        this.jdbcDriverType = jdbcDriverType;
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        ShardingSphereMetaData metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        String currentSchemaName = new DatabaseTypeRegistry(currentDatabase.getProtocolType()).getDefaultSchemaName(connection.getCurrentDatabaseName());
        sqlFederationEngine =
                new SQLFederationEngine(connection.getCurrentDatabaseName(), currentSchemaName, metaData, connection.getContextManager().getMetaDataContexts().getStatistics(), jdbcExecutor);
        RawExecutor rawExecutor = new RawExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        queryExecutor = new DriverExecuteQueryExecutor(connection, metaData, jdbcExecutor, rawExecutor, sqlFederationEngine);
        updateExecutor = new DriverExecuteUpdateExecutor(connection, metaData, jdbcExecutor, rawExecutor);
        executeExecutor = new DriverExecuteExecutor(connection, metaData, jdbcExecutor, rawExecutor, sqlFederationEngine, new DriverTransactionSQLStatementExecutor(connection));
    }
    
    /**
     * Execute query.
     *
     * @param database database
     * @param metaData metadata
     * @param queryContext query context
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public ResultSet executeQuery(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData, final QueryContext queryContext, final Statement statement,
                                  final Map<String, Integer> columnLabelAndIndexMap,
                                  final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        SQLAuditEngine.audit(queryContext, database);
        return queryExecutor.executeQuery(
                database, queryContext, createDriverExecutionPrepareEngine(database, metaData, jdbcDriverType), statement, columnLabelAndIndexMap, addCallback, replayCallback);
    }
    
    /**
     * Execute update.
     *
     * @param database database
     * @param metaData metadata
     * @param queryContext query context
     * @param executeUpdateCallback statement execute update callback
     * @param replayCallback statement replay callback
     * @param addCallback statement add callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int executeUpdate(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData, final QueryContext queryContext,
                             final StatementExecuteUpdateCallback executeUpdateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        SQLAuditEngine.audit(queryContext, database);
        return updateExecutor.executeUpdate(database, queryContext, createDriverExecutionPrepareEngine(database, metaData, jdbcDriverType), executeUpdateCallback, addCallback, replayCallback);
    }
    
    /**
     * Execute.
     *
     * @param database database
     * @param metaData metadata
     * @param queryContext query context
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData, final QueryContext queryContext,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        SQLAuditEngine.audit(queryContext, database);
        return executeExecutor.execute(database, queryContext, createDriverExecutionPrepareEngine(database, metaData, jdbcDriverType), executeCallback, addCallback, replayCallback);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData,
                                                                                                           final JDBCDriverType jdbcDriverType) {
        int maxConnectionsSizePerQuery = connection.getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(jdbcDriverType, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager, statementOption,
                database.getRuleMetaData().getRules(), metaData);
    }
    
    /**
     * Get result set.
     *
     * @param database database
     * @param queryContext query context
     * @param statement statement
     * @param statements statements
     * @return result set
     * @throws SQLException SQL exception
     */
    public Optional<ResultSet> getResultSet(final ShardingSphereDatabase database,
                                            final QueryContext queryContext, final Statement statement, final List<? extends Statement> statements) throws SQLException {
        return executeExecutor.getResultSet(database, queryContext, statement, statements);
    }
    
    @Override
    public void close() throws SQLException {
        sqlFederationEngine.close();
    }
}
