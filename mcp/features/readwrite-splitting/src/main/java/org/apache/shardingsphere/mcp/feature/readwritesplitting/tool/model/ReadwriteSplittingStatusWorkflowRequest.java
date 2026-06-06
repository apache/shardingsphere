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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

/**
 * Readwrite-splitting storage-unit status workflow request.
 */
@Getter
public final class ReadwriteSplittingStatusWorkflowRequest extends WorkflowRequest {
    
    private String ruleName = "";
    
    private String storageUnit = "";
    
    private String targetStatus = "";
    
    /**
     * Set rule name.
     *
     * @param ruleName rule name
     */
    public void setRuleName(final String ruleName) {
        this.ruleName = null == ruleName ? "" : ruleName.trim();
    }
    
    /**
     * Set storage unit.
     *
     * @param storageUnit storage unit
     */
    public void setStorageUnit(final String storageUnit) {
        this.storageUnit = null == storageUnit ? "" : storageUnit.trim();
    }
    
    /**
     * Set target status.
     *
     * @param targetStatus target status
     */
    public void setTargetStatus(final String targetStatus) {
        this.targetStatus = null == targetStatus ? "" : targetStatus.trim();
    }
    
    @Override
    public ReadwriteSplittingStatusWorkflowRequest copy() {
        ReadwriteSplittingStatusWorkflowRequest result = copyTo(new ReadwriteSplittingStatusWorkflowRequest());
        result.setRuleName(ruleName);
        result.setStorageUnit(storageUnit);
        result.setTargetStatus(targetStatus);
        return result;
    }
    
    /**
     * Merge the current request with the previous request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current readwrite-splitting status request
     * @return merged request
     */
    public static ReadwriteSplittingStatusWorkflowRequest merge(final WorkflowRequest previousRequest, final ReadwriteSplittingStatusWorkflowRequest currentRequest) {
        if (null == previousRequest && null == currentRequest) {
            return null;
        }
        ReadwriteSplittingStatusWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            currentRequest.overlayFeatureFieldsTo(result);
        }
        return result;
    }
    
    private void overlayFeatureFieldsTo(final ReadwriteSplittingStatusWorkflowRequest target) {
        if (!ruleName.isEmpty()) {
            target.setRuleName(ruleName);
        }
        if (!storageUnit.isEmpty()) {
            target.setStorageUnit(storageUnit);
        }
        if (!targetStatus.isEmpty()) {
            target.setTargetStatus(targetStatus);
        }
    }
    
    private static ReadwriteSplittingStatusWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (null == previousRequest) {
            return new ReadwriteSplittingStatusWorkflowRequest();
        }
        if (previousRequest instanceof ReadwriteSplittingStatusWorkflowRequest) {
            return ((ReadwriteSplittingStatusWorkflowRequest) previousRequest).copy();
        }
        ReadwriteSplittingStatusWorkflowRequest result = new ReadwriteSplittingStatusWorkflowRequest();
        copyFieldsTo(previousRequest, result);
        return result;
    }
}
