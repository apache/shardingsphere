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

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sharding workflow request.
 */
public final class ShardingWorkflowRequest extends WorkflowRequest {
    
    private String ruleName = "";
    
    private String dataNodes = "";
    
    private String storageUnits = "";
    
    private String strategyType = "";
    
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
        if (!(previous instanceof ShardingWorkflowRequest)) {
            return current.copy();
        }
        ShardingWorkflowRequest result = ((ShardingWorkflowRequest) previous).copy();
        current.overlayTo(result);
        return result;
    }
    
    @Override
    public ShardingWorkflowRequest copy() {
        ShardingWorkflowRequest result = copyFieldsTo(this, new ShardingWorkflowRequest());
        result.setRuleName(ruleName);
        result.setDataNodes(dataNodes);
        result.setStorageUnits(storageUnits);
        result.setStrategyType(strategyType);
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
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(final String ruleName) {
        this.ruleName = normalize(ruleName);
    }
    
    public String getDataNodes() {
        return dataNodes;
    }
    
    public void setDataNodes(final String dataNodes) {
        this.dataNodes = normalize(dataNodes);
    }
    
    public String getStorageUnits() {
        return storageUnits;
    }
    
    public void setStorageUnits(final String storageUnits) {
        this.storageUnits = normalize(storageUnits);
    }
    
    public String getStrategyType() {
        return strategyType;
    }
    
    public void setStrategyType(final String strategyType) {
        this.strategyType = normalize(strategyType);
    }
    
    public String getDefaultStrategyType() {
        return defaultStrategyType;
    }
    
    public void setDefaultStrategyType(final String defaultStrategyType) {
        this.defaultStrategyType = normalize(defaultStrategyType);
    }
    
    public String getKeyGenerateColumn() {
        return keyGenerateColumn;
    }
    
    public void setKeyGenerateColumn(final String keyGenerateColumn) {
        this.keyGenerateColumn = normalize(keyGenerateColumn);
    }
    
    public String getKeyGeneratorName() {
        return keyGeneratorName;
    }
    
    public void setKeyGeneratorName(final String keyGeneratorName) {
        this.keyGeneratorName = normalize(keyGeneratorName);
    }
    
    public String getKeyGeneratorType() {
        return keyGeneratorType;
    }
    
    public void setKeyGeneratorType(final String keyGeneratorType) {
        this.keyGeneratorType = normalize(keyGeneratorType);
    }
    
    public String getSequenceName() {
        return sequenceName;
    }
    
    public void setSequenceName(final String sequenceName) {
        this.sequenceName = normalize(sequenceName);
    }
    
    public String getKeyGenerateStrategyName() {
        return keyGenerateStrategyName;
    }
    
    public void setKeyGenerateStrategyName(final String keyGenerateStrategyName) {
        this.keyGenerateStrategyName = normalize(keyGenerateStrategyName);
    }
    
    public String getComponentType() {
        return componentType;
    }
    
    public void setComponentType(final String componentType) {
        this.componentType = normalize(componentType);
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(final String componentName) {
        this.componentName = normalize(componentName);
    }
    
    public String getAllowHintDisable() {
        return allowHintDisable;
    }
    
    public void setAllowHintDisable(final String allowHintDisable) {
        this.allowHintDisable = normalize(allowHintDisable);
    }
    
    public List<String> getReferenceTables() {
        return referenceTables;
    }
    
    public List<String> getAuditorNames() {
        return auditorNames;
    }
    
    public Map<String, String> getKeyGeneratorProperties() {
        return keyGeneratorProperties;
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
    
    private void overlayTo(final ShardingWorkflowRequest target) {
        super.overlayTo(target);
        target.setRuleName(resolveValue(target.getRuleName(), ruleName));
        target.setDataNodes(resolveValue(target.getDataNodes(), dataNodes));
        target.setStorageUnits(resolveValue(target.getStorageUnits(), storageUnits));
        target.setStrategyType(resolveValue(target.getStrategyType(), strategyType));
        target.setDefaultStrategyType(resolveValue(target.getDefaultStrategyType(), defaultStrategyType));
        target.setKeyGenerateColumn(resolveValue(target.getKeyGenerateColumn(), keyGenerateColumn));
        target.setKeyGeneratorName(resolveValue(target.getKeyGeneratorName(), keyGeneratorName));
        target.setKeyGeneratorType(resolveValue(target.getKeyGeneratorType(), keyGeneratorType));
        target.setSequenceName(resolveValue(target.getSequenceName(), sequenceName));
        target.setKeyGenerateStrategyName(resolveValue(target.getKeyGenerateStrategyName(), keyGenerateStrategyName));
        target.setComponentType(resolveValue(target.getComponentType(), componentType));
        target.setComponentName(resolveValue(target.getComponentName(), componentName));
        target.setAllowHintDisable(resolveValue(target.getAllowHintDisable(), allowHintDisable));
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
    
    private String resolveValue(final String previousValue, final String currentValue) {
        return currentValue.isEmpty() ? previousValue : currentValue;
    }
    
    private String normalize(final String value) {
        return null == value ? "" : value.trim();
    }
}
