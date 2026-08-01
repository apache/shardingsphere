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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;
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

class WorkflowValidationToolHandlerTest {
    
    @Test
    void assertGetToolDescriptor() {
        WorkflowValidationToolHandler handler = new WorkflowValidationToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(WorkflowHandlerTestFixture.createDefinition("encrypt.rule"))));
        MCPToolDescriptor actual = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(handler.getToolName());
        assertThat(actual.getName(), is("database_gateway_validate_workflow"));
    }
    
    @Test
    void assertHandle() {
        MCPWorkflowRuntimeHandler workflowRuntimeHandler = mock(MCPWorkflowRuntimeHandler.class);
        when(workflowRuntimeHandler.validate(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "validated"));
        WorkflowContextSnapshot snapshot = WorkflowHandlerTestFixture.createSnapshot();
        WorkflowHandlerTestFixture.Context fixture = WorkflowHandlerTestFixture.createContext(snapshot);
        WorkflowValidationToolHandler handler = new WorkflowValidationToolHandler(new WorkflowRuntimeDefinitionRegistry(List.of(
                WorkflowHandlerTestFixture.createDefinition("encrypt.rule", workflowRuntimeHandler, MCPWorkflowApplyArtifactValidator.NO_OP))));
        MCPSuccessPayload actual = handler.handle(fixture.requestContext(), Map.of("plan_id", "plan-1"));
        verify(workflowRuntimeHandler).validate(eq(fixture.workflowSessionContext()), eq(fixture.metadataQueryFacade()),
                eq(fixture.queryFacade()), eq(fixture.executionFacade()), eq("session-1"), eq(snapshot));
        assertThat(actual.toPayload().get("status"), is("validated"));
    }
}
