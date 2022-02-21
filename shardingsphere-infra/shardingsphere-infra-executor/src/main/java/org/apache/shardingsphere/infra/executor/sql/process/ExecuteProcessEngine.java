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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * Execute process engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecuteProcessEngine {
    
    private static final Collection<ExecuteProcessReporter> HANDLERS;
    
    static {
        ShardingSphereServiceLoader.register(ExecuteProcessReporter.class);
        HANDLERS = ShardingSphereServiceLoader.newServiceInstances(ExecuteProcessReporter.class);
    }
    
    /**
     * Initialize.
     *
     * @param logicSQL logic SQL
     * @param executionGroupContext execution group context
     * @param props configuration properties
     */
    public static void initialize(final LogicSQL logicSQL, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ConfigurationProperties props) {
        SQLStatementContext<?> context = logicSQL.getSqlStatementContext();
        if (!HANDLERS.isEmpty() && ExecuteProcessStrategyEvaluator.evaluate(context, executionGroupContext, props)) {
            ExecutorDataMap.getValue().put(ExecuteProcessConstants.EXECUTE_ID.name(), executionGroupContext.getExecutionID());
            HANDLERS.iterator().next().report(logicSQL, executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
        }
    }
    
    /**
     * Clean.
     */
    public static void clean() {
        ExecutorDataMap.getValue().remove(ExecuteProcessConstants.EXECUTE_ID.name());
    }
    
    /**
     * Finish.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     */
    public static void finish(final String executionID, final SQLExecutionUnit executionUnit) {
        if (!HANDLERS.isEmpty()) {
            HANDLERS.iterator().next().report(executionID, executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
        }
    }
    
    /**
     * Finish.
     *
     * @param executionID execution ID
     */
    public static void finish(final String executionID) {
        if (!HANDLERS.isEmpty() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            HANDLERS.iterator().next().report(executionID, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
        }
    }
}
