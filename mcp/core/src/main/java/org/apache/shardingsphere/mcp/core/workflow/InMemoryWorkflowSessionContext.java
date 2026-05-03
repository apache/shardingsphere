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

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

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
 * Session-local in-memory workflow session context.
 */
public final class InMemoryWorkflowSessionContext implements WorkflowSessionContext {
    
    private static final Duration DEFAULT_CONTEXT_TTL = Duration.ofHours(24);
    
    private static final int DEFAULT_MAX_ENTRIES = Integer.MAX_VALUE;
    
    private final Clock clock;
    
    private final Duration contextTtl;
    
    private final int maxEntries;
    
    private final Map<String, WorkflowContextSnapshot> contexts = new ConcurrentHashMap<>();
    
    public InMemoryWorkflowSessionContext() {
        this(Clock.systemUTC(), DEFAULT_CONTEXT_TTL, DEFAULT_MAX_ENTRIES);
    }
    
    public InMemoryWorkflowSessionContext(final Clock clock, final Duration contextTtl, final int maxEntries) {
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
    
    @Override
    public String createPlanId() {
        return "plan-" + UUID.randomUUID();
    }
    
    @Override
    public WorkflowContextSnapshot getOrCreate(final String sessionId, final String planId) {
        String actualPlanId = null == planId ? "" : planId.trim();
        if (!actualPlanId.isEmpty()) {
            return getRequired(actualPlanId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(createPlanId());
        result.setSessionId(sessionId);
        result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return result;
    }
    
    @Override
    public void save(final WorkflowContextSnapshot snapshot) {
        purgeExpiredContexts();
        snapshot.setUpdateTime(clock.instant());
        contexts.put(snapshot.getPlanId(), snapshot.copy());
        purgeOverflowContexts();
    }
    
    @Override
    public Optional<WorkflowContextSnapshot> find(final String planId) {
        purgeExpiredContexts();
        return Optional.ofNullable(contexts.get(planId)).map(WorkflowContextSnapshot::copy);
    }
    
    @Override
    public WorkflowContextSnapshot persist(final WorkflowContextSnapshot snapshot, final String currentStep, final String status) {
        if (null != snapshot.getInteractionPlan()) {
            snapshot.getInteractionPlan().setCurrentStep(currentStep);
        }
        snapshot.setStatus(status);
        save(snapshot);
        return snapshot;
    }
    
    @Override
    public WorkflowContextSnapshot getRequired(final String planId) {
        return find(planId).orElseThrow(() -> new MCPInvalidRequestException(String.format("Unknown plan_id `%s`.", planId)));
    }
    
    @Override
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
