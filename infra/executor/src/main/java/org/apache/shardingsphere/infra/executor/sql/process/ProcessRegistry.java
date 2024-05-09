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
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process registry.
 */
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
            Process oldProcess = processes.get(process.getId());
            merge(oldProcess, process);
            return;
        }
        processes.put(process.getId(), process);
    }
    
    private boolean isSameExecutionProcess(final Process process) {
        return !Strings.isNullOrEmpty(process.getSql()) && processes.containsKey(process.getId()) && processes.get(process.getId()).getSql().equalsIgnoreCase(process.getSql());
    }
    
    private Process merge(final Process oldProcess, final Process newProcess) {
        if (Strings.isNullOrEmpty(newProcess.getSql()) || !newProcess.getSql().equalsIgnoreCase(oldProcess.getSql())) {
            return newProcess;
        }
        ShardingSpherePreconditions.checkState(!oldProcess.isInterrupted(), SQLExecutionInterruptedException::new);
        oldProcess.getTotalUnitCount().addAndGet(newProcess.getTotalUnitCount().get());
        oldProcess.getCompletedUnitCount().addAndGet(newProcess.getCompletedUnitCount().get());
        oldProcess.getIdle().set(newProcess.getIdle().get());
        oldProcess.getInterrupted().compareAndSet(false, newProcess.getInterrupted().get());
        oldProcess.getProcessStatements().putAll(newProcess.getProcessStatements());
        return oldProcess;
    }
    
    /**
     * Get process.
     * 
     * @param id process ID
     * @return process
     */
    public Process get(final String id) {
        return processes.get(id);
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
     * List all process.
     * 
     * @return all processes
     */
    public Collection<Process> listAll() {
        return processes.values();
    }
}
