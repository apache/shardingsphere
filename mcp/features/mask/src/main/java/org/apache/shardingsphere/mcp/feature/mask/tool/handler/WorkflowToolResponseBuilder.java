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
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPropertySource;

import java.util.List;
import java.util.Map;

final class WorkflowToolResponseBuilder {
    
    private final MaskAlgorithmPropertyTemplateService propertyTemplateService;
    
    WorkflowToolResponseBuilder(final MaskAlgorithmPropertyTemplateService propertyTemplateService) {
        this.propertyTemplateService = propertyTemplateService;
    }
    
    Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        WorkflowPropertySource propertySource = getPropertySource(snapshot);
        Map<String, Object> result = snapshot.toPlanPayload();
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot, propertySource));
        result.put("derived_column_plan", null);
        result.putAll(WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, propertySource));
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
    
    private WorkflowPropertySource getPropertySource(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? algorithmRole -> Map.of() : snapshot.getRequest();
    }
}
