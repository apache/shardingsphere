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
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Derived column naming service.
 */
public final class DerivedColumnNamingService {
    
    /**
     * Create derived column plan.
     *
     * @param request workflow request
     * @param workflowState encrypt workflow state
     * @param existingNames existing names
     * @param issues workflow issues
     * @return derived column plan
     */
    public DerivedColumnPlan createPlan(final WorkflowRequest request, final EncryptWorkflowState workflowState,
                                        final Set<String> existingNames, final java.util.List<WorkflowIssue> issues) {
        boolean requiresAssistedQuery = Boolean.TRUE.equals(workflowState.getOptions().getRequiresEqualityFilter());
        boolean requiresLikeQuery = Boolean.TRUE.equals(workflowState.getOptions().getRequiresLikeQuery());
        DerivedColumnPlan result = new DerivedColumnPlan();
        String logicalColumn = WorkflowSqlUtils.trimToEmpty(request.getColumn());
        result.setLogicalColumn(logicalColumn);
        result.setCipherColumnRequired(true);
        result.setAssistedQueryColumnRequired(requiresAssistedQuery);
        result.setLikeQueryColumnRequired(requiresLikeQuery);
        result.setCipherColumnName(resolveName(logicalColumn + "_cipher", workflowState.getOptions().getCipherColumnName(), existingNames, issues, result));
        if (requiresAssistedQuery) {
            result.setAssistedQueryColumnName(resolveName(logicalColumn + "_assisted_query", workflowState.getOptions().getAssistedQueryColumnName(), existingNames, issues, result));
        }
        if (requiresLikeQuery) {
            result.setLikeQueryColumnName(resolveName(logicalColumn + "_like_query", workflowState.getOptions().getLikeQueryColumnName(), existingNames, issues, result));
        }
        return result;
    }
    
    private String resolveName(final String defaultName, final String overrideName, final Set<String> existingNames,
                               final java.util.List<WorkflowIssue> issues, final DerivedColumnPlan plan) {
        String candidate = WorkflowSqlUtils.trimToEmpty(overrideName).isEmpty() ? defaultName : overrideName.trim();
        if (!WorkflowSqlUtils.isSafeIdentifier(candidate)) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.USER_OVERRIDE_NAME_UNSAFE, "error", "planning-artifacts",
                    String.format("Generated column name `%s` is unsafe.", candidate), "Use standard SQL identifiers only.", false, Map.of("candidate", candidate)));
            candidate = defaultName;
        }
        if (!existingNames.contains(candidate)) {
            existingNames.add(candidate);
            return candidate;
        }
        int suffix = 1;
        while (existingNames.contains(candidate + "_" + suffix)) {
            suffix++;
        }
        String resolvedName = candidate + "_" + suffix;
        existingNames.add(resolvedName);
        plan.getNameCollisions().add(new LinkedHashMap<>(Map.of("original_name", candidate, "resolved_name", resolvedName)));
        issues.add(new WorkflowIssue(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT, "warning", "planning-artifacts",
                String.format("Derived column name `%s` conflicts with existing objects and has been renamed.", candidate),
                "Review the resolved derived column names.", false, Map.of("original_name", candidate, "resolved_name", resolvedName)));
        return resolvedName;
    }
}
