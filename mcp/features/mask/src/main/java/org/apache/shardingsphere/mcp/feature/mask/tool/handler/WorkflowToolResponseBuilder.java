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

package org.apache.shardingsphere.mcp.feature.mask.tool.handler;

import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPropertySources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorkflowToolResponseBuilder {
    
    private final MaskAlgorithmPropertyTemplateService propertyTemplateService;
    
    WorkflowToolResponseBuilder(final MaskAlgorithmPropertyTemplateService propertyTemplateService) {
        this.propertyTemplateService = propertyTemplateService;
    }
    
    Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        WorkflowPropertySource propertySource = WorkflowPropertySources.compose(snapshot.getRequest(), snapshot.getFeatureData());
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("plan_id", snapshot.getPlanId());
        result.put("status", snapshot.getStatus());
        result.put("pending_questions", null == snapshot.getClarifiedIntent() ? List.of() : snapshot.getClarifiedIntent().getPendingQuestions());
        result.put("issues", snapshot.getIssues().stream().map(WorkflowIssue::toMap).toList());
        result.put("global_steps", null == snapshot.getInteractionPlan() ? List.of() : snapshot.getInteractionPlan().getSteps());
        result.put("current_step", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getCurrentStep());
        result.put("algorithm_recommendations", snapshot.getAlgorithmCandidates().stream().map(AlgorithmCandidate::toMap).toList());
        result.put("property_requirements", snapshot.getPropertyRequirements().stream().map(AlgorithmPropertyRequirement::toMap).toList());
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot, propertySource));
        result.put("derived_column_plan", null);
        result.put("ddl_artifacts", List.of());
        result.put("distsql_artifacts", snapshot.getRuleArtifacts().stream().map(each -> createMaskedRuleArtifact(each, propertySource, snapshot)).toList());
        result.put("index_plan", List.of());
        result.put("validation_strategy", null == snapshot.getInteractionPlan() ? Map.of() : snapshot.getInteractionPlan().getValidationStrategy());
        result.put("delivery_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getDeliveryMode());
        result.put("execution_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getExecutionMode());
        return result;
    }
    
    private Map<String, Object> createMaskedPropertyPreview(final WorkflowContextSnapshot snapshot, final WorkflowPropertySource propertySource) {
        if (null == snapshot.getRequest()) {
            return Map.of();
        }
        return Map.of("primary", propertyTemplateService.maskProperties(filterRequirements(snapshot), propertySource.getAlgorithmProperties("primary")));
    }
    
    private List<AlgorithmPropertyRequirement> filterRequirements(final WorkflowContextSnapshot snapshot) {
        return snapshot.getPropertyRequirements().stream().filter(each -> "primary".equals(each.getAlgorithmRole())).toList();
    }
    
    private Map<String, Object> createMaskedRuleArtifact(final RuleArtifact ruleArtifact, final WorkflowPropertySource propertySource,
                                                         final WorkflowContextSnapshot snapshot) {
        return WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(ruleArtifact, propertySource, snapshot.getPropertyRequirements());
    }
}
