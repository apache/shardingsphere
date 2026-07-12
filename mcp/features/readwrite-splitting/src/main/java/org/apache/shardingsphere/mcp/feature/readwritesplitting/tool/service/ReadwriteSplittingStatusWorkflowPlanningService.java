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
    
    private final ReadwriteSplittingInspectionService inspectionService = new ReadwriteSplittingInspectionService();
    
    private final ReadwriteSplittingStatusDistSQLPlanningService distSQLPlanningService = new ReadwriteSplittingStatusDistSQLPlanningService();
    
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
        applyResolvedStatusIntent(mergedRequest, clarifiedIntent);
        if (!ensurePlanningContext(mergedRequest, clarifiedIntent, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        queryFacade.checkDatabaseCapability(mergedRequest.getDatabase());
        List<Map<String, Object>> statuses = inspectionService.queryRuleStatus(queryFacade, mergedRequest.getDatabase(), mergedRequest.getRuleName());
        if (!ensureTargetStatusRow(mergedRequest, statuses, result, queryFacade)) {
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
    
    private void applyResolvedStatusIntent(final ReadwriteSplittingStatusWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
        Object targetStatus = clarifiedIntent.getInferredValues().get(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD);
        if (request.getTargetStatus().isEmpty() && targetStatus instanceof String) {
            request.setTargetStatus((String) targetStatus);
        }
    }
    
    private boolean ensurePlanningContext(final ReadwriteSplittingStatusWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning readwrite-splitting status DistSQL.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!planningSupport.ensureOptionalSupportedIdentifiers("database", List.of(request.getDatabase()), snapshot, "intaking")
                || !planningSupport.ensureOptionalSupportedIdentifiers(ReadwriteSplittingFeatureDefinition.RULE_FIELD, List.of(request.getRuleName()), snapshot, "intaking")
                || !planningSupport.ensureOptionalSupportedIdentifiers(ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, List.of(request.getStorageUnit()), snapshot, "intaking")) {
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
    
    private boolean ensureTargetStatusRow(final ReadwriteSplittingStatusWorkflowRequest request, final List<Map<String, Object>> statuses,
                                          final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        if (statuses.stream().anyMatch(each -> matchesStatusTarget(request, each, queryFacade))) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                String.format("Readwrite-splitting status target `%s.%s` does not exist.", request.getRuleName(), request.getStorageUnit()),
                "Confirm the target rule and read storage unit before planning status changes.", false,
                Map.of("rule", request.getRuleName(), "storage_unit", request.getStorageUnit())));
        return false;
    }
    
    private boolean matchesStatusTarget(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> status, final MCPFeatureQueryFacade queryFacade) {
        return queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getRuleName(), WorkflowRuleValueUtils.getRuleValue(status, "name"))
                && queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getStorageUnit(), WorkflowRuleValueUtils.getRuleValue(status, "storage_unit"));
    }
}
