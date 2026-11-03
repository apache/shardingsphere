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

package org.apache.shardingsphere.mcp.feature.sharding.tool.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sharding workflow request.
 */
@Getter
public final class ShardingWorkflowRequest extends WorkflowRequest {
    
    private String ruleName = "";
    
    private String dataNodes = "";
    
    private String storageUnits = "";
    
    private String strategyType = "";
    
    private String shardingColumns = "";
    
    private String defaultStrategyType = "";
    
    private String keyGenerateColumn = "";
    
    private String keyGeneratorName = "";
    
    private String keyGeneratorType = "";
    
    private String sequenceName = "";
    
    private String keyGenerateStrategyName = "";
    
    private String componentType = "";
    
    private String componentName = "";
    
    private String allowHintDisable = "";
    
    private final List<String> referenceTables = new ArrayList<>();
    
    private final List<String> auditorNames = new ArrayList<>();
    
    private final Map<String, String> keyGeneratorProperties = new LinkedHashMap<>(8, 1F);
    
    /**
     * Merge previous and current requests.
     *
     * @param previous previous request
     * @param current current request
     * @return merged request
     */
    public static ShardingWorkflowRequest merge(final WorkflowRequest previous, final ShardingWorkflowRequest current) {
        ShardingWorkflowRequest result = copyPreviousRequest(previous);
        current.overlayTo(result);
        return result;
    }
    
    private static ShardingWorkflowRequest copyPreviousRequest(final WorkflowRequest previous) {
        return previous instanceof ShardingWorkflowRequest request ? request.copy() : copyFieldsTo(previous, new ShardingWorkflowRequest());
    }
    
    @Override
    public ShardingWorkflowRequest copy() {
        ShardingWorkflowRequest result = copyFieldsTo(this, new ShardingWorkflowRequest());
        result.setRuleName(ruleName);
        result.setDataNodes(dataNodes);
        result.setStorageUnits(storageUnits);
        result.setStrategyType(strategyType);
        result.setShardingColumns(shardingColumns);
        result.setDefaultStrategyType(defaultStrategyType);
        result.setKeyGenerateColumn(keyGenerateColumn);
        result.setKeyGeneratorName(keyGeneratorName);
        result.setKeyGeneratorType(keyGeneratorType);
        result.setSequenceName(sequenceName);
        result.setKeyGenerateStrategyName(keyGenerateStrategyName);
        result.setComponentType(componentType);
        result.setComponentName(componentName);
        result.setAllowHintDisable(allowHintDisable);
        result.getReferenceTables().addAll(referenceTables);
        result.getAuditorNames().addAll(auditorNames);
        result.getKeyGeneratorProperties().putAll(keyGeneratorProperties);
        return result;
    }
    
    public void setRuleName(final String ruleName) {
        this.ruleName = normalizeShardingValue(ruleName);
    }
    
    public void setDataNodes(final String dataNodes) {
        this.dataNodes = normalizeShardingValue(dataNodes);
    }
    
    public void setStorageUnits(final String storageUnits) {
        this.storageUnits = normalizeShardingValue(storageUnits);
    }
    
    public void setStrategyType(final String strategyType) {
        this.strategyType = normalizeShardingValue(strategyType);
    }
    
    public void setShardingColumns(final String shardingColumns) {
        this.shardingColumns = normalizeShardingValue(shardingColumns);
    }
    
    public void setDefaultStrategyType(final String defaultStrategyType) {
        this.defaultStrategyType = normalizeShardingValue(defaultStrategyType);
    }
    
    public void setKeyGenerateColumn(final String keyGenerateColumn) {
        this.keyGenerateColumn = normalizeShardingValue(keyGenerateColumn);
    }
    
    public void setKeyGeneratorName(final String keyGeneratorName) {
        this.keyGeneratorName = normalizeShardingValue(keyGeneratorName);
    }
    
    public void setKeyGeneratorType(final String keyGeneratorType) {
        this.keyGeneratorType = normalizeShardingValue(keyGeneratorType);
    }
    
    public void setSequenceName(final String sequenceName) {
        this.sequenceName = normalizeShardingValue(sequenceName);
    }
    
    public void setKeyGenerateStrategyName(final String keyGenerateStrategyName) {
        this.keyGenerateStrategyName = normalizeShardingValue(keyGenerateStrategyName);
    }
    
    public void setComponentType(final String componentType) {
        this.componentType = normalizeShardingValue(componentType);
    }
    
    public void setComponentName(final String componentName) {
        this.componentName = normalizeShardingValue(componentName);
    }
    
    public void setAllowHintDisable(final String allowHintDisable) {
        this.allowHintDisable = normalizeShardingValue(allowHintDisable);
    }
    
    /**
     * Put primary sharding algorithm properties.
     *
     * @param props algorithm properties
     */
    public void putAlgorithmProperties(final Map<String, String> props) {
        getPrimaryAlgorithmProperties().putAll(props);
    }
    
    /**
     * Put sharding key generator properties.
     *
     * @param props key generator properties
     */
    public void putKeyGeneratorProperties(final Map<String, String> props) {
        keyGeneratorProperties.putAll(props);
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return "key_generator".equals(algorithmRole) ? keyGeneratorProperties : super.getAlgorithmProperties(algorithmRole);
    }
    
    private void overlayTo(final ShardingWorkflowRequest target) {
        super.overlayTo(target);
        target.setRuleName(resolveShardingValue(target.getRuleName(), ruleName));
        target.setDataNodes(resolveShardingValue(target.getDataNodes(), dataNodes));
        target.setStorageUnits(resolveShardingValue(target.getStorageUnits(), storageUnits));
        target.setStrategyType(resolveShardingValue(target.getStrategyType(), strategyType));
        target.setShardingColumns(resolveShardingValue(target.getShardingColumns(), shardingColumns));
        target.setDefaultStrategyType(resolveShardingValue(target.getDefaultStrategyType(), defaultStrategyType));
        target.setKeyGenerateColumn(resolveShardingValue(target.getKeyGenerateColumn(), keyGenerateColumn));
        target.setKeyGeneratorName(resolveShardingValue(target.getKeyGeneratorName(), keyGeneratorName));
        target.setKeyGeneratorType(resolveShardingValue(target.getKeyGeneratorType(), keyGeneratorType));
        target.setSequenceName(resolveShardingValue(target.getSequenceName(), sequenceName));
        target.setKeyGenerateStrategyName(resolveShardingValue(target.getKeyGenerateStrategyName(), keyGenerateStrategyName));
        target.setComponentType(resolveShardingValue(target.getComponentType(), componentType));
        target.setComponentName(resolveShardingValue(target.getComponentName(), componentName));
        target.setAllowHintDisable(resolveShardingValue(target.getAllowHintDisable(), allowHintDisable));
        if (!referenceTables.isEmpty()) {
            target.getReferenceTables().clear();
            target.getReferenceTables().addAll(referenceTables);
        }
        if (!auditorNames.isEmpty()) {
            target.getAuditorNames().clear();
            target.getAuditorNames().addAll(auditorNames);
        }
        target.getKeyGeneratorProperties().putAll(keyGeneratorProperties);
    }
    
    private String resolveShardingValue(final String previousValue, final String currentValue) {
        return currentValue.isEmpty() ? previousValue : currentValue;
    }
    
    private String normalizeShardingValue(final String value) {
        return null == value ? "" : value.trim();
    }
}
