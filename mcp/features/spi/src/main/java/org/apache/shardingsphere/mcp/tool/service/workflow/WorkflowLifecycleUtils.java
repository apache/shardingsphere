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

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;

/**
 * Workflow lifecycle utility methods.
 */
public final class WorkflowLifecycleUtils {
    
    private WorkflowLifecycleUtils() {
    }
    
    /**
     * Resolve the workflow context store for the current request.
     *
     * @param contextStore injected workflow context store
     * @param requestContextStore request context store
     * @return workflow context store
     */
    public static WorkflowContextStore resolveContextStore(final WorkflowContextStore contextStore, final WorkflowContextStore requestContextStore) {
        return null == contextStore ? requestContextStore : contextStore;
    }
    
    /**
     * Check whether the workflow snapshot belongs to the current session.
     *
     * @param sessionId session identifier
     * @param snapshot workflow snapshot
     * @return whether the snapshot belongs to the session
     */
    public static boolean isOwnedBySession(final String sessionId, final WorkflowContextSnapshot snapshot) {
        return WorkflowSqlUtils.trimToEmpty(snapshot.getSessionId()).isEmpty() || snapshot.getSessionId().equals(sessionId);
    }
    
    /**
     * Resolve the current interaction step from the snapshot.
     *
     * @param snapshot workflow snapshot
     * @return current interaction step
     */
    public static String resolveCurrentStep(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getInteractionPlan() ? "" : WorkflowSqlUtils.trimToEmpty(snapshot.getInteractionPlan().getCurrentStep());
    }
    
    /**
     * Resolve the workflow operation type from the snapshot.
     *
     * @param snapshot workflow snapshot
     * @return workflow operation type
     */
    public static String resolveOperationType(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getClarifiedIntent() ? "" : WorkflowSqlUtils.trimToEmpty(snapshot.getClarifiedIntent().getOperationType());
    }
    
    /**
     * Check whether the workflow is a drop operation.
     *
     * @param snapshot workflow snapshot
     * @return whether the workflow is a drop operation
     */
    public static boolean isDropWorkflow(final WorkflowContextSnapshot snapshot) {
        return WorkflowLifecycle.OPERATION_DROP.equalsIgnoreCase(resolveOperationType(snapshot));
    }
}
