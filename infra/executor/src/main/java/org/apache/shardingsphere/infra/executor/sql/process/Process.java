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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process.
 */
@RequiredArgsConstructor
@Getter
public final class Process {
    
    private final String id;
    
    private final long startMillis;
    
    private final String sql;
    
    private final String databaseName;
    
    private final String username;
    
    private final String hostname;
    
    private final int totalUnitCount;
    
    private final Collection<Statement> processStatements;
    
    private final AtomicInteger completedUnitCount;
    
    private final boolean idle;
    
    public Process(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        this("", executionGroupContext, true);
    }
    
    public Process(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        this(sql, executionGroupContext, false);
    }
    
    private Process(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final boolean idle) {
        id = executionGroupContext.getReportContext().getProcessId();
        startMillis = System.currentTimeMillis();
        this.sql = sql;
        databaseName = executionGroupContext.getReportContext().getDatabaseName();
        Grantee grantee = executionGroupContext.getReportContext().getGrantee();
        username = null == grantee ? null : grantee.getUsername();
        hostname = null == grantee ? null : grantee.getHostname();
        totalUnitCount = getTotalUnitCount(executionGroupContext);
        processStatements = getProcessStatements(executionGroupContext);
        completedUnitCount = new AtomicInteger(0);
        this.idle = idle;
    }
    
    private int getTotalUnitCount(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        int result = 0;
        for (ExecutionGroup<? extends SQLExecutionUnit> each : executionGroupContext.getInputGroups()) {
            result += each.getInputs().size();
        }
        return result;
    }
    
    private Collection<Statement> getProcessStatements(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        Collection<Statement> result = new LinkedList<>();
        for (ExecutionGroup<? extends SQLExecutionUnit> each : executionGroupContext.getInputGroups()) {
            for (SQLExecutionUnit executionUnit : each.getInputs()) {
                if (executionUnit instanceof JDBCExecutionUnit) {
                    result.add(((JDBCExecutionUnit) executionUnit).getStorageResource());
                }
            }
        }
        return result;
    }
    
    /**
     * Complete execution unit.
     */
    public void completeExecutionUnit() {
        completedUnitCount.incrementAndGet();
    }
    
    /**
     * Get completed unit count.
     * 
     * @return completed unit count
     */
    public int getCompletedUnitCount() {
        return completedUnitCount.get();
    }
}
