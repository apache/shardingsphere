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

package org.apache.shardingsphere.mcp.support.workflow;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.List;
import java.util.Optional;

/**
 * Workflow session context.
 */
public interface WorkflowSessionContext {
    
    /**
     * Create plan identifier.
     *
     * @return plan identifier
     */
    String createPlanId();
    
    /**
     * Get an existing workflow snapshot or create a new one.
     *
     * @param sessionId session identifier
     * @param planId plan identifier
     * @return workflow snapshot
     */
    WorkflowContextSnapshot getOrCreate(String sessionId, String planId);
    
    /**
     * Save snapshot.
     *
     * @param snapshot workflow snapshot
     */
    void save(WorkflowContextSnapshot snapshot);
    
    /**
     * Find snapshot.
     *
     * @param planId plan identifier
     * @return snapshot when present
     */
    Optional<WorkflowContextSnapshot> find(String planId);
    
    /**
     * List snapshots owned by one MCP session.
     *
     * @param sessionId session identifier
     * @return workflow snapshots owned by the session
     */
    List<WorkflowContextSnapshot> list(String sessionId);
    
    /**
     * Persist snapshot with lifecycle state.
     *
     * @param snapshot workflow snapshot
     * @param currentStep current interaction step
     * @param status workflow status
     * @return persisted snapshot
     */
    WorkflowContextSnapshot persist(WorkflowContextSnapshot snapshot, String currentStep, String status);
    
    /**
     * Get required snapshot.
     *
     * @param planId plan identifier
     * @return workflow snapshot
     */
    WorkflowContextSnapshot getRequired(String planId);
    
    /**
     * Remove snapshot.
     *
     * @param planId plan identifier
     */
    void remove(String planId);
    
    /**
     * Remove snapshots owned by one MCP session.
     *
     * @param sessionId session identifier
     */
    void removeBySessionId(String sessionId);
}
