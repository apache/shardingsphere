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

package org.apache.shardingsphere.infra.executor.sql.process.model;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Execute process context.
 */
@Getter
public final class ExecuteProcessContext {
    
    private final String executionID;
    
    private final String databaseName;
    
    private final String username;
    
    private final String hostname;
    
    private final Map<String, ExecuteProcessUnit> processUnits = new HashMap<>();
    
    private final Collection<Statement> processStatements = new LinkedList<>();
    
    private String sql;
    
    private long startMillis;
    
    private ExecuteProcessStatus status;
    
    public ExecuteProcessContext(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessStatus status) {
        executionID = executionGroupContext.getReportContext().getExecutionID();
        databaseName = executionGroupContext.getReportContext().getDatabaseName();
        Grantee grantee = executionGroupContext.getReportContext().getGrantee();
        username = null == grantee ? null : grantee.getUsername();
        hostname = null == grantee ? null : grantee.getHostname();
        this.sql = sql;
        this.status = status;
        startMillis = System.currentTimeMillis();
        addProcessUnitsAndStatements(executionGroupContext, status);
    }
    
    private void addProcessUnitsAndStatements(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessStatus processStatus) {
        for (ExecutionGroup<? extends SQLExecutionUnit> each : executionGroupContext.getInputGroups()) {
            for (SQLExecutionUnit executionUnit : each.getInputs()) {
                ExecuteProcessUnit processUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit(), processStatus);
                processUnits.put(processUnit.getUnitID(), processUnit);
                if (executionUnit instanceof JDBCExecutionUnit) {
                    processStatements.add(((JDBCExecutionUnit) executionUnit).getStorageResource());
                }
            }
        }
    }
    
    /**
     * Reset.
     */
    public void reset() {
        sql = "";
        startMillis = System.currentTimeMillis();
        status = ExecuteProcessStatus.SLEEP;
    }
}
