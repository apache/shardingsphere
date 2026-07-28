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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

/**
 * Shadow DistSQL planning service.
 */
public final class ShadowDistSQLPlanningService {
    
    /**
     * Plan create shadow rule DistSQL.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planCreateRule(final ShadowRuleWorkflowRequest request) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_CREATE, String.format("CREATE SHADOW RULE %s(SOURCE=%s, SHADOW=%s, %s(%s))",
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getRuleName()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getSourceStorageUnit()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getShadowStorageUnit()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getTableName()),
                WorkflowSQLUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getAlgorithmProperties())));
    }
    
    /**
     * Plan alter shadow rule DistSQL.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planAlterRule(final ShadowRuleWorkflowRequest request) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_ALTER, String.format("ALTER SHADOW RULE %s(SOURCE=%s, SHADOW=%s, %s(%s))",
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getRuleName()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getSourceStorageUnit()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getShadowStorageUnit()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getTableName()),
                WorkflowSQLUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getAlgorithmProperties())));
    }
    
    /**
     * Plan drop shadow rule DistSQL.
     *
     * @param ruleName rule name
     * @return rule artifact
     */
    public RuleArtifact planDropRule(final String ruleName) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_DROP, String.format("DROP SHADOW RULE %s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(ruleName)));
    }
    
    /**
     * Plan create default shadow algorithm DistSQL.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planCreateDefaultAlgorithm(final ShadowDefaultAlgorithmWorkflowRequest request) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_CREATE, String.format("CREATE DEFAULT SHADOW ALGORITHM %s",
                createDefaultAlgorithmFragment(request)));
    }
    
    /**
     * Plan alter default shadow algorithm DistSQL.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planAlterDefaultAlgorithm(final ShadowDefaultAlgorithmWorkflowRequest request) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_ALTER, String.format("ALTER DEFAULT SHADOW ALGORITHM %s",
                createDefaultAlgorithmFragment(request)));
    }
    
    private String createDefaultAlgorithmFragment(final ShadowDefaultAlgorithmWorkflowRequest request) {
        return WorkflowSQLUtils.createAlgorithmFragmentWithExactType(ShadowFeatureDefinition.DEFAULT_ALGORITHM_TYPE, request.getAlgorithmProperties());
    }
    
    /**
     * Plan drop default shadow algorithm DistSQL.
     *
     * @return rule artifact
     */
    public RuleArtifact planDropDefaultAlgorithm() {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_DROP, "DROP DEFAULT SHADOW ALGORITHM");
    }
    
    /**
     * Plan drop shadow algorithm DistSQL.
     *
     * @param algorithmName algorithm name
     * @return rule artifact
     */
    public RuleArtifact planDropAlgorithm(final String algorithmName) {
        return new RuleArtifact(WorkflowLifecycle.OPERATION_DROP, String.format("DROP SHADOW ALGORITHM %s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(algorithmName)));
    }
}
