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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

/**
 * Workflow lifecycle utility methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowLifecycleUtils {
    
    /**
     * Check whether the workflow snapshot belongs to the current session.
     *
     * @param sessionId session identifier
     * @param snapshot workflow snapshot
     * @return whether the snapshot belongs to the session
     */
    public static boolean isOwnedBySession(final String sessionId, final WorkflowContextSnapshot snapshot) {
        return sessionId.equals(snapshot.getSessionId());
    }
    
    /**
     * Resolve the current interaction step from the snapshot.
     *
     * @param snapshot workflow snapshot
     * @return current interaction step
     */
    public static String resolveCurrentStep(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getInteractionPlan() || null == snapshot.getInteractionPlan().getCurrentStep() ? "" : snapshot.getInteractionPlan().getCurrentStep();
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
    
    private static String resolveOperationType(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getClarifiedIntent() ? "" : snapshot.getClarifiedIntent().getOperationType();
    }
}
