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

package org.apache.shardingsphere.mcp.support.workflow.model;

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
    
    private WorkflowKind workflowKind;
    
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
    
    @Setter
    private ValidationReport validationReport;
    
    private final List<DDLArtifact> ddlArtifacts = new LinkedList<>();
    
    private final List<RuleArtifact> ruleArtifacts = new LinkedList<>();
    
    private final List<IndexPlan> indexPlans = new LinkedList<>();
    
    /**
     * Clear planning artifacts before rebuilding them.
     */
    public void clearPlanningState() {
        issues.clear();
        algorithmCandidates.clear();
        propertyRequirements.clear();
        validationReport = null;
        ddlArtifacts.clear();
        ruleArtifacts.clear();
        indexPlans.clear();
    }
    
    /**
     * Create a defensive copy of the workflow snapshot.
     *
     * @return copied workflow snapshot
     */
    public WorkflowContextSnapshot copy() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setWorkflowKind(workflowKind);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setUpdateTime(copyInstant(updateTime));
        result.setRequest(null == request ? null : request.copy());
        result.setClarifiedIntent(copyClarifiedIntent(clarifiedIntent));
        result.setFeatureData(null == featureData ? null : featureData.copy());
        result.setInteractionPlan(copyInteractionPlan(interactionPlan));
        issues.forEach(each -> result.getIssues().add(copyWorkflowIssue(each)));
        result.getAlgorithmCandidates().addAll(algorithmCandidates);
        result.getPropertyRequirements().addAll(propertyRequirements);
        result.getDdlArtifacts().addAll(ddlArtifacts);
        result.getRuleArtifacts().addAll(ruleArtifacts);
        result.getIndexPlans().addAll(indexPlans);
        result.setValidationReport(copyValidationReport(validationReport));
        return result;
    }
    
    private static Instant copyInstant(final Instant original) {
        return null == original ? null : Instant.ofEpochMilli(original.toEpochMilli());
    }
    
    private static ClarifiedIntent copyClarifiedIntent(final ClarifiedIntent original) {
        if (null == original) {
            return null;
        }
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(original.getOperationType());
        result.setFieldSemantics(original.getFieldSemantics());
        result.setReasoningNotes(original.getReasoningNotes());
        result.getClarificationMessages().addAll(original.getClarificationMessages());
        result.getUnresolvedFields().addAll(original.getUnresolvedFields());
        original.getInferredValues().forEach((key, value) -> result.getInferredValues().put(key, copyValue(value)));
        return result;
    }
    
    private static InteractionPlan copyInteractionPlan(final InteractionPlan original) {
        if (null == original) {
            return null;
        }
        InteractionPlan result = new InteractionPlan();
        result.setPlanId(original.getPlanId());
        result.setSummary(original.getSummary());
        result.setCurrentStep(original.getCurrentStep());
        result.setDeliveryMode(original.getDeliveryMode());
        result.setExecutionMode(original.getExecutionMode());
        result.getSteps().addAll(original.getSteps());
        result.getValidationStrategy().putAll(copyMap(original.getValidationStrategy()));
        return result;
    }
    
    private static WorkflowIssue copyWorkflowIssue(final WorkflowIssue original) {
        return new WorkflowIssue(original.getCode(), original.getSeverity(), original.getStage(), original.getMessage(),
                original.getUserAction(), original.isRetryable(), copyMap(original.getDetails()));
    }
    
    private static ValidationReport copyValidationReport(final ValidationReport original) {
        if (null == original) {
            return null;
        }
        ValidationReport result = new ValidationReport();
        result.setDdlValidation(copyValidationSection(original.getDdlValidation()));
        result.setRuleValidation(copyValidationSection(original.getRuleValidation()));
        result.setLogicalMetadataValidation(copyValidationSection(original.getLogicalMetadataValidation()));
        result.setSqlExecutabilityValidation(copyValidationSection(original.getSqlExecutabilityValidation()));
        result.setOverallStatus(original.getOverallStatus());
        original.getMismatches().forEach(each -> result.getMismatches().add(copyMap(each)));
        return result;
    }
    
    private static ValidationSection copyValidationSection(final ValidationSection original) {
        if (null == original) {
            return null;
        }
        return new ValidationSection(original.getStatus(), copyValue(original.getEvidence()), copyValue(original.getDetails()));
    }
    
    private static Map<String, Object> copyMap(final Map<?, ?> original) {
        Map<String, Object> result = new LinkedHashMap<>(original.size(), 1F);
        original.forEach((key, value) -> result.put(String.valueOf(key), copyValue(value)));
        return result;
    }
    
    private static Object copyValue(final Object original) {
        if (original instanceof Map) {
            return copyMap((Map<?, ?>) original);
        }
        if (original instanceof List) {
            return ((List<?>) original).stream().map(WorkflowContextSnapshot::copyValue).toList();
        }
        return original;
    }
}
