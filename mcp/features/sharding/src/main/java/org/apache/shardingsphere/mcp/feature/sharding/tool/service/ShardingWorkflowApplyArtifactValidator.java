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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sharding workflow apply artifact validator.
 */
public final class ShardingWorkflowApplyArtifactValidator implements MCPWorkflowApplyArtifactValidator {
    
    @Override
    public List<Map<String, Object>> validate(final WorkflowContextSnapshot snapshot, final Collection<ExecutableWorkflowArtifact> artifacts) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (ExecutableWorkflowArtifact each : artifacts) {
            addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
        }
        return result;
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isRuleDefinitionDistSQL(sql) || !(snapshot.getRequest() instanceof ShardingWorkflowRequest)) {
            return;
        }
        ShardingWorkflowRequest request = (ShardingWorkflowRequest) snapshot.getRequest();
        if (ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addTableRuleAlgorithmIssues(issues, request, displaySql);
            return;
        }
        if (ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addShardingAlgorithmIssue(issues, request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
            return;
        }
        if (ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addKeyGeneratorTypeIssue(issues, request, displaySql);
            return;
        }
        if (ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addKeyGeneratorIssue(issues, request, displaySql);
        }
    }
    
    private boolean isRuleDefinitionDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE SHARDING ") || actualSQL.startsWith("CREATE DEFAULT SHARDING ");
    }
    
    private void addTableRuleAlgorithmIssues(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (!request.getStorageUnits().isEmpty() || !"none".equals(normalizeStrategyType(request))) {
            addShardingAlgorithmIssue(issues, request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
        }
        addKeyGeneratorIssue(issues, request, displaySql);
        for (String each : request.getAuditorNames()) {
            addAuditorIssue(issues, each, displaySql);
        }
    }
    
    private void addShardingAlgorithmIssue(final List<Map<String, Object>> issues, final String algorithmType, final Map<String, String> properties, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(ShardingAlgorithm.class, algorithmType, properties)) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references sharding algorithm `%s`, "
                    + "but it cannot be loaded or initialized by ShardingAlgorithm SPI.", algorithmType), displaySql));
        }
    }
    
    private void addKeyGeneratorIssue(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (!request.getKeyGeneratorName().isEmpty() || request.getKeyGeneratorType().isEmpty()) {
            return;
        }
        addKeyGeneratorTypeIssue(issues, request, displaySql);
    }
    
    private void addKeyGeneratorTypeIssue(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (request.getKeyGeneratorType().isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(KeyGenerateAlgorithm.class, request.getKeyGeneratorType(), request.getKeyGeneratorProperties())) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references key generator algorithm `%s`, "
                    + "but it cannot be loaded or initialized by KeyGenerateAlgorithm SPI.", request.getKeyGeneratorType()), displaySql));
        }
    }
    
    private void addAuditorIssue(final List<Map<String, Object>> issues, final String algorithmType, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(ShardingAuditAlgorithm.class, algorithmType, Map.of())) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references auditor algorithm `%s`, "
                    + "but it cannot be loaded or initialized by ShardingAuditAlgorithm SPI.", algorithmType), displaySql));
        }
    }
    
    private String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().toLowerCase(Locale.ENGLISH);
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
}
