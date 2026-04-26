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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow plan payload builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowPlanPayloadBuilder {
    
    /**
     * Build one workflow-plan payload map.
     *
     * @param snapshot workflow snapshot
     * @return workflow-plan payload
     */
    public static Map<String, Object> build(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("plan_id", snapshot.getPlanId());
        result.put("status", snapshot.getStatus());
        result.put("pending_questions", null == snapshot.getClarifiedIntent() ? List.of() : snapshot.getClarifiedIntent().getPendingQuestions());
        result.put("issues", snapshot.getIssues().stream().map(WorkflowIssue::toMap).toList());
        result.put("global_steps", null == snapshot.getInteractionPlan() ? List.of() : snapshot.getInteractionPlan().getSteps());
        result.put("current_step", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getCurrentStep());
        result.put("algorithm_recommendations", snapshot.getAlgorithmCandidates().stream().map(AlgorithmCandidate::toMap).toList());
        result.put("property_requirements", snapshot.getPropertyRequirements().stream().map(AlgorithmPropertyRequirement::toMap).toList());
        result.put("validation_strategy", null == snapshot.getInteractionPlan() ? Map.of() : snapshot.getInteractionPlan().getValidationStrategy());
        result.put("delivery_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getDeliveryMode());
        result.put("execution_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getExecutionMode());
        result.put("intent_inference", createIntentInference(snapshot.getClarifiedIntent()));
        return result;
    }
    
    private static Map<String, Object> createIntentInference(final ClarifiedIntent clarifiedIntent) {
        if (null == clarifiedIntent) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("operation_type", WorkflowSqlUtils.trimToEmpty(clarifiedIntent.getOperationType()));
        result.put("field_semantics", WorkflowSqlUtils.trimToEmpty(clarifiedIntent.getFieldSemantics()));
        result.put("inferred_values", Map.copyOf(clarifiedIntent.getInferredValues()));
        result.put("unresolved_fields", List.copyOf(clarifiedIntent.getUnresolvedFields()));
        result.put("reasoning_notes", WorkflowSqlUtils.trimToEmpty(clarifiedIntent.getReasoningNotes()));
        return result;
    }
}
