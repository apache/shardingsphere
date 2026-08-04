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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Validator for workflow planning context.
 */
public final class WorkflowPlanningContextValidator {
    
    /**
     * Ensure workflow identifiers can be rendered into reviewable DistSQL.
     *
     * @param fieldName field name for issue details
     * @param identifiers identifiers to check
     * @param snapshot workflow snapshot
     * @param issueStage issue stage
     * @return whether all identifiers are supported
     */
    public boolean ensureSupportedIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot, final String issueStage) {
        return ensureIdentifiers(fieldName, identifiers, snapshot, issueStage, false);
    }
    
    /**
     * Ensure optional workflow identifiers can be rendered into reviewable DistSQL when present.
     *
     * @param fieldName field name for issue details
     * @param identifiers identifiers to check
     * @param snapshot workflow snapshot
     * @param issueStage issue stage
     * @return whether all present identifiers are supported
     */
    public boolean ensureOptionalSupportedIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot, final String issueStage) {
        return ensureIdentifiers(fieldName, identifiers, snapshot, issueStage, true);
    }
    
    /**
     * Ensure workflow planning context is complete and valid.
     *
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade for database capability resolution
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @return whether planning context is ready
     * @throws DatabaseCapabilityNotFoundException when database profile or capability does not exist
     */
    public boolean ensurePlanningContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final WorkflowRequest request,
                                         final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (isEmptyIdentifier(request.getDatabase())) {
            clarifiedIntent.getClarificationMessages().add("Please provide logical database first.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", WorkflowLifecycle.STEP_INTAKING,
                    "Database is required before planning.", "Provide the logical database name.", true, Map.of()));
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (!ensureSupportedIdentifiers(WorkflowFieldNames.DATABASE, List.of(request.getDatabase()), snapshot, WorkflowLifecycle.STEP_DISCOVERING)
                || !ensureSupportedIdentifiers(WorkflowFieldNames.TABLE, List.of(request.getTable()), snapshot, WorkflowLifecycle.STEP_DISCOVERING)
                || !ensureSupportedIdentifiers(WorkflowFieldNames.COLUMN, List.of(request.getColumn()), snapshot, WorkflowLifecycle.STEP_DISCOVERING)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        String databaseName = WorkflowSQLUtils.normalizeIdentifier(request.getDatabase());
        metadataQueryFacade.queryDatabase(databaseName).orElseThrow(DatabaseCapabilityNotFoundException::new);
        queryFacade.checkDatabaseCapability(databaseName);
        List<ShardingSphereSchema> schemas = isEmptyIdentifier(request.getSchema()) ? metadataQueryFacade.querySchemas(databaseName) : List.of();
        request.setSchema(resolveSchema(schemas, request, clarifiedIntent, queryFacade, databaseName));
        if (!ensureSupportedIdentifiers(WorkflowFieldNames.SCHEMA, List.of(request.getSchema()), snapshot, WorkflowLifecycle.STEP_DISCOVERING)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        addMissingQuestions(request, clarifiedIntent, snapshot);
        if (isEmptyIdentifier(request.getSchema()) || isEmptyIdentifier(request.getTable()) || isEmptyIdentifier(request.getColumn())) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
            return false;
        }
        if (schemas.isEmpty()) {
            schemas = metadataQueryFacade.querySchemas(databaseName);
        }
        Optional<ShardingSphereSchema> schema = findSchema(schemas, request, queryFacade, databaseName);
        Optional<ShardingSphereTable> table = findTable(schema, request, queryFacade, databaseName);
        if (!ensureTableExists(table, request, snapshot)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        if (!ensureColumnExists(metadataQueryFacade.queryTableColumns(databaseName, schema.orElseThrow().getName(), table.orElseThrow().getName()),
                request, snapshot, queryFacade, databaseName)) {
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            return false;
        }
        return true;
    }
    
    private Optional<ShardingSphereSchema> findSchema(final Collection<ShardingSphereSchema> schemas, final WorkflowRequest request,
                                                      final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return schemas.stream().filter(each -> matchesMetadataIdentifier(
                queryFacade, databaseName, IdentifierScope.SCHEMA, request.getSchema(), each.getName())).findFirst();
    }
    
    private boolean ensureIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot, final String issueStage, final boolean allowEmpty) {
        for (String each : identifiers) {
            if (!ensureSupportedIdentifier(fieldName, each, snapshot, issueStage, allowEmpty)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isEmptyIdentifier(final String identifier) {
        return WorkflowSQLUtils.normalizeIdentifier(identifier).isEmpty();
    }
    
    private void addMissingQuestions(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        if (isEmptyIdentifier(request.getSchema())) {
            clarifiedIntent.getClarificationMessages().add("Please specify schema.");
        }
        addMissingTableQuestion(request, clarifiedIntent, snapshot, "Table is required before planning.");
        addMissingColumnQuestion(request, clarifiedIntent, snapshot, "Column is required before planning.");
    }
    
    private void addMissingTableQuestion(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot, final String message) {
        if (isEmptyIdentifier(request.getTable())) {
            clarifiedIntent.getClarificationMessages().add("Please specify target table.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.TABLE_REQUIRED, "error", WorkflowLifecycle.STEP_INTAKING,
                    message, "Provide the logical table name.", true, Map.of()));
        }
    }
    
    private void addMissingColumnQuestion(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot, final String message) {
        if (isEmptyIdentifier(request.getColumn())) {
            clarifiedIntent.getClarificationMessages().add("Please specify target column.");
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.COLUMN_REQUIRED, "error", WorkflowLifecycle.STEP_INTAKING,
                    message, "Provide the logical column name.", true, Map.of()));
        }
    }
    
    private boolean ensureSupportedIdentifier(final String fieldName, final String identifier, final WorkflowContextSnapshot snapshot, final String issueStage, final boolean allowEmpty) {
        if (allowEmpty && identifier.isEmpty() || WorkflowSQLUtils.isSupportedIdentifier(identifier)) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", issueStage, createUnsupportedIdentifierMessage(fieldName, identifier),
                createUnsupportedIdentifierAction(fieldName), false, createUnsupportedIdentifierDetails(fieldName, identifier)));
        return false;
    }
    
    private String createUnsupportedIdentifierMessage(final String fieldName, final String identifier) {
        return fieldName.isEmpty()
                ? String.format("Identifier `%s` contains unsupported characters.", identifier)
                : String.format("%s identifier `%s` contains unsupported characters.", fieldName, identifier);
    }
    
    private String createUnsupportedIdentifierAction(final String fieldName) {
        return fieldName.isEmpty()
                ? "Use reviewable logical identifiers without NUL or line terminators."
                : "Use a reviewable logical identifier without NUL or line terminators.";
    }
    
    private Map<String, Object> createUnsupportedIdentifierDetails(final String fieldName, final String identifier) {
        return fieldName.isEmpty() ? Map.of("identifier", identifier) : Map.of("field", fieldName, "identifier", identifier);
    }
    
    private String resolveSchema(final Collection<ShardingSphereSchema> schemas, final WorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                 final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        String actualSchema = request.getSchema();
        if (!isEmptyIdentifier(actualSchema)) {
            return actualSchema;
        }
        if (schemas.isEmpty()) {
            return "";
        }
        if (!WorkflowSQLUtils.normalizeIdentifier(request.getTable()).isEmpty()) {
            List<String> result = new LinkedList<>();
            for (ShardingSphereSchema each : schemas) {
                if (containsTable(queryFacade, databaseName, request.getTable(), each.getAllTables())) {
                    result.add(each.getName());
                }
            }
            if (1 == result.size()) {
                return recordInferredSchema(clarifiedIntent, result.get(0));
            }
        }
        return 1 == schemas.size() ? recordInferredSchema(clarifiedIntent, schemas.iterator().next().getName()) : "";
    }
    
    private boolean containsTable(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName, final Collection<ShardingSphereTable> tables) {
        for (ShardingSphereTable each : tables) {
            if (TableType.TABLE == each.getType() && queryFacade.isSameIdentifier(databaseName, IdentifierScope.TABLE, tableName, each.getName())) {
                return true;
            }
        }
        return false;
    }
    
    private String recordInferredSchema(final ClarifiedIntent clarifiedIntent, final String schema) {
        if (null != clarifiedIntent) {
            clarifiedIntent.getInferredValues().put(WorkflowFieldNames.SCHEMA, schema);
        }
        return schema;
    }
    
    private Optional<ShardingSphereTable> findTable(final Optional<ShardingSphereSchema> schema, final WorkflowRequest request,
                                                    final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return schema.flatMap(optional -> optional.getAllTables().stream()
                .filter(each -> TableType.TABLE == each.getType()
                        && matchesMetadataIdentifier(queryFacade, databaseName, IdentifierScope.TABLE, request.getTable(), each.getName()))
                .findFirst());
    }
    
    private boolean matchesMetadataIdentifier(final MCPFeatureQueryFacade queryFacade, final String databaseName, final IdentifierScope identifierScope,
                                              final String identifier, final String existingIdentifier) {
        return WorkflowSQLUtils.requiresExactIdentifierMatch(identifier)
                ? WorkflowSQLUtils.normalizeIdentifier(identifier).equals(existingIdentifier)
                : queryFacade.isSameIdentifier(databaseName, identifierScope, identifier, existingIdentifier);
    }
    
    private boolean ensureTableExists(final Optional<ShardingSphereTable> table, final WorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (table.isPresent()) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.TABLE_NOT_FOUND, "error", WorkflowLifecycle.STEP_DISCOVERING,
                String.format("Table `%s` does not exist in Proxy logical metadata.", request.getTable()), "Check database, schema and table name.", false, Map.of()));
        return false;
    }
    
    private boolean ensureColumnExists(final Collection<MCPColumnMetadata> columns, final WorkflowRequest request, final WorkflowContextSnapshot snapshot,
                                       final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        for (MCPColumnMetadata each : columns) {
            if (matchesMetadataIdentifier(queryFacade, databaseName, IdentifierScope.COLUMN, request.getColumn(), each.getName())) {
                return true;
            }
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.COLUMN_NOT_FOUND, "error", WorkflowLifecycle.STEP_DISCOVERING,
                String.format("Column `%s` does not exist in Proxy logical metadata.", request.getColumn()), "Check column name.", false, Map.of()));
        return false;
    }
}
