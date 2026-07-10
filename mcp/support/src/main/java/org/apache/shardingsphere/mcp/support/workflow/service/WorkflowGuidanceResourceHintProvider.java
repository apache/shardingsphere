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
    
    private static final Map<String, List<ResourceHintTemplate>> FEATURE_RESOURCE_HINTS = Map.ofEntries(
            Map.entry(WorkflowKindDescriptors.ENCRYPT_RULE, List.of(new ResourceHintTemplate("shardingsphere://features/encrypt/algorithms", "algorithm", "read_first",
                    "Read encrypt algorithm metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.MASK_RULE, List.of(new ResourceHintTemplate("shardingsphere://features/mask/algorithms", "algorithm", "read_first",
                    "Read mask algorithm metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.READWRITE_RULE,
                    List.of(new ResourceHintTemplate("shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins", "algorithm", "read_first",
                            "Read load-balance algorithm plugin metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.SHADOW_RULE, List.of(new ResourceHintTemplate("shardingsphere://features/shadow/algorithm-plugins", "algorithm", "read_first",
                    "Read shadow algorithm plugin metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.SHADOW_DEFAULT_ALGORITHM,
                    List.of(new ResourceHintTemplate("shardingsphere://features/shadow/algorithm-plugins", "algorithm", "read_first",
                            "Read shadow algorithm plugin metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_TABLE_RULE,
                    List.of(new ResourceHintTemplate("shardingsphere://features/sharding/algorithm-plugins", "algorithm", "read_first",
                            "Read sharding algorithm plugin metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_DEFAULT_STRATEGY,
                    List.of(new ResourceHintTemplate("shardingsphere://features/sharding/algorithm-plugins", "algorithm", "read_first",
                            "Read sharding algorithm plugin metadata before choosing algorithm arguments."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_KEY_GENERATOR,
                    List.of(new ResourceHintTemplate("shardingsphere://features/sharding/key-generate-algorithm-plugins", "algorithm", "read_first",
                            "Read key-generate algorithm plugin metadata before choosing generator arguments."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_KEY_GENERATE_STRATEGY,
                    List.of(new ResourceHintTemplate("shardingsphere://features/sharding/key-generate-algorithm-plugins", "algorithm", "read_first",
                            "Read key-generate algorithm plugin metadata before choosing generator arguments."))));
    
    private static final Collection<String> STORAGE_UNIT_WORKFLOW_KINDS = List.of(
            WorkflowKindDescriptors.READWRITE_RULE, WorkflowKindDescriptors.READWRITE_STATUS, WorkflowKindDescriptors.SHADOW_RULE, WorkflowKindDescriptors.SHARDING_TABLE_RULE);
    
    private static final Collection<String> SINGLE_TABLE_WORKFLOW_KINDS = List.of(WorkflowKindDescriptors.SHADOW_RULE, WorkflowKindDescriptors.SHARDING_TABLE_RULE);
    
    private static final Map<String, List<TableResourceHintTemplate>> FEATURE_TABLE_RULE_HINTS = Map.of(
            WorkflowKindDescriptors.ENCRYPT_RULE, List.of(new TableResourceHintTemplate("shardingsphere://features/encrypt/databases/%s/tables/%s/rules",
                    "Inspect current encrypt table rule DistSQL state before planning changes.")),
            WorkflowKindDescriptors.MASK_RULE, List.of(new TableResourceHintTemplate("shardingsphere://features/mask/databases/%s/tables/%s/rules",
                    "Inspect current mask table rule DistSQL state before planning changes.")),
            WorkflowKindDescriptors.SHADOW_RULE, List.of(new TableResourceHintTemplate("shardingsphere://features/shadow/databases/%s/tables/%s/rules",
                    "Inspect current shadow table rule DistSQL state before planning changes.")),
            WorkflowKindDescriptors.SHARDING_TABLE_RULE, List.of(
                    new TableResourceHintTemplate("shardingsphere://features/sharding/databases/%s/tables/%s/table-rule",
                            "Inspect current sharding table rule DistSQL state before planning changes."),
                    new TableResourceHintTemplate("shardingsphere://features/sharding/databases/%s/tables/%s/nodes",
                            "Inspect current sharding table nodes before planning changes.")));
    
    private static final Map<String, List<DatabaseResourceHintTemplate>> RULE_RESOURCE_HINTS = Map.ofEntries(
            Map.entry(WorkflowKindDescriptors.ENCRYPT_RULE, List.of(new DatabaseResourceHintTemplate("shardingsphere://features/encrypt/databases/%s/rules",
                    "Inspect current encrypt rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.MASK_RULE, List.of(new DatabaseResourceHintTemplate("shardingsphere://features/mask/databases/%s/rules",
                    "Inspect current mask rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.BROADCAST_RULE, List.of(new DatabaseResourceHintTemplate("shardingsphere://features/broadcast/databases/%s/rules",
                    "Inspect current broadcast rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.READWRITE_RULE,
                    List.of(new DatabaseResourceHintTemplate("shardingsphere://features/readwrite-splitting/databases/%s/rules",
                            "Inspect current readwrite-splitting rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.READWRITE_STATUS,
                    List.of(new DatabaseResourceHintTemplate("shardingsphere://features/readwrite-splitting/databases/%s/status",
                            "Inspect current readwrite-splitting status before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHADOW_RULE, List.of(new DatabaseResourceHintTemplate("shardingsphere://features/shadow/databases/%s/rules",
                    "Inspect current shadow rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHADOW_DEFAULT_ALGORITHM,
                    List.of(new DatabaseResourceHintTemplate("shardingsphere://features/shadow/databases/%s/default-algorithm",
                            "Inspect current default shadow algorithm before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHADOW_ALGORITHM_CLEANUP, List.of(
                    new DatabaseResourceHintTemplate("shardingsphere://features/shadow/databases/%s/algorithms", "Inspect configured shadow algorithms before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/shadow/databases/%s/table-rules", "Inspect shadow table rule references before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/shadow/databases/%s/default-algorithm",
                            "Inspect default shadow algorithm references before planning cleanup."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_TABLE_RULE, List.of(
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/table-rules", "Inspect current sharding table rules before planning changes."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/table-nodes", "Inspect current sharding table nodes before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_TABLE_REFERENCE,
                    List.of(new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/table-reference-rules",
                            "Inspect current sharding table reference rules before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_DEFAULT_STRATEGY, List.of(
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/default-strategy",
                            "Inspect current default sharding strategy before planning changes."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/algorithms",
                            "Inspect configured sharding algorithms before planning default strategy changes."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_KEY_GENERATOR,
                    List.of(new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/key-generators",
                            "Inspect current sharding key generators before planning changes."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_KEY_GENERATE_STRATEGY, List.of(
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/key-generate-strategies",
                            "Inspect current sharding key generate strategies before planning changes."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/key-generators",
                            "Inspect current sharding key generators before planning key generate strategy changes."))),
            Map.entry(WorkflowKindDescriptors.SHARDING_COMPONENT_CLEANUP, List.of(
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/algorithms", "Inspect configured sharding algorithms before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/key-generators",
                            "Inspect configured sharding key generators before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/auditors", "Inspect configured sharding auditors before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/unused-algorithms",
                            "Inspect unused sharding algorithms before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/unused-key-generators",
                            "Inspect unused sharding key generators before planning cleanup."),
                    new DatabaseResourceHintTemplate("shardingsphere://features/sharding/databases/%s/unused-auditors",
                            "Inspect unused sharding auditors before planning cleanup."))));
    
    /**
     * Create resource hints for a workflow planning response.
     *
     * @param snapshot workflow snapshot
     * @return resource hints
     */
    public List<Map<String, Object>> createResourcesToRead(final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> result = new LinkedList<>();
        addFeatureResources(result, snapshot);
        WorkflowRequest request = snapshot.getRequest();
        addDescriptorRelatedResources(result, snapshot, request);
        if (!request.getDatabase().isEmpty()) {
            addRuleResources(result, snapshot, request);
            addGovernanceMetadataResources(result, snapshot, request);
            if (WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot) && !request.getTable().isEmpty()) {
                addFeatureTableRuleResources(result, snapshot, request);
            } else if (!request.getSchema().isEmpty() && !request.getTable().isEmpty()) {
                addTableResources(result, request);
            }
        }
        return result;
    }
    
    private void addFeatureResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot) {
        for (ResourceHintTemplate each : FEATURE_RESOURCE_HINTS.getOrDefault(resolveWorkflowKind(snapshot), List.of())) {
            addResourceHint(resourcesToRead, each.uri(), each.resourceKind(), each.action(), each.reason());
        }
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
    
    private void addFeatureTableRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        for (TableResourceHintTemplate each : FEATURE_TABLE_RULE_HINTS.getOrDefault(resolveWorkflowKind(snapshot), List.of())) {
            addTableResourceHint(resourcesToRead, request, each.uriTemplate(), each.reason());
        }
    }
    
    private void addRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        for (DatabaseResourceHintTemplate each : RULE_RESOURCE_HINTS.getOrDefault(resolveWorkflowKind(snapshot), List.of())) {
            addDatabaseResourceHint(resourcesToRead, request, each.uriTemplate(), each.reason());
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
            createConcreteResourceUri(uriTemplate, request).ifPresent(uri -> addResourceHint(resourcesToRead, uri, resolveResourceKind(uriTemplate), "inspect_detail",
                    "Read descriptor-related resource before planning workflow artifacts."));
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
    
    private void addDatabaseResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request, final String uriTemplate, final String reason) {
        addResourceHint(resourcesToRead, String.format(uriTemplate, MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase())), "rule", "inspect_detail", reason);
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
    
    private void addTableResourceHint(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request, final String uriTemplate, final String reason) {
        addResourceHint(resourcesToRead, String.format(uriTemplate, MCPUriPathSegmentUtils.encodePathSegment(request.getDatabase()), MCPUriPathSegmentUtils.encodePathSegment(request.getTable())),
                "rule", "inspect_detail", reason);
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
    
    private record ResourceHintTemplate(String uri, String resourceKind, String action, String reason) {
    }
    
    private record DatabaseResourceHintTemplate(String uriTemplate, String reason) {
    }
    
    private record TableResourceHintTemplate(String uriTemplate, String reason) {
    }
}
