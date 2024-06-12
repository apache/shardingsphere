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

package org.apache.shardingsphere.driver.executor.engine.pushdown;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc.DriverJDBCPushDownExecuteUpdateExecutor;
import org.apache.shardingsphere.driver.executor.engine.pushdown.raw.DriverRawPushDownExecuteUpdateExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Driver push down execute update executor.
 */
public final class DriverPushDownExecuteUpdateExecutor {
    
    private final DriverJDBCPushDownExecuteUpdateExecutor jdbcPushDownExecutor;
    
    private final DriverRawPushDownExecuteUpdateExecutor rawPushDownExecutor;
    
    public DriverPushDownExecuteUpdateExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final Grantee grantee,
                                               final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor) {
        jdbcPushDownExecutor = new DriverJDBCPushDownExecuteUpdateExecutor(connection, metaData, grantee, jdbcExecutor);
        rawPushDownExecutor = new DriverRawPushDownExecuteUpdateExecutor(connection, metaData, grantee, rawExecutor);
    }
    
    /**
     * Execute update.
     *
     * @param database database
     * @param executionContext execution context
     * @param prepareEngine prepare engine
     * @param updateCallback statement execute update callback
     * @param replayCallback statement replay callback
     * @param addCallback statement add callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int executeUpdate(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                             final StatementExecuteUpdateCallback updateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? jdbcPushDownExecutor.executeUpdate(database, executionContext, prepareEngine, updateCallback, addCallback, replayCallback)
                : rawPushDownExecutor.executeUpdate(database, executionContext);
    }
}
