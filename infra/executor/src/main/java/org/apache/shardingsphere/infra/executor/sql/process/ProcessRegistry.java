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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ShowProcessListLock;

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
    
    @Getter
    private final Map<String, ShowProcessListLock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get show process list manager.
     *
     * @return show process list manager
     */
    public static ProcessRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put process.
     * 
     * @param processId process ID
     * @param process process
     */
    public void putProcess(final String processId, final Process process) {
        processes.put(processId, process);
    }
    
    /**
     * Get process.
     * 
     * @param processId process ID
     * @return process
     */
    public Process getProcess(final String processId) {
        return processes.get(processId);
    }
    
    /**
     * Remove process.
     * 
     * @param processId process ID
     */
    public void removeProcess(final String processId) {
        processes.remove(processId);
    }
    
    /**
     * Get all process.
     * 
     * @return all processes
     */
    public Collection<Process> getAllProcesses() {
        return processes.values();
    }
}
