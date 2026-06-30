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

package org.apache.shardingsphere.mcp.feature.shadow.tool.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shadow rule workflow request.
 */
@Getter
public final class ShadowRuleWorkflowRequest extends WorkflowRequest {
    
    private String ruleName = "";
    
    private String sourceStorageUnit = "";
    
    private String shadowStorageUnit = "";
    
    private String tableName = "";
    
    private String algorithmType = "";
    
    private final Map<String, String> algorithmProperties = new LinkedHashMap<>(8, 1F);
    
    /**
     * Set rule name.
     *
     * @param ruleName rule name
     */
    public void setRuleName(final String ruleName) {
        this.ruleName = null == ruleName ? "" : ruleName.trim();
    }
    
    /**
     * Set source storage unit.
     *
     * @param sourceStorageUnit source storage unit
     */
    public void setSourceStorageUnit(final String sourceStorageUnit) {
        this.sourceStorageUnit = null == sourceStorageUnit ? "" : sourceStorageUnit.trim();
    }
    
    /**
     * Set shadow storage unit.
     *
     * @param shadowStorageUnit shadow storage unit
     */
    public void setShadowStorageUnit(final String shadowStorageUnit) {
        this.shadowStorageUnit = null == shadowStorageUnit ? "" : shadowStorageUnit.trim();
    }
    
    /**
     * Set table name.
     *
     * @param tableName table name
     */
    public void setTableName(final String tableName) {
        this.tableName = null == tableName ? "" : tableName.trim();
        setTable(this.tableName);
    }
    
    /**
     * Set algorithm type.
     *
     * @param algorithmType algorithm type
     */
    public void setAlgorithmType(final String algorithmType) {
        this.algorithmType = null == algorithmType ? "" : algorithmType.trim();
    }
    
    /**
     * Put algorithm properties.
     *
     * @param properties algorithm properties
     */
    public void putAlgorithmProperties(final Map<String, String> properties) {
        if (null != properties) {
            algorithmProperties.putAll(properties);
        }
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return "primary".equals(algorithmRole) ? algorithmProperties : super.getAlgorithmProperties(algorithmRole);
    }
    
    @Override
    public ShadowRuleWorkflowRequest copy() {
        ShadowRuleWorkflowRequest result = copyTo(new ShadowRuleWorkflowRequest());
        result.setRuleName(ruleName);
        result.setSourceStorageUnit(sourceStorageUnit);
        result.setShadowStorageUnit(shadowStorageUnit);
        result.setTableName(tableName);
        result.setAlgorithmType(algorithmType);
        result.algorithmProperties.putAll(algorithmProperties);
        return result;
    }
    
    /**
     * Merge current request with previous workflow request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current request
     * @return merged request
     */
    public static ShadowRuleWorkflowRequest merge(final WorkflowRequest previousRequest, final ShadowRuleWorkflowRequest currentRequest) {
        ShadowRuleWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            currentRequest.overlayFeatureFieldsTo(result);
        }
        return result;
    }
    
    private void overlayFeatureFieldsTo(final ShadowRuleWorkflowRequest target) {
        if (!ruleName.isEmpty()) {
            target.setRuleName(ruleName);
        }
        if (!sourceStorageUnit.isEmpty()) {
            target.setSourceStorageUnit(sourceStorageUnit);
        }
        if (!shadowStorageUnit.isEmpty()) {
            target.setShadowStorageUnit(shadowStorageUnit);
        }
        if (!tableName.isEmpty()) {
            target.setTableName(tableName);
        }
        if (!algorithmType.isEmpty()) {
            target.setAlgorithmType(algorithmType);
        }
        target.algorithmProperties.putAll(algorithmProperties);
    }
    
    private static ShadowRuleWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (previousRequest instanceof ShadowRuleWorkflowRequest) {
            return ((ShadowRuleWorkflowRequest) previousRequest).copy();
        }
        ShadowRuleWorkflowRequest result = new ShadowRuleWorkflowRequest();
        if (null != previousRequest) {
            copyFieldsTo(previousRequest, result);
        }
        return result;
    }
}
