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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow context snapshot utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowContextSnapshots {
    
    /**
     * Create a defensive copy of the workflow snapshot.
     *
     * @param snapshot workflow snapshot
     * @return copied workflow snapshot
     */
    public static WorkflowContextSnapshot copy(final WorkflowContextSnapshot snapshot) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(snapshot.getPlanId());
        result.setSessionId(snapshot.getSessionId());
        result.setStatus(snapshot.getStatus());
        result.setUpdateTime(copyInstant(snapshot.getUpdateTime()));
        result.setRequest(null == snapshot.getRequest() ? null : snapshot.getRequest().copy());
        result.setClarifiedIntent(copyClarifiedIntent(snapshot.getClarifiedIntent()));
        result.setFeatureData(null == snapshot.getFeatureData() ? null : snapshot.getFeatureData().copy());
        result.setInteractionPlan(copyInteractionPlan(snapshot.getInteractionPlan()));
        snapshot.getIssues().forEach(each -> result.getIssues().add(copyWorkflowIssue(each)));
        snapshot.getAlgorithmCandidates().forEach(each -> result.getAlgorithmCandidates().add(copyAlgorithmCandidate(each)));
        snapshot.getPropertyRequirements().forEach(each -> result.getPropertyRequirements().add(copyPropertyRequirement(each)));
        snapshot.getDdlArtifacts().forEach(each -> result.getDdlArtifacts().add(new DDLArtifact(each.getArtifactType(), each.getSql(), each.getExecutionOrder())));
        snapshot.getRuleArtifacts().forEach(each -> result.getRuleArtifacts().add(new RuleArtifact(each.getOperationType(), each.getSql())));
        snapshot.getIndexPlans().forEach(each -> result.getIndexPlans().add(new IndexPlan(each.getIndexName(), each.getColumnName(), each.getReason(), each.getSql())));
        result.setValidationReport(copyValidationReport(snapshot.getValidationReport()));
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
        result.getPendingQuestions().addAll(original.getPendingQuestions());
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
    
    private static AlgorithmCandidate copyAlgorithmCandidate(final AlgorithmCandidate original) {
        return new AlgorithmCandidate(original.getAlgorithmRole(), original.getAlgorithmType(), original.getSource(), original.getSupportsDecrypt(),
                original.getSupportsEquivalentFilter(), original.getSupportsLike(), original.getRecommendationScore(),
                original.getRecommendationReason(), original.getRiskNotes());
    }
    
    private static AlgorithmPropertyRequirement copyPropertyRequirement(final AlgorithmPropertyRequirement original) {
        return new AlgorithmPropertyRequirement(original.getAlgorithmRole(), original.getPropertyKey(), original.isRequired(),
                original.isSecret(), original.getDescription(), original.getDefaultValue());
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
            return ((List<?>) original).stream().map(WorkflowContextSnapshots::copyValue).toList();
        }
        return original;
    }
}
