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
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Derived column naming service.
 */
public final class DerivedColumnNamingService {
    
    /**
     * Create derived column plan.
     *
     * @param request encrypt workflow request
     * @param existingNames existing names
     * @param issues workflow issues
     * @return derived column plan
     */
    public DerivedColumnPlan createPlan(final EncryptWorkflowRequest request, final Set<String> existingNames, final List<WorkflowIssue> issues) {
        return createPlan(request, existingNames, issues, "");
    }
    
    /**
     * Create derived column plan.
     *
     * @param request encrypt workflow request
     * @param existingNames existing names
     * @param issues workflow issues
     * @param databaseType database type
     * @return derived column plan
     */
    public DerivedColumnPlan createPlan(final EncryptWorkflowRequest request, final Set<String> existingNames, final List<WorkflowIssue> issues, final String databaseType) {
        boolean requiresAssistedQuery = Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter());
        boolean requiresLikeQuery = Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery());
        DerivedColumnPlan result = new DerivedColumnPlan();
        String logicalColumn = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, request.getColumn());
        result.setLogicalColumn(logicalColumn);
        result.setCipherColumnRequired(true);
        result.setAssistedQueryColumnRequired(requiresAssistedQuery);
        result.setLikeQueryColumnRequired(requiresLikeQuery);
        result.setCipherColumnName(resolveName(logicalColumn + "_cipher", request.getOptions().getCipherColumnName(), existingNames, issues, result, databaseType));
        if (requiresAssistedQuery) {
            result.setAssistedQueryColumnName(resolveName(logicalColumn + "_assisted_query", request.getOptions().getAssistedQueryColumnName(), existingNames, issues, result, databaseType));
        }
        if (requiresLikeQuery) {
            result.setLikeQueryColumnName(resolveName(logicalColumn + "_like_query", request.getOptions().getLikeQueryColumnName(), existingNames, issues, result, databaseType));
        }
        return result;
    }
    
    private String resolveName(final String defaultName, final String overrideName, final Set<String> existingNames,
                               final List<WorkflowIssue> issues, final DerivedColumnPlan plan, final String databaseType) {
        String actualCandidate = overrideName.isEmpty() ? WorkflowSQLUtils.canonicalizeIdentifier(databaseType, defaultName) : overrideName;
        if (!WorkflowSQLUtils.isSupportedIdentifier(actualCandidate)) {
            issues.add(new WorkflowIssue(WorkflowIssueCode.USER_OVERRIDE_NAME_UNSAFE, "error", "planning-artifacts",
                    String.format("Generated column name `%s` is unsafe.", actualCandidate), "Use a reviewable SQL identifier without NUL or line terminators.", false,
                    Map.of("candidate", actualCandidate)));
            actualCandidate = WorkflowSQLUtils.canonicalizeIdentifier(databaseType, defaultName);
        }
        if (!containsIdentifier(databaseType, existingNames, actualCandidate)) {
            existingNames.add(WorkflowSQLUtils.canonicalizeIdentifier(databaseType, actualCandidate));
            return actualCandidate;
        }
        int suffix = 1;
        while (containsIdentifier(databaseType, existingNames, appendSuffix(actualCandidate, suffix))) {
            suffix++;
        }
        String resolvedName = appendSuffix(actualCandidate, suffix);
        existingNames.add(WorkflowSQLUtils.canonicalizeIdentifier(databaseType, resolvedName));
        plan.getNameCollisions().add(new LinkedHashMap<>(Map.of("original_name", actualCandidate, "resolved_name", resolvedName)));
        issues.add(new WorkflowIssue(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT, "warning", "planning-artifacts",
                String.format("Derived column name `%s` conflicts with existing objects and has been renamed.", actualCandidate),
                "Review the resolved derived column names.", false, Map.of("original_name", actualCandidate, "resolved_name", resolvedName)));
        return resolvedName;
    }
    
    private String appendSuffix(final String identifier, final int suffix) {
        if (isDelimitedBy(identifier, '"', '"') || isDelimitedBy(identifier, '`', '`') || isDelimitedBy(identifier, '[', ']')) {
            return identifier.substring(0, identifier.length() - 1) + "_" + suffix + identifier.charAt(identifier.length() - 1);
        }
        return identifier + "_" + suffix;
    }
    
    private boolean isDelimitedBy(final String identifier, final char startDelimiter, final char endDelimiter) {
        return identifier.length() >= 2 && startDelimiter == identifier.charAt(0) && endDelimiter == identifier.charAt(identifier.length() - 1);
    }
    
    private boolean containsIdentifier(final String databaseType, final Set<String> identifiers, final String targetIdentifier) {
        return identifiers.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, targetIdentifier, each));
    }
}
