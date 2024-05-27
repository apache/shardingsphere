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

import lombok.Getter;
import org.apache.shardingsphere.driver.executor.callback.ExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.executor.TrafficExecutorCallback;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Driver executor.
 */
public final class DriverExecutor implements AutoCloseable {
    
    private final ShardingSphereConnection connection;
    
    @Getter
    private final DriverJDBCExecutor regularExecutor;
    
    @Getter
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private ExecuteType executeType = ExecuteType.REGULAR;
    
    public DriverExecutor(final ShardingSphereConnection connection) {
        this.connection = connection;
        MetaDataContexts metaDataContexts = connection.getContextManager().getMetaDataContexts();
        ExecutorEngine executorEngine = connection.getContextManager().getExecutorEngine();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        rawExecutor = new RawExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName());
        String schemaName = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(connection.getDatabaseName());
        trafficExecutor = new TrafficExecutor();
        sqlFederationEngine = new SQLFederationEngine(connection.getDatabaseName(), schemaName, metaDataContexts.getMetaData(), metaDataContexts.getStatistics(), jdbcExecutor);
    }
    
    /**
     * Execute advance query.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @return result set
     * @throws SQLException SQL exception
     */
    public Optional<ResultSet> executeAdvanceQuery(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                   final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            TrafficExecutorCallback<ResultSet> trafficCallback = JDBCDriverType.STATEMENT.equals(prepareEngine.getType())
                    ? Statement::executeQuery
                    : ((statement, sql) -> ((PreparedStatement) statement).executeQuery());
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, trafficCallback));
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            ExecuteQueryCallback sqlFederationCallback = JDBCDriverType.STATEMENT.equals(prepareEngine.getType())
                    ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                            queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                    : new PreparedStatementExecuteQueryCallback(database.getProtocolType(),
                            database.getResourceMetaData(), queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
            return Optional.of(sqlFederationEngine.executeQuery(prepareEngine, sqlFederationCallback, new SQLFederationContext(false, queryContext, metaData, connection.getProcessId())));
        }
        return Optional.empty();
    }
    
    /**
     * Execute advance update.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @return updated row count
     * @throws SQLException SQL exception
     */
    public Optional<Integer> executeAdvanceUpdate(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(),
                    trafficInstanceId.get(), queryContext, prepareEngine, (statement, sql) -> ((PreparedStatement) statement).executeUpdate()));
        }
        return Optional.empty();
    }
    
    /**
     * Execute advance update.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param trafficCallback traffic callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    public Optional<Integer> executeAdvanceUpdate(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                                  final TrafficExecutorCallback<Integer> trafficCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, trafficCallback));
        }
        return Optional.empty();
    }
    
    /**
     * Execute advance.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param trafficCallback traffic callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    public Optional<Boolean> executeAdvance(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database,
                                            final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                            final TrafficExecutorCallback<Boolean> trafficCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            executeType = ExecuteType.TRAFFIC;
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, trafficCallback));
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            executeType = ExecuteType.FEDERATION;
            ExecuteQueryCallback sqlFederationCallback = JDBCDriverType.STATEMENT.equals(prepareEngine.getType())
                    ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                            queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                    : new PreparedStatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                            queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
            ResultSet resultSet = sqlFederationEngine.executeQuery(prepareEngine, sqlFederationCallback, new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
            return Optional.of(null != resultSet);
        }
        return Optional.empty();
    }
    
    /**
     * Get advanced result set.
     *
     * @return advanced result set
     */
    public Optional<ResultSet> getAdvancedResultSet() {
        switch (executeType) {
            case TRAFFIC:
                return Optional.of(trafficExecutor.getResultSet());
            case FEDERATION:
                return Optional.of(sqlFederationEngine.getResultSet());
            default:
                return Optional.empty();
        }
    }
    
    @Override
    public void close() throws SQLException {
        sqlFederationEngine.close();
        trafficExecutor.close();
    }
    
    public enum ExecuteType {
        
        TRAFFIC, FEDERATION, REGULAR
    }
}
