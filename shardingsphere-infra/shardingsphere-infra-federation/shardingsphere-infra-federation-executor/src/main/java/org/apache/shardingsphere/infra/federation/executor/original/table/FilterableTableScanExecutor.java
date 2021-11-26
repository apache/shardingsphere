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

package org.apache.shardingsphere.infra.federation.executor.original.table;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.federation.executor.original.sql.FilterableExecutionContextGenerator;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationTableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Filterable table scan executor.
 */
public final class FilterableTableScanExecutor {
    
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> callback;
    
    private final ConfigurationProperties props;
    
    private final FilterableExecutionContextGenerator executionContextGenerator;
    
    public FilterableTableScanExecutor(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                       final JDBCExecutor jdbcExecutor, final JDBCExecutorCallback<? extends ExecuteResult> callback,
                                       final ConfigurationProperties props, final ExecutionContext routeExecutionContext, final QuoteCharacter quoteCharacter) {
        this.jdbcExecutor = jdbcExecutor;
        this.callback = callback;
        this.prepareEngine = prepareEngine;
        this.props = props;
        executionContextGenerator = new FilterableExecutionContextGenerator(routeExecutionContext, quoteCharacter);
    }
    
    /**
     * Execute.
     *
     * @param tableMetaData federation table meta data
     * @param scanContext filterable table scan context
     * @return query results
     */
    public Collection<QueryResult> execute(final FederationTableMetaData tableMetaData, final FilterableTableScanContext scanContext) {
        ExecutionContext context = executionContextGenerator.generate(tableMetaData, scanContext);
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(context.getRouteContext(), context.getExecutionUnits());
            ExecuteProcessEngine.initialize(context.getLogicSQL(), executionGroupContext, props);
            Collection<QueryResult> result = jdbcExecutor.execute(executionGroupContext, callback).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
}
