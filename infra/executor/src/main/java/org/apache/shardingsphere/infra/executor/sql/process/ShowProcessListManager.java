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

import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Show process list manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowProcessListManager {
    
    private static final ShowProcessListManager INSTANCE = new ShowProcessListManager();
    
    private final Map<String, ProcessContext> processContexts = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<String, Collection<Statement>> processStatements = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<String, ShowProcessListLock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get show process list manager.
     *
     * @return show process list manager
     */
    public static ShowProcessListManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put process context.
     * 
     * @param processID process ID
     * @param processContext process context
     */
    public void putProcessContext(final String processID, final ProcessContext processContext) {
        processContexts.put(processID, processContext);
    }
    
    /**
     * Put process statements.
     *
     * @param processID process ID
     * @param statements statements
     */
    public void putProcessStatement(final String processID, final Collection<Statement> statements) {
        if (statements.isEmpty()) {
            return;
        }
        processStatements.put(processID, statements);
    }
    
    /**
     * Get process context.
     * 
     * @param processID process ID
     * @return process context
     */
    public ProcessContext getProcessContext(final String processID) {
        return processContexts.get(processID);
    }
    
    /**
     * Get process statement.
     *
     * @param processID process ID
     * @return execute statements
     */
    public Collection<Statement> getProcessStatement(final String processID) {
        return processStatements.getOrDefault(processID, Collections.emptyList());
    }
    
    /**
     * Remove process context.
     * 
     * @param processID process ID
     */
    public void removeProcessContext(final String processID) {
        processContexts.remove(processID);
    }
    
    /**
     * Remove process statement.
     *
     * @param processID process ID
     */
    public void removeProcessStatement(final String processID) {
        processStatements.remove(processID);
    }
    
    /**
     * Get all process contexts.
     * 
     * @return all process contexts
     */
    public Collection<ProcessContext> getAllProcessContexts() {
        return processContexts.values();
    }
}
