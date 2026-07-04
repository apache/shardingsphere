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

import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shadow workflow validation service.
 */
public final class ShadowWorkflowValidationService implements MCPWorkflowRuntimeHandler, MCPWorkflowApplyArtifactValidator {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ShadowInspectionService inspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public ShadowWorkflowValidationService() {
        inspectionService = new ShadowInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        return validationSupport.validateAndFinalize(workflowSessionContext, sessionId, snapshot, () -> createValidationReport(snapshot, queryFacade));
    }
    
    @Override
    public List<Map<String, Object>> validate(final WorkflowContextSnapshot snapshot, final Collection<ExecutableWorkflowArtifact> artifacts) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (ExecutableWorkflowArtifact each : artifacts) {
            if (each.ruleDistSql()) {
                addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
            }
        }
        return result;
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isCreateOrAlterShadowDistSQL(sql)) {
            return;
        }
        if (snapshot.getRequest() instanceof ShadowRuleWorkflowRequest) {
            ShadowRuleWorkflowRequest request = (ShadowRuleWorkflowRequest) snapshot.getRequest();
            addShadowAlgorithmIssue(issues, request.getAlgorithmType(), request.getAlgorithmProperties(), displaySql);
        }
        if (snapshot.getRequest() instanceof ShadowDefaultAlgorithmWorkflowRequest) {
            ShadowDefaultAlgorithmWorkflowRequest request = (ShadowDefaultAlgorithmWorkflowRequest) snapshot.getRequest();
            addShadowAlgorithmIssue(issues, request.getAlgorithmType(), request.getAlgorithmProperties(), displaySql);
        }
    }
    
    private boolean isCreateOrAlterShadowDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE SHADOW RULE") || actualSQL.startsWith("ALTER SHADOW RULE")
                || actualSQL.startsWith("CREATE DEFAULT SHADOW ALGORITHM") || actualSQL.startsWith("ALTER DEFAULT SHADOW ALGORITHM");
    }
    
    private void addShadowAlgorithmIssue(final List<Map<String, Object>> issues, final String algorithmType, final Map<String, String> properties, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(ShadowAlgorithm.class, algorithmType, properties)) {
            issues.add(createValidationIssue(String.format("Generated shadow DistSQL references algorithm `%s`, but it cannot be loaded or initialized by ShadowAlgorithm SPI.",
                    algorithmType), displaySql));
        }
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        String databaseType = queryFacade.getDatabaseType(snapshot.getRequest().getDatabase());
        result.setRuleValidation(validateByRequestType(snapshot, queryFacade, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateByRequestType(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade,
                                                    final ValidationReport validationReport, final String databaseType) {
        if (snapshot.getRequest() instanceof ShadowRuleWorkflowRequest) {
            return validateRule(snapshot, queryFacade, validationReport, databaseType);
        }
        if (snapshot.getRequest() instanceof ShadowDefaultAlgorithmWorkflowRequest) {
            return validateDefaultAlgorithm(snapshot, queryFacade, validationReport);
        }
        return validateAlgorithmCleanup(snapshot, queryFacade, validationReport, databaseType);
    }
    
    private ValidationSection validateRule(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade,
                                           final ValidationReport validationReport, final String databaseType) {
        ShadowRuleWorkflowRequest request = (ShadowRuleWorkflowRequest) snapshot.getRequest();
        List<Map<String, Object>> rules = inspectionService.queryRules(queryFacade, request.getDatabase());
        boolean ruleExists = containsRule(rules, databaseType, request.getRuleName());
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && ruleExists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !ruleExists) {
            addMismatch(validationReport, "shadow_rule", request.getRuleName(), "Shadow rule state does not match the planned DistSQL artifact.");
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Shadow rule state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !matchesRuleShape(rules, databaseType, request)) {
            addMismatch(validationReport, "shadow_rule", request.getRuleName(), "Shadow rule exists but source, shadow, table or algorithm type differs.");
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Shadow rule shape differs from the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rules, "Shadow rule state matches the planned DistSQL artifact.");
    }
    
    private ValidationSection validateDefaultAlgorithm(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade,
                                                       final ValidationReport validationReport) {
        ShadowDefaultAlgorithmWorkflowRequest request = (ShadowDefaultAlgorithmWorkflowRequest) snapshot.getRequest();
        List<Map<String, Object>> defaultAlgorithm = inspectionService.queryDefaultAlgorithm(queryFacade, request.getDatabase());
        boolean exists = !defaultAlgorithm.isEmpty();
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && exists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !exists) {
            addMismatch(validationReport, "default_shadow_algorithm", request.getAlgorithmType(), "Default shadow algorithm state does not match the planned DistSQL artifact.");
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, defaultAlgorithm, "Default shadow algorithm state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && defaultAlgorithm.stream()
                .noneMatch(each -> WorkflowRuleValueUtils.getRuleValue(each, "type").equalsIgnoreCase(request.getAlgorithmType()))) {
            addMismatch(validationReport, "default_shadow_algorithm", request.getAlgorithmType(), "Default shadow algorithm type differs from the planned artifact.");
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, defaultAlgorithm, "Default shadow algorithm type differs from the planned artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, defaultAlgorithm, "Default shadow algorithm state matches the planned DistSQL artifact.");
    }
    
    private ValidationSection validateAlgorithmCleanup(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade,
                                                       final ValidationReport validationReport, final String databaseType) {
        ShadowAlgorithmCleanupWorkflowRequest request = (ShadowAlgorithmCleanupWorkflowRequest) snapshot.getRequest();
        List<Map<String, Object>> algorithms = inspectionService.queryAlgorithms(queryFacade, request.getDatabase());
        if (containsAlgorithm(algorithms, databaseType, request.getAlgorithmName())) {
            addMismatch(validationReport, "shadow_algorithm", request.getAlgorithmName(), "Shadow algorithm still exists after cleanup.");
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, algorithms, "Shadow algorithm still exists after cleanup.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, algorithms, "Shadow algorithm cleanup state matches the planned DistSQL artifact.");
    }
    
    private boolean containsRule(final List<Map<String, Object>> rules, final String databaseType, final String ruleName) {
        return rules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, ruleName, WorkflowRuleValueUtils.getRuleValue(each, "rule_name")));
    }
    
    private boolean matchesRuleShape(final List<Map<String, Object>> rules, final String databaseType, final ShadowRuleWorkflowRequest request) {
        return rules.stream().filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(each, "rule_name")))
                .anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getSourceStorageUnit(), WorkflowRuleValueUtils.getRuleValue(each, "source_name"))
                        && WorkflowSQLUtils.isSameIdentifier(databaseType, request.getShadowStorageUnit(), WorkflowRuleValueUtils.getRuleValue(each, "shadow_name"))
                        && WorkflowSQLUtils.isSameIdentifier(databaseType, request.getTableName(), WorkflowRuleValueUtils.getRuleValue(each, "shadow_table"))
                        && WorkflowRuleValueUtils.getRuleValue(each, "algorithm_type").equalsIgnoreCase(request.getAlgorithmType()));
    }
    
    private boolean containsAlgorithm(final List<Map<String, Object>> algorithms, final String databaseType, final String algorithmName) {
        return algorithms.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, algorithmName,
                WorkflowRuleValueUtils.getRuleValue(each, "shadow_algorithm_name")));
    }
    
    private void addMismatch(final ValidationReport validationReport, final String field, final String expected, final String impact) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, field, expected, "",
                impact, "Inspect current shadow DistSQL-visible state and re-apply or re-plan the workflow."));
    }
}
