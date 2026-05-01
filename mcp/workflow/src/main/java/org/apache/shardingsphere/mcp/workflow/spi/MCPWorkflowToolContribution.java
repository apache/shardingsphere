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

package org.apache.shardingsphere.mcp.workflow.spi;

import lombok.Getter;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolInvoker;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;

import java.util.Objects;

/**
 * Workflow tool contribution owned by one feature.
 */
@Getter
public final class MCPWorkflowToolContribution implements MCPToolContribution {
    
    private final MCPToolDescriptor planningToolDescriptor;
    
    private final MCPToolInvoker planningToolInvoker;
    
    private final String applyToolName;
    
    private final String validateToolName;
    
    private final MCPWorkflowValidationHandler workflowValidationHandler;
    
    /**
     * Create workflow contribution with planning, execution and validation tools.
     *
     * @param planningToolDescriptor planning tool descriptor
     * @param planningToolInvoker planning tool invoker
     * @param applyToolName apply tool name
     * @param validateToolName validate tool name
     * @param workflowValidationHandler workflow validation handler
     */
    public MCPWorkflowToolContribution(final MCPToolDescriptor planningToolDescriptor, final MCPToolInvoker planningToolInvoker,
                                       final String applyToolName, final String validateToolName, final MCPWorkflowValidationHandler workflowValidationHandler) {
        this.planningToolDescriptor = Objects.requireNonNull(planningToolDescriptor, "planningToolDescriptor is required.");
        this.planningToolInvoker = Objects.requireNonNull(planningToolInvoker, "planningToolInvoker is required.");
        this.applyToolName = Objects.requireNonNull(applyToolName, "applyToolName is required.");
        this.validateToolName = Objects.requireNonNull(validateToolName, "validateToolName is required.");
        this.workflowValidationHandler = Objects.requireNonNull(workflowValidationHandler, "workflowValidationHandler is required.");
    }
}
