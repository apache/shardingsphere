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
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sharding DistSQL planning service.
 */
public final class ShardingDistSQLPlanningService {
    
    /**
     * Plan DistSQL artifact for sharding table rule lifecycle operation.
     *
     * @param request workflow request
     * @param operationType lifecycle operation type
     * @return rule artifact
     */
    public RuleArtifact planTableRule(final ShardingWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            return new RuleArtifact("drop", String.format("DROP SHARDING TABLE RULE %s", format(request.getTable())));
        }
        String command = "alter".equalsIgnoreCase(operationType) ? "ALTER" : "CREATE";
        return new RuleArtifact(command.toLowerCase(Locale.ENGLISH), String.format("%s SHARDING TABLE RULE %s(%s)",
                command, format(request.getTable()), createTableRuleBody(request)));
    }
    
    /**
     * Plan DistSQL artifact for sharding table reference rule lifecycle operation.
     *
     * @param request workflow request
     * @param operationType lifecycle operation type
     * @return rule artifact
     */
    public RuleArtifact planTableReferenceRule(final ShardingWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            return new RuleArtifact("drop", String.format("DROP SHARDING TABLE REFERENCE RULE %s", format(request.getRuleName())));
        }
        String command = "alter".equalsIgnoreCase(operationType) ? "ALTER" : "CREATE";
        return new RuleArtifact(command.toLowerCase(Locale.ENGLISH), String.format("%s SHARDING TABLE REFERENCE RULE %s(%s)",
                command, format(request.getRuleName()), joinIdentifiers(request.getReferenceTables())));
    }
    
    /**
     * Plan DistSQL artifact for default sharding strategy lifecycle operation.
     *
     * @param request workflow request
     * @param operationType lifecycle operation type
     * @return rule artifact
     */
    public RuleArtifact planDefaultStrategy(final ShardingWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            return new RuleArtifact("drop", String.format("DROP DEFAULT SHARDING %s STRATEGY", request.getDefaultStrategyType().toUpperCase(Locale.ENGLISH)));
        }
        String command = "alter".equalsIgnoreCase(operationType) ? "ALTER" : "CREATE";
        return new RuleArtifact(command.toLowerCase(Locale.ENGLISH), String.format("%s DEFAULT SHARDING %s STRATEGY (%s)",
                command, request.getDefaultStrategyType().toUpperCase(Locale.ENGLISH), createShardingStrategy(request)));
    }
    
    /**
     * Plan DistSQL artifact for sharding key generator lifecycle operation.
     *
     * @param request workflow request
     * @param operationType lifecycle operation type
     * @return rule artifact
     */
    public RuleArtifact planKeyGenerator(final ShardingWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            return new RuleArtifact("drop", String.format("DROP SHARDING KEY GENERATOR %s", format(request.getKeyGeneratorName())));
        }
        String command = "alter".equalsIgnoreCase(operationType) ? "ALTER" : "CREATE";
        return new RuleArtifact(command.toLowerCase(Locale.ENGLISH), String.format("%s SHARDING KEY GENERATOR %s(%s)",
                command, format(request.getKeyGeneratorName()), createKeyGeneratorFragment(request)));
    }
    
    /**
     * Plan DistSQL artifact for sharding key generate strategy lifecycle operation.
     *
     * @param request workflow request
     * @param operationType lifecycle operation type
     * @return rule artifact
     */
    public RuleArtifact planKeyGenerateStrategy(final ShardingWorkflowRequest request, final String operationType) {
        if (WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(operationType)) {
            return new RuleArtifact("drop", String.format("DROP SHARDING KEY GENERATE STRATEGY %s", format(request.getKeyGenerateStrategyName())));
        }
        String command = "alter".equalsIgnoreCase(operationType) ? "ALTER" : "CREATE";
        return new RuleArtifact(command.toLowerCase(Locale.ENGLISH), String.format("%s SHARDING KEY GENERATE STRATEGY %s(%s)",
                command, format(request.getKeyGenerateStrategyName()), createKeyGenerateStrategyBody(request)));
    }
    
    /**
     * Plan DistSQL artifact for unused sharding rule component cleanup.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planComponentCleanup(final ShardingWorkflowRequest request) {
        switch (normalizeComponentType(request.getComponentType())) {
            case "algorithm":
                return new RuleArtifact("drop", String.format("DROP SHARDING ALGORITHM %s", format(request.getComponentName())));
            case "key-generator":
                return new RuleArtifact("drop", String.format("DROP SHARDING KEY GENERATOR %s", format(request.getComponentName())));
            case "auditor":
                return new RuleArtifact("drop", String.format("DROP SHARDING AUDITOR %s", format(request.getComponentName())));
            default:
                return new RuleArtifact("drop", "");
        }
    }
    
    private String createTableRuleBody(final ShardingWorkflowRequest request) {
        List<String> segments = new ArrayList<>();
        if (!request.getStorageUnits().isEmpty()) {
            segments.add(String.format("STORAGE_UNITS(%s)", joinIdentifiers(splitCsv(request.getStorageUnits()))));
            segments.add(String.format("SHARDING_COLUMN=%s", format(request.getColumn())));
            segments.add(createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
        } else {
            segments.add(String.format("DATANODES('%s')", WorkflowSQLUtils.escapeLiteral(request.getDataNodes())));
            String tableStrategy = createTableStrategy(request);
            if (!tableStrategy.isEmpty()) {
                segments.add(tableStrategy);
            }
        }
        if (!request.getKeyGenerateColumn().isEmpty()) {
            segments.add(String.format("KEY_GENERATE_STRATEGY(COLUMN=%s, %s)", format(request.getKeyGenerateColumn()), createKeyGenerateAlgorithmDefinition(request)));
        }
        if (!request.getAuditorNames().isEmpty()) {
            segments.add(String.format("AUDIT_STRATEGY(%s, ALLOW_HINT_DISABLE=%s)", createAuditAlgorithms(request.getAuditorNames()),
                    request.getAllowHintDisable().isEmpty() ? "FALSE" : request.getAllowHintDisable().toUpperCase(Locale.ENGLISH)));
        }
        return String.join(", ", segments);
    }
    
    private String createTableStrategy(final ShardingWorkflowRequest request) {
        return "none".equals(normalizeStrategyType(request)) ? "" : String.format("TABLE_STRATEGY(%s)", createShardingStrategy(request));
    }
    
    private String createShardingStrategy(final ShardingWorkflowRequest request) {
        String strategyType = normalizeStrategyType(request);
        switch (strategyType) {
            case "complex":
                return String.format("TYPE='%s', SHARDING_COLUMNS=%s, SHARDING_ALGORITHM(%s)",
                        WorkflowSQLUtils.escapeLiteral(strategyType), joinIdentifiers(splitCsv(request.getShardingColumns())),
                        createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
            case "hint":
                return String.format("TYPE='%s', SHARDING_ALGORITHM(%s)",
                        WorkflowSQLUtils.escapeLiteral(strategyType), createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
            case "none":
                return "TYPE='none'";
            default:
                return String.format("TYPE='%s', SHARDING_COLUMN=%s, SHARDING_ALGORITHM(%s)",
                        WorkflowSQLUtils.escapeLiteral(strategyType), format(request.getColumn()), createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
        }
    }
    
    private String createKeyGenerateStrategyBody(final ShardingWorkflowRequest request) {
        String generator = createKeyGenerateAlgorithmDefinition(request);
        return request.getSequenceName().isEmpty()
                ? String.format("TABLE=%s, COLUMN=%s, %s", format(request.getTable()), format(request.getColumn()), generator)
                : String.format("SEQUENCE='%s', %s", WorkflowSQLUtils.escapeLiteral(request.getSequenceName()), generator);
    }
    
    private String createKeyGenerateAlgorithmDefinition(final ShardingWorkflowRequest request) {
        return request.getKeyGeneratorName().isEmpty()
                ? createKeyGeneratorFragment(request)
                : String.format("GENERATOR=%s", format(request.getKeyGeneratorName()));
    }
    
    private String createKeyGeneratorFragment(final ShardingWorkflowRequest request) {
        String keyGeneratorType = request.getKeyGeneratorType().isEmpty() ? request.getAlgorithmType() : request.getKeyGeneratorType();
        return createAlgorithmFragment(keyGeneratorType, request.getKeyGeneratorProperties());
    }
    
    private String createAlgorithmFragment(final String algorithmType, final Map<String, String> properties) {
        return WorkflowSQLUtils.createAlgorithmFragment(algorithmType, properties);
    }
    
    private String createAuditAlgorithms(final Collection<String> auditorNames) {
        return auditorNames.stream().map(each -> WorkflowSQLUtils.createAlgorithmFragment(each, Map.of())).toList().stream().reduce((left, right) -> left + ", " + right).orElse("");
    }
    
    private List<String> splitCsv(final String value) {
        List<String> result = new ArrayList<>();
        for (String each : value.split(",")) {
            String trimmed = each.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
    
    private String joinIdentifiers(final Collection<String> identifiers) {
        return identifiers.stream().map(this::format).reduce((left, right) -> left + ", " + right).orElse("");
    }
    
    private String normalizeComponentType(final String componentType) {
        return componentType.trim().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
    
    private String normalizeStrategyType(final ShardingWorkflowRequest request) {
        return request.getStrategyType().isEmpty() ? "standard" : request.getStrategyType().toLowerCase(Locale.ENGLISH);
    }
    
    private String format(final String value) {
        return WorkflowSQLUtils.formatDistSQLIdentifier(value);
    }
}
