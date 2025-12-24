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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process registry.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessRegistry {
    
    private static final ProcessRegistry INSTANCE = new ProcessRegistry();
    
    private final Map<String, Process> processes = new ConcurrentHashMap<>();
    
    /**
     * Get process registry.
     *
     * @return got instance
     */
    public static ProcessRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put process.
     *
     * @param process process
     */
    public void add(final Process process) {
        if (isSameExecutionProcess(process)) {
            merge(processes.get(process.getId()), process);
        } else {
            processes.put(process.getId(), process);
        }
    }
    
    private boolean isSameExecutionProcess(final Process process) {
        return !Strings.isNullOrEmpty(process.getSql()) && processes.containsKey(process.getId()) && processes.get(process.getId()).getSql().equalsIgnoreCase(process.getSql());
    }
    
    private void merge(final Process oldProcess, final Process newProcess) {
        ShardingSpherePreconditions.checkState(!oldProcess.isInterrupted(), SQLExecutionInterruptedException::new);
        oldProcess.getTotalUnitCount().addAndGet(newProcess.getTotalUnitCount().get());
        oldProcess.getCompletedUnitCount().addAndGet(newProcess.getCompletedUnitCount().get());
        oldProcess.getIdle().set(newProcess.getIdle().get());
        oldProcess.getInterrupted().compareAndSet(false, newProcess.getInterrupted().get());
        oldProcess.getProcessStatements().putAll(newProcess.getProcessStatements());
    }
    
    /**
     * Get process.
     *
     * @param id process ID
     * @return process
     */
    public Process get(final String id) {
        Process result = processes.get(id);
        if (null != result) {
            return result;
        }
        // Support lookup by MySQL thread id (numeric 32-bit id provided by client handshake).
        if (id != null) {
            try {
                long threadId = Long.parseLong(id);
                for (Process each : processes.values()) {
                    Long mysqlThreadId = each.getMySQLThreadId();
                    if (null != mysqlThreadId && mysqlThreadId.longValue() == threadId) {
                        return each;
                    }
                }
            } catch (final NumberFormatException ignored) {
                // not numeric, ignore
            }
        }
        return null;
    }
    
    /**
     * Remove process.
     *
     * @param id process ID
     */
    public void remove(final String id) {
        processes.remove(id);
    }
    
    /**
     * List all processes.
     *
     * @return all processes
     */
    public Collection<Process> listAll() {
        return processes.values();
    }
    
    /**
     * Kill process.
     *
     * @param processId process ID
     * @throws SQLException SQL exception
     */
    public void kill(final String processId) throws SQLException {
        Process process = getInstance().get(processId);
        if (null != process) {
            process.kill();
        }
    }
}
