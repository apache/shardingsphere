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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Workflow planning context utility methods.
 */
public final class WorkflowPlanningContextUtils {
    
    private WorkflowPlanningContextUtils() {
    }
    
    /**
     * Get an existing workflow snapshot or create a new one.
     *
     * @param contextStore workflow context store
     * @param sessionId session identifier
     * @param planId plan identifier
     * @return workflow snapshot
     */
    public static WorkflowContextSnapshot getOrCreateSnapshot(final WorkflowContextStore contextStore, final String sessionId, final String planId) {
        String actualPlanId = WorkflowSqlUtils.trimToEmpty(planId);
        if (!actualPlanId.isEmpty()) {
            return contextStore.getRequired(actualPlanId);
        }
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(contextStore.createPlanId());
        result.setSessionId(sessionId);
        result.setStatus("clarifying");
        return result;
    }
    
    /**
     * Clear planning state on snapshot before rebuilding artifacts.
     *
     * @param snapshot workflow snapshot
     */
    public static void clearPlanningState(final WorkflowContextSnapshot snapshot) {
        snapshot.getIssues().clear();
        snapshot.getAlgorithmCandidates().clear();
        snapshot.getPropertyRequirements().clear();
        snapshot.getDdlArtifacts().clear();
        snapshot.getRuleArtifacts().clear();
        snapshot.getIndexPlans().clear();
        snapshot.setValidationReport(null);
    }
    
    /**
     * Persist workflow snapshot with lifecycle state.
     *
     * @param contextStore workflow context store
     * @param snapshot workflow snapshot
     * @param currentStep current interaction step
     * @param status workflow status
     * @return persisted snapshot
     */
    public static WorkflowContextSnapshot persistSnapshot(final WorkflowContextStore contextStore,
                                                          final WorkflowContextSnapshot snapshot, final String currentStep, final String status) {
        snapshot.getInteractionPlan().setCurrentStep(currentStep);
        snapshot.setStatus(status);
        contextStore.save(snapshot);
        return snapshot;
    }
    
    /**
     * Ensure workflow planning context is complete and valid.
     *
     * @param metadataQueryFacade metadata query facade
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @return whether planning context is ready
     */
    public static boolean ensurePlanningContext(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                                final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (WorkflowSqlUtils.trimToEmpty(request.getDatabase()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请先提供 logical database。");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus("clarifying");
            return false;
        }
        if (!ensureSupportedIdentifier("database", request.getDatabase(), snapshot)
                || !ensureSupportedIdentifier("table", request.getTable(), snapshot)
                || !ensureSupportedIdentifier("column", request.getColumn(), snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        request.setSchema(resolveSchema(metadataQueryFacade, request));
        if (!ensureSupportedIdentifier("schema", request.getSchema(), snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        addMissingQuestions(request, clarifiedIntent);
        if (WorkflowSqlUtils.trimToEmpty(request.getSchema()).isEmpty()
                || WorkflowSqlUtils.trimToEmpty(request.getTable()).isEmpty()
                || WorkflowSqlUtils.trimToEmpty(request.getColumn()).isEmpty()) {
            snapshot.setStatus("clarifying");
            return false;
        }
        if (!ensureTableExists(metadataQueryFacade, request, snapshot) || !ensureColumnExists(metadataQueryFacade, request, snapshot)) {
            snapshot.setStatus("failed");
            return false;
        }
        return true;
    }
    
    private static void addMissingQuestions(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (WorkflowSqlUtils.trimToEmpty(request.getSchema()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确 schema。");
        }
        if (WorkflowSqlUtils.trimToEmpty(request.getTable()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确目标表。");
        }
        if (WorkflowSqlUtils.trimToEmpty(request.getColumn()).isEmpty()) {
            clarifiedIntent.getPendingQuestions().add("请明确目标列。");
        }
    }
    
    private static boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot) {
        String actualIdentifier = WorkflowSqlUtils.trimToEmpty(identifier);
        if (actualIdentifier.isEmpty() || WorkflowSqlUtils.isSafeIdentifier(actualIdentifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "discovering",
                String.format("%s identifier `%s` contains unsupported characters.", fieldName, actualIdentifier),
                "Use unquoted SQL identifiers only in V1.", false, Map.of("field", fieldName, "identifier", actualIdentifier)));
        return false;
    }
    
    private static String resolveSchema(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request) {
        String actualSchema = WorkflowSqlUtils.trimToEmpty(request.getSchema());
        if (!actualSchema.isEmpty()) {
            return actualSchema;
        }
        Optional<MCPDatabaseMetadata> databaseMetadata = metadataQueryFacade.queryDatabase(request.getDatabase());
        if (databaseMetadata.isEmpty()) {
            return "";
        }
        String tableName = WorkflowSqlUtils.trimToEmpty(request.getTable());
        if (!tableName.isEmpty()) {
            List<String> matchedSchemas = new LinkedList<>();
            for (MCPSchemaMetadata each : databaseMetadata.get().getSchemas()) {
                if (each.getTables().stream().anyMatch(table -> tableName.equals(table.getTable()))) {
                    matchedSchemas.add(each.getSchema());
                }
            }
            if (1 == matchedSchemas.size()) {
                return matchedSchemas.get(0);
            }
        }
        return 1 == databaseMetadata.get().getSchemas().size() ? databaseMetadata.get().getSchemas().iterator().next().getSchema() : "";
    }
    
    private static boolean ensureTableExists(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                             final WorkflowContextSnapshot snapshot) {
        if (metadataQueryFacade.queryTable(request.getDatabase(), request.getSchema(), request.getTable()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.TABLE_NOT_FOUND, "error", "discovering",
                String.format("Table `%s` does not exist in Proxy logical metadata.", request.getTable()), "Check database, schema and table name.", false, Map.of()));
        return false;
    }
    
    private static boolean ensureColumnExists(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                              final WorkflowContextSnapshot snapshot) {
        if (metadataQueryFacade.queryTableColumn(request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.COLUMN_NOT_FOUND, "error", "discovering",
                String.format("Column `%s` does not exist in Proxy logical metadata.", request.getColumn()), "Check column name.", false, Map.of()));
        return false;
    }
}
