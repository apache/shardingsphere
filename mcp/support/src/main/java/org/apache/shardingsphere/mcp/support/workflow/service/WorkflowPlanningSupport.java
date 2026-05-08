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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Workflow planning support.
 */
public final class WorkflowPlanningSupport {
    
    /**
     * Apply resolved intent fields to the workflow request.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     */
    public void applyResolvedIntent(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setOperationType(clarifiedIntent.getOperationType());
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
    }
    
    /**
     * Prepare workflow snapshot for planning.
     *
     * @param snapshot workflow snapshot
     * @param workflowKind workflow kind
     * @param request merged request
     * @param featureData feature-scoped workflow data
     * @param clarifiedIntent clarified intent
     * @param summary interaction summary
     * @param interactionSteps interaction steps
     * @param validationLayers validation layers
     * @param <T> request type
     * @return prepared request
     */
    public <T extends WorkflowRequest> T prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowKind workflowKind, final T request, final WorkflowFeatureData featureData,
                                                         final ClarifiedIntent clarifiedIntent, final String summary,
                                                         final List<String> interactionSteps, final List<String> validationLayers) {
        snapshot.setWorkflowKind(workflowKind);
        snapshot.setRequest(request);
        snapshot.setFeatureData(featureData);
        snapshot.setInteractionPlan(InteractionPlan.create(snapshot.getPlanId(), request, summary, interactionSteps, validationLayers));
        snapshot.clearPlanningState();
        snapshot.setClarifiedIntent(clarifiedIntent);
        return request;
    }
    
    /**
     * Ensure lifecycle state matches the requested workflow operation.
     *
     * @param ruleLabel rule label for user-facing issues
     * @param clarifiedIntent clarified intent
     * @param ruleExists whether the target rule already exists
     * @param snapshot workflow snapshot
     * @return whether lifecycle state is valid
     */
    public boolean ensureLifecycleState(final String ruleLabel, final ClarifiedIntent clarifiedIntent,
                                        final boolean ruleExists, final WorkflowContextSnapshot snapshot) {
        String actualOperationType = clarifiedIntent.getOperationType().toLowerCase(Locale.ENGLISH);
        if ("create".equals(actualOperationType) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("%s already exists for the target column.", ruleLabel), "Use alter instead of create.", false, Map.of()));
            return false;
        }
        if ("alter".equals(actualOperationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("%s does not exist for the target column.", ruleLabel), "Use create instead of alter or confirm the target column.", false, Map.of()));
            return false;
        }
        if ("drop".equals(actualOperationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                    String.format("%s does not exist for the target column.", ruleLabel), "Confirm target table and column or skip the drop request.", false, Map.of()));
            return false;
        }
        return true;
    }
    
    /**
     * Add one fallback clarification question when algorithm selection is blocked.
     *
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param fallbackQuestion fallback question
     * @return whether there is any blocking algorithm issue
     */
    public boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot, final String fallbackQuestion) {
        boolean result = snapshot.getIssues().stream().anyMatch(each -> "selecting-algorithm".equals(each.getStage()) && "error".equals(each.getSeverity()));
        if (result && clarifiedIntent.getClarificationMessages().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add(fallbackQuestion);
        }
        return result;
    }
    
    /**
     * Collect required algorithm properties and emit missing-property clarification prompts.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param propertyRequirements property requirements
     * @return whether all required properties are present
     */
    public boolean collectPropertyRequirements(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                               final WorkflowContextSnapshot snapshot, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        snapshot.getPropertyRequirements().addAll(propertyRequirements);
        applyDefaultProperties(request, propertyRequirements);
        List<String> missingRequiredProperties = findMissingRequiredProperties(request, propertyRequirements);
        if (missingRequiredProperties.isEmpty()) {
            return true;
        }
        for (String each : missingRequiredProperties) {
            clarifiedIntent.getClarificationMessages().add(String.format("Please provide property `%s`.", each));
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING, "error", "collecting-properties",
                "Required algorithm properties are still missing.", "Provide all required algorithm properties.", true, Map.of("missing_properties", missingRequiredProperties)));
        return false;
    }
    
    /**
     * Judge whether workflow planning can continue to artifact generation.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param propertyRequirements property requirements
     * @param fallbackQuestion fallback question
     * @return whether artifact planning can continue
     */
    public boolean isReadyForArtifactPlanning(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot,
                                              final List<AlgorithmPropertyRequirement> propertyRequirements, final String fallbackQuestion) {
        if (hasBlockingAlgorithmIssues(clarifiedIntent, snapshot, fallbackQuestion) || !clarifiedIntent.getClarificationMessages().isEmpty()) {
            return false;
        }
        return collectPropertyRequirements(request, clarifiedIntent, snapshot, propertyRequirements);
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
    public boolean ensurePlanningContext(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                         final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (request.getDatabase().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking",
                    "Database is required before planning.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureSupportedIdentifier("database", request.getDatabase(), snapshot)
                || !ensureSupportedIdentifier("table", request.getTable(), snapshot)
                || !ensureSupportedIdentifier("column", request.getColumn(), snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        request.setSchema(resolveSchema(metadataQueryFacade, request, clarifiedIntent));
        if (!ensureSupportedIdentifier("schema", request.getSchema(), snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        addMissingQuestions(request, clarifiedIntent);
        if (request.getSchema().isEmpty() || request.getTable().isEmpty() || request.getColumn().isEmpty()) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureTableExists(metadataQueryFacade, request, snapshot) || !ensureColumnExists(metadataQueryFacade, request, snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        return true;
    }
    
    private void addMissingQuestions(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (request.getSchema().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please specify schema.");
        }
        if (request.getTable().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please specify target table.");
        }
        if (request.getColumn().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add("Please specify target column.");
        }
    }
    
    private boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot) {
        if (identifier.isEmpty() || WorkflowSQLUtils.isSafeIdentifier(identifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", "discovering",
                String.format("%s identifier `%s` contains unsupported characters.", fieldName, identifier),
                "Use unquoted SQL identifiers only in V1.", false, Map.of("field", fieldName, "identifier", identifier)));
        return false;
    }
    
    private String resolveSchema(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        String actualSchema = request.getSchema();
        if (!actualSchema.isEmpty()) {
            return actualSchema;
        }
        Optional<MCPDatabaseMetadata> databaseMetadata = metadataQueryFacade.queryDatabase(request.getDatabase());
        if (databaseMetadata.isEmpty()) {
            return "";
        }
        String tableName = request.getTable();
        if (!tableName.isEmpty()) {
            List<String> result = new LinkedList<>();
            for (MCPSchemaMetadata each : databaseMetadata.get().getSchemas()) {
                if (each.getTables().stream().anyMatch(table -> tableName.equals(table.getTable()))) {
                    result.add(each.getSchema());
                }
            }
            if (1 == result.size()) {
                return recordInferredSchema(clarifiedIntent, result.get(0));
            }
        }
        return 1 == databaseMetadata.get().getSchemas().size() ? recordInferredSchema(clarifiedIntent, databaseMetadata.get().getSchemas().iterator().next().getSchema()) : "";
    }
    
    private String recordInferredSchema(final ClarifiedIntent clarifiedIntent, final String schema) {
        if (null != clarifiedIntent) {
            clarifiedIntent.getInferredValues().put("schema", schema);
        }
        return schema;
    }
    
    private boolean ensureTableExists(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                      final WorkflowContextSnapshot snapshot) {
        if (metadataQueryFacade.queryTable(request.getDatabase(), request.getSchema(), request.getTable()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.TABLE_NOT_FOUND, "error", "discovering",
                String.format("Table `%s` does not exist in Proxy logical metadata.", request.getTable()), "Check database, schema and table name.", false, Map.of()));
        return false;
    }
    
    private boolean ensureColumnExists(final MCPMetadataQueryFacade metadataQueryFacade, final WorkflowRequest request,
                                       final WorkflowContextSnapshot snapshot) {
        if (metadataQueryFacade.queryTableColumn(request.getDatabase(), request.getSchema(), request.getTable(), request.getColumn()).isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.COLUMN_NOT_FOUND, "error", "discovering",
                String.format("Column `%s` does not exist in Proxy logical metadata.", request.getColumn()), "Check column name.", false, Map.of()));
        return false;
    }
    
    private void applyDefaultProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.getDefaultValue().isEmpty()) {
                request.getAlgorithmProperties(each.getAlgorithmRole()).putIfAbsent(each.getPropertyKey(), each.getDefaultValue());
            }
        }
    }
    
    private List<String> findMissingRequiredProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            String actualValue = request.getAlgorithmProperties(each.getAlgorithmRole()).get(each.getPropertyKey());
            if (each.isRequired() && (null == actualValue || actualValue.isBlank())) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
}
