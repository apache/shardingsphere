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
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting status workflow validation service.
 */
public final class ReadwriteSplittingStatusWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ReadwriteSplittingInspectionService inspectionService = new ReadwriteSplittingInspectionService();
    
    private final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService = new ReadwriteSplittingStatusDistSQLPlanningService();
    
    @Override
    public ValidationReport validate(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        ReadwriteSplittingStatusWorkflowRequest request = (ReadwriteSplittingStatusWorkflowRequest) snapshot.getRequest();
        queryFacade.checkDatabaseCapability(request.getDatabase());
        List<Map<String, Object>> statuses = inspectionService.queryRuleStatus(queryFacade, request.getDatabase(), request.getRuleName());
        result.setRuleValidation(validateStatus(request, statuses, result, queryFacade));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateStatus(final ReadwriteSplittingStatusWorkflowRequest request, final List<Map<String, Object>> statuses,
                                             final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade) {
        String expectedStatus = "ENABLE".equals(distSQLPlanningService.resolveStatusOperation(request)) ? "ENABLED" : "DISABLED";
        List<Map<String, Object>> targetStatuses = statuses.stream().filter(each -> matchesStatusTarget(request, each, queryFacade)).toList();
        if (1 != targetStatuses.size() || !expectedStatus.equalsIgnoreCase(WorkflowRuleValueUtils.getRuleValue(targetStatuses.getFirst(), "status"))) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "status", expectedStatus, request.getStorageUnit(),
                    "Readwrite-splitting storage-unit status does not match the planned artifact.",
                    "Apply the readwrite-splitting status DistSQL again or inspect current status."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, statuses, "Readwrite-splitting status state does not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, statuses, "Readwrite-splitting status state matches the planned DistSQL artifact.");
    }
    
    private boolean matchesStatusTarget(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> status, final MCPFeatureQueryFacade queryFacade) {
        return queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(status, "name"))
                && queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getStorageUnit(), WorkflowRuleValueUtils.getRuleValue(status, "storage_unit"));
    }
}
