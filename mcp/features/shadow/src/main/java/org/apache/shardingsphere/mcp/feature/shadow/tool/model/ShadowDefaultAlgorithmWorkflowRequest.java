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
 * Default shadow algorithm workflow request.
 */
@Getter
public final class ShadowDefaultAlgorithmWorkflowRequest extends WorkflowRequest {
    
    private String algorithmType = "";
    
    private final Map<String, String> algorithmProperties = new LinkedHashMap<>(8, 1F);
    
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
    public ShadowDefaultAlgorithmWorkflowRequest copy() {
        ShadowDefaultAlgorithmWorkflowRequest result = copyTo(new ShadowDefaultAlgorithmWorkflowRequest());
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
    public static ShadowDefaultAlgorithmWorkflowRequest merge(final WorkflowRequest previousRequest, final ShadowDefaultAlgorithmWorkflowRequest currentRequest) {
        ShadowDefaultAlgorithmWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            if (!currentRequest.algorithmType.isEmpty()) {
                result.setAlgorithmType(currentRequest.algorithmType);
            }
            result.algorithmProperties.putAll(currentRequest.algorithmProperties);
        }
        return result;
    }
    
    private static ShadowDefaultAlgorithmWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (previousRequest instanceof ShadowDefaultAlgorithmWorkflowRequest) {
            return ((ShadowDefaultAlgorithmWorkflowRequest) previousRequest).copy();
        }
        ShadowDefaultAlgorithmWorkflowRequest result = new ShadowDefaultAlgorithmWorkflowRequest();
        if (null != previousRequest) {
            copyFieldsTo(previousRequest, result);
        }
        return result;
    }
}
