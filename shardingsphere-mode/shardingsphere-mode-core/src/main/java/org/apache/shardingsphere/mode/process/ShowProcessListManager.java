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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;

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
    
    @Getter
    private final Map<String, ExecuteProcessContext> processContexts = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<String, Collection<Statement>> processStatements = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<String, ShowProcessListSimpleLock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get show process list manager.
     *
     * @return show process list manager
     */
    public static ShowProcessListManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put execute process context.
     * 
     * @param executionId execution id
     * @param processContext process context
     */
    public void putProcessContext(final String executionId, final ExecuteProcessContext processContext) {
        processContexts.put(executionId, processContext);
    }
    
    /**
     * Put process statements.
     *
     * @param executionId execution id
     * @param statements statements
     */
    public void putProcessStatement(final String executionId, final Collection<Statement> statements) {
        if (statements.isEmpty()) {
            return;
        }
        processStatements.put(executionId, statements);
    }
    
    /**
     * Get execute process context.
     * 
     * @param executionId execution id
     * @return execute process context
     */
    public ExecuteProcessContext getProcessContext(final String executionId) {
        return processContexts.get(executionId);
    }
    
    /**
     * Get execute process statement.
     *
     * @param executionId execution id
     * @return execute statements
     */
    public Collection<Statement> getProcessStatement(final String executionId) {
        return processStatements.getOrDefault(executionId, Collections.emptyList());
    }
    
    /**
     * Remove execute process context.
     * 
     * @param executionId execution id
     */
    public void removeProcessContext(final String executionId) {
        processContexts.remove(executionId);
    }
    
    /**
     * Remove execute process statement.
     *
     * @param executionId execution id
     */
    public void removeProcessStatement(final String executionId) {
        processStatements.remove(executionId);
    }
    
    /**
     * Get all execute process context.
     * 
     * @return collection execute process context
     */
    public Collection<ExecuteProcessContext> getAllProcessContext() {
        return processContexts.values();
    }
}
