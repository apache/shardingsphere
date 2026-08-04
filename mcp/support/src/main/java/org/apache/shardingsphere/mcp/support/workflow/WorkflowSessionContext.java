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

/**
 * Workflow context bound to one MCP session.
 */
public interface WorkflowSessionContext {
    
    /**
     * Get an existing workflow snapshot or create a new one.
     *
     * @param planId plan identifier
     * @return workflow snapshot
     */
    WorkflowContextSnapshot getOrCreate(String planId);
    
    /**
     * Save snapshot for the current MCP session.
     *
     * @param snapshot workflow snapshot
     */
    void save(WorkflowContextSnapshot snapshot);
    
    /**
     * List snapshots owned by the current MCP session.
     *
     * @return workflow snapshots owned by the current MCP session
     */
    List<WorkflowContextSnapshot> list();
    
    /**
     * Persist snapshot with lifecycle state for the current MCP session.
     *
     * @param snapshot workflow snapshot
     * @param currentStep current interaction step
     * @param status workflow status
     * @return persisted snapshot
     */
    WorkflowContextSnapshot persist(WorkflowContextSnapshot snapshot, String currentStep, String status);
    
    /**
     * Get required snapshot owned by the current MCP session.
     *
     * @param planId plan identifier
     * @return workflow snapshot
     */
    WorkflowContextSnapshot getRequired(String planId);
    
}
