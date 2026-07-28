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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides model-facing workflow resource read hints.
 */
public final class WorkflowGuidanceResourceHintProvider {
    
    /**
     * Create resource hints for a workflow planning response.
     *
     * @param snapshot workflow snapshot
     * @return resource hints
     */
    public List<Map<String, Object>> createResourcesToRead(final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> result = new LinkedList<>();
        WorkflowRequest request = snapshot.getRequest();
        addResources(result, snapshot.getResourceUriTemplates(), request);
        addDescriptorRelatedResources(result, snapshot, request);
        return result;
    }
    
    private void addDescriptorRelatedResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        Optional<String> planningToolName = MCPDescriptorCatalogIndex.findPlanningToolNameByWorkflowKind(resolveWorkflowKind(snapshot));
        if (planningToolName.isEmpty()) {
            return;
        }
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(planningToolName.get());
        Object relatedResourceUris = descriptor.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS);
        if (!(relatedResourceUris instanceof Collection)) {
            return;
        }
        addResources(resourcesToRead, (Collection<?>) relatedResourceUris, request);
    }
    
    private void addResources(final Collection<Map<String, Object>> resourcesToRead, final Collection<?> uriTemplates, final WorkflowRequest request) {
        for (Object each : uriTemplates) {
            String uriTemplate = String.valueOf(each);
            findResourceDescriptor(uriTemplate).ifPresent(resourceDescriptor -> createConcreteResourceUri(uriTemplate, request)
                    .ifPresent(uri -> addResourceHint(resourcesToRead, uri, MCPDescriptorCatalogIndex.resolveResourceKind(uriTemplate), resolveDescriptorResourceAction(resourceDescriptor),
                            resourceDescriptor.getDescription())));
        }
    }
    
    private Optional<MCPResourceDescriptor> findResourceDescriptor(final String uriTemplate) {
        return MCPDescriptorCatalogIndex.getResourceDescriptors().stream().filter(each -> uriTemplate.equals(each.getUriTemplate())).findFirst();
    }
    
    private Optional<String> createConcreteResourceUri(final String uriTemplate, final WorkflowRequest request) {
        Optional<String> result = replaceRequiredVariable(uriTemplate, "database", request.getDatabase());
        if (result.isEmpty()) {
            return result;
        }
        result = replaceRequiredVariable(result.get(), "schema", request.getSchema());
        if (result.isEmpty()) {
            return result;
        }
        result = replaceRequiredVariable(result.get(), "table", request.getTable());
        return result.filter(each -> !each.contains("{"));
    }
    
    private Optional<String> replaceRequiredVariable(final String uriTemplate, final String variableName, final String value) {
        String placeholder = "{" + variableName + "}";
        if (!uriTemplate.contains(placeholder)) {
            return Optional.of(uriTemplate);
        }
        return value.isEmpty() ? Optional.empty() : Optional.of(uriTemplate.replace(placeholder, MCPUriPathSegmentUtils.encodePathSegment(value)));
    }
    
    private String resolveDescriptorResourceAction(final MCPResourceDescriptor descriptor) {
        return descriptor.isTemplated() ? "inspect_detail" : "read_first";
    }
    
    private void addResourceHint(final Collection<Map<String, Object>> resourcesToRead, final String uri, final String resourceKind, final String action, final String reason) {
        if (resourcesToRead.stream().anyMatch(each -> uri.equals(each.get("uri")))) {
            return;
        }
        resourcesToRead.add(MCPResourceHintUtils.create(uri, resourceKind, action, reason, MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private String resolveWorkflowKind(final WorkflowContextSnapshot snapshot) {
        WorkflowKind workflowKind = snapshot.getWorkflowKind();
        return null == workflowKind ? "" : workflowKind.getValue();
    }
    
}
