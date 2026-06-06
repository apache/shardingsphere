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

import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Map;
import java.util.Set;

/**
 * Workflow artifact payload utility methods.
 */
public final class WorkflowArtifactPayloadUtils {
    
    public static final String ARTIFACT_TYPE_CREATE_INDEX = "create-index";
    
    public static final String ARTIFACT_TYPE_RULE_DISTSQL = "rule_distsql";
    
    public static final String PAYLOAD_KEY_DDL_ARTIFACTS = "ddl_artifacts";
    
    public static final String PAYLOAD_KEY_DISTSQL_ARTIFACTS = "distsql_artifacts";
    
    public static final String PAYLOAD_KEY_INDEX_PLAN = "index_plan";
    
    public static final String PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE = "manual_artifact_package";
    
    public static final String STEP_DDL = "ddl";
    
    public static final String STEP_INDEX_DDL = "index_ddl";
    
    public static final String STEP_RULE_DISTSQL = "rule_distsql";
    
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
    
    private static final Set<String> RULE_DISTSQL_ONLY_WORKFLOW_KINDS = Set.of(ENCRYPT_RULE_WORKFLOW_KIND, MASK_RULE_WORKFLOW_KIND, BROADCAST_RULE_WORKFLOW_KIND, READWRITE_RULE_WORKFLOW_KIND,
            READWRITE_STATUS_WORKFLOW_KIND, SHADOW_RULE_WORKFLOW_KIND, SHADOW_DEFAULT_ALGORITHM_WORKFLOW_KIND, SHADOW_ALGORITHM_CLEANUP_WORKFLOW_KIND, SHARDING_TABLE_RULE_WORKFLOW_KIND,
            SHARDING_TABLE_REFERENCE_WORKFLOW_KIND, SHARDING_DEFAULT_STRATEGY_WORKFLOW_KIND, SHARDING_KEY_GENERATOR_WORKFLOW_KIND, SHARDING_KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
            SHARDING_COMPONENT_CLEANUP_WORKFLOW_KIND);
    
    private WorkflowArtifactPayloadUtils() {
    }
    
    /**
     * Create masked workflow artifact payload.
     *
     * @param snapshot workflow snapshot
     * @param propertySource workflow property source
     * @return masked workflow artifact payload
     */
    public static Map<String, Object> createArtifactPayload(final WorkflowContextSnapshot snapshot, final WorkflowPropertySource propertySource) {
        return WorkflowArtifactBundle.from(snapshot).toPayload(propertySource, snapshot.getPropertyRequirements());
    }
    
    /**
     * Create rule DistSQL only workflow artifact payload.
     *
     * @param snapshot workflow snapshot
     * @param propertySource workflow property source
     * @return rule DistSQL only workflow artifact payload
     */
    public static Map<String, Object> createRuleArtifactPayload(final WorkflowContextSnapshot snapshot, final WorkflowPropertySource propertySource) {
        return WorkflowArtifactBundle.from(snapshot).toRulePayload(propertySource, snapshot.getPropertyRequirements());
    }
    
    /**
     * Judge whether workflow must expose rule DistSQL artifacts only.
     *
     * @param snapshot workflow snapshot
     * @return whether workflow is rule DistSQL only
     */
    public static boolean isRuleDistSQLOnlyWorkflow(final WorkflowContextSnapshot snapshot) {
        String workflowKind = null == snapshot.getWorkflowKind() ? "" : snapshot.getWorkflowKind().getValue();
        return RULE_DISTSQL_ONLY_WORKFLOW_KINDS.contains(workflowKind);
    }
}
