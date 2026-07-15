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

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ShardingWorkflowInputValidator {
    
    boolean hasDatabase(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getDatabase().isEmpty()) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add("Please provide logical database first.");
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    boolean ensureIdentifiers(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (areSupportedIdentifiers(List.of(
                request.getDatabase(), request.getTable(), request.getColumn(), request.getRuleName(), request.getKeyGeneratorName(), request.getKeyGenerateStrategyName()))
                && areSupportedIdentifiers(splitCsv(request.getShardingColumns()))) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER, "error", WorkflowLifecycle.STEP_INTAKING,
                "A sharding identifier contains unsupported characters.", "Use reviewable DistSQL identifiers only.", false, Map.of()));
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    boolean hasRequiredTableRuleInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getTable(), "Please provide target logical table.");
        }
        if (!require(request, request.getTable(), "Please provide target logical table.")
                || !require(request, request.getDataNodes().isEmpty() ? request.getStorageUnits() : request.getDataNodes(), "Please provide data nodes or storage units.")) {
            return false;
        }
        return (request.getStorageUnits().isEmpty() ? hasRequiredStrategyInputs(request) : hasRequiredAutoTableRuleInputs(request))
                && hasRequiredTableRuleKeyGenerateInputs(request);
    }
    
    private boolean hasRequiredAutoTableRuleInputs(final ShardingWorkflowRequest request) {
        return require(request, request.getColumn(), "Please provide auto table sharding column.")
                && require(request, request.getAlgorithmType(), "Please provide auto table sharding algorithm type.");
    }
    
    private boolean hasRequiredTableRuleKeyGenerateInputs(final ShardingWorkflowRequest request) {
        if (request.getKeyGenerateColumn().isEmpty() || !request.getKeyGeneratorName().isEmpty()) {
            return true;
        }
        return require(request, request.getKeyGeneratorType(), "Please provide key generator type or key generator name for key generate strategy.");
    }
    
    boolean hasRequiredReferenceRuleInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getRuleName(), "Please provide table reference rule name.");
        }
        return require(request, request.getRuleName(), "Please provide table reference rule name.")
                && require(request, request.getReferenceTables().isEmpty() ? "" : "ok", "Please provide reference tables.");
    }
    
    boolean hasRequiredDefaultStrategyInputs(final ShardingWorkflowRequest request) {
        if (!require(request, request.getDefaultStrategyType(), "Please provide DATABASE or TABLE default strategy type.")) {
            return false;
        }
        if (!isDefaultStrategyType(request.getDefaultStrategyType())) {
            request.setFieldSemantics("Please provide default_strategy_type as DATABASE or TABLE.");
            return false;
        }
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType()) || "none".equalsIgnoreCase(request.getStrategyType())) {
            return true;
        }
        return hasRequiredStrategyInputs(request);
    }
    
    private boolean isDefaultStrategyType(final String defaultStrategyType) {
        return "DATABASE".equalsIgnoreCase(defaultStrategyType) || "TABLE".equalsIgnoreCase(defaultStrategyType);
    }
    
    boolean hasRequiredStrategyInputs(final ShardingWorkflowRequest request) {
        switch (normalizeStrategyType(request)) {
            case "standard":
                return require(request, request.getColumn(), "Please provide sharding column.")
                        && require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
            case "complex":
                return hasRequiredComplexStrategyInputs(request);
            case "hint":
                return require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
            case "none":
                return true;
            default:
                request.setFieldSemantics("Please provide strategy_type as standard, complex, hint, or none.");
                return false;
        }
    }
    
    private boolean hasRequiredComplexStrategyInputs(final ShardingWorkflowRequest request) {
        if (!require(request, request.getShardingColumns(), "Please provide at least two sharding columns for complex strategy.")) {
            return false;
        }
        if (splitCsv(request.getShardingColumns()).size() < 2) {
            request.setFieldSemantics("Please provide at least two sharding columns for complex strategy.");
            return false;
        }
        return require(request, request.getAlgorithmType(), "Please provide sharding algorithm type.");
    }
    
    boolean hasRequiredKeyGeneratorInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getKeyGeneratorName(), "Please provide key generator name.");
        }
        return require(request, request.getKeyGeneratorName(), "Please provide key generator name.")
                && require(request, request.getKeyGeneratorType(), "Please provide key generator type.");
    }
    
    boolean hasRequiredKeyGenerateStrategyInputs(final ShardingWorkflowRequest request) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(request.getOperationType())) {
            return require(request, request.getKeyGenerateStrategyName(), "Please provide key generate strategy name.");
        }
        return require(request, request.getKeyGenerateStrategyName(), "Please provide key generate strategy name.")
                && require(request, request.getSequenceName().isEmpty() ? request.getTable() : request.getSequenceName(), "Please provide table or sequence.")
                && require(request, request.getSequenceName().isEmpty() ? request.getColumn() : "ok", "Please provide key generate column.")
                && require(request, request.getKeyGeneratorName().isEmpty() ? request.getKeyGeneratorType() : request.getKeyGeneratorName(), "Please provide generator or key generator type.");
    }
    
    boolean hasRequiredCleanupInputs(final ShardingWorkflowRequest request, final WorkflowContextSnapshot snapshot) {
        if (!request.getComponentType().isEmpty() && !request.getComponentName().isEmpty()) {
            return true;
        }
        snapshot.getClarifiedIntent().getClarificationMessages().add("Please provide component type and component name.");
        snapshot.setStatus(WorkflowLifecycle.STATUS_CLARIFYING);
        return false;
    }
    
    private boolean require(final ShardingWorkflowRequest request, final String value, final String message) {
        if (!value.isEmpty()) {
            return true;
        }
        request.setFieldSemantics(message);
        return false;
    }
    
    List<String> splitCsv(final String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
    }
    
    String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().trim().toLowerCase(Locale.ENGLISH);
    }
    
    private boolean areSupportedIdentifiers(final Collection<String> identifiers) {
        return identifiers.stream().allMatch(WorkflowSQLUtils::isSupportedIdentifier);
    }
}
