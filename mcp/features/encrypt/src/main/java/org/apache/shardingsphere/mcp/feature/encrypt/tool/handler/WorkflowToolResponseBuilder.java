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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.handler;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactMaskUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorkflowToolResponseBuilder {
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService;
    
    WorkflowToolResponseBuilder(final EncryptAlgorithmPropertyTemplateService propertyTemplateService) {
        this.propertyTemplateService = propertyTemplateService;
    }
    
    Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("plan_id", snapshot.getPlanId());
        result.put("status", snapshot.getStatus());
        result.put("pending_questions", null == snapshot.getClarifiedIntent() ? List.of() : snapshot.getClarifiedIntent().getPendingQuestions());
        result.put("issues", snapshot.getIssues().stream().map(WorkflowIssue::toMap).toList());
        result.put("global_steps", null == snapshot.getInteractionPlan() ? List.of() : snapshot.getInteractionPlan().getSteps());
        result.put("current_step", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getCurrentStep());
        result.put("algorithm_recommendations", snapshot.getAlgorithmCandidates().stream().map(AlgorithmCandidate::toMap).toList());
        result.put("property_requirements", snapshot.getPropertyRequirements().stream().map(AlgorithmPropertyRequirement::toMap).toList());
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot));
        result.put("derived_column_plan", null == snapshot.getDerivedColumnPlan() ? null : snapshot.getDerivedColumnPlan().toMap());
        result.put("ddl_artifacts", snapshot.getDdlArtifacts().stream().map(DDLArtifact::toMap).toList());
        result.put("distsql_artifacts", snapshot.getRuleArtifacts().stream().map(each -> createMaskedRuleArtifact(each, snapshot)).toList());
        result.put("index_plan", snapshot.getIndexPlans().stream().map(IndexPlan::toMap).toList());
        result.put("validation_strategy", null == snapshot.getInteractionPlan() ? Map.of() : snapshot.getInteractionPlan().getValidationStrategy());
        result.put("delivery_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getDeliveryMode());
        result.put("execution_mode", null == snapshot.getInteractionPlan() ? "" : snapshot.getInteractionPlan().getExecutionMode());
        return result;
    }
    
    private Map<String, Object> createMaskedPropertyPreview(final WorkflowContextSnapshot snapshot) {
        if (null == snapshot.getRequest()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("primary", propertyTemplateService.maskProperties(filterRequirements(snapshot, "primary"), snapshot.getRequest().getPrimaryAlgorithmProperties()));
        result.put("assisted_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "assisted_query"), snapshot.getRequest().getAssistedQueryAlgorithmProperties()));
        result.put("like_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "like_query"), snapshot.getRequest().getLikeQueryAlgorithmProperties()));
        return result;
    }
    
    private List<AlgorithmPropertyRequirement> filterRequirements(final WorkflowContextSnapshot snapshot, final String role) {
        return snapshot.getPropertyRequirements().stream().filter(each -> role.equals(each.getAlgorithmRole())).toList();
    }
    
    private Map<String, Object> createMaskedRuleArtifact(final RuleArtifact ruleArtifact, final WorkflowContextSnapshot snapshot) {
        return WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(ruleArtifact, snapshot.getRequest(), snapshot.getPropertyRequirements());
    }
}
