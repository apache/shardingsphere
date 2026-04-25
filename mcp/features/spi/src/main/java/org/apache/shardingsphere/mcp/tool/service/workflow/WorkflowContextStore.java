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
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshots;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-local in-memory workflow context store.
 */
public final class WorkflowContextStore {
    
    private static final Duration DEFAULT_CONTEXT_TTL = Duration.ofHours(24);
    
    private static final int DEFAULT_MAX_ENTRIES = Integer.MAX_VALUE;
    
    private final Clock clock;
    
    private final Duration contextTtl;
    
    private final int maxEntries;
    
    private final Map<String, WorkflowContextSnapshot> contexts = new ConcurrentHashMap<>();
    
    public WorkflowContextStore() {
        this(Clock.systemUTC(), DEFAULT_CONTEXT_TTL, DEFAULT_MAX_ENTRIES);
    }
    
    public WorkflowContextStore(final Clock clock, final Duration contextTtl, final int maxEntries) {
        if (contextTtl.isZero() || contextTtl.isNegative()) {
            throw new IllegalArgumentException("Context TTL must be positive.");
        }
        if (0 >= maxEntries) {
            throw new IllegalArgumentException("Max entries must be positive.");
        }
        this.clock = clock;
        this.contextTtl = contextTtl;
        this.maxEntries = maxEntries;
    }
    
    /**
     * Create plan identifier.
     *
     * @return plan identifier
     */
    public String createPlanId() {
        return "plan-" + UUID.randomUUID();
    }
    
    /**
     * Get an existing workflow snapshot or create a new one.
     *
     * @param sessionId session identifier
     * @param planId plan identifier
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot getOrCreate(final String sessionId, final String planId) {
        String actualPlanId = WorkflowSqlUtils.trimToEmpty(planId);
        if (!actualPlanId.isEmpty()) {
            return getRequired(actualPlanId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(createPlanId());
        result.setSessionId(sessionId);
        result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return result;
    }
    
    /**
     * Save snapshot.
     *
     * @param snapshot workflow snapshot
     */
    public void save(final WorkflowContextSnapshot snapshot) {
        purgeExpiredContexts();
        Instant now = clock.instant();
        snapshot.setUpdateTime(now);
        contexts.put(snapshot.getPlanId(), WorkflowContextSnapshots.copy(snapshot));
        purgeOverflowContexts();
    }
    
    /**
     * Find snapshot.
     *
     * @param planId plan identifier
     * @return snapshot when present
     */
    public Optional<WorkflowContextSnapshot> find(final String planId) {
        purgeExpiredContexts();
        return Optional.ofNullable(contexts.get(planId)).map(WorkflowContextSnapshots::copy);
    }
    
    /**
     * Persist snapshot with lifecycle state.
     *
     * @param snapshot workflow snapshot
     * @param currentStep current interaction step
     * @param status workflow status
     * @return persisted snapshot
     */
    public WorkflowContextSnapshot persist(final WorkflowContextSnapshot snapshot, final String currentStep, final String status) {
        if (null != snapshot.getInteractionPlan()) {
            snapshot.getInteractionPlan().setCurrentStep(currentStep);
        }
        snapshot.setStatus(status);
        save(snapshot);
        return snapshot;
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
        Instant expirationTime = clock.instant().minus(contextTtl);
        contexts.entrySet().removeIf(each -> null == each.getValue().getUpdateTime() || each.getValue().getUpdateTime().isBefore(expirationTime));
    }
    
    private void purgeOverflowContexts() {
        while (contexts.size() > maxEntries) {
            String planIdToEvict = contexts.entrySet().stream()
                    .min(Comparator.comparing((Entry<String, WorkflowContextSnapshot> each) -> null == each.getValue().getUpdateTime() ? Instant.EPOCH : each.getValue().getUpdateTime())
                            .thenComparing(Entry::getKey))
                    .map(Entry::getKey).orElse(null);
            if (null == planIdToEvict) {
                return;
            }
            contexts.remove(planIdToEvict);
        }
    }
}
