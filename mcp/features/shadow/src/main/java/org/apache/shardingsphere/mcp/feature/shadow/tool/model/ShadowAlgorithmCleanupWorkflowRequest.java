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

/**
 * Shadow algorithm cleanup workflow request.
 */
@Getter
public final class ShadowAlgorithmCleanupWorkflowRequest extends WorkflowRequest {
    
    private String algorithmName = "";
    
    /**
     * Set algorithm name.
     *
     * @param algorithmName algorithm name
     */
    public void setAlgorithmName(final String algorithmName) {
        this.algorithmName = null == algorithmName ? "" : algorithmName.trim();
    }
    
    @Override
    public ShadowAlgorithmCleanupWorkflowRequest copy() {
        ShadowAlgorithmCleanupWorkflowRequest result = copyTo(new ShadowAlgorithmCleanupWorkflowRequest());
        result.setAlgorithmName(algorithmName);
        return result;
    }
    
    /**
     * Merge current request with previous workflow request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current request
     * @return merged request
     */
    public static ShadowAlgorithmCleanupWorkflowRequest merge(final WorkflowRequest previousRequest, final ShadowAlgorithmCleanupWorkflowRequest currentRequest) {
        ShadowAlgorithmCleanupWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            if (!currentRequest.algorithmName.isEmpty()) {
                result.setAlgorithmName(currentRequest.algorithmName);
            }
        }
        return result;
    }
    
    private static ShadowAlgorithmCleanupWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (previousRequest instanceof ShadowAlgorithmCleanupWorkflowRequest) {
            return ((ShadowAlgorithmCleanupWorkflowRequest) previousRequest).copy();
        }
        ShadowAlgorithmCleanupWorkflowRequest result = new ShadowAlgorithmCleanupWorkflowRequest();
        if (null != previousRequest) {
            copyFieldsTo(previousRequest, result);
        }
        return result;
    }
}
