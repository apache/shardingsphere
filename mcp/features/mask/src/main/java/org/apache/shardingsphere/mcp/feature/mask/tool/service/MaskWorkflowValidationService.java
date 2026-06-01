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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mask workflow validation service.
 */
public final class MaskWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public MaskWorkflowValidationService() {
        ruleInspectionService = new MaskRuleInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    /**
     * Validate workflow artifacts.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param executionFacade execution facade
     * @param sessionId session id
     * @param snapshot workflow snapshot
     * @return validation payload
     */
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        Map<String, Object> rejectedResponse = validationSupport.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = createValidationReport(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId);
        snapshot.setValidationReport(validationReport);
        return validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId));
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                                                    final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        ValidationReport result = new ValidationReport();
        String databaseType = metadataQueryFacade.queryDatabase(WorkflowSQLUtils.normalizeIdentifier(snapshot.getRequest().getDatabase()))
                .map(each -> each.getDatabaseType()).orElse("");
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(queryFacade, snapshot.getRequest().getDatabase(), snapshot.getRequest().getTable());
        result.setDdlValidation(validateDdl());
        result.setRuleValidation(validateRules(snapshot, maskRules, result, databaseType));
        result.setLogicalMetadataValidation(validationSupport.validateLogicalMetadata(snapshot, metadataQueryFacade, result));
        result.setSqlExecutabilityValidation(validateSqlExecutability(executionFacade, sessionId, snapshot, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getDdlValidation(), result.getRuleValidation(),
                result.getLogicalMetadataValidation(), result.getSqlExecutabilityValidation()));
        return result;
    }
    
    private ValidationSection validateDdl() {
        return new ValidationSection(WorkflowLifecycle.STATUS_SKIPPED, List.of(), "Mask workflows do not require physical DDL validation.");
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> maskRules,
                                            final ValidationReport validationReport, final String databaseType) {
        Optional<Map<String, Object>> actualRule = maskRules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, snapshot.getRequest().getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "column"))).findFirst();
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Mask rule has been removed.");
            }
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no mask rule", String.valueOf(actualRule.get()),
                    "Mask rule still exists after drop.", "Drop the mask rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Mask rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Mask rule is missing.", "Create or alter the mask rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Mask rule is missing.");
        }
        String actualAlgorithmType = WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "algorithm_type");
        if (!snapshot.getRequest().getAlgorithmType().equalsIgnoreCase(actualAlgorithmType)) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getAlgorithmType(), actualAlgorithmType,
                    "Mask algorithm type does not match.", "Re-apply the intended mask rule."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Mask algorithm type does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, actualRule.get(), "Mask rule matches the planned algorithm.");
    }
    
    private ValidationSection validateSqlExecutability(final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                                       final WorkflowContextSnapshot snapshot, final ValidationReport validationReport, final String databaseType) {
        return validationSupport.validateSqlExecutability(executionFacade, sessionId, snapshot, validationReport,
                List.of(validationSupport.createProjectionValidationSql(snapshot, databaseType)), "Validation SQL is executable from the logical view.");
    }
}
