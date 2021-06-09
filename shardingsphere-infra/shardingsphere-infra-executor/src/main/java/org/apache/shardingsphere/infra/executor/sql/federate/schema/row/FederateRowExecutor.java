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

package org.apache.shardingsphere.infra.executor.sql.federate.schema.row;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.federate.schema.table.generator.FederateExecutionContextGenerator;
import org.apache.shardingsphere.infra.executor.sql.federate.schema.table.generator.FederateExecutionSQLGenerator;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Federate row executor.
 */
@RequiredArgsConstructor
public final class FederateRowExecutor {
    
    // TODO Consider use emptyList
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    private final ExecutorJDBCManager jdbcManager;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final ExecutionContext routeExecutionContext;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> callback;
    
    /**
     * Execute.
     *
     * @param logicTable logic table
     * @param root root
     * @param filters filter
     * @param projects projects
     * @return a query result list
     */
    public Collection<QueryResult> execute(final String logicTable, final DataContext root, final List<RexNode> filters, final int[] projects) {
        FederateExecutionContextGenerator generator = new FederateExecutionContextGenerator(logicTable, routeExecutionContext, new FederateExecutionSQLGenerator(root, filters, projects));
        return execute(generator.generate());
    }
    
    private Collection<QueryResult> execute(final ExecutionContext context) {
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(context);
            ExecuteProcessEngine.initialize(context.getSqlStatementContext(), executionGroupContext, props);
            Collection<QueryResult> result = jdbcExecutor.execute(executionGroupContext, callback).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ExecutionContext executionContext) throws SQLException {
        // TODO Set parameters for StatementOption
        int maxConnectionsSizePerQuery = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Collection<ExecutionUnit> executionUnits = executionContext.getExecutionUnits();
        String type = executionUnits.stream().anyMatch(each -> !each.getSqlUnit().getParameters().isEmpty()) ? JDBCDriverType.PREPARED_STATEMENT : JDBCDriverType.STATEMENT;
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                type, maxConnectionsSizePerQuery, jdbcManager, new StatementOption(true), rules);
        return prepareEngine.prepare(executionContext.getRouteContext(), executionUnits);
    }
}
