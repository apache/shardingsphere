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

package org.apache.shardingsphere.mcp.support.workflow.spi;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

/**
 * Workflow runtime handler for feature-specific validation of the state visible from ShardingSphere Proxy.
 *
 * <p>Implementations must be side-effect free and repeatable because core workflow execution may poll the report after applying artifacts.</p>
 */
@FunctionalInterface
public interface MCPWorkflowRuntimeHandler {
    
    /**
     * Validate the runtime state for one workflow plan.
     *
     * @param snapshot workflow snapshot
     * @param queryFacade direct query facade
     * @return feature-specific validation report
     */
    ValidationReport validate(WorkflowContextSnapshot snapshot, MCPFeatureQueryFacade queryFacade);
}
