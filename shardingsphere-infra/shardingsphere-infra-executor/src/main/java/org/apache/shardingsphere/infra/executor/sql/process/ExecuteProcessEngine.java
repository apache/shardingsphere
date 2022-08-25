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

package org.apache.shardingsphere.infra.executor.sql.process;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporterFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Optional;

/**
 * Execute process engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecuteProcessEngine {
    
    /**
     * Initialize.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @param eventBusContext event bus context             
     */
    public static void initialize(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final EventBusContext eventBusContext) {
        SQLStatementContext<?> context = queryContext.getSqlStatementContext();
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && (context.getSqlStatement() instanceof DDLStatement || context.getSqlStatement() instanceof DMLStatement)) {
            ExecutorDataMap.getValue().put(ExecuteProcessConstants.EXECUTE_ID.name(), executionGroupContext.getExecutionID());
            reporter.get().report(queryContext, executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START, eventBusContext);
        }
    }
    
    /**
     * Clean.
     */
    public static void clean() {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.get().reportClean(ExecutorDataMap.getValue().get(ExecuteProcessConstants.EXECUTE_ID.name()).toString());
        }
        ExecutorDataMap.getValue().remove(ExecuteProcessConstants.EXECUTE_ID.name());
    }
    
    /**
     * Finish.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     * @param eventBusContext event bus context                      
     */
    public static void finish(final String executionID, final SQLExecutionUnit executionUnit, final EventBusContext eventBusContext) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        reporter.ifPresent(optional -> optional.report(executionID, executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE, eventBusContext));
    }
    
    /**
     * Finish.
     *
     * @param executionID execution ID
     * @param eventBusContext event bus context                    
     */
    public static void finish(final String executionID, final EventBusContext eventBusContext) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.get().report(executionID, ExecuteProcessConstants.EXECUTE_STATUS_DONE, eventBusContext);
        }
    }
}
