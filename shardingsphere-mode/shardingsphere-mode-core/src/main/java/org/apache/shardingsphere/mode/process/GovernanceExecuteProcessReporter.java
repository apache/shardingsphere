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

package org.apache.shardingsphere.mode.process;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

import java.util.Optional;

/**
 * Governance execute process reporter.
 */
public final class GovernanceExecuteProcessReporter implements ExecuteProcessReporter {
    
    @Override
    public void report(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext,
                       final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(queryContext.getSql(), executionGroupContext, constants);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), executeProcessContext);
        ShowProcessListManager.getInstance().putProcessStatement(executeProcessContext.getExecutionID(), executeProcessContext.getProcessStatements());
    }
    
    @Override
    public void report(final String executionID, final SQLExecutionUnit executionUnit, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit(), constants);
        ExecuteProcessContext executeProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        Optional.ofNullable(executeProcessContext.getProcessUnits().get(executeProcessUnit.getUnitID())).ifPresent(optional -> optional.setStatus(executeProcessUnit.getStatus()));
    }
    
    @Override
    public void report(final String executionID, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
    }
    
    @Override
    public void reportClean(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
    }
}
