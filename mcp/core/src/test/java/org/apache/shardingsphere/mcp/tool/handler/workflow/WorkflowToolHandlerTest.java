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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.tool.service.workflow.InMemoryWorkflowSessionContext;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSessionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowToolHandlerTest {
    
    @Test
    void assertGetExecutionToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowExecutionToolHandler("apply_encrypt_rule").getToolDescriptor();
        assertThat(actual.getName(), is("apply_encrypt_rule"));
    }
    
    @Test
    void assertHandleExecution() {
        WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
        when(executionService.apply(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "completed"));
        RequestContextFixture fixture = createRequestContextFixture();
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler("apply_encrypt_rule", executionService);
        MCPResponse actual = handler.handle(fixture.requestContext, "session-1", Map.of("plan_id", "plan-1", "approved_steps", List.of("ddl"), "execution_mode", "manual-only"));
        verify(executionService).apply(eq(fixture.workflowSessionContext), eq(fixture.executionFacade), eq("session-1"), eq("plan-1"), eq(List.of("ddl")), eq("manual-only"));
        assertThat(actual.toPayload().get("status"), is("completed"));
    }
    
    @Test
    void assertGetValidationToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowValidationToolHandler("validate_encrypt_rule",
                (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of()).getToolDescriptor();
        assertThat(actual.getName(), is("validate_encrypt_rule"));
    }
    
    @Test
    void assertHandleValidation() {
        MCPWorkflowValidationHandler workflowValidationHandler = mock(MCPWorkflowValidationHandler.class);
        when(workflowValidationHandler.validate(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "validated"));
        RequestContextFixture fixture = createRequestContextFixture();
        WorkflowValidationToolHandler handler = new WorkflowValidationToolHandler("validate_encrypt_rule", workflowValidationHandler);
        MCPResponse actual = handler.handle(fixture.requestContext, "session-1", Map.of("plan_id", "plan-1"));
        verify(workflowValidationHandler).validate(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade),
                eq(fixture.queryFacade), eq(fixture.executionFacade), eq("session-1"), eq("plan-1"));
        assertThat(actual.toPayload().get("status"), is("validated"));
    }
    
    private RequestContextFixture createRequestContextFixture() {
        MCPFeatureContext result = mock(MCPFeatureContext.class);
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        when(result.getExecutionFacade()).thenReturn(executionFacade);
        return new RequestContextFixture(result, workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    private record RequestContextFixture(MCPFeatureContext requestContext, WorkflowSessionContext workflowSessionContext,
                                         MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade,
                                         MCPFeatureExecutionFacade executionFacade) {
    }
}
