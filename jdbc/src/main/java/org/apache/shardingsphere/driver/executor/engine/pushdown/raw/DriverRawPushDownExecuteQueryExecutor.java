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
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Driver raw push down execute query executor.
 */
public final class DriverRawPushDownExecuteQueryExecutor {
    
    private final ConnectionContext connectionContext;
    
    private final String processId;
    
    private final ShardingSphereMetaData metaData;
    
    private final ConfigurationProperties props;
    
    private final RawExecutor rawExecutor;
    
    public DriverRawPushDownExecuteQueryExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final RawExecutor rawExecutor) {
        connectionContext = connection.getDatabaseConnectionManager().getConnectionContext();
        processId = connection.getProcessId();
        this.metaData = metaData;
        props = metaData.getProps();
        this.rawExecutor = rawExecutor;
    }
    
    /**
     * Execute query.
     *
     * @param database database
     * @param queryContext query context
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @return result set
     * @throws SQLException SQL exception
     */
    public ResultSet executeQuery(final ShardingSphereDatabase database, final QueryContext queryContext, final Statement statement,
                                  final Map<String, Integer> columnLabelAndIndexMap) throws SQLException {
        List<QueryResult> queryResults = getQueryResults(database, queryContext);
        return new ShardingSphereResultSetFactory(connectionContext, metaData, props, Collections.emptyList())
                .newInstance(database, queryContext, queryResults, statement, columnLabelAndIndexMap);
    }
    
    private List<QueryResult> getQueryResults(final ShardingSphereDatabase database, final QueryContext queryContext) throws SQLException {
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), props);
        return rawExecutor.execute(
                createRawExecutionGroupContext(database, executionContext), queryContext, new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = props.<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules()).prepare(database.getName(),
                executionContext, executionContext.getExecutionUnits(), new ExecutionGroupReportContext(processId, database.getName(), connectionContext.getGrantee()));
    }
}
