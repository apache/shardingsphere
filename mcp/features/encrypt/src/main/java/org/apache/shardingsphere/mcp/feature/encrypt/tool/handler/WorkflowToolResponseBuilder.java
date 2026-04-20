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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPropertySources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorkflowToolResponseBuilder {
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService;
    
    WorkflowToolResponseBuilder(final EncryptAlgorithmPropertyTemplateService propertyTemplateService) {
        this.propertyTemplateService = propertyTemplateService;
    }
    
    Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        EncryptWorkflowState workflowState = getEncryptWorkflowState(snapshot);
        WorkflowPropertySource propertySource = WorkflowPropertySources.compose(snapshot.getRequest(), workflowState);
        Map<String, Object> result = snapshot.toPlanPayload();
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot, propertySource));
        result.put("derived_column_plan", null == workflowState.getDerivedColumnPlan() ? null : workflowState.getDerivedColumnPlan().toMap());
        result.putAll(WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, propertySource));
        return result;
    }
    
    private EncryptWorkflowState getEncryptWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private Map<String, Object> createMaskedPropertyPreview(final WorkflowContextSnapshot snapshot, final WorkflowPropertySource propertySource) {
        if (null == snapshot.getRequest() && null == snapshot.getFeatureData()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("primary", propertyTemplateService.maskProperties(filterRequirements(snapshot, "primary"), propertySource.getAlgorithmProperties("primary")));
        result.put("assisted_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "assisted_query"), propertySource.getAlgorithmProperties("assisted_query")));
        result.put("like_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "like_query"), propertySource.getAlgorithmProperties("like_query")));
        return result;
    }
    
    private List<AlgorithmPropertyRequirement> filterRequirements(final WorkflowContextSnapshot snapshot, final String role) {
        return snapshot.getPropertyRequirements().stream().filter(each -> role.equals(each.getAlgorithmRole())).toList();
    }
}
