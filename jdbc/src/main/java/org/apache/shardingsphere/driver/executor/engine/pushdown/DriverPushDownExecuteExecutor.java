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
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc.DriverJDBCPushDownExecuteExecutor;
import org.apache.shardingsphere.driver.executor.engine.pushdown.raw.DriverRawPushDownExecuteExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Driver push down execute executor.
 */
public final class DriverPushDownExecuteExecutor {
    
    private final DriverJDBCPushDownExecuteExecutor jdbcPushDownExecutor;
    
    private final DriverRawPushDownExecuteExecutor rawPushDownExecutor;
    
    public DriverPushDownExecuteExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final Grantee grantee,
                                         final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor) {
        jdbcPushDownExecutor = new DriverJDBCPushDownExecuteExecutor(connection, metaData, grantee, jdbcExecutor);
        rawPushDownExecutor = new DriverRawPushDownExecuteExecutor(connection, metaData, grantee, rawExecutor);
    }
    
    /**
     * Execute.
     * 
     * @param database database
     * @param executionContext execution context
     * @param prepareEngine prepare engine
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return is query
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? jdbcPushDownExecutor.execute(database, executionContext, prepareEngine, executeCallback, addCallback, replayCallback)
                : rawPushDownExecutor.execute(database, executionContext);
    }
    
    /**
     * Get result set.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @param statement statement
     * @param statements statements
     * @return result set
     * @throws SQLException SQL exception
     */
    public Optional<ResultSet> getResultSet(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext,
                                            final Statement statement, final List<? extends Statement> statements) throws SQLException {
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? jdbcPushDownExecutor.getResultSet(database, sqlStatementContext, statement, statements)
                : Optional.empty();
    }
}
