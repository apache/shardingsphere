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
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessStatusEnum;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;

import java.util.Optional;

/**
 * Execute process report.
 */
public final class ExecuteProcessReporter {
    
    /**
     * Report this connection for proxy.
     *
     * @param executionGroupContext execution group context
     */
    public void report(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext("", executionGroupContext, ExecuteProcessStatusEnum.SLEEP, true);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), executeProcessContext);
    }
    
    /**
     * Report the summary of this task.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @param processStatus process status
     */
    public void report(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext,
                       final ExecuteProcessStatusEnum processStatus) {
        ExecuteProcessContext originExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionGroupContext.getReportContext().getExecutionID());
        boolean isProxyContext = null != originExecuteProcessContext && originExecuteProcessContext.isProxyContext();
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(queryContext.getSql(), executionGroupContext, processStatus, isProxyContext);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), executeProcessContext);
        ShowProcessListManager.getInstance().putProcessStatement(executeProcessContext.getExecutionID(), executeProcessContext.getProcessStatements());
    }
    
    /**
     * Report a unit of this task.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     * @param processStatus process status
     */
    public void report(final String executionID, final SQLExecutionUnit executionUnit, final ExecuteProcessStatusEnum processStatus) {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit(), processStatus);
        ExecuteProcessContext executeProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        Optional.ofNullable(executeProcessContext.getProcessUnits().get(executeProcessUnit.getUnitID())).ifPresent(optional -> optional.setProcessStatus(executeProcessUnit.getProcessStatus()));
    }
    
    /**
     * Report clean the task.
     *
     * @param executionID execution ID
     */
    public void reportClean(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ExecuteProcessContext executeProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        if (null == executeProcessContext) {
            return;
        }
        if (executeProcessContext.isProxyContext()) {
            executeProcessContext.resetExecuteProcessContextToSleep();
        } else {
            ShowProcessListManager.getInstance().removeProcessContext(executionID);
        }
    }
    
    /**
     * Report remove process context.
     *
     * @param executionID execution ID
     */
    public void reportRemove(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
    }
}
