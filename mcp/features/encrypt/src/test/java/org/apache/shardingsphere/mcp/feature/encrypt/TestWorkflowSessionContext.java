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

import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TestWorkflowSessionContext implements WorkflowSessionContext {
    
    private static final String SESSION_ID = "session-1";
    
    private final Map<String, WorkflowContextSnapshot> contexts = new ConcurrentHashMap<>();
    
    @Override
    public WorkflowContextSnapshot getOrCreate(final String planId) {
        if (null != planId && !planId.isBlank()) {
            return getRequired(planId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-" + UUID.randomUUID());
        result.setSessionId(SESSION_ID);
        result.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return result;
    }
    
    @Override
    public void save(final WorkflowContextSnapshot snapshot) {
        snapshot.setSessionId(SESSION_ID);
        contexts.put(snapshot.getPlanId(), snapshot.copy());
    }
    
    @Override
    public List<WorkflowContextSnapshot> list() {
        return contexts.values().stream().map(WorkflowContextSnapshot::copy).toList();
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
        WorkflowContextSnapshot result = contexts.get(planId);
        if (null == result) {
            throw new MCPInvalidRequestException(String.format("Unknown plan_id `%s`.", planId));
        }
        return result.copy();
    }
    
}
