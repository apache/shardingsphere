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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sharding workflow validation service.
 */
public final class ShardingWorkflowValidationService implements MCPWorkflowRuntimeHandler, MCPWorkflowApplyArtifactValidator {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final ShardingInspectionService inspectionService = new ShardingInspectionService();
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(
            WorkflowSynchronizationSupport.DEFAULT_SYNCHRONIZATION_WINDOW, WorkflowSynchronizationSupport.DEFAULT_POLL_INTERVAL);
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        return validationSupport.validateAndFinalize(workflowSessionContext, sessionId, snapshot, () -> createValidationReport(snapshot, queryFacade));
    }
    
    @Override
    public List<Map<String, Object>> validate(final WorkflowContextSnapshot snapshot, final Collection<ExecutableWorkflowArtifact> artifacts) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (ExecutableWorkflowArtifact each : artifacts) {
            addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
        }
        return result;
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isRuleDefinitionDistSQL(sql) || !(snapshot.getRequest() instanceof ShardingWorkflowRequest)) {
            return;
        }
        ShardingWorkflowRequest request = (ShardingWorkflowRequest) snapshot.getRequest();
        if (ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addTableRuleAlgorithmIssues(issues, request, displaySql);
            return;
        }
        if (ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addShardingAlgorithmIssue(issues, request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
            return;
        }
        if (ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addKeyGeneratorTypeIssue(issues, request, displaySql);
            return;
        }
        if (ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            addKeyGeneratorIssue(issues, request, displaySql);
        }
    }
    
    private boolean isRuleDefinitionDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE SHARDING ") || actualSQL.startsWith("CREATE DEFAULT SHARDING ");
    }
    
    private void addTableRuleAlgorithmIssues(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (!request.getStorageUnits().isEmpty() || !"none".equals(normalizeStrategyType(request))) {
            addShardingAlgorithmIssue(issues, request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
        }
        addKeyGeneratorIssue(issues, request, displaySql);
        for (String each : request.getAuditorNames()) {
            addAuditorIssue(issues, each, displaySql);
        }
    }
    
    private void addShardingAlgorithmIssue(final List<Map<String, Object>> issues, final String algorithmType, final Map<String, String> properties, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(ShardingAlgorithm.class, algorithmType, properties)) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references sharding algorithm `%s`, "
                    + "but it cannot be loaded or initialized by ShardingAlgorithm SPI.", algorithmType), displaySql));
        }
    }
    
    private void addKeyGeneratorIssue(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (!request.getKeyGeneratorName().isEmpty() || request.getKeyGeneratorType().isEmpty()) {
            return;
        }
        addKeyGeneratorTypeIssue(issues, request, displaySql);
    }
    
    private void addKeyGeneratorTypeIssue(final List<Map<String, Object>> issues, final ShardingWorkflowRequest request, final String displaySql) {
        if (request.getKeyGeneratorType().isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(KeyGenerateAlgorithm.class, request.getKeyGeneratorType(), request.getKeyGeneratorProperties())) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references key generator algorithm `%s`, "
                    + "but it cannot be loaded or initialized by KeyGenerateAlgorithm SPI.", request.getKeyGeneratorType()), displaySql));
        }
    }
    
    private void addAuditorIssue(final List<Map<String, Object>> issues, final String algorithmType, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(ShardingAuditAlgorithm.class, algorithmType, Map.of())) {
            issues.add(createValidationIssue(String.format("Generated sharding DistSQL references auditor algorithm `%s`, "
                    + "but it cannot be loaded or initialized by ShardingAuditAlgorithm SPI.", algorithmType), displaySql));
        }
    }
    
    private String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().toLowerCase(Locale.ENGLISH);
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        result.setRuleValidation(validateByWorkflowKind(snapshot, queryFacade, result));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateByWorkflowKind(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade, final ValidationReport validationReport) {
        ShardingWorkflowRequest request = (ShardingWorkflowRequest) snapshot.getRequest();
        queryFacade.checkDatabaseCapability(request.getDatabase());
        if (ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            List<Map<String, Object>> rows = inspectionService.queryTableRule(queryFacade, request.getDatabase(), request.getTable());
            return validateNamedState(snapshot, validationReport, rows, queryFacade, request.getDatabase(), "table", request.getTable(),
                    matchesTableRule(rows, queryFacade, request));
        }
        if (ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            List<Map<String, Object>> rows = inspectionService.queryTableReferenceRule(queryFacade, request.getDatabase(), request.getRuleName());
            return validateNamedState(snapshot, validationReport, rows, queryFacade, request.getDatabase(), "name", request.getRuleName(),
                    matchesTableReferenceRule(rows, queryFacade, request));
        }
        if (ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            List<Map<String, Object>> rows = inspectionService.queryKeyGenerator(queryFacade, request.getDatabase(), request.getKeyGeneratorName());
            return validateNamedState(snapshot, validationReport, rows, queryFacade, request.getDatabase(), "name", request.getKeyGeneratorName(),
                    matchesKeyGenerator(rows, queryFacade, request));
        }
        if (ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            List<Map<String, Object>> rows = inspectionService.queryKeyGenerateStrategy(queryFacade, request.getDatabase(), request.getKeyGenerateStrategyName());
            return validateNamedState(snapshot, validationReport, rows, queryFacade, request.getDatabase(), "name", request.getKeyGenerateStrategyName(),
                    matchesKeyGenerateStrategy(rows, queryFacade, request));
        }
        if (ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND.equals(snapshot.getWorkflowKind())) {
            return validateDefaultStrategy(snapshot, validationReport, inspectionService.queryDefaultStrategy(queryFacade, request.getDatabase()), queryFacade, request);
        }
        return validateCleanup(validationReport, queryFacade, request);
    }
    
    private ValidationSection validateNamedState(final WorkflowContextSnapshot snapshot, final ValidationReport validationReport,
                                                 final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final String databaseName,
                                                 final String fieldName, final String expected, final boolean shapeMatches) {
        boolean exists = containsNamedRow(rows, queryFacade, databaseName, fieldName, expected);
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && exists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !exists) {
            addMismatch(validationReport, fieldName, expected);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rows, "Sharding rule state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !shapeMatches) {
            addMismatch(validationReport, fieldName, expected);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rows, "Sharding rule fields do not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rows, "Sharding rule state matches the planned DistSQL artifact.");
    }
    
    private boolean matchesTableRule(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        if (1 != rows.size() || !containsNamedRow(rows, queryFacade, request.getDatabase(), "table", request.getTable())) {
            return false;
        }
        Map<String, Object> row = rows.getFirst();
        boolean dataSourceShapeMatches = request.getStorageUnits().isEmpty()
                ? WorkflowRuleValueUtils.getRuleValue(row, "actual_data_nodes").equals(request.getDataNodes())
                : matchesIdentifiers(WorkflowRuleValueUtils.getRuleValue(row, "actual_data_sources"), splitValues(request.getStorageUnits()), IdentifierScope.TABLE,
                        queryFacade, request.getDatabase());
        return dataSourceShapeMatches && matchesTableStrategy(row, queryFacade, request) && matchesTableKeyGenerator(row, queryFacade, request)
                && matchesTableAuditors(row, request);
    }
    
    private boolean matchesTableStrategy(final Map<String, Object> row, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        String strategyType = request.getStorageUnits().isEmpty() ? normalizeStrategyType(request) : "standard";
        if ("none".equals(strategyType)) {
            return true;
        }
        if (!WorkflowRuleValueUtils.getRuleValue(row, "table_strategy_type").equalsIgnoreCase(strategyType)
                || !WorkflowRuleValueUtils.getRuleValue(row, "table_sharding_algorithm_type").equalsIgnoreCase(request.getAlgorithmType())
                || !WorkflowAlgorithmUtils.createPropertyMap(row.get("table_sharding_algorithm_props")).equals(request.getPrimaryAlgorithmProperties())) {
            return false;
        }
        if ("hint".equals(strategyType)) {
            return true;
        }
        String actualColumns = WorkflowRuleValueUtils.getRuleValue(row, "table_sharding_column");
        return "complex".equals(strategyType)
                ? matchesIdentifiers(actualColumns, splitValues(request.getShardingColumns()), IdentifierScope.COLUMN, queryFacade, request.getDatabase())
                : queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.COLUMN, request.getColumn(), actualColumns);
    }
    
    private boolean matchesTableKeyGenerator(final Map<String, Object> row, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        if (request.getKeyGenerateColumn().isEmpty()) {
            return true;
        }
        if (!queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.COLUMN, request.getKeyGenerateColumn(),
                WorkflowRuleValueUtils.getRuleValue(row, "key_generate_column"))) {
            return false;
        }
        if (!request.getKeyGeneratorName().isEmpty()) {
            return containsNamedRow(inspectionService.queryTableRulesUsedKeyGenerator(queryFacade, request.getDatabase(), request.getKeyGeneratorName()),
                    queryFacade, request.getDatabase(), "name", request.getTable());
        }
        return WorkflowRuleValueUtils.getRuleValue(row, "key_generator_type").equalsIgnoreCase(request.getKeyGeneratorType())
                && WorkflowAlgorithmUtils.createPropertyMap(row.get("key_generator_props")).equals(request.getKeyGeneratorProperties());
    }
    
    private boolean matchesTableAuditors(final Map<String, Object> row, final ShardingWorkflowRequest request) {
        if (request.getAuditorNames().isEmpty()) {
            return true;
        }
        String allowHintDisable = request.getAllowHintDisable().isEmpty() ? "false" : request.getAllowHintDisable();
        return matchesValues(WorkflowRuleValueUtils.getRuleValue(row, "auditor_types"), request.getAuditorNames())
                && WorkflowRuleValueUtils.getRuleValue(row, "allow_hint_disable").equalsIgnoreCase(allowHintDisable);
    }
    
    private boolean matchesTableReferenceRule(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        return 1 == rows.size() && containsNamedRow(rows, queryFacade, request.getDatabase(), "name", request.getRuleName())
                && matchesIdentifiers(WorkflowRuleValueUtils.getRuleValue(rows.getFirst(), "sharding_table_reference"), request.getReferenceTables(), IdentifierScope.TABLE,
                        queryFacade, request.getDatabase());
    }
    
    private boolean matchesKeyGenerator(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        return 1 == rows.size() && containsNamedRow(rows, queryFacade, request.getDatabase(), "name", request.getKeyGeneratorName())
                && WorkflowRuleValueUtils.getRuleValue(rows.getFirst(), "type").equalsIgnoreCase(request.getKeyGeneratorType())
                && WorkflowAlgorithmUtils.createPropertyMap(rows.getFirst().get("props")).equals(request.getKeyGeneratorProperties());
    }
    
    private boolean matchesKeyGenerateStrategy(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        if (1 != rows.size() || !containsNamedRow(rows, queryFacade, request.getDatabase(), "name", request.getKeyGenerateStrategyName())) {
            return false;
        }
        Map<String, Object> row = rows.getFirst();
        boolean sequenceStrategy = !request.getSequenceName().isEmpty();
        if (!WorkflowRuleValueUtils.getRuleValue(row, "type").equalsIgnoreCase(sequenceStrategy ? "sequence" : "column")) {
            return false;
        }
        boolean targetMatches = sequenceStrategy
                ? request.getSequenceName().equals(WorkflowRuleValueUtils.getRuleValue(row, "sequence"))
                : queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getTable(), WorkflowRuleValueUtils.getRuleValue(row, "table"))
                        && queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.COLUMN, request.getColumn(), WorkflowRuleValueUtils.getRuleValue(row, "column"));
        if (!targetMatches) {
            return false;
        }
        return request.getKeyGeneratorName().isEmpty()
                ? WorkflowRuleValueUtils.getRuleValue(row, "generator_type").equalsIgnoreCase(request.getKeyGeneratorType())
                        && WorkflowAlgorithmUtils.createPropertyMap(row.get("generator_props")).equals(request.getKeyGeneratorProperties())
                : queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getKeyGeneratorName(),
                        WorkflowRuleValueUtils.getRuleValue(row, "generator_name"));
    }
    
    private ValidationSection validateDefaultStrategy(final WorkflowContextSnapshot snapshot, final ValidationReport validationReport,
                                                      final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        boolean exists = rows.stream().anyMatch(each -> queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE, request.getDefaultStrategyType(),
                WorkflowRuleValueUtils.getRuleValue(each, "name")) && !WorkflowRuleValueUtils.getRuleValue(each, "type").isEmpty());
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot) && exists || !WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !exists) {
            addMismatch(validationReport, "name", request.getDefaultStrategyType());
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rows, "Default sharding strategy state does not match the planned DistSQL artifact.");
        }
        if (!WorkflowLifecycleUtils.isDropWorkflow(snapshot) && !matchesDefaultStrategy(rows, queryFacade, request)) {
            addMismatch(validationReport, "name", request.getDefaultStrategyType());
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rows, "Default sharding strategy fields do not match the planned DistSQL artifact.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rows, "Default sharding strategy state matches the planned DistSQL artifact.");
    }
    
    private boolean matchesDefaultStrategy(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        List<Map<String, Object>> matchingRows = rows.stream().filter(each -> queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.TABLE,
                request.getDefaultStrategyType(), WorkflowRuleValueUtils.getRuleValue(each, "name"))).toList();
        if (1 != matchingRows.size()) {
            return false;
        }
        Map<String, Object> row = matchingRows.getFirst();
        String strategyType = normalizeStrategyType(request);
        if (!WorkflowRuleValueUtils.getRuleValue(row, "type").equalsIgnoreCase(strategyType)) {
            return false;
        }
        if ("none".equals(strategyType)) {
            return true;
        }
        if (!"hint".equals(strategyType)) {
            String actualColumns = WorkflowRuleValueUtils.getRuleValue(row, "sharding_column");
            boolean columnsMatch = "complex".equals(strategyType)
                    ? matchesIdentifiers(actualColumns, splitValues(request.getShardingColumns()), IdentifierScope.COLUMN, queryFacade, request.getDatabase())
                    : queryFacade.isSameIdentifier(request.getDatabase(), IdentifierScope.COLUMN, request.getColumn(), actualColumns);
            if (!columnsMatch) {
                return false;
            }
        }
        return WorkflowRuleValueUtils.getRuleValue(row, "sharding_algorithm_type").equalsIgnoreCase(request.getAlgorithmType())
                && WorkflowAlgorithmUtils.createPropertyMap(row.get("sharding_algorithm_props")).equals(request.getPrimaryAlgorithmProperties());
    }
    
    private ValidationSection validateCleanup(final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        List<Map<String, Object>> rows = queryCleanupRows(queryFacade, request);
        if (containsNamedRow(rows, queryFacade, request.getDatabase(), "name", request.getComponentName())) {
            addMismatch(validationReport, "name", request.getComponentName());
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, rows, "Sharding component still exists after cleanup.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, rows, "Sharding component cleanup state matches the planned DistSQL artifact.");
    }
    
    private List<Map<String, Object>> queryCleanupRows(final MCPFeatureQueryFacade queryFacade, final ShardingWorkflowRequest request) {
        switch (request.getComponentType().toLowerCase().replace('_', '-')) {
            case "algorithm":
                return inspectionService.queryAlgorithms(queryFacade, request.getDatabase());
            case "key-generator":
                return inspectionService.queryKeyGenerators(queryFacade, request.getDatabase());
            case "auditor":
                return inspectionService.queryAuditors(queryFacade, request.getDatabase());
            default:
                return List.of(Map.of("unsupported_component_type", request.getComponentType()));
        }
    }
    
    private boolean containsNamedRow(final List<Map<String, Object>> rows, final MCPFeatureQueryFacade queryFacade, final String databaseName,
                                     final String fieldName, final String expected) {
        return rows.stream().anyMatch(each -> queryFacade.isSameIdentifier(databaseName, IdentifierScope.TABLE, expected, WorkflowRuleValueUtils.getRuleValue(each, fieldName)));
    }
    
    private boolean matchesIdentifiers(final String actualValues, final Collection<String> expectedValues, final IdentifierScope identifierScope,
                                       final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        List<String> actual = splitValues(actualValues);
        return actual.size() == expectedValues.size() && expectedValues.stream().allMatch(expected -> actual.stream()
                .anyMatch(each -> queryFacade.isSameIdentifier(databaseName, identifierScope, expected, each)));
    }
    
    private boolean matchesValues(final String actualValues, final Collection<String> expectedValues) {
        List<String> actual = splitValues(actualValues);
        return actual.size() == expectedValues.size() && expectedValues.stream().allMatch(expected -> actual.stream().anyMatch(expected::equalsIgnoreCase));
    }
    
    private List<String> splitValues(final String values) {
        return Arrays.stream(values.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
    }
    
    private void addMismatch(final ValidationReport validationReport, final String field, final String expected) {
        validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, field, expected, "",
                "Sharding DistSQL-visible state differs from the planned artifact.", "Inspect current sharding state and re-apply or re-plan the workflow."));
    }
}
