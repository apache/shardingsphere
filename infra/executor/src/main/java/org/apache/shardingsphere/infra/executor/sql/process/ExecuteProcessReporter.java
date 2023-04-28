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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Optional;

/**
 * Execute process report.
 */
public final class ExecuteProcessReporter {
    
    /**
     * Report connect.
     *
     * @param executionGroupContext execution group context
     */
    public void reportConnect(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(executionGroupContext);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), executeProcessContext);
    }
    
    /**
     * Report execute.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     */
    public void reportExecute(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(queryContext.getSql(), executionGroupContext);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), executeProcessContext);
        ShowProcessListManager.getInstance().putProcessStatement(executeProcessContext.getExecutionID(), executeProcessContext.getProcessStatements());
    }
    
    /**
     * Report complete execution unit.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     */
    public void reportComplete(final String executionID, final SQLExecutionUnit executionUnit) {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit());
        ExecuteProcessContext executeProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        Optional.ofNullable(executeProcessContext.getProcessUnits().get(executeProcessUnit.getUnitID())).ifPresent(ExecuteProcessUnit::switchComplete);
    }
    
    /**
     * Reset report.
     *
     * @param executionID execution ID
     */
    public void reset(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ExecuteProcessContext context = ShowProcessListManager.getInstance().getProcessContext(executionID);
        if (null == context) {
            return;
        }
        for (ExecuteProcessReporterCleaner each : ShardingSphereServiceLoader.getServiceInstances(ExecuteProcessReporterCleaner.class)) {
            each.reset(context);
        }
    }
    
    /**
     * Remove process context.
     *
     * @param executionID execution ID
     */
    public void remove(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
    }
}
