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

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteQueryCallbackFactory;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc.DriverJDBCPushDownExecuteQueryExecutor;
import org.apache.shardingsphere.driver.executor.engine.pushdown.raw.DriverRawPushDownExecuteQueryExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Driver execute query executor.
 */
public final class DriverExecuteQueryExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final DriverJDBCPushDownExecuteQueryExecutor driverJDBCPushDownExecutor;
    
    private final DriverRawPushDownExecuteQueryExecutor driverRawPushDownExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    public DriverExecuteQueryExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor,
                                      final SQLFederationEngine sqlFederationEngine) {
        this.connection = connection;
        this.metaData = metaData;
        driverJDBCPushDownExecutor = new DriverJDBCPushDownExecuteQueryExecutor(connection, metaData, jdbcExecutor);
        driverRawPushDownExecutor = new DriverRawPushDownExecuteQueryExecutor(connection, metaData, rawExecutor);
        this.sqlFederationEngine = sqlFederationEngine;
    }
    
    /**
     * Execute query.
     *
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public ResultSet executeQuery(final ShardingSphereDatabase database, final QueryContext queryContext,
                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final Statement statement, final Map<String, Integer> columnLabelAndIndexMap,
                                  final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        if (sqlFederationEngine.decide(queryContext, metaData.getGlobalRuleMetaData())) {
            return sqlFederationEngine.executeQuery(prepareEngine,
                    new ExecuteQueryCallbackFactory(prepareEngine.getType()).newInstance(database, queryContext), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
        }
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? driverJDBCPushDownExecutor.executeQuery(database, queryContext, prepareEngine, statement, columnLabelAndIndexMap, addCallback, replayCallback)
                : driverRawPushDownExecutor.executeQuery(database, queryContext, statement, columnLabelAndIndexMap);
    }
}
