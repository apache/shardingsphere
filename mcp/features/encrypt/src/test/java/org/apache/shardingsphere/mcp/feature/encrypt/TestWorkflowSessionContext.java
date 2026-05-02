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

package org.apache.shardingsphere.mcp.feature.encrypt;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.workflow.service.WorkflowSqlUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TestWorkflowSessionContext implements WorkflowSessionContext {
    
    private final Map<String, WorkflowContextSnapshot> contexts = new ConcurrentHashMap<>();
    
    @Override
    public String createPlanId() {
        return "plan-" + UUID.randomUUID();
    }
    
    @Override
    public WorkflowContextSnapshot getOrCreate(final String sessionId, final String planId) {
        if (!WorkflowSqlUtils.trimToEmpty(planId).isEmpty()) {
            return getRequired(planId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(createPlanId());
        result.setSessionId(sessionId);
        result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return result;
    }
    
    @Override
    public void save(final WorkflowContextSnapshot snapshot) {
        contexts.put(snapshot.getPlanId(), snapshot.copy());
    }
    
    @Override
    public Optional<WorkflowContextSnapshot> find(final String planId) {
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
}
