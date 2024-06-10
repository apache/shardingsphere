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

package org.apache.shardingsphere.driver.executor.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteQueryCallbackFactory;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.pushdown.DriverPushDownExecuteExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Driver execute executor.
 */
@RequiredArgsConstructor
public final class DriverExecuteExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final DriverPushDownExecuteExecutor pushDownExecuteExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private ExecuteType executeType = ExecuteType.PUSH_DOWN;
    
    public DriverExecuteExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final Grantee grantee,
                                 final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor,
                                 final TrafficExecutor trafficExecutor, final SQLFederationEngine sqlFederationEngine) {
        this.connection = connection;
        this.metaData = metaData;
        pushDownExecuteExecutor = new DriverPushDownExecuteExecutor(connection, metaData, grantee, jdbcExecutor, rawExecutor);
        this.trafficExecutor = trafficExecutor;
        this.sqlFederationEngine = sqlFederationEngine;
    }
    
    /**
     * Execute.
     *
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            executeType = ExecuteType.TRAFFIC;
            return trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, executeCallback::execute);
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            executeType = ExecuteType.FEDERATION;
            ResultSet resultSet = sqlFederationEngine.executeQuery(prepareEngine,
                    new ExecuteQueryCallbackFactory(prepareEngine.getType()).newInstance(database, queryContext), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
            return null != resultSet;
        }
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(
                queryContext, database, metaData.getGlobalRuleMetaData(), metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
        return pushDownExecuteExecutor.execute(database, executionContext, prepareEngine, executeCallback, addCallback, replayCallback);
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public Optional<ResultSet> getResultSet() {
        switch (executeType) {
            case TRAFFIC:
                return Optional.of(trafficExecutor.getResultSet());
            case FEDERATION:
                return Optional.of(sqlFederationEngine.getResultSet());
            default:
                return Optional.empty();
        }
    }
    
    public enum ExecuteType {
        
        TRAFFIC,
        
        FEDERATION,
        
        PUSH_DOWN
    }
}
