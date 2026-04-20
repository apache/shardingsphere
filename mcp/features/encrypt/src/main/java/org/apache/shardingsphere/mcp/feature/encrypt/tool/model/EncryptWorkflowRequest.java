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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.Map;

/**
 * Encrypt workflow request.
 */
public final class EncryptWorkflowRequest extends WorkflowRequest {
    
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final EncryptWorkflowOptions options = new EncryptWorkflowOptions();
    
    /**
     * Create a defensive copy of the encrypt workflow request.
     *
     * @return copied encrypt workflow request
     */
    @Override
    public EncryptWorkflowRequest copy() {
        EncryptWorkflowRequest result = copyTo(new EncryptWorkflowRequest());
        copyOptionsTo(result.options);
        return result;
    }
    
    /**
     * Merge the current request with the previous request and feature state.
     *
     * @param previousRequest previous workflow request
     * @param previousState previous encrypt workflow state
     * @param currentRequest current encrypt workflow request
     * @return merged encrypt workflow request
     */
    public static EncryptWorkflowRequest merge(final WorkflowRequest previousRequest,
                                               final EncryptWorkflowState previousState, final EncryptWorkflowRequest currentRequest) {
        if (null == previousRequest && null == previousState && null == currentRequest) {
            return null;
        }
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        if (null != previousRequest) {
            copyCommonFields(previousRequest, result);
        }
        if (null != previousState) {
            previousState.copyOptionsTo(result.options);
        }
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            currentRequest.overlayOptionsTo(result.options);
        }
        return result;
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        if ("assisted_query".equals(algorithmRole)) {
            return getAssistedQueryAlgorithmProperties();
        }
        if ("like_query".equals(algorithmRole)) {
            return getLikeQueryAlgorithmProperties();
        }
        return super.getAlgorithmProperties(algorithmRole);
    }
    
    /**
     * Get whether index DDL is allowed.
     *
     * @return whether index DDL is allowed
     */
    public Boolean getAllowIndexDDL() {
        return options.getAllowIndexDDL();
    }
    
    /**
     * Set whether index DDL is allowed.
     *
     * @param allowIndexDDL whether index DDL is allowed
     */
    public void setAllowIndexDDL(final Boolean allowIndexDDL) {
        options.setAllowIndexDDL(allowIndexDDL);
    }
    
    /**
     * Get whether decrypt capability is required.
     *
     * @return whether decrypt capability is required
     */
    public Boolean getRequiresDecrypt() {
        return options.getRequiresDecrypt();
    }
    
    /**
     * Set whether decrypt capability is required.
     *
     * @param requiresDecrypt whether decrypt capability is required
     */
    public void setRequiresDecrypt(final Boolean requiresDecrypt) {
        options.setRequiresDecrypt(requiresDecrypt);
    }
    
    /**
     * Get whether equality filtering is required.
     *
     * @return whether equality filtering is required
     */
    public Boolean getRequiresEqualityFilter() {
        return options.getRequiresEqualityFilter();
    }
    
    /**
     * Set whether equality filtering is required.
     *
     * @param requiresEqualityFilter whether equality filtering is required
     */
    public void setRequiresEqualityFilter(final Boolean requiresEqualityFilter) {
        options.setRequiresEqualityFilter(requiresEqualityFilter);
    }
    
    /**
     * Get whether like-query capability is required.
     *
     * @return whether like-query capability is required
     */
    public Boolean getRequiresLikeQuery() {
        return options.getRequiresLikeQuery();
    }
    
    /**
     * Set whether like-query capability is required.
     *
     * @param requiresLikeQuery whether like-query capability is required
     */
    public void setRequiresLikeQuery(final Boolean requiresLikeQuery) {
        options.setRequiresLikeQuery(requiresLikeQuery);
    }
    
    /**
     * Get the assisted-query algorithm type.
     *
     * @return assisted-query algorithm type
     */
    public String getAssistedQueryAlgorithmType() {
        return options.getAssistedQueryAlgorithmType();
    }
    
    /**
     * Set the assisted-query algorithm type.
     *
     * @param assistedQueryAlgorithmType assisted-query algorithm type
     */
    public void setAssistedQueryAlgorithmType(final String assistedQueryAlgorithmType) {
        options.setAssistedQueryAlgorithmType(assistedQueryAlgorithmType);
    }
    
    /**
     * Get the like-query algorithm type.
     *
     * @return like-query algorithm type
     */
    public String getLikeQueryAlgorithmType() {
        return options.getLikeQueryAlgorithmType();
    }
    
    /**
     * Set the like-query algorithm type.
     *
     * @param likeQueryAlgorithmType like-query algorithm type
     */
    public void setLikeQueryAlgorithmType(final String likeQueryAlgorithmType) {
        options.setLikeQueryAlgorithmType(likeQueryAlgorithmType);
    }
    
    /**
     * Get the cipher column name.
     *
     * @return cipher column name
     */
    public String getCipherColumnName() {
        return options.getCipherColumnName();
    }
    
    /**
     * Set the cipher column name.
     *
     * @param cipherColumnName cipher column name
     */
    public void setCipherColumnName(final String cipherColumnName) {
        options.setCipherColumnName(cipherColumnName);
    }
    
    /**
     * Get the assisted-query column name.
     *
     * @return assisted-query column name
     */
    public String getAssistedQueryColumnName() {
        return options.getAssistedQueryColumnName();
    }
    
    /**
     * Set the assisted-query column name.
     *
     * @param assistedQueryColumnName assisted-query column name
     */
    public void setAssistedQueryColumnName(final String assistedQueryColumnName) {
        options.setAssistedQueryColumnName(assistedQueryColumnName);
    }
    
    /**
     * Get the like-query column name.
     *
     * @return like-query column name
     */
    public String getLikeQueryColumnName() {
        return options.getLikeQueryColumnName();
    }
    
    /**
     * Set the like-query column name.
     *
     * @param likeQueryColumnName like-query column name
     */
    public void setLikeQueryColumnName(final String likeQueryColumnName) {
        options.setLikeQueryColumnName(likeQueryColumnName);
    }
    
    /**
     * Get the assisted-query algorithm properties.
     *
     * @return assisted-query algorithm properties
     */
    public Map<String, String> getAssistedQueryAlgorithmProperties() {
        return options.getAssistedQueryAlgorithmProperties();
    }
    
    /**
     * Get the like-query algorithm properties.
     *
     * @return like-query algorithm properties
     */
    public Map<String, String> getLikeQueryAlgorithmProperties() {
        return options.getLikeQueryAlgorithmProperties();
    }
    
    void copyOptionsTo(final EncryptWorkflowOptions target) {
        options.copyTo(target);
    }
    
    private void overlayOptionsTo(final EncryptWorkflowOptions target) {
        options.overlayTo(target);
    }
    
    private static void copyCommonFields(final WorkflowRequest source, final EncryptWorkflowRequest target) {
        target.setPlanId(source.getPlanId());
        target.setDatabase(source.getDatabase());
        target.setSchema(source.getSchema());
        target.setTable(source.getTable());
        target.setColumn(source.getColumn());
        target.setOperationType(source.getOperationType());
        target.setNaturalLanguageIntent(source.getNaturalLanguageIntent());
        target.setFieldSemantics(source.getFieldSemantics());
        target.setDeliveryMode(source.getDeliveryMode());
        target.setExecutionMode(source.getExecutionMode());
        target.setAlgorithmType(source.getAlgorithmType());
        target.getPrimaryAlgorithmProperties().putAll(source.getPrimaryAlgorithmProperties());
        target.getApprovedSteps().addAll(source.getApprovedSteps());
    }
}
