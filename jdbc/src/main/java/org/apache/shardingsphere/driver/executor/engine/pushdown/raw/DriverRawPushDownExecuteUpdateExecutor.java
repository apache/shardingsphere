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

package org.apache.shardingsphere.driver.executor.engine.pushdown.raw;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Driver raw push down execute update executor.
 */
public final class DriverRawPushDownExecuteUpdateExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final String processId;
    
    private final ConfigurationProperties props;
    
    private final RawExecutor rawExecutor;
    
    public DriverRawPushDownExecuteUpdateExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final RawExecutor rawExecutor) {
        this.connection = connection;
        processId = connection.getProcessId();
        props = metaData.getProps();
        this.rawExecutor = rawExecutor;
    }
    
    /**
     * Execute update.
     *
     * @param database database
     * @param executionContext execution context
     * @return updated row count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        return accumulate(rawExecutor.execute(createRawExecutionGroupContext(database, executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback()));
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = props.<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules()).prepare(database.getName(), executionContext,
                executionContext.getExecutionUnits(), new ExecutionGroupReportContext(processId, database.getName(), connection.getDatabaseConnectionManager().getConnectionContext().getGrantee()));
    }
}
