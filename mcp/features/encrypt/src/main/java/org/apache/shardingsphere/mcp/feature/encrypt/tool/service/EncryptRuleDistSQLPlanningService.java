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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Encrypt rule DistSQL planning service.
 */
public final class EncryptRuleDistSQLPlanningService {
    
    /**
     * Plan encrypt rule artifact.
     *
     * @param request workflow request
     * @param workflowState encrypt workflow state
     * @param existingRules existing table rule rows
     * @return rule artifact
     */
    public RuleArtifact planEncryptRule(final WorkflowRequest request, final EncryptWorkflowState workflowState,
                                        final List<Map<String, Object>> existingRules) {
        DerivedColumnPlan derivedColumnPlan = workflowState.getDerivedColumnPlan();
        validateEncryptIdentifiers(request, derivedColumnPlan);
        String lineSeparator = System.lineSeparator();
        String prefix = "alter".equalsIgnoreCase(request.getOperationType()) || !existingRules.isEmpty() ? "ALTER ENCRYPT RULE" : "CREATE ENCRYPT RULE";
        return new RuleArtifact(request.getOperationType(), String.format("%s %s (%sCOLUMNS(%s%s))", prefix, request.getTable(), lineSeparator, lineSeparator,
                String.join(", ", buildEncryptColumnSegments(request, workflowState, derivedColumnPlan, existingRules))));
    }
    
    /**
     * Plan encrypt drop artifact.
     *
     * @param request workflow request
     * @param existingRules existing table rule rows
     * @return rule artifact
     */
    public RuleArtifact planEncryptDropRule(final WorkflowRequest request, final List<Map<String, Object>> existingRules) {
        validateEncryptDropIdentifiers(request);
        List<String> remainingColumnSegments = new LinkedList<>();
        for (Map<String, Object> each : existingRules) {
            if (!request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column"))) {
                remainingColumnSegments.add(createExistingEncryptColumnSegment(each));
            }
        }
        return remainingColumnSegments.isEmpty()
                ? new RuleArtifact("drop", String.format("DROP ENCRYPT RULE %s", request.getTable()))
                : new RuleArtifact("drop", String.format("ALTER ENCRYPT RULE %s (%sCOLUMNS(%s%s%s))", request.getTable(),
                        System.lineSeparator(), System.lineSeparator(), String.join(", " + System.lineSeparator(), remainingColumnSegments), System.lineSeparator()));
    }
    
    private void validateEncryptIdentifiers(final WorkflowRequest request, final DerivedColumnPlan derivedColumnPlan) {
        WorkflowSqlUtils.checkSafeIdentifier("table", request.getTable());
        WorkflowSqlUtils.checkSafeIdentifier("column", request.getColumn());
        WorkflowSqlUtils.checkSafeIdentifier("cipher_column", derivedColumnPlan.getCipherColumnName());
        WorkflowSqlUtils.checkSafeIdentifier("assisted_query_column", derivedColumnPlan.getAssistedQueryColumnName());
        WorkflowSqlUtils.checkSafeIdentifier("like_query_column", derivedColumnPlan.getLikeQueryColumnName());
    }
    
    private void validateEncryptDropIdentifiers(final WorkflowRequest request) {
        WorkflowSqlUtils.checkSafeIdentifier("table", request.getTable());
        WorkflowSqlUtils.checkSafeIdentifier("column", request.getColumn());
    }
    
    private List<String> buildEncryptColumnSegments(final WorkflowRequest request, final EncryptWorkflowState workflowState, final DerivedColumnPlan derivedColumnPlan,
                                                    final List<Map<String, Object>> existingRules) {
        List<String> result = new LinkedList<>();
        boolean targetColumnHandled = false;
        for (Map<String, Object> each : existingRules) {
            if (request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column"))) {
                result.add(createTargetEncryptColumnSegment(request, workflowState, derivedColumnPlan));
                targetColumnHandled = true;
                continue;
            }
            result.add(createExistingEncryptColumnSegment(each));
        }
        if (!targetColumnHandled) {
            result.add(createTargetEncryptColumnSegment(request, workflowState, derivedColumnPlan));
        }
        return result;
    }
    
