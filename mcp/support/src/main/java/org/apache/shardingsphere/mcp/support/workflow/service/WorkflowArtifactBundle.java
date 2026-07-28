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
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Workflow artifact bundle.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowArtifactBundle {
    
    private final Collection<RuleArtifact> ruleArtifacts;
    
    /**
     * Create artifact bundle from workflow snapshot.
     *
     * @param snapshot workflow snapshot
     * @return artifact bundle
     */
    public static WorkflowArtifactBundle from(final WorkflowContextSnapshot snapshot) {
        return new WorkflowArtifactBundle(snapshot.getRuleArtifacts());
    }
    
    /**
     * Convert workflow artifacts into executable artifacts with masked display SQL.
     *
     * @param propertySource workflow property source
     * @param propertyRequirements property requirements
     * @return executable workflow artifacts
     */
    public List<ExecutableWorkflowArtifact> toExecutableArtifacts(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        return ruleArtifacts.stream().map(each -> createExecutableArtifact(each, propertySource, propertyRequirements)).toList();
    }
    
    private ExecutableWorkflowArtifact createExecutableArtifact(final RuleArtifact ruleArtifact, final WorkflowPropertySource propertySource,
                                                                final List<AlgorithmPropertyRequirement> propertyRequirements) {
        return new ExecutableWorkflowArtifact(ruleArtifact.getSql(), WorkflowArtifactMaskUtils.maskSensitiveSql(ruleArtifact.getSql(), propertySource, propertyRequirements));
    }
    
    Map<String, Object> toPayload(final WorkflowPropertySource propertySource, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        return Map.of(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS, ruleArtifacts.stream()
                .map(each -> WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(each, propertySource, propertyRequirements)).toList());
    }
    
    public record ExecutableWorkflowArtifact(String sql, String displaySql) {
    }
}
