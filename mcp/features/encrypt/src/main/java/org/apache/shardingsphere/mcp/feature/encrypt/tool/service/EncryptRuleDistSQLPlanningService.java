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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.List;

/**
 * Encrypt rule DistSQL planning service.
 */
public final class EncryptRuleDistSQLPlanningService {
    
    /**
     * Plan encrypt rule artifact.
     *
     * @param request workflow request
     * @return rule artifacts
     */
    public List<RuleArtifact> planEncryptRule(final EncryptWorkflowRequest request) {
        validateEncryptIdentifiers(request);
        return List.of(new RuleArtifact("create", createEncryptRuleSql(request.getTable(), List.of(createTargetEncryptColumnSegment(request)))));
    }
    
    /**
     * Plan encrypt drop artifact.
     *
     * @param request workflow request
     * @return rule artifacts
     */
    public List<RuleArtifact> planEncryptDropRule(final EncryptWorkflowRequest request) {
        validateEncryptDropIdentifiers(request);
        return List.of(new RuleArtifact("drop", createDropRuleSql(request.getTable())));
    }
    
    private String createDropRuleSql(final String tableName) {
        return String.format("DROP ENCRYPT RULE %s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(tableName));
    }
    
    private String createEncryptRuleSql(final String tableName, final List<String> columnSegments) {
        return String.format("CREATE ENCRYPT RULE %s (%sCOLUMNS(%s%s%s))", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(tableName), System.lineSeparator(), System.lineSeparator(),
                String.join(", " + System.lineSeparator(), columnSegments), System.lineSeparator());
    }
    
    private void validateEncryptIdentifiers(final EncryptWorkflowRequest request) {
        WorkflowSQLUtils.checkSupportedIdentifier("table", request.getTable());
        WorkflowSQLUtils.checkSupportedIdentifier("column", request.getColumn());
        WorkflowSQLUtils.checkSupportedIdentifier("cipher_column", request.getOptions().getCipherColumnName());
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            WorkflowSQLUtils.checkSupportedIdentifier("assisted_query_column", request.getOptions().getAssistedQueryColumnName());
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            WorkflowSQLUtils.checkSupportedIdentifier("like_query_column", request.getOptions().getLikeQueryColumnName());
        }
    }
    
    private void validateEncryptDropIdentifiers(final EncryptWorkflowRequest request) {
        WorkflowSQLUtils.checkSupportedIdentifier("table", request.getTable());
        WorkflowSQLUtils.checkSupportedIdentifier("column", request.getColumn());
    }
    
    private String createTargetEncryptColumnSegment(final EncryptWorkflowRequest request) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("(NAME=%s, CIPHER=%s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getColumn()),
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getOptions().getCipherColumnName())));
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            result.append(String.format(", ASSISTED_QUERY_COLUMN=%s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getOptions().getAssistedQueryColumnName())));
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            result.append(String.format(", LIKE_QUERY_COLUMN=%s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getOptions().getLikeQueryColumnName())));
        }
        result.append(String.format(", ENCRYPT_ALGORITHM(%s)", WorkflowSQLUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties())));
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            result.append(String.format(", ASSISTED_QUERY_ALGORITHM(%s)",
                    WorkflowSQLUtils.createAlgorithmFragment(request.getOptions().getAssistedQueryAlgorithmType(), request.getOptions().getAssistedQueryAlgorithmProperties())));
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            result.append(String.format(", LIKE_QUERY_ALGORITHM(%s)",
                    WorkflowSQLUtils.createAlgorithmFragment(request.getOptions().getLikeQueryAlgorithmType(), request.getOptions().getLikeQueryAlgorithmProperties())));
        }
        result.append(")");
        return result.toString();
    }
}
