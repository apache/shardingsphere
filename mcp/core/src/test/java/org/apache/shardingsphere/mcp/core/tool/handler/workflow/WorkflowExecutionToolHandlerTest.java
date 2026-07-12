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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowExecutionToolHandlerTest {
    
    @Test
    void assertGetExecutionToolDescriptor() {
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(WorkflowHandlerTestFixture.createDefinition("encrypt.rule"))));
        MCPToolDescriptor actual = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(handler.getToolName());
        assertThat(actual.getName(), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertHandleExecution() {
        WorkflowContextSnapshot snapshot = WorkflowHandlerTestFixture.createSnapshot();
        WorkflowHandlerTestFixture.Context fixture = WorkflowHandlerTestFixture.createContext(snapshot);
        MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler = mock(MCPWorkflowApplySynchronizationHandler.class);
        MCPWorkflowApplyArtifactValidator workflowApplyArtifactValidator = mock(MCPWorkflowApplyArtifactValidator.class);
        try (
                MockedConstruction<WorkflowExecutionService> mockedExecutionServices = mockConstruction(WorkflowExecutionService.class,
                        (mock, context) -> when(mock.apply(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "completed")))) {
            WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(
                    WorkflowHandlerTestFixture.createDefinition("encrypt.rule", mock(MCPWorkflowValidationHandler.class), workflowApplySynchronizationHandler, workflowApplyArtifactValidator))));
            MCPResponse actual = handler.handle(fixture.workflowContext(), new MCPToolCall("session-1",
                    Map.of(WorkflowFieldNames.PLAN_ID, "plan-1", WorkflowFieldNames.APPROVED_STEPS, List.of("ddl"), WorkflowFieldNames.EXECUTION_MODE, "manual-only")));
            WorkflowExecutionService executionService = mockedExecutionServices.constructed().getFirst();
            verify(executionService).apply(eq(fixture.workflowSessionContext()), eq(fixture.metadataQueryFacade()), eq(fixture.queryFacade()), eq(fixture.executionFacade()),
                    eq(workflowApplySynchronizationHandler), eq(workflowApplyArtifactValidator), eq("session-1"), eq(snapshot), eq(List.of("ddl")), eq("manual-only"));
            assertThat(actual.toPayload().get("status"), is("completed"));
        }
    }
    
    @Test
    void assertHandleExecutionWithMissingWorkflowKind() {
        WorkflowContextSnapshot snapshot = WorkflowHandlerTestFixture.createSnapshotWithoutWorkflowKind();
        WorkflowHandlerTestFixture.Context fixture = WorkflowHandlerTestFixture.createContext(snapshot);
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(WorkflowHandlerTestFixture.createDefinition("encrypt.rule"))));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> handler.handle(fixture.workflowContext(), new MCPToolCall("session-1", Map.of(WorkflowFieldNames.PLAN_ID, "plan-1", WorkflowFieldNames.EXECUTION_MODE, "manual-only"))));
        assertThat(actual.getMessage(), is("Workflow kind is required for plan_id `plan-1`."));
    }
    
    @Test
    void assertHandleExecutionWithMissingExecutionMode() {
        WorkflowContextSnapshot snapshot = WorkflowHandlerTestFixture.createSnapshot();
        WorkflowHandlerTestFixture.Context fixture = WorkflowHandlerTestFixture.createContext(snapshot);
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(WorkflowHandlerTestFixture.createDefinition("encrypt.rule"))));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> handler.handle(fixture.workflowContext(), new MCPToolCall("session-1", Map.of(WorkflowFieldNames.PLAN_ID, "plan-1"))));
        assertThat(actual.getMessage(), is("database_gateway_apply_workflow execution_mode is required."));
    }
    
}
