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

package org.apache.shardingsphere.infra.optimize.schema.row;

import lombok.RequiredArgsConstructor;

/**
 * Calcite internal executor.
 */
@RequiredArgsConstructor
public final class CalciteRowExecutor {
    
    // TODO Consider use emptyList
    /*private final Collection<ShardingSphereRule> rules;
    
    private final int maxConnectionsSizePerQuery;
    
    private final ExecutorJDBCManager jdbcManager;
    
    private final JDBCExecutor jdbcExecutor;
    
    @Getter
    private final ExecutionContext initialExecutionContext;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> callback;
    
    *
     * Execute.
     *
     * @param context context
     * @return a query result list

    public Collection<QueryResult> execute(final ExecutionContext context) {
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(context);
            return jdbcExecutor.execute(executionGroupContext, callback).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ExecutionContext executionContext) throws SQLException {
        // TODO Set parameters for StatementOption
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, jdbcManager, new StatementOption(true), rules);
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }*/
}
