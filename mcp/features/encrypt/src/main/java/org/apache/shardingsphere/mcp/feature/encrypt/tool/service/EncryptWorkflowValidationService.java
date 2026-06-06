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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt workflow validation service.
 */
public final class EncryptWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    
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
        EncryptWorkflowRequest request = getWorkflowRequest(snapshot);
        String databaseType = queryFacade.getDatabaseType(request.getDatabase());
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(queryFacade, request.getDatabase(), request.getTable());
        result.setRuleValidation(validateRules(snapshot, request, encryptRules, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private EncryptWorkflowRequest getWorkflowRequest(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getRequest() instanceof EncryptWorkflowRequest) {
            return (EncryptWorkflowRequest) snapshot.getRequest();
        }
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), null);
        return null == result ? new EncryptWorkflowRequest() : result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot,
                                            final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules, final ValidationReport validationReport, final String databaseType) {
        Optional<Map<String, Object>> actualRule = findEncryptRule(snapshot, encryptRules, databaseType);
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Encrypt rule has been removed.");
            }
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no encrypt rule", String.valueOf(actualRule.get()),
                    "Encrypt rule still exists after drop.", "Drop the encrypt rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Encrypt rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Encrypt rule is missing.", "Create or alter the encrypt rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Encrypt rule is missing.");
        }
        List<Map<String, Object>> mismatches = new LinkedList<>();
        addRuleValueMismatch(mismatches, "cipher_column", request.getOptions().getCipherColumnName(),
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "cipher_column"), "Cipher column mapping does not match.");
        addRuleValueMismatch(mismatches, "assisted_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "assisted_query_column"), "Assisted-query column mapping does not match.");
        addRuleValueMismatch(mismatches, "like_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "like_query_column"), "LIKE-query column mapping does not match.");
        addAlgorithmTypeMismatch(mismatches, "encryptor_type", request.getAlgorithmType(),
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "encryptor_type"), "Encrypt algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "assisted_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "assisted_query_type"), "Assisted-query algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "like_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "like_query_type"), "LIKE-query algorithm type does not match.");
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Encrypt rule configuration does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, actualRule.get(), "Encrypt rule matches the planned columns and algorithms.");
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> encryptRules, final String databaseType) {
        return encryptRules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, snapshot.getRequest().getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
    }
    
    private void addRuleValueMismatch(final List<Map<String, Object>> mismatches, final String fieldName, final String expected, final String actual, final String impact) {
        if (expected.equals(actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private void addAlgorithmTypeMismatch(final List<Map<String, Object>> mismatches, final String fieldName, final String expected, final String actual, final String impact) {
        if (matchesValue(expected, actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private boolean matchesValue(final String expected, final String actual) {
        return expected.equalsIgnoreCase(actual);
    }
    
    private String formatFieldValue(final String fieldName, final String value) {
        return String.format("%s=%s", fieldName, value);
    }
}
