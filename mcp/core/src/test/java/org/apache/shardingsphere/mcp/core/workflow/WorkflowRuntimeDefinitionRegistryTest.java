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
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class WorkflowRuntimeDefinitionRegistryTest {
    
    @Test
    void assertLoad() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class, withSettings().extraInterfaces(MCPWorkflowDefinitionProvider.class));
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = (MCPWorkflowDefinitionProvider) handlerProvider;
        WorkflowRuntimeDefinition definition = createDefinition("encrypt.rule");
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(List.of(definition));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            WorkflowRuntimeDefinitionRegistry registry = WorkflowRuntimeDefinitionRegistry.load();
            assertThat(registry.findRegisteredDefinition(WorkflowKind.valueOf("encrypt.rule")), is(Optional.of(definition)));
        }
    }
    
    @Test
    void assertLoadWithoutWorkflowDefinitionProvider() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            WorkflowRuntimeDefinitionRegistry registry = WorkflowRuntimeDefinitionRegistry.load();
            assertThat(registry.findRegisteredDefinition(WorkflowKind.valueOf("encrypt.rule")), is(Optional.empty()));
        }
    }
    
    @Test
    void assertLoadWithNullDefinitions() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class, withSettings().extraInterfaces(MCPWorkflowDefinitionProvider.class));
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = (MCPWorkflowDefinitionProvider) handlerProvider;
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(null);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            NullPointerException actual = assertThrows(NullPointerException.class, WorkflowRuntimeDefinitionRegistry::load);
            assertThat(actual.getMessage(), is(String.format("Workflow definitions are required for `%s`.", workflowDefinitionProvider.getClass().getName())));
        }
    }
    
    @Test
    void assertLoadWithNullDefinition() {
        MCPHandlerProvider handlerProvider = mock(MCPHandlerProvider.class, withSettings().extraInterfaces(MCPWorkflowDefinitionProvider.class));
        MCPWorkflowDefinitionProvider workflowDefinitionProvider = (MCPWorkflowDefinitionProvider) handlerProvider;
        List<WorkflowRuntimeDefinition> definitions = new LinkedList<>();
        definitions.add(createDefinition("encrypt.rule"));
        definitions.add(null);
        when(workflowDefinitionProvider.getWorkflowDefinitions()).thenReturn(definitions);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(handlerProvider));
            NullPointerException actual = assertThrows(NullPointerException.class, WorkflowRuntimeDefinitionRegistry::load);
            assertThat(actual.getMessage(), is(String.format("Workflow definition is required for `%s`.", workflowDefinitionProvider.getClass().getName())));
        }
    }
    
    @Test
    void assertCreateRegistryWithNullWorkflowKind() {
        WorkflowRuntimeDefinition definition = new WorkflowRuntimeDefinition(
                null, mock(MCPWorkflowValidationHandler.class), mock(MCPWorkflowApplySynchronizationHandler.class), MCPWorkflowApplyArtifactValidator.NO_OP);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> new WorkflowRuntimeDefinitionRegistry(List.of(definition)));
        assertThat(actual.getMessage(), is(String.format("Workflow kind is required for `%s`.", definition.getClass().getName())));
    }
    
    @Test
    void assertCreateRegistryWithNullDefinition() {
        List<WorkflowRuntimeDefinition> definitions = new LinkedList<>();
        definitions.add(createDefinition("encrypt.rule"));
        definitions.add(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> new WorkflowRuntimeDefinitionRegistry(definitions));
        assertThat(actual.getMessage(), is("Workflow definition is required."));
    }
    
    @Test
    void assertCreateRegistryWithNullValidationHandler() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new WorkflowRuntimeDefinitionRegistry(
                List.of(new WorkflowRuntimeDefinition(
                        WorkflowKind.valueOf("encrypt.rule"), null, mock(MCPWorkflowApplySynchronizationHandler.class), MCPWorkflowApplyArtifactValidator.NO_OP))));
        assertThat(actual.getMessage(), is("Workflow validation handler is required for `encrypt.rule`."));
    }
    
    @Test
    void assertCreateRegistryWithNullApplySynchronizationHandler() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new WorkflowRuntimeDefinitionRegistry(
                List.of(new WorkflowRuntimeDefinition(
                        WorkflowKind.valueOf("encrypt.rule"), mock(MCPWorkflowValidationHandler.class), null, MCPWorkflowApplyArtifactValidator.NO_OP))));
        assertThat(actual.getMessage(), is("Workflow apply synchronization handler is required for `encrypt.rule`."));
    }
    
    @Test
    void assertCreateRegistryWithNullApplyArtifactValidator() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new WorkflowRuntimeDefinitionRegistry(
                List.of(new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), mock(MCPWorkflowValidationHandler.class), mock(MCPWorkflowApplySynchronizationHandler.class), null))));
        assertThat(actual.getMessage(), is("Workflow apply artifact validator is required for `encrypt.rule`."));
    }
    
    @Test
    void assertCreateRegistryWithDuplicateWorkflowKind() {
        WorkflowRuntimeDefinition firstDefinition = createDefinition("encrypt.rule");
        WorkflowRuntimeDefinition secondDefinition = createDefinition("encrypt.rule");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new WorkflowRuntimeDefinitionRegistry(List.of(firstDefinition, secondDefinition)));
        assertThat(actual.getMessage(), is(String.format("Duplicate workflow kind `encrypt.rule` with `%s` and `%s`.",
                firstDefinition.getClass().getName(), secondDefinition.getClass().getName())));
    }
    
    @Test
    void assertFindRegisteredDefinition() {
        WorkflowRuntimeDefinition definition = createDefinition("encrypt.rule");
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(definition));
        Optional<WorkflowRuntimeDefinition> actual = registry.findRegisteredDefinition(WorkflowKind.valueOf("encrypt.rule"));
        assertThat(actual, is(Optional.of(definition)));
    }
    
    @Test
    void assertFindRegisteredDefinitionWithUnknownWorkflowKind() {
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule")));
        Optional<WorkflowRuntimeDefinition> actual = registry.findRegisteredDefinition(WorkflowKind.valueOf("mask.rule"));
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    void assertGetRequired() {
        WorkflowRuntimeDefinition definition = createDefinition("encrypt.rule");
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(definition));
        WorkflowRuntimeDefinition actual = registry.getRequired(WorkflowKind.valueOf("encrypt.rule"));
        assertThat(actual, is(definition));
    }
    
    @Test
    void assertGetRequiredWithUnknownWorkflowKind() {
        WorkflowRuntimeDefinitionRegistry registry = new WorkflowRuntimeDefinitionRegistry(List.of(createDefinition("encrypt.rule")));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> registry.getRequired(WorkflowKind.valueOf("mask.rule")));
        assertThat(actual.getMessage(), is("Unknown workflow_kind `mask.rule`."));
    }
    
    private static WorkflowRuntimeDefinition createDefinition(final String workflowKind) {
        return new WorkflowRuntimeDefinition(WorkflowKind.valueOf(workflowKind), (workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, sessionId, snapshot) -> Map.of(),
                (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                }, MCPWorkflowApplyArtifactValidator.NO_OP);
    }
}
