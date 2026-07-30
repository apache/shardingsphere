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

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ShardingWorkflowInputValidator {
    
    boolean ensureDatabase(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getDatabase().isEmpty()) {
            return true;
        }
        snapshot.getClarifiedIntent().getUnresolvedFields().add("database");
        snapshot.getClarifiedIntent().getClarificationMessages().add("Please provide logical database first.");
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", WorkflowLifecycle.STEP_INTAKING,
                "Database is required before planning sharding DistSQL.", "Provide the logical database name.", true, Map.of()));
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    boolean ensureIdentifiers(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        return ensureIdentifiers(areSupportedIdentifiers(List.of(
                request.getDatabase(), request.getTable(), request.getColumn(), request.getKeyGenerateColumn(), request.getRuleName(), request.getKeyGeneratorName(),
                request.getKeyGenerateStrategyName(), request.getSequenceName(), request.getComponentName()))
                && areSupportedIdentifiers(splitCsv(request.getShardingColumns()))
                && areSupportedIdentifiers(splitCsv(request.getStorageUnits()))
                && areSupportedIdentifiers(request.getReferenceTables()), snapshot);
    }
    
    private boolean ensureIdentifiers(final boolean supported, final WorkflowContextSnapshot snapshot) {
        if (supported) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", WorkflowLifecycle.STEP_INTAKING,
                "A sharding identifier contains unsupported characters.", "Use reviewable DistSQL identifiers only.", false, Map.of()));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    boolean ensureCleanupIdentifiers(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        return ensureIdentifiers(areSupportedIdentifiers(List.of(request.getDatabase(), request.getComponentName())), snapshot);
    }
    
    boolean ensureCompatibleInputs(final WorkflowKind workflowKind, final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return true;
        }
        if (ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.equals(workflowKind)) {
            return ensureCompatibleTableRuleInputs(request, snapshot);
        }
        if (ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND.equals(workflowKind)) {
            return ensureCompatibleStrategyInputs(request, snapshot);
        }
        if (ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND.equals(workflowKind)) {
            return ensureCompatibleKeyGenerateStrategyInputs(request, snapshot);
        }
        return true;
    }
    
    private boolean ensureCompatibleTableRuleInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getDataNodes().isEmpty() && !request.getStorageUnits().isEmpty()) {
            return addConflict(snapshot, "Sharding table rule accepts data_nodes or storage_units, but not both.", List.of("data_nodes", "storage_units"));
        }
        if (!request.getStorageUnits().isEmpty()) {
            if (!request.getStrategyType().isEmpty() && !"standard".equalsIgnoreCase(request.getStrategyType())) {
                return addConflict(snapshot, "Auto table rules created from storage_units only support the standard strategy shape.", List.of("storage_units", "strategy_type"));
            }
            if (!request.getShardingColumns().isEmpty()) {
                return addConflict(snapshot, "Auto table rules use column rather than sharding_columns.", List.of("storage_units", "sharding_columns"));
            }
        } else if (!ensureCompatibleStrategyInputs(request, snapshot)) {
            return false;
        }
        return ensureCompatibleKeyGeneratorInputs(request, snapshot);
    }
    
    private boolean ensureCompatibleStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        String strategyType = normalizeStrategyType(request);
        List<String> conflictingInputs = new LinkedList<>();
        conflictingInputs.add("strategy_type");
        switch (strategyType) {
            case "standard":
                addIfPresent(conflictingInputs, "sharding_columns", request.getShardingColumns());
                break;
            case "complex":
                addIfPresent(conflictingInputs, "column", request.getColumn());
                break;
            case "hint":
                addIfPresent(conflictingInputs, "column", request.getColumn());
                addIfPresent(conflictingInputs, "sharding_columns", request.getShardingColumns());
                break;
            case "none":
                addIfPresent(conflictingInputs, "column", request.getColumn());
                addIfPresent(conflictingInputs, "sharding_columns", request.getShardingColumns());
                addIfPresent(conflictingInputs, "algorithm_type", request.getAlgorithmType());
                if (!request.getPrimaryAlgorithmProperties().isEmpty()) {
                    conflictingInputs.add("algorithm_properties");
                }
                break;
            default:
                return true;
        }
        if (1 == conflictingInputs.size()) {
            return true;
        }
        return addConflict(snapshot, String.format("Sharding strategy inputs do not match strategy_type `%s`.", strategyType), conflictingInputs);
    }
    
    private boolean ensureCompatibleKeyGenerateStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getSequenceName().isEmpty() && (!request.getTable().isEmpty() || !request.getColumn().isEmpty())) {
            List<String> conflictingInputs = new LinkedList<>();
            conflictingInputs.add("sequence");
            addIfPresent(conflictingInputs, "table", request.getTable());
            addIfPresent(conflictingInputs, "column", request.getColumn());
            return addConflict(snapshot, "Key generate strategy accepts sequence or table and column, but not both target modes.", conflictingInputs);
        }
        return ensureCompatibleKeyGeneratorInputs(request, snapshot);
    }
    
    private boolean ensureCompatibleKeyGeneratorInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (request.getKeyGeneratorName().isEmpty() || request.getKeyGeneratorType().isEmpty() && request.getKeyGeneratorProperties().isEmpty()) {
            return true;
        }
        List<String> conflictingInputs = new LinkedList<>();
        conflictingInputs.add("key_generator");
        addIfPresent(conflictingInputs, "key_generator_type", request.getKeyGeneratorType());
        if (!request.getKeyGeneratorProperties().isEmpty()) {
            conflictingInputs.add("key_generator_properties");
        }
        return addConflict(snapshot, "Use key_generator for a named generator or key_generator_type with key_generator_properties for an inline generator, but not both.", conflictingInputs);
    }
    
    private void addIfPresent(final Collection<String> inputs, final String fieldName, final String value) {
        if (!value.isEmpty()) {
            inputs.add(fieldName);
        }
    }
    
    private boolean addConflict(final WorkflowContextSnapshot snapshot, final String message, final List<String> conflictingInputs) {
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_CONFLICT, "error", WorkflowLifecycle.STEP_INTAKING,
                message, "Remove conflicting inputs and start a new plan without plan_id.", false, Map.of("conflicting_inputs", conflictingInputs)));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    boolean ensureRequiredTableRuleInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, snapshot, request.getTable(), "table", "Please provide target logical table.");
        }
        if (!require(request, snapshot, request.getTable(), "table", "Please provide target logical table.")) {
            return false;
        }
        if (request.getDataNodes().isEmpty() && request.getStorageUnits().isEmpty()) {
            request.setFieldSemantics("Please provide data nodes or storage units.");
            return false;
        }
        return (request.getStorageUnits().isEmpty() ? ensureRequiredStrategyInputs(request, snapshot) : ensureRequiredAutoTableRuleInputs(request, snapshot))
                && ensureRequiredTableRuleKeyGenerateInputs(request, snapshot) && ensureRequiredAuditInputs(request, snapshot);
    }
    
    private boolean ensureRequiredAutoTableRuleInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        return require(request, snapshot, request.getColumn(), "column", "Please provide auto table sharding column.")
                && require(request, snapshot, request.getAlgorithmType(), "algorithm_type", "Please provide auto table sharding algorithm type.");
    }
    
    private boolean ensureRequiredTableRuleKeyGenerateInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        boolean hasGeneratorInputs = !request.getKeyGeneratorName().isEmpty() || !request.getKeyGeneratorType().isEmpty() || !request.getKeyGeneratorProperties().isEmpty();
        if (request.getKeyGenerateColumn().isEmpty()) {
            if (!hasGeneratorInputs) {
                return true;
            }
            return require(request, snapshot, "", "key_generate_column", "Please provide key generate column when configuring a key generator.");
        }
        if (!request.getKeyGeneratorName().isEmpty()) {
            return true;
        }
        return require(request, snapshot, request.getKeyGeneratorType(), "key_generator_type", "Please provide key generator type or key generator name for key generate strategy.");
    }
    
    private boolean ensureRequiredAuditInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (request.getAllowHintDisable().isEmpty()) {
            return true;
        }
        if (request.getAuditorNames().isEmpty()) {
            return require(request, snapshot, "", "auditors", "Please provide auditors when allow_hint_disable is configured.");
        }
        if ("true".equalsIgnoreCase(request.getAllowHintDisable()) || "false".equalsIgnoreCase(request.getAllowHintDisable())) {
            return true;
        }
        return clarify(request, snapshot, "allow_hint_disable", "Please provide allow_hint_disable as true or false.");
    }
    
    boolean ensureRequiredReferenceRuleInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, snapshot, request.getRuleName(), "rule", "Please provide table reference rule name.");
        }
        if (!require(request, snapshot, request.getRuleName(), "rule", "Please provide table reference rule name.")) {
            return false;
        }
        if (!request.getReferenceTables().isEmpty()) {
            return true;
        }
        return clarify(request, snapshot, "reference_tables", "Please provide reference tables.");
    }
    
    boolean ensureRequiredDefaultStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!require(request, snapshot, request.getDefaultStrategyType(), "default_strategy_type", "Please provide DATABASE or TABLE default strategy type.")) {
            return false;
        }
        if (!isDefaultStrategyType(request.getDefaultStrategyType())) {
            return clarify(request, snapshot, "default_strategy_type", "Please provide default_strategy_type as DATABASE or TABLE.");
        }
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) || "none".equalsIgnoreCase(request.getStrategyType())) {
            return true;
        }
        return ensureRequiredStrategyInputs(request, snapshot);
    }
    
    private boolean isDefaultStrategyType(final String defaultStrategyType) {
        return "DATABASE".equalsIgnoreCase(defaultStrategyType) || "TABLE".equalsIgnoreCase(defaultStrategyType);
    }
    
    boolean ensureRequiredStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        switch (normalizeStrategyType(request)) {
            case "standard":
                return require(request, snapshot, request.getColumn(), "column", "Please provide sharding column.")
                        && require(request, snapshot, request.getAlgorithmType(), "algorithm_type", "Please provide sharding algorithm type.");
            case "complex":
                return ensureRequiredComplexStrategyInputs(request, snapshot);
            case "hint":
                return require(request, snapshot, request.getAlgorithmType(), "algorithm_type", "Please provide sharding algorithm type.");
            case "none":
                return true;
            default:
                return clarify(request, snapshot, "strategy_type", "Please provide strategy_type as standard, complex, hint, or none.");
        }
    }
    
    private boolean ensureRequiredComplexStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!require(request, snapshot, request.getShardingColumns(), "sharding_columns", "Please provide at least two sharding columns for complex strategy.")) {
            return false;
        }
        if (splitCsv(request.getShardingColumns()).size() < 2) {
            return clarify(request, snapshot, "sharding_columns", "Please provide at least two sharding columns for complex strategy.");
        }
        return require(request, snapshot, request.getAlgorithmType(), "algorithm_type", "Please provide sharding algorithm type.");
    }
    
    boolean ensureRequiredKeyGeneratorInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, snapshot, request.getKeyGeneratorName(), "key_generator", "Please provide key generator name.");
        }
        return require(request, snapshot, request.getKeyGeneratorName(), "key_generator", "Please provide key generator name.")
                && require(request, snapshot, request.getKeyGeneratorType(), "key_generator_type", "Please provide key generator type.");
    }
    
    boolean ensureRequiredKeyGenerateStrategyInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, snapshot, request.getKeyGenerateStrategyName(), "key_generate_strategy", "Please provide key generate strategy name.");
        }
        if (!require(request, snapshot, request.getKeyGenerateStrategyName(), "key_generate_strategy", "Please provide key generate strategy name.")) {
            return false;
        }
        if (request.getSequenceName().isEmpty() && request.getTable().isEmpty() && request.getColumn().isEmpty()) {
            request.setFieldSemantics("Please provide table and column, or sequence.");
            return false;
        }
        if (request.getSequenceName().isEmpty() && (!require(request, snapshot, request.getTable(), "table", "Please provide target logical table.")
                || !require(request, snapshot, request.getColumn(), "column", "Please provide key generate column."))) {
            return false;
        }
        if (request.getKeyGeneratorName().isEmpty() && request.getKeyGeneratorType().isEmpty()) {
            request.setFieldSemantics("Please provide key_generator or key_generator_type.");
            return false;
        }
        return true;
    }
    
    boolean ensureRequiredCleanupInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (request.getComponentType().isEmpty()) {
            return require(request, snapshot, "", "component_type", "Please provide component type.");
        }
        if (request.getComponentName().isEmpty()) {
            return require(request, snapshot, "", "component_name", "Please provide component name.");
        }
        return isComponentType(request.getComponentType())
                || clarify(request, snapshot, "component_type", "Please provide component_type as algorithm, key-generator, or auditor.");
    }
    
    void addRequiredInputIssue(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        Map<String, Object> details = snapshot.getClarifiedIntent().getUnresolvedFields().isEmpty()
                ? Map.of()
                : Map.of("missing_inputs", snapshot.getClarifiedIntent().getUnresolvedFields());
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_INPUT_REQUIRED, "error", WorkflowLifecycle.STEP_INTAKING,
                request.getFieldSemantics().isEmpty() ? "Sharding workflow requires additional input." : request.getFieldSemantics(),
                "Provide the missing inputs and continue with the existing plan_id.", true, details));
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
    }
    
    private boolean require(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot, final String value, final String fieldName, final String message) {
        if (!value.isEmpty()) {
            return true;
        }
        return clarify(request, snapshot, fieldName, message);
    }
    
    private boolean clarify(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot, final String fieldName, final String message) {
        request.setFieldSemantics(message);
        if (!snapshot.getClarifiedIntent().getUnresolvedFields().contains(fieldName)) {
            snapshot.getClarifiedIntent().getUnresolvedFields().add(fieldName);
        }
        return false;
    }
    
    List<String> splitCsv(final String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
    }
    
    String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().trim().toLowerCase(Locale.ENGLISH);
    }
    
    String normalizeComponentType(final String componentType) {
        return componentType.trim().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
    
    private boolean isComponentType(final String componentType) {
        return List.of("algorithm", "key-generator", "auditor").contains(normalizeComponentType(componentType));
    }
    
    private boolean areSupportedIdentifiers(final Collection<String> identifiers) {
        return identifiers.stream().allMatch(WorkflowSQLUtils::isSupportedIdentifier);
    }
}
