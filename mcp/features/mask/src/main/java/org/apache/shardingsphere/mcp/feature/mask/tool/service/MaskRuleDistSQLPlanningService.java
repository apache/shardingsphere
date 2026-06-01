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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mask rule DistSQL planning service.
 */
public final class MaskRuleDistSQLPlanningService {
    
    /**
     * Plan mask rule artifact.
     *
     * @param request workflow request
     * @param existingRules existing table rule rows
     * @return rule artifact
     */
    public RuleArtifact planMaskRule(final WorkflowRequest request, final List<Map<String, Object>> existingRules) {
        validateMaskIdentifiers(request);
        String prefix = "alter".equalsIgnoreCase(request.getOperationType()) || !existingRules.isEmpty() ? "ALTER MASK RULE" : "CREATE MASK RULE";
        return new RuleArtifact(request.getOperationType(), createMaskRuleSql(prefix, request.getTable(), buildMaskColumnSegments(request, existingRules)));
    }
    
    /**
     * Plan mask drop artifact.
     *
     * @param request workflow request
     * @param existingRules existing table rule rows
     * @return rule artifact
     */
    public RuleArtifact planMaskDropRule(final WorkflowRequest request, final List<Map<String, Object>> existingRules) {
        validateMaskIdentifiers(request);
        List<String> remainingColumnSegments = new LinkedList<>();
        for (Map<String, Object> each : existingRules) {
            if (!request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.getRuleValue(each, "column"))) {
                remainingColumnSegments.add(createExistingMaskColumnSegment(each));
            }
        }
        return remainingColumnSegments.isEmpty()
                ? new RuleArtifact("drop", String.format("DROP MASK RULE %s", request.getTable()))
                : new RuleArtifact("drop", createMaskRuleSql("ALTER MASK RULE", request.getTable(), remainingColumnSegments));
    }
    
    private void validateMaskIdentifiers(final WorkflowRequest request) {
        WorkflowSQLUtils.checkSafeIdentifier("table", request.getTable());
        WorkflowSQLUtils.checkSafeIdentifier("column", request.getColumn());
    }
    
    private List<String> buildMaskColumnSegments(final WorkflowRequest request, final List<Map<String, Object>> existingRules) {
        List<String> result = new LinkedList<>();
        boolean targetColumnHandled = false;
        for (Map<String, Object> each : existingRules) {
            if (request.getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.getRuleValue(each, "column"))) {
                result.add(createTargetMaskColumnSegment(request));
                targetColumnHandled = true;
                continue;
            }
            result.add(createExistingMaskColumnSegment(each));
        }
        if (!targetColumnHandled) {
            result.add(createTargetMaskColumnSegment(request));
        }
        return result;
    }
    
    private String createTargetMaskColumnSegment(final WorkflowRequest request) {
        return String.format("(NAME=%s, %s)", request.getColumn(), WorkflowSQLUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
    }
    
    private String createExistingMaskColumnSegment(final Map<String, Object> rule) {
        String columnName = WorkflowRuleValueUtils.getRuleValue(rule, "column");
        WorkflowSQLUtils.checkSafeIdentifier("column", columnName);
        String algorithmType = WorkflowRuleValueUtils.getRuleValue(rule, "algorithm_type");
        Map<String, String> algorithmProperties = WorkflowSQLUtils.createPropertyMap(rule.get("algorithm_props"));
        return String.format("(NAME=%s, %s)", columnName, WorkflowSQLUtils.createAlgorithmFragment(algorithmType, algorithmProperties));
    }
    
    private String createMaskRuleSql(final String prefix, final String tableName, final List<String> columnSegments) {
        WorkflowSQLUtils.checkSafeIdentifier("table", tableName);
        return String.format("%s %s (%sCOLUMNS(%s%s%s))", prefix, tableName, System.lineSeparator(), System.lineSeparator(),
                String.join(", " + System.lineSeparator(), columnSegments), System.lineSeparator());
    }
}
