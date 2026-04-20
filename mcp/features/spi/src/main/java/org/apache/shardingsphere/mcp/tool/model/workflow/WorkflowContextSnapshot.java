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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow context snapshot.
 */
@Getter
@Setter
public final class WorkflowContextSnapshot {
    
    private String planId;
    
    private String sessionId;
    
    private String status;
    
    private Instant updateTime = Instant.now();
    
    private WorkflowRequest request;
    
    private ClarifiedIntent clarifiedIntent;
    
    private WorkflowFeatureData featureData;
    
    private InteractionPlan interactionPlan;
    
    private final List<WorkflowIssue> issues = new LinkedList<>();
    
    private final List<AlgorithmCandidate> algorithmCandidates = new LinkedList<>();
    
    private final List<AlgorithmPropertyRequirement> propertyRequirements = new LinkedList<>();
    
    private final List<DDLArtifact> ddlArtifacts = new LinkedList<>();
    
    private final List<RuleArtifact> ruleArtifacts = new LinkedList<>();
    
    private final List<IndexPlan> indexPlans = new LinkedList<>();
    
    private ValidationReport validationReport;
    
    /**
     * Convert current snapshot to one workflow-plan payload map.
     *
     * @return workflow-plan payload
     */
    public Map<String, Object> toPlanPayload() {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("plan_id", planId);
        result.put("status", status);
        result.put("pending_questions", null == clarifiedIntent ? List.of() : clarifiedIntent.getPendingQuestions());
        result.put("issues", issues.stream().map(WorkflowIssue::toMap).toList());
        result.put("global_steps", null == interactionPlan ? List.of() : interactionPlan.getSteps());
        result.put("current_step", null == interactionPlan ? "" : interactionPlan.getCurrentStep());
        result.put("algorithm_recommendations", algorithmCandidates.stream().map(AlgorithmCandidate::toMap).toList());
        result.put("property_requirements", propertyRequirements.stream().map(AlgorithmPropertyRequirement::toMap).toList());
        result.put("validation_strategy", null == interactionPlan ? Map.of() : interactionPlan.getValidationStrategy());
        result.put("delivery_mode", null == interactionPlan ? "" : interactionPlan.getDeliveryMode());
        result.put("execution_mode", null == interactionPlan ? "" : interactionPlan.getExecutionMode());
        return result;
    }
}
