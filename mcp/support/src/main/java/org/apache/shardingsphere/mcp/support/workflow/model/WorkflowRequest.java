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

package org.apache.shardingsphere.mcp.support.workflow.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow request.
 */
@Getter
public class WorkflowRequest implements WorkflowPropertySource {
    
    private String planId = "";
    
    private String database = "";
    
    private String schema = "";
    
    private String table = "";
    
    private String column = "";
    
    private String operationType = "";
    
    private String naturalLanguageIntent = "";
    
    private String fieldSemantics = "";
    
    private String deliveryMode = "all-at-once";
    
    private String executionMode = "review-then-execute";
    
    private String algorithmType = "";
    
    private final Map<String, String> primaryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    private final Map<String, SecretReferenceValue> primaryAlgorithmSecretReferences = new LinkedHashMap<>(8, 1F);
    
    private final List<String> approvedSteps = new LinkedList<>();
    
    /**
     * Create a defensive copy of the workflow request.
     *
     * @return copied workflow request
     */
    public WorkflowRequest copy() {
        return copyTo(new WorkflowRequest());
    }
    
    public void setPlanId(final String planId) {
        this.planId = normalize(planId);
    }
    
    public void setDatabase(final String database) {
        this.database = normalize(database);
    }
    
    public void setSchema(final String schema) {
        this.schema = normalize(schema);
    }
    
    public void setTable(final String table) {
        this.table = normalize(table);
    }
    
    public void setColumn(final String column) {
        this.column = normalize(column);
    }
    
    public void setOperationType(final String operationType) {
        this.operationType = normalize(operationType);
    }
    
    public void setNaturalLanguageIntent(final String naturalLanguageIntent) {
        this.naturalLanguageIntent = normalize(naturalLanguageIntent);
    }
    
    public void setFieldSemantics(final String fieldSemantics) {
        this.fieldSemantics = normalize(fieldSemantics);
    }
    
    public void setDeliveryMode(final String deliveryMode) {
        this.deliveryMode = normalize(deliveryMode);
    }
    
    public void setExecutionMode(final String executionMode) {
        this.executionMode = normalize(executionMode);
    }
    
    public void setAlgorithmType(final String algorithmType) {
        this.algorithmType = normalize(algorithmType);
    }
    
    /**
     * Merge the current workflow request into the previous request.
     *
     * @param previous previous workflow request
     * @param current current workflow request
     * @return merged workflow request
     */
    public static WorkflowRequest merge(final WorkflowRequest previous, final WorkflowRequest current) {
        if (null == previous) {
            return current.copy();
        }
        WorkflowRequest result = previous.copy();
        current.overlayTo(result);
        return result;
    }
    
    /**
     * Copy workflow request fields to the target request.
     *
     * @param source source request
     * @param target target request
     * @param <T> target request type
     * @return target request
     */
    public static <T extends WorkflowRequest> T copyFieldsTo(final WorkflowRequest source, final T target) {
        return null == source ? target : source.copyTo(target);
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return "primary".equals(algorithmRole) ? primaryAlgorithmProperties : Map.of();
    }
    
    @Override
    public Map<String, Map<String, SecretReferenceValue>> getSecretReferences() {
        if (primaryAlgorithmSecretReferences.isEmpty()) {
            return Map.of();
        }
        Map<String, Map<String, SecretReferenceValue>> result = new LinkedHashMap<>(1, 1F);
        result.put("primary", primaryAlgorithmSecretReferences);
        return result;
    }
    
    @Override
    public Map<String, SecretReferenceValue> getSecretReferences(final String algorithmRole) {
        return "primary".equals(algorithmRole) ? primaryAlgorithmSecretReferences : Map.of();
    }
    
    /**
     * Copy current request values to the target request.
     *
     * @param target target request
     * @param <T> target request type
     * @return target request
     */
    protected final <T extends WorkflowRequest> T copyTo(final T target) {
        target.setPlanId(planId);
        target.setDatabase(database);
        target.setSchema(schema);
        target.setTable(table);
        target.setColumn(column);
        target.setOperationType(operationType);
        target.setNaturalLanguageIntent(naturalLanguageIntent);
        target.setFieldSemantics(fieldSemantics);
        target.setDeliveryMode(deliveryMode);
        target.setExecutionMode(executionMode);
        target.setAlgorithmType(algorithmType);
        target.getPrimaryAlgorithmProperties().putAll(primaryAlgorithmProperties);
        target.getPrimaryAlgorithmSecretReferences().putAll(primaryAlgorithmSecretReferences);
        target.getApprovedSteps().addAll(approvedSteps);
        return target;
    }
    
    /**
     * Overlay current non-empty values onto the target request.
     *
     * @param target target request
     */
    protected final void overlayTo(final WorkflowRequest target) {
        if (hasText(planId)) {
            target.setPlanId(planId);
        }
        target.setDatabase(resolveValue(target.getDatabase(), database));
        target.setSchema(resolveValue(target.getSchema(), schema));
        target.setTable(resolveValue(target.getTable(), table));
        target.setColumn(resolveValue(target.getColumn(), column));
        target.setOperationType(resolveValue(target.getOperationType(), operationType));
        target.setNaturalLanguageIntent(resolveValue(target.getNaturalLanguageIntent(), naturalLanguageIntent));
        target.setFieldSemantics(resolveValue(target.getFieldSemantics(), fieldSemantics));
        target.setDeliveryMode(resolveValue(target.getDeliveryMode(), deliveryMode));
        target.setExecutionMode(resolveValue(target.getExecutionMode(), executionMode));
        target.setAlgorithmType(resolveValue(target.getAlgorithmType(), algorithmType));
        target.getPrimaryAlgorithmProperties().putAll(primaryAlgorithmProperties);
        target.getPrimaryAlgorithmSecretReferences().putAll(primaryAlgorithmSecretReferences);
        if (!approvedSteps.isEmpty()) {
            target.getApprovedSteps().clear();
            target.getApprovedSteps().addAll(approvedSteps);
        }
    }
    
    private String resolveValue(final String previousValue, final String currentValue) {
        return hasText(currentValue) ? currentValue : previousValue;
    }
    
    private static boolean hasText(final String value) {
        return !value.isEmpty();
    }
    
    private static String normalize(final String value) {
        return null == value ? "" : value.trim();
    }
}
