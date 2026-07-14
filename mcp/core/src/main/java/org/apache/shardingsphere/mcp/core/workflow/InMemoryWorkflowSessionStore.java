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

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

import java.time.Clock;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory workflow session store.
 */
public final class InMemoryWorkflowSessionStore {
    
    private final Clock clock = Clock.systemUTC();
    
    private final Map<String, Map<String, WorkflowContextSnapshot>> sessionSnapshots = new ConcurrentHashMap<>();
    
    /**
     * Get a workflow context bound to one MCP session.
     *
     * @param sessionId session identifier
     * @return session-bound workflow context
     */
    public WorkflowSessionContext getSessionContext(final String sessionId) {
        return new SessionContext(sessionId);
    }
    
    /**
     * Remove all workflow snapshots owned by one MCP session.
     *
     * @param sessionId session identifier
     */
    public void removeSession(final String sessionId) {
        sessionSnapshots.remove(sessionId);
    }
    
    private final class SessionContext implements WorkflowSessionContext {
        
        private final String sessionId;
        
        private SessionContext(final String sessionId) {
            this.sessionId = sessionId;
        }
        
        @Override
        public WorkflowContextSnapshot getOrCreate(final String planId) {
            String actualPlanId = null == planId ? "" : planId.trim();
            if (!actualPlanId.isEmpty()) {
                return getRequired(actualPlanId);
            }
            WorkflowContextSnapshot result = new WorkflowContextSnapshot();
            result.setPlanId("plan-" + UUID.randomUUID());
            result.setSessionId(sessionId);
            result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return result;
        }
        
        @Override
        public void save(final WorkflowContextSnapshot snapshot) {
            snapshot.setSessionId(sessionId);
            snapshot.setUpdateTime(clock.instant());
            getSnapshots().put(snapshot.getPlanId(), snapshot.copy());
        }
        
        @Override
        public List<WorkflowContextSnapshot> list() {
            return findSnapshots().values().stream().map(WorkflowContextSnapshot::copy)
                    .sorted(Comparator.comparing(WorkflowContextSnapshot::getUpdateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(WorkflowContextSnapshot::getPlanId))
                    .toList();
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
            return Optional.ofNullable(findSnapshots().get(planId)).map(WorkflowContextSnapshot::copy).orElseThrow(() -> new MCPWorkflowStateException(
                    String.format("Unknown or unavailable plan_id `%s`. Call the planning tool again in the current MCP session.", planId), planId));
        }
        
        private Map<String, WorkflowContextSnapshot> getSnapshots() {
            return sessionSnapshots.computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>());
        }
        
        private Map<String, WorkflowContextSnapshot> findSnapshots() {
            return sessionSnapshots.getOrDefault(sessionId, Collections.emptyMap());
        }
    }
}
