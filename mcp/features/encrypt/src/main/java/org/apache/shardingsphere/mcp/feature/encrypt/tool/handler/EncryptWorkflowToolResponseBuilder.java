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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class EncryptWorkflowToolResponseBuilder {
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService;
    
    Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        WorkflowPropertySource propertySource = getPropertySource(snapshot);
        Map<String, Object> result = WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, propertySource);
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot, propertySource));
        return result;
    }
    
    private Map<String, Object> createMaskedPropertyPreview(final WorkflowContextSnapshot snapshot, final WorkflowPropertySource propertySource) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("primary", propertyTemplateService.maskProperties(filterRequirements(snapshot, "primary"), propertySource.getAlgorithmProperties("primary")));
        result.put("assisted_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "assisted_query"), propertySource.getAlgorithmProperties("assisted_query")));
        result.put("like_query", propertyTemplateService.maskProperties(filterRequirements(snapshot, "like_query"), propertySource.getAlgorithmProperties("like_query")));
        return result;
    }
    
    private List<AlgorithmPropertyRequirement> filterRequirements(final WorkflowContextSnapshot snapshot, final String role) {
        return snapshot.getPropertyRequirements().stream().filter(each -> role.equals(each.getAlgorithmRole())).toList();
    }
    
    private WorkflowPropertySource getPropertySource(final WorkflowContextSnapshot snapshot) {
        return snapshot.getRequest();
    }
}
