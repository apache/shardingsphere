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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRequestMerger;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

/**
 * Encrypt workflow request merger.
 */
final class EncryptWorkflowRequestMerger {
    
    private final WorkflowRequestMerger requestMerger = new WorkflowRequestMerger();
    
    EncryptWorkflowRequest merge(final WorkflowRequest previousRequest, final EncryptWorkflowState previousState, final EncryptWorkflowRequest currentRequest) {
        EncryptWorkflowRequest result = requestMerger.merge(previousRequest, currentRequest, EncryptWorkflowRequest::new);
        result.setAllowIndexDDL(null == currentRequest.getAllowIndexDDL() ? null == previousState ? null : previousState.getAllowIndexDDL() : currentRequest.getAllowIndexDDL());
        result.setRequiresDecrypt(null == currentRequest.getRequiresDecrypt() ? null == previousState ? null : previousState.getRequiresDecrypt() : currentRequest.getRequiresDecrypt());
        result.setRequiresEqualityFilter(null == currentRequest.getRequiresEqualityFilter()
                ? null == previousState ? null : previousState.getRequiresEqualityFilter()
                : currentRequest.getRequiresEqualityFilter());
        result.setRequiresLikeQuery(null == currentRequest.getRequiresLikeQuery() ? null == previousState ? null : previousState.getRequiresLikeQuery() : currentRequest.getRequiresLikeQuery());
        result.setAssistedQueryAlgorithmType(mergeString(null == previousState ? null : previousState.getAssistedQueryAlgorithmType(), currentRequest.getAssistedQueryAlgorithmType()));
        result.setLikeQueryAlgorithmType(mergeString(null == previousState ? null : previousState.getLikeQueryAlgorithmType(), currentRequest.getLikeQueryAlgorithmType()));
        result.setCipherColumnName(mergeString(null == previousState ? null : previousState.getCipherColumnName(), currentRequest.getCipherColumnName()));
        result.setAssistedQueryColumnName(mergeString(null == previousState ? null : previousState.getAssistedQueryColumnName(), currentRequest.getAssistedQueryColumnName()));
        result.setLikeQueryColumnName(mergeString(null == previousState ? null : previousState.getLikeQueryColumnName(), currentRequest.getLikeQueryColumnName()));
        if (null != previousState) {
            result.getAssistedQueryAlgorithmProperties().putAll(previousState.getAssistedQueryAlgorithmProperties());
            result.getLikeQueryAlgorithmProperties().putAll(previousState.getLikeQueryAlgorithmProperties());
        }
        result.getAssistedQueryAlgorithmProperties().putAll(currentRequest.getAssistedQueryAlgorithmProperties());
        result.getLikeQueryAlgorithmProperties().putAll(currentRequest.getLikeQueryAlgorithmProperties());
        return result;
    }
    
    EncryptWorkflowState createState(final EncryptWorkflowRequest request) {
        EncryptWorkflowState result = new EncryptWorkflowState();
        result.setAllowIndexDDL(request.getAllowIndexDDL());
        result.setRequiresDecrypt(request.getRequiresDecrypt());
        result.setRequiresEqualityFilter(request.getRequiresEqualityFilter());
        result.setRequiresLikeQuery(request.getRequiresLikeQuery());
        result.setAssistedQueryAlgorithmType(request.getAssistedQueryAlgorithmType());
        result.setLikeQueryAlgorithmType(request.getLikeQueryAlgorithmType());
        result.setCipherColumnName(request.getCipherColumnName());
        result.setAssistedQueryColumnName(request.getAssistedQueryColumnName());
        result.setLikeQueryColumnName(request.getLikeQueryColumnName());
        result.getAssistedQueryAlgorithmProperties().putAll(request.getAssistedQueryAlgorithmProperties());
        result.getLikeQueryAlgorithmProperties().putAll(request.getLikeQueryAlgorithmProperties());
        return result;
    }
    
    private String mergeString(final String previous, final String current) {
        return WorkflowSqlUtils.trimToEmpty(current).isEmpty() ? previous : current;
    }
}
