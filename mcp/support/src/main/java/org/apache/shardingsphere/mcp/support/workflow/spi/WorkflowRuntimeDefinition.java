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

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

/**
 * Workflow runtime definition.
 */
@Getter
public final class WorkflowRuntimeDefinition {
    
    private final WorkflowKind workflowKind;
    
    private final MCPWorkflowValidationHandler validationHandler;
    
    private final MCPWorkflowApplySynchronizationHandler applySynchronizationHandler;
    
    private final MCPWorkflowApplyArtifactValidator applyArtifactValidator;
    
    /**
     * Create workflow runtime definition.
     *
     * @param workflowKind workflow kind
     * @param validationHandler workflow validation handler
     * @param applySynchronizationHandler workflow apply synchronization handler
     */
    public WorkflowRuntimeDefinition(final WorkflowKind workflowKind, final MCPWorkflowValidationHandler validationHandler,
                                     final MCPWorkflowApplySynchronizationHandler applySynchronizationHandler) {
        this(workflowKind, validationHandler, applySynchronizationHandler, MCPWorkflowApplyArtifactValidator.NO_OP);
    }
    
    /**
     * Create workflow runtime definition.
     *
     * @param workflowKind workflow kind
     * @param validationHandler workflow validation handler
     * @param applySynchronizationHandler workflow apply synchronization handler
     * @param applyArtifactValidator workflow apply artifact validator
     */
    public WorkflowRuntimeDefinition(final WorkflowKind workflowKind, final MCPWorkflowValidationHandler validationHandler,
                                     final MCPWorkflowApplySynchronizationHandler applySynchronizationHandler, final MCPWorkflowApplyArtifactValidator applyArtifactValidator) {
        this.workflowKind = workflowKind;
        this.validationHandler = validationHandler;
        this.applySynchronizationHandler = applySynchronizationHandler;
        this.applyArtifactValidator = applyArtifactValidator;
    }
    
    /**
     * Create workflow runtime definition with one handler for validation and apply synchronization.
     *
     * @param workflowKind workflow kind
     * @param runtimeHandler workflow runtime handler
     */
    public WorkflowRuntimeDefinition(final WorkflowKind workflowKind, final MCPWorkflowRuntimeHandler runtimeHandler) {
        this(workflowKind, runtimeHandler, runtimeHandler, createApplyArtifactValidator(runtimeHandler));
    }
    
    private static MCPWorkflowApplyArtifactValidator createApplyArtifactValidator(final MCPWorkflowRuntimeHandler runtimeHandler) {
        return runtimeHandler instanceof MCPWorkflowApplyArtifactValidator ? (MCPWorkflowApplyArtifactValidator) runtimeHandler : MCPWorkflowApplyArtifactValidator.NO_OP;
    }
}
