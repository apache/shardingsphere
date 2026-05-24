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

package org.apache.shardingsphere.mcp.core.tool.handler.workflow;

import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class WorkflowHandlerTestFixture {
    
    private WorkflowHandlerTestFixture() {
    }
    
    static Context createContext(final WorkflowContextSnapshot snapshot) {
        MCPWorkflowHandlerContext result = mock(MCPWorkflowHandlerContext.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getDatabaseContext()).thenReturn(databaseContext);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        when(workflowSessionContext.getRequired("plan-1")).thenReturn(snapshot);
        return new Context(result, workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    static WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = createSnapshotWithoutWorkflowKind();
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        return result;
    }
    
    static WorkflowContextSnapshot createSnapshotWithoutWorkflowKind() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        return result;
    }
    
    static WorkflowRuntimeDefinition createDefinition(final String workflowKind) {
        return new WorkflowRuntimeDefinition(WorkflowKind.valueOf(workflowKind), (workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, sessionId, snapshot) -> Map.of(),
                (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                });
    }
    
    static WorkflowRuntimeDefinition createDefinition(final String workflowKind, final MCPWorkflowValidationHandler validationHandler,
                                                      final MCPWorkflowApplySynchronizationHandler applySynchronizationHandler) {
        return new WorkflowRuntimeDefinition(WorkflowKind.valueOf(workflowKind), validationHandler, applySynchronizationHandler);
    }
    
    record Context(MCPWorkflowHandlerContext workflowContext, WorkflowSessionContext workflowSessionContext,
                   MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade) {
    }
}
