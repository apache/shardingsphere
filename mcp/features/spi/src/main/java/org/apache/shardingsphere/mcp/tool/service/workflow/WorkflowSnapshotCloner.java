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

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Workflow snapshot cloner.
 */
final class WorkflowSnapshotCloner {
    
    private static final WorkflowRequestMerger REQUEST_MERGER = new WorkflowRequestMerger();
    
    private WorkflowSnapshotCloner() {
    }
    
    static WorkflowContextSnapshot cloneSnapshot(final WorkflowContextSnapshot original) {
        if (null == original) {
            return null;
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(original.getPlanId());
        result.setSessionId(original.getSessionId());
        result.setStatus(original.getStatus());
        result.setUpdateTime(copyInstant(original.getUpdateTime()));
        result.setRequest(REQUEST_MERGER.copy(original.getRequest()));
        result.setClarifiedIntent(cloneClarifiedIntent(original.getClarifiedIntent()));
        result.setInteractionPlan(cloneInteractionPlan(original.getInteractionPlan()));
        original.getIssues().forEach(each -> result.getIssues().add(cloneIssue(each)));
        original.getAlgorithmCandidates().forEach(each -> result.getAlgorithmCandidates().add(cloneAlgorithmCandidate(each)));
        original.getPropertyRequirements().forEach(each -> result.getPropertyRequirements().add(clonePropertyRequirement(each)));
        original.getDdlArtifacts().forEach(each -> result.getDdlArtifacts().add(new DDLArtifact(each.getArtifactType(), each.getSql(), each.getExecutionOrder())));
        original.getRuleArtifacts().forEach(each -> result.getRuleArtifacts().add(new RuleArtifact(each.getOperationType(), each.getSql())));
        original.getIndexPlans().forEach(each -> result.getIndexPlans().add(new IndexPlan(each.getIndexName(), each.getColumnName(), each.getReason(), each.getSql())));
        result.setDerivedColumnPlan(cloneDerivedColumnPlan(original.getDerivedColumnPlan()));
        result.setValidationReport(cloneValidationReport(original.getValidationReport()));
        return result;
    }
    
    private static Instant copyInstant(final Instant original) {
        return null == original ? null : Instant.ofEpochMilli(original.toEpochMilli());
    }
    
    private static ClarifiedIntent cloneClarifiedIntent(final ClarifiedIntent original) {
        if (null == original) {
            return null;
        }
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(original.getOperationType());
        result.setFieldSemantics(original.getFieldSemantics());
        result.setReasoningNotes(original.getReasoningNotes());
        result.setRequiresDecrypt(original.getRequiresDecrypt());
        result.setRequiresEqualityFilter(original.getRequiresEqualityFilter());
        result.setRequiresLikeQuery(original.getRequiresLikeQuery());
        result.getPendingQuestions().addAll(original.getPendingQuestions());
        return result;
    }
    
    private static InteractionPlan cloneInteractionPlan(final InteractionPlan original) {
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
    
    private static WorkflowIssue cloneIssue(final WorkflowIssue original) {
        return new WorkflowIssue(original.getCode(), original.getSeverity(), original.getStage(), original.getMessage(),
                original.getUserAction(), original.isRetryable(), copyMap(original.getDetails()));
    }
    
    private static AlgorithmCandidate cloneAlgorithmCandidate(final AlgorithmCandidate original) {
        return new AlgorithmCandidate(original.getAlgorithmRole(), original.getAlgorithmType(), original.getSource(), original.getSupportsDecrypt(),
                original.getSupportsEquivalentFilter(), original.getSupportsLike(), original.getRecommendationScore(),
                original.getRecommendationReason(), original.getRiskNotes());
    }
    
    private static AlgorithmPropertyRequirement clonePropertyRequirement(final AlgorithmPropertyRequirement original) {
        return new AlgorithmPropertyRequirement(original.getAlgorithmRole(), original.getPropertyKey(), original.isRequired(),
                original.isSecret(), original.getDescription(), original.getDefaultValue());
    }
    
    private static DerivedColumnPlan cloneDerivedColumnPlan(final DerivedColumnPlan original) {
        if (null == original) {
            return null;
        }
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setLogicalColumn(original.getLogicalColumn());
        result.setCipherColumnName(original.getCipherColumnName());
        result.setAssistedQueryColumnName(original.getAssistedQueryColumnName());
        result.setLikeQueryColumnName(original.getLikeQueryColumnName());
        result.setCipherColumnRequired(original.isCipherColumnRequired());
        result.setAssistedQueryColumnRequired(original.isAssistedQueryColumnRequired());
        result.setLikeQueryColumnRequired(original.isLikeQueryColumnRequired());
        result.setDataTypeStrategy(original.getDataTypeStrategy());
        original.getNameCollisions().forEach(each -> result.getNameCollisions().add(copyStringMap(each)));
        return result;
    }
    
    private static ValidationReport cloneValidationReport(final ValidationReport original) {
        if (null == original) {
            return null;
        }
        ValidationReport result = new ValidationReport();
        result.setDdlValidation(cloneValidationSection(original.getDdlValidation()));
        result.setRuleValidation(cloneValidationSection(original.getRuleValidation()));
        result.setLogicalMetadataValidation(cloneValidationSection(original.getLogicalMetadataValidation()));
        result.setSqlExecutabilityValidation(cloneValidationSection(original.getSqlExecutabilityValidation()));
        result.setOverallStatus(original.getOverallStatus());
        original.getMismatches().forEach(each -> result.getMismatches().add(copyMap(each)));
        return result;
    }
    
    private static ValidationSection cloneValidationSection(final ValidationSection original) {
        if (null == original) {
            return null;
        }
        return new ValidationSection(original.getStatus(), cloneObject(original.getEvidence()), cloneObject(original.getDetails()));
    }
    
    private static Map<String, Object> copyMap(final Map<?, ?> original) {
        Map<String, Object> result = new LinkedHashMap<>(original.size(), 1F);
        for (Entry<?, ?> entry : original.entrySet()) {
            result.put(String.valueOf(entry.getKey()), cloneObject(entry.getValue()));
        }
        return result;
    }
    
    private static Map<String, String> copyStringMap(final Map<String, String> original) {
        return new LinkedHashMap<>(original);
    }
    
    private static Object cloneObject(final Object original) {
        if (original instanceof Map) {
            return copyMap((Map<?, ?>) original);
        }
        if (original instanceof List) {
            return ((List<?>) original).stream().map(WorkflowSnapshotCloner::cloneObject).toList();
        }
        return original;
    }
}
