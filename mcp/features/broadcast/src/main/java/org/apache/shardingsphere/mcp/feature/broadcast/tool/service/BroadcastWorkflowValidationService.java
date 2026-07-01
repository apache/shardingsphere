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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.service;

import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
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

import java.util.List;
import java.util.Map;

/**
 * Broadcast workflow validation service.
 */
public final class BroadcastWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final BroadcastRuleInspectionService ruleInspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public BroadcastWorkflowValidationService() {
        ruleInspectionService = new BroadcastRuleInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    BroadcastWorkflowValidationService(final BroadcastRuleInspectionService ruleInspectionService, final WorkflowSynchronizationSupport workflowSynchronizationSupport) {
        this.ruleInspectionService = ruleInspectionService;
        this.workflowSynchronizationSupport = workflowSynchronizationSupport;
    }
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        return validationSupport.validateAndFinalize(workflowSessionContext, sessionId, snapshot, () -> createValidationReport(snapshot, queryFacade));
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        String databaseType = queryFacade.getDatabaseType(snapshot.getRequest().getDatabase());
        List<Map<String, Object>> broadcastRules = ruleInspectionService.queryBroadcastRules(queryFacade, snapshot.getRequest().getDatabase());
        result.setRuleValidation(validateRules(snapshot, broadcastRules, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> broadcastRules,
                                            final ValidationReport validationReport, final String databaseType) {
        BroadcastWorkflowRequest request = (BroadcastWorkflowRequest) snapshot.getRequest();
        boolean dropWorkflow = WorkflowLifecycleUtils.isDropWorkflow(snapshot);
        for (String each : request.getTargetTables()) {
            boolean ruleExists = containsBroadcastTable(broadcastRules, databaseType, each);
            if (dropWorkflow && ruleExists || !dropWorkflow && !ruleExists) {
                addRuleMismatch(validationReport, dropWorkflow, each);
                return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, broadcastRules, "Broadcast rule state does not match the planned DistSQL artifact.");
            }
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, broadcastRules, "Broadcast rule state matches the planned DistSQL artifact.");
    }
    
    private boolean containsBroadcastTable(final List<Map<String, Object>> broadcastRules, final String databaseType, final String tableName) {
        return broadcastRules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, tableName, WorkflowRuleValueUtils.getRuleValue(each, "broadcast_table")));
    }
    
    private void addRuleMismatch(final ValidationReport validationReport, final boolean dropWorkflow, final String tableName) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                dropWorkflow ? "table absent" : "table present", tableName,
                dropWorkflow ? "Broadcast table still exists after drop." : "Broadcast table is missing after create.",
                dropWorkflow ? "Drop the broadcast table rule again or investigate the failure." : "Create the broadcast table rule again."));
    }
}
