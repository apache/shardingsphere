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
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Getter
public final class Process {
    private volatile Long mysqlThreadId;

    private final Map<Integer, Statement> processStatements = new ConcurrentHashMap<>();
    
    private final String id;
    
    private final long startMillis;
    
    private final String sql;
    
    private final String databaseName;
    
    private final String username;
    
    private final String hostname;
    
    private final AtomicInteger totalUnitCount;
    
    private final AtomicInteger completedUnitCount;
    
    private final AtomicBoolean idle;
    
    private final AtomicBoolean interrupted;
    
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
        Optional<Grantee> grantee = executionGroupContext.getReportContext().getGrantee();
        username = grantee.map(Grantee::getUsername).orElse("");
        hostname = grantee.map(Grantee::getHostname).orElse("");
        totalUnitCount = new AtomicInteger(getTotalUnitCount(executionGroupContext));
        processStatements.putAll(createProcessStatements(executionGroupContext));
        completedUnitCount = new AtomicInteger(0);
        this.idle = new AtomicBoolean(idle);
        interrupted = new AtomicBoolean();
    }
    
    private int getTotalUnitCount(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        int result = 0;
        for (ExecutionGroup<? extends SQLExecutionUnit> each : executionGroupContext.getInputGroups()) {
            result += each.getInputs().size();
        }
        return result;
    }
    
    private Map<Integer, Statement> createProcessStatements(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        Map<Integer, Statement> result = new LinkedHashMap<>();
        for (ExecutionGroup<? extends SQLExecutionUnit> each : executionGroupContext.getInputGroups()) {
            for (SQLExecutionUnit executionUnit : each.getInputs()) {
                if (executionUnit instanceof JDBCExecutionUnit) {
                    JDBCExecutionUnit jdbcExecutionUnit = (JDBCExecutionUnit) executionUnit;
                    result.put(System.identityHashCode(jdbcExecutionUnit.getExecutionUnit()), jdbcExecutionUnit.getStorageResource());
                }
            }
        }
        return result;
    }
    public void setMySQLThreadId(final long threadId) {
        this.mysqlThreadId = threadId;
    }

    public Optional<Long> getMySQLThreadId() {
        return Optional.ofNullable(mysqlThreadId);
    }


    /**
     * Complete execution unit.
     */
    public void completeExecutionUnit() {
        completedUnitCount.incrementAndGet();
    }
    
    /**
     * Is interrupted.
     *
     * @return interrupted
     */
    public boolean isInterrupted() {
        return interrupted.get();
    }
    
    /**
     * Set interrupted.
     *
     * @param interrupted interrupted
     */
    public void setInterrupted(final boolean interrupted) {
        this.interrupted.set(interrupted);
    }
    
    /**
     * Is idle.
     *
     * @return idle
     */
    public boolean isIdle() {
        return idle.get();
    }
    
    /**
     * Remove process statement.
     *
     * @param executionUnit execution unit
     */
    public void removeProcessStatement(final ExecutionUnit executionUnit) {
        processStatements.remove(System.identityHashCode(executionUnit));
    }
    
    /**
     * Kill process.
     *
     * @throws SQLException SQL exception
     */
    public void kill() throws SQLException {
        setInterrupted(true);
        for (Statement each : processStatements.values()) {
            each.cancel();
        }
    }
}
