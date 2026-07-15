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

import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowKindDescriptors;
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
    
    private static final Collection<String> STORAGE_UNIT_WORKFLOW_KINDS = List.of(
            WorkflowKindDescriptors.READWRITE_RULE, WorkflowKindDescriptors.READWRITE_STATUS, WorkflowKindDescriptors.SHADOW_RULE, WorkflowKindDescriptors.SHARDING_TABLE_RULE);
    
    private static final Collection<String> SINGLE_TABLE_WORKFLOW_KINDS = List.of(WorkflowKindDescriptors.SHADOW_RULE, WorkflowKindDescriptors.SHARDING_TABLE_RULE);
    
    /**
     * Create resource hints for a workflow planning response.
     *
     * @param snapshot workflow snapshot
     * @return resource hints
     */
    public List<Map<String, Object>> createResourcesToRead(final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> result = new LinkedList<>();
        WorkflowRequest request = snapshot.getRequest();
        addDescriptorRelatedResources(result, snapshot, request);
        if (!request.getDatabase().isEmpty()) {
            addGovernanceMetadataResources(result, snapshot, request);
            if (!WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot) && !request.getSchema().isEmpty() && !request.getTable().isEmpty()) {
                addTableResources(result, request);
            }
        }
        return result;
    }
    
    private void addGovernanceMetadataResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        String workflowKind = resolveWorkflowKind(snapshot);
        if (STORAGE_UNIT_WORKFLOW_KINDS.contains(workflowKind)) {
            addStorageUnitsResourceHint(resourcesToRead, request);
        }
        if (SINGLE_TABLE_WORKFLOW_KINDS.contains(workflowKind)) {
            addSingleTablesResourceHint(resourcesToRead, request);
            if (!request.getTable().isEmpty()) {
                addSingleTableResourceHint(resourcesToRead, request);
            }
        }
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
        for (Object each : (Collection<?>) relatedResourceUris) {
            String uriTemplate = String.valueOf(each);
            MCPResourceDescriptor resourceDescriptor = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(uriTemplate);
            createConcreteResourceUri(uriTemplate, request)
                    .ifPresent(uri -> addResourceHint(resourcesToRead, uri, resolveResourceKind(uriTemplate), resolveDescriptorResourceAction(resourceDescriptor),
                            resourceDescriptor.getDescription()));
        }
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
    
    private String resolveResourceKind(final String uriTemplate) {
        ShardingSphereMCPResourceMetadata metadata = MCPDescriptorCatalogIndex.getRequiredShardingSphereResourceMetadata(uriTemplate);
        String result = metadata.getObjectScope();
        return null == result ? metadata.getResourceKind() : result;
    }
    
    private String resolveDescriptorResourceAction(final MCPResourceDescriptor descriptor) {
        return descriptor.isTemplated() ? "inspect_detail" : "read_first";
    }
    
    private void addStorageUnitsResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/storage-units", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())),
                "storage-unit", "validate_scope", "Read storage units before planning DistSQL that references storage units.");
    }
    
    private void addSingleTablesResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/single-tables", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())),
                "single-table", "validate_scope", "Read single table mappings before planning table-level DistSQL.");
    }
    
    private void addSingleTableResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addResourceHint(resourcesToRead, String.format("shardingsphere://databases/%s/single-tables/%s", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()),
                MCPUriPathSegmentUtils.encodePathSegment(request.getTable())), "single-table", "validate_scope",
                "Read the target single table mapping before planning table-level DistSQL.");
    }
    
    private void addResourceHint(final Collection<Map<String, Object>> resourcesToRead, final String uri, final String resourceKind, final String action, final String reason) {
        if (resourcesToRead.stream().anyMatch(each -> uri.equals(each.get("uri")))) {
            return;
        }
        resourcesToRead.add(MCPResourceHintUtils.create(uri, resourceKind, action, reason, MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private void addTableResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        resourcesToRead.add(MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()),
                MCPUriPathSegmentUtils.encodePathSegment(request.getSchema()), MCPUriPathSegmentUtils.encodePathSegment(request.getTable())),
                "column", "validate_scope", "Read table columns before planning column-level workflow changes.", MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private String resolveWorkflowKind(final WorkflowContextSnapshot snapshot) {
        WorkflowKind workflowKind = snapshot.getWorkflowKind();
        return null == workflowKind ? "" : workflowKind.getValue();
    }
    
}
