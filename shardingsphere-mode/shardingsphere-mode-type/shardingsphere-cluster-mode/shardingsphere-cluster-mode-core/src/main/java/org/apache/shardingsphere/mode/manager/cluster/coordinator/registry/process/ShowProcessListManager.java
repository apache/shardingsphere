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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.lock.ShowProcessListSimpleLock;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Show process list manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowProcessListManager {
    
    private static final ShowProcessListManager INSTANCE = new ShowProcessListManager();
    
    private final Map<String, YamlExecuteProcessContext> processContextMap = new ConcurrentHashMap<>();
    
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
    public void putProcessContext(final String executionId, final YamlExecuteProcessContext processContext) {
        processContextMap.put(executionId, processContext);
    }
    
    /**
     * Get execute process context.
     * 
     * @param executionId execution id
     * @return execute process context
     */
    public YamlExecuteProcessContext getProcessContext(final String executionId) {
        return processContextMap.get(executionId);
    }
    
    /**
     * Remove execute process context.
     * 
     * @param executionId execution id
     */
    public void removeProcessContext(final String executionId) {
        processContextMap.remove(executionId);
    }
    
    /**
     * Get all execute process context.
     * 
     * @return collection execute process context
     */
    public Collection<YamlExecuteProcessContext> getAllProcessContext() {
        return processContextMap.values();
    }
}
