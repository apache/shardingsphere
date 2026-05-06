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
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowHandlersTest {
    
    @Test
    void assertGetExecutionToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule")))).getToolDescriptor();
        assertThat(actual.getName(), is("apply_workflow"));
    }
    
    @Test
    void assertHandleExecution() throws ReflectiveOperationException {
        WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
        when(executionService.apply(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "completed"));
        WorkflowContextSnapshot snapshot = createSnapshot();
        WorkflowContextFixture fixture = createWorkflowContextFixture(snapshot);
        MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler = mock(MCPWorkflowApplySynchronizationHandler.class);
        WorkflowExecutionToolHandler handler = createExecutionToolHandler(executionService, new WorkflowRuntimeDefinitionRegistry(List.of(
                new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), mock(MCPWorkflowValidationHandler.class), workflowApplySynchronizationHandler))));
        MCPResponse actual = handler.handle(fixture.workflowContext, new MCPToolCall("session-1",
                Map.of("plan_id", "plan-1", "approved_steps", List.of("ddl"), "execution_mode", "manual-only")));
        verify(executionService).apply(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), eq(fixture.executionFacade),
                eq(workflowApplySynchronizationHandler), eq("session-1"), eq(snapshot), eq(List.of("ddl")), eq("manual-only"));
        assertThat(actual.toPayload().get("status"), is("completed"));
    }
    
    @Test
    void assertHandleExecutionWithMissingWorkflowKind() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        WorkflowContextFixture fixture = createWorkflowContextFixture(snapshot);
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule"))));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> handler.handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("plan_id", "plan-1", "execution_mode", "manual-only"))));
        assertThat(actual.getMessage(), is("Workflow kind is required for plan_id `plan-1`."));
    }
    
    @Test
    void assertHandleExecutionWithMissingExecutionMode() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        WorkflowContextFixture fixture = createWorkflowContextFixture(snapshot);
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule"))));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> handler.handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("plan_id", "plan-1"))));
        assertThat(actual.getMessage(), is("apply_workflow execution_mode is required."));
    }
    
    @Test
    void assertGetValidationToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowValidationToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule")))).getToolDescriptor();
        assertThat(actual.getName(), is("validate_workflow"));
    }
    
    @Test
    void assertHandleValidation() {
        MCPWorkflowValidationHandler workflowValidationHandler = mock(MCPWorkflowValidationHandler.class);
        when(workflowValidationHandler.validate(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "validated"));
        WorkflowContextSnapshot snapshot = createSnapshot();
        WorkflowContextFixture fixture = createWorkflowContextFixture(snapshot);
        WorkflowValidationToolHandler handler = new WorkflowValidationToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(
                new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), workflowValidationHandler, mock(MCPWorkflowApplySynchronizationHandler.class)))));
        MCPResponse actual = handler.handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("plan_id", "plan-1")));
        verify(workflowValidationHandler).validate(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade),
                eq(fixture.queryFacade), eq(fixture.executionFacade), eq("session-1"), eq(snapshot));
        assertThat(actual.toPayload().get("status"), is("validated"));
    }
    
    private WorkflowExecutionToolHandler createExecutionToolHandler(final WorkflowExecutionService executionService,
                                                                    final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) throws ReflectiveOperationException {
        WorkflowExecutionToolHandler result = new WorkflowExecutionToolHandler(workflowRuntimeDefinitionRegistry);
        setField(result, "executionService", executionService);
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private WorkflowContextFixture createWorkflowContextFixture(final WorkflowContextSnapshot snapshot) {
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
        return new WorkflowContextFixture(result, workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        return result;
    }
    
    private static WorkflowRuntimeDefinition createDefinition(final String workflowKind) {
        return new WorkflowRuntimeDefinition(WorkflowKind.valueOf(workflowKind), (workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, sessionId, snapshot) -> Map.of(),
                (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                });
    }
    
    private record WorkflowContextFixture(MCPWorkflowHandlerContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade,
                                          MCPFeatureExecutionFacade executionFacade) {
    }
}
