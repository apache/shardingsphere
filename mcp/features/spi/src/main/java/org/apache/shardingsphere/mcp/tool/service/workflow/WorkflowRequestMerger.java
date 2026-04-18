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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

/**
 * Workflow request merger.
 */
public final class WorkflowRequestMerger {
    
    /**
     * Merge the current workflow request into the previous snapshot.
     *
     * @param previous previous request
     * @param current current request
     * @return merged workflow request
     */
    public WorkflowRequest merge(final WorkflowRequest previous, final WorkflowRequest current) {
        if (null == previous) {
            return copy(current);
        }
        WorkflowRequest result = new WorkflowRequest();
        result.setPlanId(WorkflowSqlUtils.trimToEmpty(current.getPlanId()).isEmpty() ? previous.getPlanId() : current.getPlanId());
        result.setDatabase(mergeString(previous.getDatabase(), current.getDatabase()));
        result.setSchema(mergeString(previous.getSchema(), current.getSchema()));
        result.setTable(mergeString(previous.getTable(), current.getTable()));
        result.setColumn(mergeString(previous.getColumn(), current.getColumn()));
        result.setOperationType(mergeString(previous.getOperationType(), current.getOperationType()));
        result.setNaturalLanguageIntent(mergeString(previous.getNaturalLanguageIntent(), current.getNaturalLanguageIntent()));
        result.setFieldSemantics(mergeString(previous.getFieldSemantics(), current.getFieldSemantics()));
        result.setDeliveryMode(mergeString(previous.getDeliveryMode(), current.getDeliveryMode()));
        result.setExecutionMode(mergeString(previous.getExecutionMode(), current.getExecutionMode()));
        result.setAllowIndexDDL(null == current.getAllowIndexDDL() ? previous.getAllowIndexDDL() : current.getAllowIndexDDL());
        result.setRequiresDecrypt(null == current.getRequiresDecrypt() ? previous.getRequiresDecrypt() : current.getRequiresDecrypt());
        result.setRequiresEqualityFilter(null == current.getRequiresEqualityFilter() ? previous.getRequiresEqualityFilter() : current.getRequiresEqualityFilter());
        result.setRequiresLikeQuery(null == current.getRequiresLikeQuery() ? previous.getRequiresLikeQuery() : current.getRequiresLikeQuery());
        result.setAlgorithmType(mergeString(previous.getAlgorithmType(), current.getAlgorithmType()));
        result.setAssistedQueryAlgorithmType(mergeString(previous.getAssistedQueryAlgorithmType(), current.getAssistedQueryAlgorithmType()));
        result.setLikeQueryAlgorithmType(mergeString(previous.getLikeQueryAlgorithmType(), current.getLikeQueryAlgorithmType()));
        result.setCipherColumnName(mergeString(previous.getCipherColumnName(), current.getCipherColumnName()));
        result.setAssistedQueryColumnName(mergeString(previous.getAssistedQueryColumnName(), current.getAssistedQueryColumnName()));
        result.setLikeQueryColumnName(mergeString(previous.getLikeQueryColumnName(), current.getLikeQueryColumnName()));
        result.getPrimaryAlgorithmProperties().putAll(previous.getPrimaryAlgorithmProperties());
        result.getPrimaryAlgorithmProperties().putAll(current.getPrimaryAlgorithmProperties());
        result.getAssistedQueryAlgorithmProperties().putAll(previous.getAssistedQueryAlgorithmProperties());
        result.getAssistedQueryAlgorithmProperties().putAll(current.getAssistedQueryAlgorithmProperties());
        result.getLikeQueryAlgorithmProperties().putAll(previous.getLikeQueryAlgorithmProperties());
        result.getLikeQueryAlgorithmProperties().putAll(current.getLikeQueryAlgorithmProperties());
        result.getApprovedSteps().addAll(current.getApprovedSteps().isEmpty() ? previous.getApprovedSteps() : current.getApprovedSteps());
        return result;
    }
    
    /**
     * Create a defensive copy of a workflow request.
     *
     * @param original original request
     * @return copied workflow request
     */
    public WorkflowRequest copy(final WorkflowRequest original) {
        if (null == original) {
            return null;
        }
        WorkflowRequest result = new WorkflowRequest();
        result.setPlanId(original.getPlanId());
        result.setDatabase(original.getDatabase());
        result.setSchema(original.getSchema());
        result.setTable(original.getTable());
        result.setColumn(original.getColumn());
        result.setOperationType(original.getOperationType());
        result.setNaturalLanguageIntent(original.getNaturalLanguageIntent());
        result.setFieldSemantics(original.getFieldSemantics());
        result.setDeliveryMode(original.getDeliveryMode());
        result.setExecutionMode(original.getExecutionMode());
        result.setAllowIndexDDL(original.getAllowIndexDDL());
        result.setRequiresDecrypt(original.getRequiresDecrypt());
        result.setRequiresEqualityFilter(original.getRequiresEqualityFilter());
        result.setRequiresLikeQuery(original.getRequiresLikeQuery());
        result.setAlgorithmType(original.getAlgorithmType());
        result.setAssistedQueryAlgorithmType(original.getAssistedQueryAlgorithmType());
        result.setLikeQueryAlgorithmType(original.getLikeQueryAlgorithmType());
        result.setCipherColumnName(original.getCipherColumnName());
        result.setAssistedQueryColumnName(original.getAssistedQueryColumnName());
        result.setLikeQueryColumnName(original.getLikeQueryColumnName());
        result.getPrimaryAlgorithmProperties().putAll(original.getPrimaryAlgorithmProperties());
        result.getAssistedQueryAlgorithmProperties().putAll(original.getAssistedQueryAlgorithmProperties());
        result.getLikeQueryAlgorithmProperties().putAll(original.getLikeQueryAlgorithmProperties());
        result.getApprovedSteps().addAll(original.getApprovedSteps());
        return result;
    }
    
    private String mergeString(final String previous, final String current) {
        return WorkflowSqlUtils.trimToEmpty(current).isEmpty() ? previous : current;
    }
}
