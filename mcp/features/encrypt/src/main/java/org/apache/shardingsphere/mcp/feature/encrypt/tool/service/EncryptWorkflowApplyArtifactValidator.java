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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Encrypt workflow apply artifact validator.
 */
public final class EncryptWorkflowApplyArtifactValidator implements MCPWorkflowApplyArtifactValidator {
    
    private static final Pattern UNQUOTED_RESERVED_NAME_COLUMN_PATTERN = Pattern.compile("\\(\\s*NAME\\s*=\\s*name\\s*,\\s*CIPHER\\s*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern UNQUOTED_AES_TYPE_PATTERN = Pattern.compile("TYPE\\s*\\(\\s*NAME\\s*=\\s*AES\\b", Pattern.CASE_INSENSITIVE);
    
    @Override
    public List<Map<String, Object>> validate(final WorkflowContextSnapshot snapshot, final Collection<ExecutableWorkflowArtifact> artifacts) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (ExecutableWorkflowArtifact each : artifacts) {
            addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
        }
        return result;
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isEncryptRuleDistSQL(sql)) {
            return;
        }
        if (UNQUOTED_RESERVED_NAME_COLUMN_PATTERN.matcher(sql).find()) {
            issues.add(createValidationIssue("Generated encrypt DistSQL uses reserved logical column identifier `name` without DistSQL quoting.", displaySql));
        }
        if (UNQUOTED_AES_TYPE_PATTERN.matcher(sql).find()) {
            issues.add(createValidationIssue("Generated encrypt DistSQL uses AES algorithm type without a string literal.", displaySql));
        }
        String actualSQL = sql.toLowerCase(Locale.ENGLISH);
        if (actualSQL.contains("encrypt_algorithm") && actualSQL.contains("'aes-key-value'") && !actualSQL.contains("'digest-algorithm-name'")) {
            issues.add(createValidationIssue("Generated AES encrypt DistSQL is missing `digest-algorithm-name`.", displaySql));
        }
        addAlgorithmIssues(issues, getWorkflowRequest(snapshot), displaySql);
    }
    
    private void addAlgorithmIssues(final List<Map<String, Object>> issues, final EncryptWorkflowRequest request, final String displaySql) {
        addEncryptAlgorithmIssue(issues, "encrypt", request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            addEncryptAlgorithmIssue(issues, "assisted query", request.getOptions().getAssistedQueryAlgorithmType(), request.getOptions().getAssistedQueryAlgorithmProperties(), displaySql);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            addEncryptAlgorithmIssue(issues, "like query", request.getOptions().getLikeQueryAlgorithmType(), request.getOptions().getLikeQueryAlgorithmProperties(), displaySql);
        }
    }
    
    private void addEncryptAlgorithmIssue(final List<Map<String, Object>> issues, final String role, final String algorithmType,
                                          final Map<String, String> properties, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(EncryptAlgorithm.class, algorithmType, properties)) {
            issues.add(createValidationIssue(String.format("Generated encrypt DistSQL references %s algorithm `%s`, but it cannot be loaded or initialized by EncryptAlgorithm SPI.",
                    role, algorithmType), displaySql));
        }
    }
    
    private EncryptWorkflowRequest getWorkflowRequest(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getRequest() instanceof EncryptWorkflowRequest) {
            return (EncryptWorkflowRequest) snapshot.getRequest();
        }
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), null);
        return null == result ? new EncryptWorkflowRequest() : result;
    }
    
    private boolean isEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE ENCRYPT RULE");
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
}
