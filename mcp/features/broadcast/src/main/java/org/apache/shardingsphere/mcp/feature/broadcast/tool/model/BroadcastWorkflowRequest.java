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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Broadcast workflow request.
 */
@Getter
public final class BroadcastWorkflowRequest extends WorkflowRequest {
    
    private final List<String> tables = new LinkedList<>();
    
    /**
     * Set target broadcast tables from a comma-separated value.
     *
     * @param tables target broadcast tables
     */
    public void setTables(final String tables) {
        setTables(Arrays.stream(null == tables ? new String[0] : tables.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList());
    }
    
    /**
     * Set target broadcast tables.
     *
     * @param tables target broadcast tables
     */
    public void setTables(final Collection<String> tables) {
        if (null == tables || tables.isEmpty()) {
            return;
        }
        this.tables.clear();
        tables.stream().map(each -> null == each ? "" : each.trim()).filter(each -> !each.isEmpty()).forEach(this.tables::add);
    }
    
    /**
     * Get target broadcast tables from the dedicated list or the common table field.
     *
     * @return target broadcast tables
     */
    public List<String> getTargetTables() {
        return tables.isEmpty() && !getTable().isEmpty() ? List.of(getTable()) : tables;
    }
    
    @Override
    public BroadcastWorkflowRequest copy() {
        BroadcastWorkflowRequest result = copyTo(new BroadcastWorkflowRequest());
        result.tables.addAll(tables);
        return result;
    }
    
    /**
     * Merge the current request with the previous request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current broadcast workflow request
     * @return merged broadcast workflow request
     */
    public static BroadcastWorkflowRequest merge(final WorkflowRequest previousRequest, final BroadcastWorkflowRequest currentRequest) {
        if (null == previousRequest && null == currentRequest) {
            return null;
        }
        BroadcastWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            if (!currentRequest.tables.isEmpty()) {
                result.tables.clear();
                result.tables.addAll(currentRequest.tables);
            }
        }
        return result;
    }
    
    private static BroadcastWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (null == previousRequest) {
            return new BroadcastWorkflowRequest();
        }
        if (previousRequest instanceof BroadcastWorkflowRequest) {
            return ((BroadcastWorkflowRequest) previousRequest).copy();
        }
        BroadcastWorkflowRequest result = new BroadcastWorkflowRequest();
        copyFieldsTo(previousRequest, result);
        return result;
    }
}
