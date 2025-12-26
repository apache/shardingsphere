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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Process engine.
 */
@HighFrequencyInvocation
public final class ProcessEngine {
    
    /**
     * Connect.
     *
     * @param databaseName database name
     * @return process ID
     */
    public String connect(final String databaseName) {
        return connect(new ExecutionGroupReportContext(getProcessId(), databaseName));
    }
    
    /**
     * Connect.
     *
     * @param databaseName database name
     * @param grantee grantee
     * @return process ID
     */
    public String connect(final String databaseName, final Grantee grantee) {
        return connect(new ExecutionGroupReportContext(getProcessId(), databaseName, grantee));
    }
    
    private String connect(final ExecutionGroupReportContext reportContext) {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext =
                new ExecutionGroupContext<>(Collections.emptyList(), reportContext);
        // Create Process ONCE at connect time (idle state)
        ProcessRegistry.getInstance().add(new Process(executionGroupContext));
        return reportContext.getProcessId();
    }
    
    private String getProcessId() {
        return new UUID(
                ThreadLocalRandom.current().nextLong(),
                ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
    }
    
    /**
     * Disconnect.
     *
     * @param processId process ID
     */
    public void disconnect(final String processId) {
        ProcessRegistry.getInstance().remove(processId);
    }
    
    /**
     * Execute SQL.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     * @throws IllegalStateException if process does not exist and connect() was not called
     */
    public void executeSQL(
                           final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext,
                           final QueryContext queryContext) {
        
        String processId = executionGroupContext.getReportContext().getProcessId();
        Process process = ProcessRegistry.getInstance().get(processId);
        
        // ✅ FIX: lazy create process if missing
        if (null == process) {
            process = new Process("", executionGroupContext);
            ProcessRegistry.getInstance().add(process);
        }
        
        process.mergeExecutionGroupContext(executionGroupContext, queryContext.getSql());
    }
    
    /**
     * Complete SQL unit execution.
     *
     * @param executionUnit execution unit
     * @param processId process ID
     */
    public void completeSQLUnitExecution(final SQLExecutionUnit executionUnit, final String processId) {
        if (Strings.isNullOrEmpty(processId)) {
            return;
        }
        Process process = ProcessRegistry.getInstance().get(processId);
        if (null == process) {
            return;
        }
        process.completeExecutionUnit();
        process.removeProcessStatement(executionUnit.getExecutionUnit());
    }
    
    /**
     * Complete SQL execution.
     *
     * @param processId process ID
     */
    public void completeSQLExecution(final String processId) {
        if (Strings.isNullOrEmpty(processId)) {
            return;
        }
        Process process = ProcessRegistry.getInstance().get(processId);
        if (null != process) {
            process.getIdle().set(true);
        }
    }
}
