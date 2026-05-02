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

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class WorkflowRuntimeDefinitionRegistryTest {
    
    @Test
    void assertLoadDefinitions() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class, withSettings().extraInterfaces(MCPWorkflowDefinitionProvider.class));
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = (MCPWorkflowDefinitionProvider) handlerProvider;
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(List.of(createDefinition("encrypt.rule")));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            Collection<WorkflowRuntimeDefinition> actual = WorkflowRuntimeDefinitionRegistry.loadDefinitions();
            assertThat(actual.size(), is(1));
            assertThat(actual.iterator().next().getWorkflowKind(), is(WorkflowKind.valueOf("encrypt.rule")));
            assertThrows(UnsupportedOperationException.class, actual::clear);
        }
    }
    
    @Test
    void assertLoadDefinitionsWithoutWorkflowDefinitionProvider() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            Collection<WorkflowRuntimeDefinition> actual = WorkflowRuntimeDefinitionRegistry.loadDefinitions();
            assertThat(actual.size(), is(0));
            assertThrows(UnsupportedOperationException.class, actual::clear);
        }
    }
    
    @Test
    void assertCreateDefinitionsWithNullDefinitions() {
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = mock(MCPWorkflowDefinitionProvider.class);
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> WorkflowRuntimeDefinitionRegistry.createDefinitions(workflowDefinitionProvider));
        assertThat(actual.getMessage(), is(String.format("Workflow definitions are required for `%s`.", workflowDefinitionProvider.getClass().getName())));
    }
    
    @Test
    void assertCreateDefinitionsWithNullDefinition() {
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = mock(MCPWorkflowDefinitionProvider.class);
        List<WorkflowRuntimeDefinition> definitions = new LinkedList<>();
        definitions.add(createDefinition("encrypt.rule"));
        definitions.add(null);
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(definitions);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> WorkflowRuntimeDefinitionRegistry.createDefinitions(workflowDefinitionProvider));
        assertThat(actual.getMessage(), is(String.format("Workflow definition is required for `%s`.", workflowDefinitionProvider.getClass().getName())));
    }
    
    @Test
    void assertCreateRegisteredDefinitionsWithNullWorkflowKind() {
        WorkflowRuntimeDefinition definition = new WorkflowRuntimeDefinition(null, mock(MCPWorkflowValidationHandler.class), mock(MCPWorkflowApplySynchronizationHandler.class));
        NullPointerException actual = assertThrows(NullPointerException.class, () -> WorkflowRuntimeDefinitionRegistry.createRegisteredDefinitions(List.of(definition)));
        assertThat(actual.getMessage(), is(String.format("Workflow kind is required for `%s`.", definition.getClass().getName())));
    }
    
    @Test
    void assertCreateRegisteredDefinitionsWithNullDefinition() {
        List<WorkflowRuntimeDefinition> definitions = new LinkedList<>();
        definitions.add(createDefinition("encrypt.rule"));
        definitions.add(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> WorkflowRuntimeDefinitionRegistry.createRegisteredDefinitions(definitions));
        assertThat(actual.getMessage(), is("Workflow definition is required."));
    }
    
    @Test
    void assertCreateRegisteredDefinitionsWithNullValidationHandler() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> WorkflowRuntimeDefinitionRegistry.createRegisteredDefinitions(
                List.of(new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), null, mock(MCPWorkflowApplySynchronizationHandler.class)))));
        assertThat(actual.getMessage(), is("Workflow validation handler is required for `encrypt.rule`."));
    }
    
    @Test
    void assertCreateRegisteredDefinitionsWithNullApplySynchronizationHandler() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> WorkflowRuntimeDefinitionRegistry.createRegisteredDefinitions(
                List.of(new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), mock(MCPWorkflowValidationHandler.class), null))));
        assertThat(actual.getMessage(), is("Workflow apply synchronization handler is required for `encrypt.rule`."));
    }
    
    @Test
    void assertCreateRegisteredDefinitionsWithDuplicateWorkflowKind() {
        WorkflowRuntimeDefinition firstDefinition = createDefinition("encrypt.rule");
        WorkflowRuntimeDefinition secondDefinition = createDefinition("encrypt.rule");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> WorkflowRuntimeDefinitionRegistry.createRegisteredDefinitions(List.of(firstDefinition, secondDefinition)));
        assertThat(actual.getMessage(), is(String.format("Duplicate workflow kind `encrypt.rule` with `%s` and `%s`.",
                firstDefinition.getClass().getName(), secondDefinition.getClass().getName())));
    }
    
    @Test
    void assertGetRequiredWithUnknownWorkflowKind() {
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule")));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> registry.getRequired(WorkflowKind.valueOf("mask.rule")));
        assertThat(actual.getMessage(), is("Unknown workflow_kind `mask.rule`."));
    }
    
    @Test
    void assertGetRegisteredDefinitions() {
        WorkflowRuntimeDefinition definition = createDefinition("encrypt.rule");
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(definition));
        assertThat(registry.getRegisteredDefinitions().size(), is(1));
        assertThat(registry.getRegisteredDefinitions().iterator().next(), is(definition));
    }
    
    private static WorkflowRuntimeDefinition createDefinition(final String workflowKind) {
        return new WorkflowRuntimeDefinition(WorkflowKind.valueOf(workflowKind), (workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, sessionId, snapshot) -> Map.of(),
                (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                });
    }
}
