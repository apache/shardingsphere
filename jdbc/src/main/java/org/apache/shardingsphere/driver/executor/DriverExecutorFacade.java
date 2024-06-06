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

package org.apache.shardingsphere.driver.executor;

import org.apache.shardingsphere.driver.executor.batch.engine.DriverExecuteBatchExecutor;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.keygen.GeneratedKeyCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.PreparedStatementParametersReplayCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteExecutor;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteQueryExecutor;
import org.apache.shardingsphere.driver.executor.engine.DriverExecuteUpdateExecutor;
import org.apache.shardingsphere.driver.executor.engine.DriverJDBCExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
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
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Driver executor facade.
 */
public final class DriverExecutorFacade implements AutoCloseable {
    
    private final ShardingSphereConnection connection;
    
    private final StatementOption statementOption;
    
    private final StatementManager statementManager;
    
    private final String jdbcDriverType;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private final DriverExecuteQueryExecutor queryExecutor;
    
    private final DriverExecuteUpdateExecutor updateExecutor;
    
    private final DriverExecuteExecutor executeExecutor;
    
    private final DriverExecuteBatchExecutor executeBatchExecutor;
    
    public DriverExecutorFacade(final ShardingSphereConnection connection, final StatementOption statementOption, final StatementManager statementManager) {
        this(connection, statementOption, statementManager, null, JDBCDriverType.STATEMENT);
    }
    
    public DriverExecutorFacade(final ShardingSphereConnection connection, final StatementOption statementOption, final StatementManager statementManager, final ShardingSphereDatabase database) {
        this(connection, statementOption, statementManager, database, JDBCDriverType.PREPARED_STATEMENT);
    }
    
    private DriverExecutorFacade(final ShardingSphereConnection connection, final StatementOption statementOption, final StatementManager statementManager,
                                 final ShardingSphereDatabase database, final String jdbcDriverType) {
        this.connection = connection;
        this.statementOption = statementOption;
        this.statementManager = statementManager;
        this.jdbcDriverType = jdbcDriverType;
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        DriverJDBCExecutor regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        RawExecutor rawExecutor = new RawExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        trafficExecutor = new TrafficExecutor();
        ShardingSphereMetaData metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        String schemaName = new DatabaseTypeRegistry(metaData.getDatabase(connection.getDatabaseName()).getProtocolType()).getDefaultSchemaName(connection.getDatabaseName());
        sqlFederationEngine = new SQLFederationEngine(connection.getDatabaseName(), schemaName, metaData, connection.getContextManager().getMetaDataContexts().getStatistics(), jdbcExecutor);
        queryExecutor = new DriverExecuteQueryExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor, sqlFederationEngine);
        updateExecutor = new DriverExecuteUpdateExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor);
        executeExecutor = new DriverExecuteExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor, sqlFederationEngine);
        executeBatchExecutor = null == database ? null : new DriverExecuteBatchExecutor(connection, metaData, database, jdbcExecutor);
    }
    
    /**
     * Execute query.
     *
     * @param database database
     * @param queryContext query context
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public ResultSet executeQuery(final ShardingSphereDatabase database, final QueryContext queryContext, final Statement statement, final Map<String, Integer> columnLabelAndIndexMap,
                                  final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database, jdbcDriverType);
        return queryExecutor.executeQuery(database, queryContext, prepareEngine, statement, columnLabelAndIndexMap, addCallback, replayCallback);
    }
    
    /**
     * Execute update.
     *
     * @param database database
     * @param queryContext query context
     * @param updateCallback statement execute update callback
     * @param replayCallback statement replay callback
     * @param addCallback statement add callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int executeUpdate(final ShardingSphereDatabase database, final QueryContext queryContext,
                             final StatementExecuteUpdateCallback updateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database, jdbcDriverType);
        return updateExecutor.executeUpdate(database, queryContext, prepareEngine, updateCallback, addCallback, replayCallback);
    }
    
    /**
     * Execute.
     *
     * @param database database
     * @param queryContext query context
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final QueryContext queryContext,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database, jdbcDriverType);
        return executeExecutor.execute(database, queryContext, prepareEngine, executeCallback, addCallback, replayCallback);
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public Optional<ResultSet> getResultSet() {
        return executeExecutor.getResultSet();
    }
    
    /**
     * Add batch.
     *
     * @param queryContext query context
     * @param database database
     */
    public void addBatch(final QueryContext queryContext, final ShardingSphereDatabase database) {
        executeBatchExecutor.addBatch(queryContext, database);
    }
    
    /**
     * Execute batch.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @param generatedValues generated values
     * @param statementOption statement option
     * @param addCallback statement add callback
     * @param replayCallback prepared statement parameters replay callback
     * @param generatedKeyCallback generated key callback
     * @return generated keys
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int[] executeBatch(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final Collection<Comparable<?>> generatedValues,
                              final StatementOption statementOption, final StatementAddCallback addCallback, final PreparedStatementParametersReplayCallback replayCallback,
                              final GeneratedKeyCallback generatedKeyCallback) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database, jdbcDriverType);
        return executeBatchExecutor.executeBatch(database, sqlStatementContext, generatedValues, statementOption, prepareEngine, addCallback, replayCallback, generatedKeyCallback);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereDatabase database, final String jdbcDriverType) {
        int maxConnectionsSizePerQuery = connection.getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(jdbcDriverType, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager, statementOption,
                database.getRuleMetaData().getRules(), database.getResourceMetaData().getStorageUnits());
    }
    
    /**
     * Clear.
     */
    public void clear() {
        if (null != executeBatchExecutor) {
            executeBatchExecutor.clear();
        }
    }
    
    @Override
    public void close() throws SQLException {
        trafficExecutor.close();
        sqlFederationEngine.close();
    }
}
