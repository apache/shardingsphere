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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Governance execute process reporter.
 */
public final class GovernanceExecuteProcessReporter implements ExecuteProcessReporter {
    
    @Override
    public void report(final LogicSQL logicSQL, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext,
                       final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(logicSQL.getSql(), executionGroupContext, constants);
        ShowProcessListManager.getInstance().putProcessContext(executeProcessContext.getExecutionID(), new YamlExecuteProcessContext(executeProcessContext));
        ShowProcessListManager.getInstance().putProcessStatement(executeProcessContext.getExecutionID(), collectProcessStatement(executionGroupContext));
    }
    
    @Override
    public void report(final String executionID, final SQLExecutionUnit executionUnit, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit(), constants);
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        for (YamlExecuteProcessUnit each : yamlExecuteProcessContext.getUnitStatuses()) {
            if (each.getUnitID().equals(executeProcessUnit.getUnitID())) {
                each.setStatus(executeProcessUnit.getStatus());
            }
        }
    }
    
    @Override
    public void report(final String executionID, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        for (YamlExecuteProcessUnit each : yamlExecuteProcessContext.getUnitStatuses()) {
            if (each.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                return;
            }
        }
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
    }
    
    @Override
    public void reportClean(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
    }
    
    private List<Statement> collectProcessStatement(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        List<Statement> statements = new ArrayList<>();
        if (null == executionGroupContext.getInputGroups()) {
            return statements;
        }
        for (ExecutionGroup<? extends SQLExecutionUnit> inputGroup : executionGroupContext.getInputGroups()) {
            if (null == inputGroup.getInputs()) {
                continue;
            }
            for (SQLExecutionUnit executionUnit : inputGroup.getInputs()) {
                if (executionUnit instanceof JDBCExecutionUnit) {
                    JDBCExecutionUnit jdbcExecutionUnit = (JDBCExecutionUnit) executionUnit;
                    statements.add(jdbcExecutionUnit.getStorageResource());
                }
            }
        }
        return statements;
    }
}
