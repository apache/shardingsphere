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
     * Create a defensive copy of the workflow snapshot.
     *
     * @return copied workflow snapshot
     */
    public WorkflowContextSnapshot copy() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setUpdateTime(copyInstant(updateTime));
        result.setRequest(null == request ? null : request.copy());
        result.setClarifiedIntent(copyClarifiedIntent());
        result.setFeatureData(null == featureData ? null : featureData.copy());
        result.setInteractionPlan(copyInteractionPlan());
        issues.forEach(each -> result.getIssues().add(copyWorkflowIssue(each)));
        algorithmCandidates.forEach(each -> result.getAlgorithmCandidates().add(copyAlgorithmCandidate(each)));
        propertyRequirements.forEach(each -> result.getPropertyRequirements().add(copyPropertyRequirement(each)));
        ddlArtifacts.forEach(each -> result.getDdlArtifacts().add(new DDLArtifact(each.getArtifactType(), each.getSql(), each.getExecutionOrder())));
        ruleArtifacts.forEach(each -> result.getRuleArtifacts().add(new RuleArtifact(each.getOperationType(), each.getSql())));
        indexPlans.forEach(each -> result.getIndexPlans().add(new IndexPlan(each.getIndexName(), each.getColumnName(), each.getReason(), each.getSql())));
        result.setValidationReport(copyValidationReport());
        return result;
    }
    
    /**
     * Clear planning artifacts before rebuilding them.
     */
    public void clearPlanningState() {
        issues.clear();
        algorithmCandidates.clear();
        propertyRequirements.clear();
        ddlArtifacts.clear();
        ruleArtifacts.clear();
        indexPlans.clear();
        validationReport = null;
    }
    
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
    
    private Instant copyInstant(final Instant original) {
        return null == original ? null : Instant.ofEpochMilli(original.toEpochMilli());
    }
    
    private ClarifiedIntent copyClarifiedIntent() {
        if (null == clarifiedIntent) {
            return null;
        }
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(clarifiedIntent.getOperationType());
        result.setFieldSemantics(clarifiedIntent.getFieldSemantics());
        result.setReasoningNotes(clarifiedIntent.getReasoningNotes());
        result.getPendingQuestions().addAll(clarifiedIntent.getPendingQuestions());
        return result;
    }
    
    private InteractionPlan copyInteractionPlan() {
        if (null == interactionPlan) {
            return null;
        }
        InteractionPlan result = new InteractionPlan();
        result.setPlanId(interactionPlan.getPlanId());
        result.setSummary(interactionPlan.getSummary());
        result.setCurrentStep(interactionPlan.getCurrentStep());
        result.setDeliveryMode(interactionPlan.getDeliveryMode());
        result.setExecutionMode(interactionPlan.getExecutionMode());
        result.getSteps().addAll(interactionPlan.getSteps());
        result.getValidationStrategy().putAll(copyMap(interactionPlan.getValidationStrategy()));
        return result;
    }
    
    private WorkflowIssue copyWorkflowIssue(final WorkflowIssue original) {
        return new WorkflowIssue(original.getCode(), original.getSeverity(), original.getStage(), original.getMessage(),
                original.getUserAction(), original.isRetryable(), copyMap(original.getDetails()));
    }
    
    private AlgorithmCandidate copyAlgorithmCandidate(final AlgorithmCandidate original) {
        return new AlgorithmCandidate(original.getAlgorithmRole(), original.getAlgorithmType(), original.getSource(), original.getSupportsDecrypt(),
                original.getSupportsEquivalentFilter(), original.getSupportsLike(), original.getRecommendationScore(),
                original.getRecommendationReason(), original.getRiskNotes());
    }
    
    private AlgorithmPropertyRequirement copyPropertyRequirement(final AlgorithmPropertyRequirement original) {
        return new AlgorithmPropertyRequirement(original.getAlgorithmRole(), original.getPropertyKey(), original.isRequired(),
                original.isSecret(), original.getDescription(), original.getDefaultValue());
    }
    
    private ValidationReport copyValidationReport() {
        if (null == validationReport) {
            return null;
        }
        ValidationReport result = new ValidationReport();
        result.setDdlValidation(copyValidationSection(validationReport.getDdlValidation()));
        result.setRuleValidation(copyValidationSection(validationReport.getRuleValidation()));
        result.setLogicalMetadataValidation(copyValidationSection(validationReport.getLogicalMetadataValidation()));
        result.setSqlExecutabilityValidation(copyValidationSection(validationReport.getSqlExecutabilityValidation()));
        result.setOverallStatus(validationReport.getOverallStatus());
        validationReport.getMismatches().forEach(each -> result.getMismatches().add(copyMap(each)));
        return result;
    }
    
    private ValidationSection copyValidationSection(final ValidationSection original) {
        if (null == original) {
            return null;
        }
        return new ValidationSection(original.getStatus(), copyValue(original.getEvidence()), copyValue(original.getDetails()));
    }
    
    private Map<String, Object> copyMap(final Map<?, ?> original) {
        Map<String, Object> result = new LinkedHashMap<>(original.size(), 1F);
        original.forEach((key, value) -> result.put(String.valueOf(key), copyValue(value)));
        return result;
    }
    
    private Object copyValue(final Object original) {
        if (original instanceof Map) {
            return copyMap((Map<?, ?>) original);
        }
        if (original instanceof List) {
            return ((List<?>) original).stream().map(this::copyValue).toList();
        }
        return original;
    }
}
