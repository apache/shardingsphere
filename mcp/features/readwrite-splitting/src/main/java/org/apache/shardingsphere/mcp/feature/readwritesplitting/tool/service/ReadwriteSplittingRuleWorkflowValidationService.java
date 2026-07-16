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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
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
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Readwrite-splitting rule workflow validation service.
 */
public final class ReadwriteSplittingRuleWorkflowValidationService implements MCPWorkflowRuntimeHandler, MCPWorkflowApplyArtifactValidator {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ReadwriteSplittingInspectionService inspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public ReadwriteSplittingRuleWorkflowValidationService() {
        inspectionService = new ReadwriteSplittingInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport(
                WorkflowSynchronizationSupport.DEFAULT_SYNCHRONIZATION_WINDOW, WorkflowSynchronizationSupport.DEFAULT_POLL_INTERVAL);
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
            addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
        }
        return result;
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isReadwriteSplittingRuleDistSQL(sql) || !(snapshot.getRequest() instanceof ReadwriteSplittingRuleWorkflowRequest)) {
            return;
        }
        ReadwriteSplittingRuleWorkflowRequest request = (ReadwriteSplittingRuleWorkflowRequest) snapshot.getRequest();
        addLoadBalancerIssue(issues, request, displaySql);
        addWeightIssues(issues, request, displaySql);
    }
    
    private boolean isReadwriteSplittingRuleDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE READWRITE_SPLITTING RULE") || actualSQL.startsWith("ALTER READWRITE_SPLITTING RULE");
    }
    
    private void addLoadBalancerIssue(final List<Map<String, Object>> issues, final ReadwriteSplittingRuleWorkflowRequest request, final String displaySql) {
        if (request.getLoadBalancerType().isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(LoadBalanceAlgorithm.class, request.getLoadBalancerType(), request.getLoadBalancerProperties())) {
            issues.add(createValidationIssue(String.format("Generated readwrite-splitting DistSQL references load balancer algorithm `%s`, "
                    + "but it cannot be loaded or initialized by LoadBalanceAlgorithm SPI.", request.getLoadBalancerType()), displaySql));
        }
    }
    
    private void addWeightIssues(final List<Map<String, Object>> issues, final ReadwriteSplittingRuleWorkflowRequest request, final String displaySql) {
        if (!"WEIGHT".equalsIgnoreCase(request.getLoadBalancerType())) {
            return;
        }
        if (request.getLoadBalancerProperties().isEmpty()) {
            issues.add(createValidationIssue("Generated readwrite-splitting DistSQL uses WEIGHT load balancer without weight properties.", displaySql));
            return;
        }
        for (String each : request.getLoadBalancerProperties().keySet()) {
            if (!request.getReadStorageUnits().contains(each)) {
                issues.add(createValidationIssue(String.format("Generated readwrite-splitting DistSQL defines weight for unknown read storage unit `%s`.", each), displaySql));
            }
        }
        for (String each : request.getReadStorageUnits()) {
            if (!request.getLoadBalancerProperties().containsKey(each)) {
                issues.add(createValidationIssue(String.format("Generated readwrite-splitting DistSQL is missing weight for read storage unit `%s`.", each), displaySql));
            }
        }
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        queryFacade.checkDatabaseCapability(snapshot.getRequest().getDatabase());
        List<Map<String, Object>> rules = inspectionService.queryRules(queryFacade, snapshot.getRequest().getDatabase());
        result.setRuleValidation(validateRules(snapshot, rules, result, queryFacade));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules,
                                            final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade) {
        ReadwriteSplittingRuleWorkflowRequest request = (ReadwriteSplittingRuleWorkflowRequest) snapshot.getRequest();
        boolean ruleExists = containsRule(rules, queryFacade, request.getDatabase(), request.getRuleName());
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && ruleExists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !ruleExists) {
            addRuleMismatch(validationReport, request.getRuleName(), WorkflowLifecycleUtils.isDropWorkflow(snapshot));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Readwrite-splitting rule state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !matchesRuleShape(rules, queryFacade, request)) {
            addRuleShapeMismatch(validationReport, request.getRuleName());
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Readwrite-splitting rule fields do not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rules, "Readwrite-splitting rule state matches the planned DistSQL artifact.");
    }
    
    private boolean containsRule(final List<Map<String, Object>> rules, final MCPFeatureQueryFacade queryFacade, final String databaseName, final String ruleName) {
        return rules.stream().anyMatch(each -> queryFacade.isSameIdentifier(databaseName, IdentifierScope.TABLE, ruleName, WorkflowRuleValueUtils.getRuleValue(each, "name")));
    }
    
    private boolean matchesRuleShape(final List<Map<String, Object>> rules, final MCPFeatureQueryFacade queryFacade, final ReadwriteSplittingRuleWorkflowRequest request) {
        List<Map<String, Object>> targetRules = rules.stream().filter(each -> queryFacade.isSameIdentifier(
                request.getDatabase(), IdentifierScope.TABLE, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(each, "name")))
                .toList();
        return 1 == targetRules.size() && matchesRuleShape(targetRules.getFirst(), queryFacade, request);
    }
    
    private boolean matchesRuleShape(final Map<String, Object> rule, final MCPFeatureQueryFacade queryFacade, final ReadwriteSplittingRuleWorkflowRequest request) {
        return queryFacade.isSameIdentifier(
                request.getDatabase(), IdentifierScope.TABLE, request.getWriteStorageUnit(), WorkflowRuleValueUtils.getRuleValue(rule, "write_storage_unit_name"))
                && matchesReadStorageUnits(rule, queryFacade, request)
                && WorkflowRuleValueUtils.getRuleValue(rule, "transactional_read_query_strategy").equalsIgnoreCase(request.getTransactionalReadQueryStrategy())
                && WorkflowRuleValueUtils.getRuleValue(rule, "load_balancer_type").equalsIgnoreCase(request.getLoadBalancerType())
                && WorkflowAlgorithmUtils.createPropertyMap(rule.get("load_balancer_props")).equals(request.getLoadBalancerProperties());
    }
    
    private boolean matchesReadStorageUnits(final Map<String, Object> rule, final MCPFeatureQueryFacade queryFacade, final ReadwriteSplittingRuleWorkflowRequest request) {
        String readStorageUnits = WorkflowRuleValueUtils.getRuleValue(rule, "read_storage_unit_names");
        List<String> actualReadStorageUnits = Arrays.stream(readStorageUnits.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
        return actualReadStorageUnits.size() == request.getReadStorageUnits().size() && request.getReadStorageUnits().stream().allMatch(each -> actualReadStorageUnits.stream()
                .anyMatch(actual -> queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, each, actual)));
    }
    
    private void addRuleMismatch(final ValidationReport validationReport, final String ruleName, final boolean dropWorkflow) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                dropWorkflow ? "rule absent" : "rule present", ruleName,
                dropWorkflow ? "Readwrite-splitting rule still exists after drop." : "Readwrite-splitting rule is missing after create or alter.",
                dropWorkflow ? "Drop the readwrite-splitting rule again or investigate the failure." : "Apply the planned readwrite-splitting rule DistSQL again."));
    }
    
    private void addRuleShapeMismatch(final ValidationReport validationReport, final String ruleName) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "matching fields", ruleName,
                "Readwrite-splitting rule exists but its storage units, transactional strategy or load balancer differ from the planned artifact.",
                "Review the current rule state and re-apply or re-plan the readwrite-splitting rule DistSQL."));
    }
}
