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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow artifact bundle.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowArtifactBundle {
    
    private final List<DDLArtifact> ddlArtifacts;
    
    private final List<IndexPlan> indexPlans;
    
    private final List<RuleArtifact> ruleArtifacts;
    
    /**
     * Create artifact bundle from workflow snapshot.
     *
     * @param snapshot workflow snapshot
     * @return artifact bundle
     */
    public static WorkflowArtifactBundle from(final WorkflowContextSnapshot snapshot) {
        return new WorkflowArtifactBundle(snapshot.getDdlArtifacts(), snapshot.getIndexPlans(), snapshot.getRuleArtifacts());
    }
    
    /**
     * Convert workflow artifacts into executable artifacts.
     *
     * @return executable workflow artifacts
     */
    public List<ExecutableWorkflowArtifact> toExecutableArtifacts() {
        List<ExecutableWorkflowArtifact> result = new LinkedList<>();
        for (DDLArtifact each : ddlArtifacts) {
            result.add(new ExecutableWorkflowArtifact(WorkflowArtifactPayloadUtils.STEP_DDL, each.getArtifactType(), each.getSql(), false));
        }
        for (IndexPlan each : indexPlans) {
            result.add(new ExecutableWorkflowArtifact(WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_CREATE_INDEX, each.getSql(), false));
        }
        for (RuleArtifact each : ruleArtifacts) {
            result.add(new ExecutableWorkflowArtifact(WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL, each.getSql(), true));
        }
        return result;
    }
    
    Map<String, Object> toPayload(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS, ddlArtifacts.stream().map(DDLArtifact::toMap).toList());
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN, indexPlans.stream().map(IndexPlan::toMap).toList());
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS, ruleArtifacts.stream()
                .map(each -> WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(each, propertySource, propertyRequirements)).toList());
        return result;
    }
    
    public record ExecutableWorkflowArtifact(String approvalStep, String artifactType, String sql, boolean ruleDistSql) {
    }
}