    private String createTargetEncryptColumnSegment(final WorkflowRequest request, final EncryptWorkflowState workflowState, final DerivedColumnPlan derivedColumnPlan) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("(NAME=%s, CIPHER=%s", request.getColumn(), derivedColumnPlan.getCipherColumnName()));
        if (derivedColumnPlan.isAssistedQueryColumnRequired()) {
            result.append(String.format(", ASSISTED_QUERY=%s", derivedColumnPlan.getAssistedQueryColumnName()));
        }
        if (derivedColumnPlan.isLikeQueryColumnRequired()) {
            result.append(String.format(", LIKE_QUERY=%s", derivedColumnPlan.getLikeQueryColumnName()));
        }
        result.append(String.format(", ENCRYPT_ALGORITHM(%s)", WorkflowSqlUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties())));
        if (Boolean.TRUE.equals(workflowState.getOptions().getRequiresEqualityFilter())) {
            result.append(String.format(", ASSISTED_QUERY_ALGORITHM(%s)",
                    WorkflowSqlUtils.createAlgorithmFragment(workflowState.getOptions().getAssistedQueryAlgorithmType(), workflowState.getOptions().getAssistedQueryAlgorithmProperties())));
        }
        if (Boolean.TRUE.equals(workflowState.getOptions().getRequiresLikeQuery())) {
            result.append(String.format(", LIKE_QUERY_ALGORITHM(%s)",
                    WorkflowSqlUtils.createAlgorithmFragment(workflowState.getOptions().getLikeQueryAlgorithmType(), workflowState.getOptions().getLikeQueryAlgorithmProperties())));
        }
        result.append(")");
        return result.toString();
    }
    
    private String createExistingEncryptColumnSegment(final Map<String, Object> rule) {
        String logicColumn = WorkflowRuleValueUtils.findRuleValue(rule, "logic_column", "column");
        String cipherColumn = WorkflowRuleValueUtils.findRuleValue(rule, "cipher_column");
        String assistedQueryColumn = WorkflowRuleValueUtils.findRuleValue(rule, "assisted_query_column", "assisted_query");
        String likeQueryColumn = WorkflowRuleValueUtils.findRuleValue(rule, "like_query_column", "like_query");
        WorkflowSqlUtils.checkSafeIdentifier("column", logicColumn);
        WorkflowSqlUtils.checkSafeIdentifier("cipher_column", cipherColumn);
        WorkflowSqlUtils.checkSafeIdentifier("assisted_query_column", assistedQueryColumn);
        WorkflowSqlUtils.checkSafeIdentifier("like_query_column", likeQueryColumn);
        StringBuilder result = new StringBuilder(String.format("(NAME=%s, CIPHER=%s", logicColumn, cipherColumn));
        if (!assistedQueryColumn.isEmpty()) {
            result.append(String.format(", ASSISTED_QUERY=%s", assistedQueryColumn));
        }
        if (!likeQueryColumn.isEmpty()) {
            result.append(String.format(", LIKE_QUERY=%s", likeQueryColumn));
        }
        result.append(String.format(", ENCRYPT_ALGORITHM(%s)",
                WorkflowSqlUtils.createAlgorithmFragment(WorkflowRuleValueUtils.findRuleValue(rule, "encryptor_type"), WorkflowSqlUtils.createPropertyMap(rule.get("encryptor_props")))));
        String assistedQueryType = WorkflowRuleValueUtils.findRuleValue(rule, "assisted_query_type");
        if (!assistedQueryType.isEmpty()) {
            result.append(String.format(", ASSISTED_QUERY_ALGORITHM(%s)",
                    WorkflowSqlUtils.createAlgorithmFragment(assistedQueryType, WorkflowSqlUtils.createPropertyMap(rule.get("assisted_query_props")))));
        }
        String likeQueryType = WorkflowRuleValueUtils.findRuleValue(rule, "like_query_type");
        if (!likeQueryType.isEmpty()) {
            result.append(String.format(", LIKE_QUERY_ALGORITHM(%s)",
                    WorkflowSqlUtils.createAlgorithmFragment(likeQueryType, WorkflowSqlUtils.createPropertyMap(rule.get("like_query_props")))));
        }
        result.append(")");
        return result.toString();
    }
}
