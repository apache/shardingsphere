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

import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting status workflow planning service.
 */
public final class ReadwriteSplittingStatusWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, rule, read storage unit and target status",
            "Inspect DistSQL-visible readwrite-splitting status",
            "Generate readwrite-splitting status DistSQL artifact",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("status");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final ReadwriteSplittingWorkflowIntentResolver intentResolver = new ReadwriteSplittingWorkflowIntentResolver();
    
    private final ReadwriteSplittingInspectionService inspectionService;
    
    private final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService;
    
    public ReadwriteSplittingStatusWorkflowPlanningService() {
        inspectionService = new ReadwriteSplittingInspectionService();
        distSQLPlanningService = new ReadwriteSplittingStatusDistSQLPlanningService();
    }
    
    ReadwriteSplittingStatusWorkflowPlanningService(final ReadwriteSplittingInspectionService inspectionService,
                                                    final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService) {
        this.inspectionService = inspectionService;
        this.distSQLPlanningService = distSQLPlanningService;
    }
    
    /**
     * Plan readwrite-splitting storage-unit status workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade, final String sessionId,
                                        final ReadwriteSplittingStatusWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        ReadwriteSplittingStatusWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        if (intentResolver.hasConflictingStatusInputs(mergedRequest)) {
            clarifiedIntent.getClarificationMessages().add("Please provide matching target_status and operation_type, or provide only one status field.");
            result.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", "intaking",
                    "Readwrite-splitting status request has conflicting target_status and operation_type values.",
                    "Use enable/enabled or disable/disabled consistently.", true,
                    Map.of("operation_type", mergedRequest.getOperationType(), "target_status", mergedRequest.getTargetStatus())));
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!ensurePlanningContext(mergedRequest, clarifiedIntent, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        List<Map<String, Object>> statuses = inspectionService.queryRuleStatus(queryFacade, mergedRequest.getDatabase(), mergedRequest.getRuleName());
        if (!ensureTargetStatusRow(mergedRequest, statuses, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        result.getRuleArtifacts().add(distSQLPlanningService.planStatus(mergedRequest));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private ReadwriteSplittingStatusWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final ReadwriteSplittingStatusWorkflowRequest request) {
        ReadwriteSplittingStatusWorkflowRequest result = ReadwriteSplittingStatusWorkflowRequest.merge(snapshot.getRequest(), request);
        return planningSupport.prepareSnapshot(snapshot, ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND, result, null,
                intentResolver.resolveStatusIntent(result), "Readwrite-splitting status workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private boolean ensurePlanningContext(final ReadwriteSplittingStatusWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning readwrite-splitting status DistSQL.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureSupportedIdentifier("database", request.getDatabase(), snapshot) || !ensureSupportedIdentifier(ReadwriteSplittingFeatureDefinition.RULE_FIELD, request.getRuleName(), snapshot)
                || !ensureSupportedIdentifier(ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, request.getStorageUnit(), snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        List<String> missingInputs = new LinkedList<>();
        addMissingInput(missingInputs, request.getRuleName(), ReadwriteSplittingFeatureDefinition.RULE_FIELD);
        addMissingInput(missingInputs, request.getStorageUnit(), ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD);
        addMissingInput(missingInputs, distSQLPlanningService.resolveStatusOperation(request), ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD);
        if (!missingInputs.isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide readwrite-splitting rule name, read storage unit and target status.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", "intaking",
                    "Readwrite-splitting status DistSQL requires explicit rule, storage unit and status inputs.",
                    "Provide target_status as enable or disable.", true, Map.of("missing_inputs", missingInputs)));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        return true;
    }
    
    private void addMissingInput(final List<String> missingInputs, final String value, final String fieldName) {
        if (value.isEmpty()) {
            missingInputs.add(fieldName);
        }
    }
    
    private boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot) {
        if (identifier.isEmpty() || WorkflowSQLUtils.isSupportedIdentifier(identifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "intaking",
                String.format("%s identifier `%s` contains unsupported characters.", fieldName, identifier),
                "Use a reviewable logical identifier without NUL or line terminators.", false, Map.of("field", fieldName, "identifier", identifier)));
        return false;
    }
    
    private boolean ensureTargetStatusRow(final ReadwriteSplittingStatusWorkflowRequest request, final List<Map<String, Object>> statuses,
                                          final WorkflowContextSnapshot snapshot, final String databaseType) {
        if (statuses.stream().anyMatch(each -> matchesStatusTarget(request, each, databaseType))) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                String.format("Readwrite-splitting status target `%s.%s` does not exist.", request.getRuleName(), request.getStorageUnit()),
                "Confirm the target rule and read storage unit before planning status changes.", false,
                Map.of("rule", request.getRuleName(), "storage_unit", request.getStorageUnit())));
        return false;
    }
    
    private boolean matchesStatusTarget(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> status, final String databaseType) {
        return WorkflowSQLUtils.isSameIdentifier(databaseType, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(status, "name"))
                && WorkflowSQLUtils.isSameIdentifier(databaseType, request.getStorageUnit(), WorkflowRuleValueUtils.getRuleValue(status, "storage_unit"));
    }
}
