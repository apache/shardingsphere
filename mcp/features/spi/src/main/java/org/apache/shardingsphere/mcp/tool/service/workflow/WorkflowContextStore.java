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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Workflow context store.
 */
public final class WorkflowContextStore {
    
    private static final Duration CONTEXT_TTL = Duration.ofHours(24);
    
    private final Map<String, WorkflowContextSnapshot> contexts = new ConcurrentHashMap<>();
    
    /**
     * Create plan identifier.
     *
     * @return plan identifier
     */
    public String createPlanId() {
        return "plan-" + UUID.randomUUID();
    }
    
    /**
     * Save snapshot.
     *
     * @param snapshot workflow snapshot
     */
    public void save(final WorkflowContextSnapshot snapshot) {
        purgeExpiredContexts();
        Instant now = Instant.now();
        snapshot.setUpdateTime(now);
        WorkflowContextSnapshot copiedSnapshot = WorkflowSnapshotCloner.cloneSnapshot(snapshot);
        copiedSnapshot.setUpdateTime(now);
        contexts.put(copiedSnapshot.getPlanId(), copiedSnapshot);
    }
    
    /**
     * Find snapshot.
     *
     * @param planId plan identifier
     * @return snapshot when present
     */
    public Optional<WorkflowContextSnapshot> find(final String planId) {
        purgeExpiredContexts();
        return Optional.ofNullable(contexts.get(planId)).map(WorkflowSnapshotCloner::cloneSnapshot);
    }
    
    /**
     * Get required snapshot.
     *
     * @param planId plan identifier
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot getRequired(final String planId) {
        return find(planId).orElseThrow(() -> new MCPInvalidRequestException(String.format("Unknown plan_id `%s`.", planId)));
    }
    
    /**
     * Remove snapshot.
     *
     * @param planId plan identifier
     */
    public void remove(final String planId) {
        contexts.remove(planId);
    }
    
    private void purgeExpiredContexts() {
        Instant expirationTime = Instant.now().minus(CONTEXT_TTL);
        contexts.entrySet().removeIf(each -> null == each.getValue().getUpdateTime() || each.getValue().getUpdateTime().isBefore(expirationTime));
    }
}
