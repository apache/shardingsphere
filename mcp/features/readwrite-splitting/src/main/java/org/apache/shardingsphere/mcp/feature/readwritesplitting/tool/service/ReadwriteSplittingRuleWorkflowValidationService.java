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

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting rule workflow validation service.
 */
public final class ReadwriteSplittingRuleWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ReadwriteSplittingInspectionService inspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public ReadwriteSplittingRuleWorkflowValidationService() {
        inspectionService = new ReadwriteSplittingInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    ReadwriteSplittingRuleWorkflowValidationService(final ReadwriteSplittingInspectionService inspectionService,
                                                    final WorkflowSynchronizationSupport workflowSynchronizationSupport) {
        this.inspectionService = inspectionService;
        this.workflowSynchronizationSupport = workflowSynchronizationSupport;
    }
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        Map<String, Object> rejectedResponse = validationSupport.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = createValidationReport(snapshot, queryFacade);
        snapshot.setValidationReport(validationReport);
        return validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        String databaseType = queryFacade.getDatabaseType(snapshot.getRequest().getDatabase());
        List<Map<String, Object>> rules = inspectionService.queryRules(queryFacade, snapshot.getRequest().getDatabase());
        result.setRuleValidation(validateRules(snapshot, rules, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules,
                                            final ValidationReport validationReport, final String databaseType) {
        ReadwriteSplittingRuleWorkflowRequest request = (ReadwriteSplittingRuleWorkflowRequest) snapshot.getRequest();
        boolean ruleExists = containsRule(rules, databaseType, request.getRuleName());
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && ruleExists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !ruleExists) {
            addRuleMismatch(validationReport, request.getRuleName(), WorkflowLifecycleUtils.isDropWorkflow(snapshot));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Readwrite-splitting rule state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !matchesRuleShape(rules, databaseType, request)) {
            addRuleShapeMismatch(validationReport, request.getRuleName());
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rules, "Readwrite-splitting rule fields do not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rules, "Readwrite-splitting rule state matches the planned DistSQL artifact.");
    }
    
    private boolean containsRule(final List<Map<String, Object>> rules, final String databaseType, final String ruleName) {
        return rules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, ruleName, WorkflowRuleValueUtils.getRuleValue(each, "name")));
    }
    
    private boolean matchesRuleShape(final List<Map<String, Object>> rules, final String databaseType, final ReadwriteSplittingRuleWorkflowRequest request) {
        return rules.stream().filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(each, "name")))
                .anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, request.getWriteStorageUnit(), WorkflowRuleValueUtils.getRuleValue(each, "write_storage_unit_name"))
                        && containsAllReadStorageUnits(each, databaseType, request)
                        && WorkflowRuleValueUtils.getRuleValue(each, "transactional_read_query_strategy").equalsIgnoreCase(request.getTransactionalReadQueryStrategy()));
    }
    
    private boolean containsAllReadStorageUnits(final Map<String, Object> rule, final String databaseType, final ReadwriteSplittingRuleWorkflowRequest request) {
        String readStorageUnits = WorkflowRuleValueUtils.getRuleValue(rule, "read_storage_unit_names");
        List<String> actualReadStorageUnits = Arrays.stream(readStorageUnits.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
        return request.getReadStorageUnits().stream().allMatch(each -> actualReadStorageUnits.stream().anyMatch(actual -> WorkflowSQLUtils.isSameIdentifier(databaseType, each, actual)));
    }
    
    private void addRuleMismatch(final ValidationReport validationReport, final String ruleName, final boolean dropWorkflow) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                dropWorkflow ? "rule absent" : "rule present", ruleName,
                dropWorkflow ? "Readwrite-splitting rule still exists after drop." : "Readwrite-splitting rule is missing after create or alter.",
                dropWorkflow ? "Drop the readwrite-splitting rule again or investigate the failure." : "Apply the planned readwrite-splitting rule DistSQL again."));
    }
    
    private void addRuleShapeMismatch(final ValidationReport validationReport, final String ruleName) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "matching fields", ruleName,
                "Readwrite-splitting rule exists but its write/read storage units or transactional strategy differ from the planned artifact.",
                "Review the current rule state and re-apply or re-plan the readwrite-splitting rule DistSQL."));
    }
}
