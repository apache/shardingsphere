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

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting status workflow validation service.
 */
public final class ReadwriteSplittingStatusWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ReadwriteSplittingInspectionService inspectionService;
    
    private final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public ReadwriteSplittingStatusWorkflowValidationService() {
        inspectionService = new ReadwriteSplittingInspectionService();
        distSQLPlanningService = new ReadwriteSplittingStatusDistSQLPlanningService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    ReadwriteSplittingStatusWorkflowValidationService(final ReadwriteSplittingInspectionService inspectionService,
                                                      final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService,
                                                      final WorkflowSynchronizationSupport workflowSynchronizationSupport) {
        this.inspectionService = inspectionService;
        this.distSQLPlanningService = distSQLPlanningService;
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
        ReadwriteSplittingStatusWorkflowRequest request = (ReadwriteSplittingStatusWorkflowRequest) snapshot.getRequest();
        String databaseType = queryFacade.getDatabaseType(request.getDatabase());
        List<Map<String, Object>> statuses = inspectionService.queryRuleStatus(queryFacade, request.getDatabase(), request.getRuleName());
        result.setRuleValidation(validateStatus(request, statuses, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateStatus(final ReadwriteSplittingStatusWorkflowRequest request, final List<Map<String, Object>> statuses,
                                             final ValidationReport validationReport, final String databaseType) {
        String expectedStatus = "ENABLE".equals(distSQLPlanningService.resolveStatusOperation(request)) ? "ENABLED" : "DISABLED";
        boolean matched = statuses.stream().anyMatch(each -> matchesStatus(request, each, databaseType, expectedStatus));
        if (!matched) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "status", expectedStatus, request.getStorageUnit(),
                    "Readwrite-splitting storage-unit status does not match the planned artifact.",
                    "Apply the readwrite-splitting status DistSQL again or inspect current status."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, statuses, "Readwrite-splitting status state does not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, statuses, "Readwrite-splitting status state matches the planned DistSQL artifact.");
    }
    
    private boolean matchesStatus(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> status, final String databaseType, final String expectedStatus) {
        return WorkflowSQLUtils.isSameIdentifier(databaseType, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(status, "name"))
                && WorkflowSQLUtils.isSameIdentifier(databaseType, request.getStorageUnit(), WorkflowRuleValueUtils.getRuleValue(status, "storage_unit"))
                && expectedStatus.equalsIgnoreCase(WorkflowRuleValueUtils.getRuleValue(status, "status"));
    }
}
