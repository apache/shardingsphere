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

/**
 * Provides model-facing workflow resource read hints.
 */
public final class WorkflowGuidanceResourceHintProvider {
    
    private static final String ENCRYPT_RULE_WORKFLOW_KIND = "encrypt.rule";
    
    private static final String MASK_RULE_WORKFLOW_KIND = "mask.rule";
    
    private static final String BROADCAST_RULE_WORKFLOW_KIND = "broadcast.rule";
    
    private static final String READWRITE_RULE_WORKFLOW_KIND = "readwrite.rule";
    
    private static final String READWRITE_STATUS_WORKFLOW_KIND = "readwrite.status";
    
    private static final String SHADOW_RULE_WORKFLOW_KIND = "shadow.rule";
    
    private static final String SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND = "shadow.default";
    
    private static final String SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND = "shadow.cleanup";
    
    private static final String SHARDING_TABLE_RULE_WORKFLOW_KIND = "sharding.table.rule";
    
    private static final String SHARDING_TABLE_REFERENCE_WORKFLOW_KIND = "sharding.table.reference";
    
    private static final String SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND = "sharding.default.strategy";
    
    private static final String SHARDING_KEY_GENERATOR_WORKFLOW_KIND = "sharding.key.generator";
    
    private static final String SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND = "sharding.key.generate.strategy";
    
    private static final String SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND = "sharding.component.cleanup";
    
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
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/encrypt/algorithms", "algorithm", "read_first",
                    "Read encrypt algorithm metadata before choosing algorithm arguments.");
            case MASK_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/mask/algorithms", "algorithm", "read_first",
                    "Read mask algorithm metadata before choosing algorithm arguments.");
            case READWRITE_RULE_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins", "algorithm", "read_first",
                    "Read load-balance algorithm plugin metadata before choosing algorithm arguments.");
            case SHADOW_RULE_WORKFLOW_KIND, SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/shadow/algorithm-plugins", "algorithm",
                    "read_first", "Read shadow algorithm plugin metadata before choosing algorithm arguments.");
            case SHARDING_TABLE_RULE_WORKFLOW_KIND, SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND -> addResourceHint(resourcesToRead, "shardingsphere://features/sharding/algorithm-plugins",
                    "algorithm", "read_first", "Read sharding algorithm plugin metadata before choosing algorithm arguments.");
            case SHARDING_KEY_GENERATOR_WORKFLOW_KIND, SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND -> addResourceHint(resourcesToRead,
                    "shardingsphere://features/sharding/key-generate-algorithm-plugins", "algorithm", "read_first",
                    "Read key-generate algorithm plugin metadata before choosing generator arguments.");
            default -> {
            }
        }
    }
    
    private void addGovernanceMetadataResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        String workflowKind = resolveWorkflowKind(snapshot);
        switch (workflowKind) {
            case READWRITE_RULE_WORKFLOW_KIND, READWRITE_STATUS_WORKFLOW_KIND, SHADOW_RULE_WORKFLOW_KIND, SHARDING_TABLE_RULE_WORKFLOW_KIND -> addStorageUnitsResourceHint(resourcesToRead,
                    request);
            default -> {
            }
        }
        switch (workflowKind) {
            case SHADOW_RULE_WORKFLOW_KIND, SHARDING_TABLE_RULE_WORKFLOW_KIND -> {
                addSingleTablesResourceHint(resourcesToRead, request);
                if (!request.getTable().isEmpty()) {
                    addSingleTableResourceHint(resourcesToRead, request);
                }
            }
            default -> {
            }
        }
    }
    
    private void addFeatureTableRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/encrypt/databases/%s/tables/%s/rules",
                    "Inspect current encrypt table rule DistSQL state before planning changes.");
            case MASK_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/mask/databases/%s/tables/%s/rules",
                    "Inspect current mask table rule DistSQL state before planning changes.");
            case SHADOW_RULE_WORKFLOW_KIND -> addTableResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/tables/%s/rules",
                    "Inspect current shadow table rule DistSQL state before planning changes.");
            case SHARDING_TABLE_RULE_WORKFLOW_KIND -> {
                addTableResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/tables/%s/table-rule",
                        "Inspect current sharding table rule DistSQL state before planning changes.");
                addTableResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/tables/%s/nodes",
                        "Inspect current sharding table nodes before planning changes.");
            }
            default -> {
            }
        }
    }
    
    private void addRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        switch (resolveWorkflowKind(snapshot)) {
            case ENCRYPT_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/encrypt/databases/%s/rules",
                    "Inspect current encrypt rules before planning changes.");
            case MASK_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/mask/databases/%s/rules",
                    "Inspect current mask rules before planning changes.");
            case BROADCAST_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/broadcast/databases/%s/rules",
                    "Inspect current broadcast rules before planning changes.");
            case READWRITE_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/readwrite-splitting/databases/%s/rules",
                    "Inspect current readwrite-splitting rules before planning changes.");
            case READWRITE_STATUS_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/readwrite-splitting/databases/%s/status",
                    "Inspect current readwrite-splitting status before planning changes.");
            case SHADOW_RULE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/rules",
                    "Inspect current shadow rules before planning changes.");
            case SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/default-algorithm",
                    "Inspect current default shadow algorithm before planning changes.");
            case SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND -> {
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/algorithms",
                        "Inspect configured shadow algorithms before planning cleanup.");
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/table-rules",
                        "Inspect shadow table rule references before planning cleanup.");
                addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/shadow/databases/%s/default-algorithm",
                        "Inspect default shadow algorithm references before planning cleanup.");
            }
            case SHARDING_TABLE_RULE_WORKFLOW_KIND -> addShardingTableRuleResources(resourcesToRead, request);
            case SHARDING_TABLE_REFERENCE_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-reference-rules",
                    "Inspect current sharding table reference rules before planning changes.");
            case SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND -> addShardingDefaultStrategyResources(resourcesToRead, request);
            case SHARDING_KEY_GENERATOR_WORKFLOW_KIND -> addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                    "Inspect current sharding key generators before planning changes.");
            case SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND -> addShardingKeyGenerateStrategyResources(resourcesToRead, request);
            case SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND -> addShardingComponentCleanupResources(resourcesToRead, request);
            default -> {
            }
        }
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
        resourcesToRead.add(MCPResourceHintUtils.create(uri, resourceKind, action, reason, MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private void addShardingTableRuleResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-rules",
                "Inspect current sharding table rules before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/table-nodes",
                "Inspect current sharding table nodes before planning changes.");
    }
    
    private void addShardingDefaultStrategyResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/default-strategy",
                "Inspect current default sharding strategy before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/algorithms",
                "Inspect configured sharding algorithms before planning default strategy changes.");
    }
    
    private void addShardingKeyGenerateStrategyResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generate-strategies",
                "Inspect current sharding key generate strategies before planning changes.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                "Inspect current sharding key generators before planning key generate strategy changes.");
    }
    
    private void addShardingComponentCleanupResources(final Collection<Map<String, Object>> resourcesToRead, final WorkflowRequest request) {
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/algorithms",
                "Inspect configured sharding algorithms before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/key-generators",
                "Inspect configured sharding key generators before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/auditors",
                "Inspect configured sharding auditors before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-algorithms",
                "Inspect unused sharding algorithms before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-key-generators",
                "Inspect unused sharding key generators before planning cleanup.");
        addDatabaseResourceHint(resourcesToRead, request, "shardingsphere://features/sharding/databases/%s/unused-auditors",
                "Inspect unused sharding auditors before planning cleanup.");
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
