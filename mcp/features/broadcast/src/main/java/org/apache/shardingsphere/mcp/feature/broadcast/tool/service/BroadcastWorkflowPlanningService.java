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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
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

import java.util.List;
import java.util.Map;

/**
 * Broadcast workflow planning service.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BroadcastWorkflowPlanningService {
    
    private static final List<String> INTERACTION_STEPS = List.of(
            "Confirm database, broadcast tables and target lifecycle",
            "Inspect DistSQL-visible broadcast rules",
            "Generate broadcast rule DistSQL artifacts",
            "Review artifacts and choose execution mode",
            "Execute or export artifacts",
            "Validate and summarize");
    
    private static final List<String> VALIDATION_LAYERS = List.of("rules");
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    private final BroadcastWorkflowIntentResolver intentResolver = new BroadcastWorkflowIntentResolver();
    
    private final BroadcastRuleInspectionService ruleInspectionService;
    
    private final BroadcastRuleDistSQLPlanningService ruleDistSQLPlanningService;
    
    public BroadcastWorkflowPlanningService() {
        ruleInspectionService = new BroadcastRuleInspectionService();
        ruleDistSQLPlanningService = new BroadcastRuleDistSQLPlanningService();
    }
    
    /**
     * Plan broadcast workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade query facade
     * @param sessionId session id
     * @param request workflow request
     * @return workflow snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade, final String sessionId,
                                        final BroadcastWorkflowRequest request) {
        WorkflowContextSnapshot result = workflowSessionContext.getOrCreate(sessionId, request.getPlanId());
        BroadcastWorkflowRequest mergedRequest = prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningSupport.applyResolvedIntent(mergedRequest, clarifiedIntent);
        if (!ensurePlanningContext(mergedRequest, clarifiedIntent, result)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        String databaseType = queryFacade.getDatabaseType(mergedRequest.getDatabase());
        List<Map<String, Object>> existingRules = ruleInspectionService.queryBroadcastRules(queryFacade, mergedRequest.getDatabase());
        if (!ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result, databaseType)) {
            return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        result.getRuleArtifacts().add(WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType())
                ? ruleDistSQLPlanningService.planDropRule(mergedRequest.getTargetTables())
                : ruleDistSQLPlanningService.planCreateRule(mergedRequest.getTargetTables()));
        return workflowSessionContext.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    private BroadcastWorkflowRequest prepareSnapshot(final WorkflowContextSnapshot snapshot, final BroadcastWorkflowRequest request) {
        BroadcastWorkflowRequest result = BroadcastWorkflowRequest.merge(snapshot.getRequest(), request);
        if (result.getTable().isEmpty() && !result.getTables().isEmpty()) {
            result.setTable(result.getTables().iterator().next());
        }
        return planningSupport.prepareSnapshot(snapshot, BroadcastFeatureDefinition.WORKFLOW_KIND, result, null,
                intentResolver.resolve(result), "Broadcast workflow plan.", INTERACTION_STEPS, VALIDATION_LAYERS);
    }
    
    private boolean ensurePlanningContext(final BroadcastWorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning broadcast rule DistSQL.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!planningSupport.ensureSupportedIdentifiers("database", List.of(request.getDatabase()), snapshot, "discovering")
                || !planningSupport.ensureSupportedIdentifiers("tables", request.getTargetTables(), snapshot, "discovering")) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        if (request.getTargetTables().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide one or more logical table names for broadcast rule planning.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", "intaking",
                    "Broadcast rule DistSQL requires at least one logical table.", "Provide tables as a comma-separated list or table as a single value.", true,
                    Map.of("missing_inputs", List.of(BroadcastFeatureDefinition.TABLES_FIELD))));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        return true;
    }
    
    private boolean ensureLifecycleState(final ClarifiedIntent clarifiedIntent, final BroadcastWorkflowRequest request, final List<Map<String, Object>> broadcastRules,
                                         final WorkflowContextSnapshot snapshot, final String databaseType) {
        boolean dropWorkflow = WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(clarifiedIntent.getOperationType());
        boolean result = true;
        for (String each : request.getTargetTables()) {
            boolean ruleExists = containsBroadcastTable(broadcastRules, databaseType, each);
            result = dropWorkflow ? ensureDropState(each, ruleExists, snapshot) && result : ensureCreateState(each, ruleExists, snapshot) && result;
        }
        return result;
    }
    
    private boolean containsBroadcastTable(final List<Map<String, Object>> broadcastRules, final String databaseType, final String tableName) {
        return broadcastRules.stream().anyMatch(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, tableName, WorkflowRuleValueUtils.getRuleValue(each, "broadcast_table")));
    }
    
    private boolean ensureCreateState(final String tableName, final boolean ruleExists, final WorkflowContextSnapshot snapshot) {
        if (!ruleExists) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                String.format("Broadcast rule already contains table `%s`.", tableName), "Remove existing table from the create request or choose drop.", false, Map.of("table", tableName)));
        return false;
    }
    
    private boolean ensureDropState(final String tableName, final boolean ruleExists, final WorkflowContextSnapshot snapshot) {
        if (ruleExists) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                String.format("Broadcast rule does not contain table `%s`.", tableName), "Confirm target table or skip the drop request.", false, Map.of("table", tableName)));
        return false;
    }
}
