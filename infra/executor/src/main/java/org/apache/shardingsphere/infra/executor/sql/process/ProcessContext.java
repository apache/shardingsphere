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
 * Process context.
 */
@Getter
public final class ProcessContext {
    
    private final String processID;
    
    private final String databaseName;
    
    private final String username;
    
    private final String hostname;
    
    private final int totalUnitCount;
    
    private final Collection<Statement> processStatements;
    
    private final AtomicInteger completedUnitCount;
    
    private String sql;
    
    private long startMillis;
    
    private volatile boolean executing;
    
    public ProcessContext(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        this("", executionGroupContext, false);
    }
    
    public ProcessContext(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        this(sql, executionGroupContext, true);
    }
    
    private ProcessContext(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final boolean executing) {
        processID = executionGroupContext.getReportContext().getExecutionID();
        databaseName = executionGroupContext.getReportContext().getDatabaseName();
        Grantee grantee = executionGroupContext.getReportContext().getGrantee();
        username = null == grantee ? null : grantee.getUsername();
        hostname = null == grantee ? null : grantee.getHostname();
        totalUnitCount = executionGroupContext.getInputGroups().stream().mapToInt(each -> each.getInputs().size()).sum();
        processStatements = getProcessStatements(executionGroupContext);
        completedUnitCount = new AtomicInteger(0);
        this.sql = sql;
        startMillis = System.currentTimeMillis();
        this.executing = executing;
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
     * Complete execution units.
     * 
     * @param completedExecutionUnitCount completed execution unit count
     */
    public void completeExecutionUnits(final int completedExecutionUnitCount) {
        completedUnitCount.addAndGet(completedExecutionUnitCount);
    }
    
    /**
     * Get completed unit count.
     * 
     * @return completed unit count
     */
    public int getCompletedUnitCount() {
        return completedUnitCount.get();
    }
    
    /**
     * Reset.
     */
    public void reset() {
        sql = "";
        startMillis = System.currentTimeMillis();
        executing = false;
    }
}
