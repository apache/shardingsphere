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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowKindDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Map;

/**
 * Workflow artifact payload utility methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        return WorkflowKindDescriptors.isRuleDistSQLOnly(workflowKind);
    }
}
