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

import lombok.Getter;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt workflow request.
 */
@Getter
public final class EncryptWorkflowRequest extends WorkflowRequest {
    
    private final EncryptWorkflowOptions options = new EncryptWorkflowOptions();
    
    @Override
    public EncryptWorkflowRequest copy() {
        EncryptWorkflowRequest result = copyTo(new EncryptWorkflowRequest());
        options.copyTo(result.options);
        return result;
    }
    
    /**
     * Merge the current request with the previous request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current encrypt workflow request
     * @return merged encrypt workflow request
     */
    public static EncryptWorkflowRequest merge(final WorkflowRequest previousRequest, final EncryptWorkflowRequest currentRequest) {
        if (null == previousRequest && null == currentRequest) {
            return null;
        }
        EncryptWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            currentRequest.getOptions().overlayTo(result.options);
        }
        return result;
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        Map<String, String> result = options.getAlgorithmProperties(algorithmRole);
        return result.isEmpty() ? super.getAlgorithmProperties(algorithmRole) : result;
    }
    
    @Override
    public Map<String, Map<String, SecretReferenceValue>> getSecretReferences() {
        Map<String, Map<String, SecretReferenceValue>> result = new LinkedHashMap<>(3, 1F);
        appendSecretReferences(result, EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, super.getSecretReferences(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY));
        appendSecretReferences(result, EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY, options.getSecretReferences(EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY));
        appendSecretReferences(result, EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY, options.getSecretReferences(EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY));
        return result;
    }
    
    @Override
    public Map<String, SecretReferenceValue> getSecretReferences(final String algorithmRole) {
        Map<String, SecretReferenceValue> result = options.getSecretReferences(algorithmRole);
        return result.isEmpty() ? super.getSecretReferences(algorithmRole) : result;
    }
    
    private static EncryptWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (null == previousRequest) {
            return new EncryptWorkflowRequest();
        }
        if (previousRequest instanceof EncryptWorkflowRequest) {
            return ((EncryptWorkflowRequest) previousRequest).copy();
        }
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        copyFieldsTo(previousRequest, result);
        return result;
    }
    
    private void appendSecretReferences(final Map<String, Map<String, SecretReferenceValue>> target, final String algorithmRole,
                                        final Map<String, SecretReferenceValue> secretReferences) {
        if (!secretReferences.isEmpty()) {
            target.put(algorithmRole, secretReferences);
        }
    }
}
