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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Workflow runtime definition registry.
 */
public final class WorkflowRuntimeDefinitionRegistry {
    
    private final Map<WorkflowKind, WorkflowRuntimeDefinition> registeredDefinitions;
    
    public WorkflowRuntimeDefinitionRegistry(final Collection<WorkflowRuntimeDefinition> definitions) {
        registeredDefinitions = createRegisteredDefinitions(definitions);
    }
    
    private static Map<WorkflowKind, WorkflowRuntimeDefinition> createRegisteredDefinitions(final Collection<WorkflowRuntimeDefinition> definitions) {
        Map<WorkflowKind, WorkflowRuntimeDefinition> result = new LinkedHashMap<>(Math.max(definitions.size(), 1), 1F);
        for (WorkflowRuntimeDefinition each : definitions) {
            Objects.requireNonNull(each, "Workflow definition is required.");
            WorkflowKind workflowKind = Objects.requireNonNull(each.getWorkflowKind(),
                    () -> String.format("Workflow kind is required for `%s`.", each.getClass().getName()));
            ShardingSpherePreconditions.checkNotNull(each.getValidationHandler(),
                    () -> new IllegalArgumentException(String.format("Workflow validation handler is required for `%s`.", workflowKind.getValue())));
            ShardingSpherePreconditions.checkNotNull(each.getApplySynchronizationHandler(),
                    () -> new IllegalArgumentException(String.format("Workflow apply synchronization handler is required for `%s`.", workflowKind.getValue())));
            ShardingSpherePreconditions.checkNotNull(each.getApplyArtifactValidator(),
                    () -> new IllegalArgumentException(String.format("Workflow apply artifact validator is required for `%s`.", workflowKind.getValue())));
            WorkflowRuntimeDefinition previousDefinition = result.putIfAbsent(workflowKind, each);
            ShardingSpherePreconditions.checkState(null == previousDefinition,
                    () -> new IllegalArgumentException(String.format("Duplicate workflow kind `%s` with `%s` and `%s`.",
                            workflowKind.getValue(), previousDefinition.getClass().getName(), each.getClass().getName())));
        }
        return result;
    }
    
    /**
     * Load workflow runtime definitions from providers.
     *
     * @return workflow runtime definition registry
     */
    public static WorkflowRuntimeDefinitionRegistry load() {
        return new WorkflowRuntimeDefinitionRegistry(loadDefinitions());
    }
    
    private static Collection<WorkflowRuntimeDefinition> loadDefinitions() {
        Collection<WorkflowRuntimeDefinition> result = new LinkedList<>();
        for (MCPHandlerProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)) {
            if (each instanceof MCPWorkflowDefinitionProvider) {
                result.addAll(createDefinitions((MCPWorkflowDefinitionProvider) each));
            }
        }
        return result;
    }
    
    private static Collection<WorkflowRuntimeDefinition> createDefinitions(final MCPWorkflowDefinitionProvider workflowDefinitionProvider) {
        Collection<WorkflowRuntimeDefinition> result = Objects.requireNonNull(workflowDefinitionProvider.getWorkflowDefinitions(),
                () -> String.format("Workflow definitions are required for `%s`.", workflowDefinitionProvider.getClass().getName()));
        result.forEach(each -> Objects.requireNonNull(each, () -> String.format("Workflow definition is required for `%s`.", workflowDefinitionProvider.getClass().getName())));
        return result;
    }
    
    /**
     * Find registered workflow runtime definition.
     *
     * @param workflowKind workflow kind
     * @return workflow runtime definition
     */
    public Optional<WorkflowRuntimeDefinition> findRegisteredDefinition(final WorkflowKind workflowKind) {
        return Optional.ofNullable(registeredDefinitions.get(workflowKind));
    }
    
    /**
     * Get required workflow runtime definition.
     *
     * @param workflowKind workflow kind
     * @return workflow runtime definition
     */
    public WorkflowRuntimeDefinition getRequired(final WorkflowKind workflowKind) {
        return findRegisteredDefinition(workflowKind).orElseThrow(() -> new MCPInvalidRequestException(String.format("Unknown workflow_kind `%s`.", workflowKind.getValue())));
    }
    
    /**
     * Get the workflow runtime definition required by a snapshot.
     *
     * @param snapshot workflow snapshot
     * @return workflow runtime definition
     */
    public WorkflowRuntimeDefinition getRequired(final WorkflowContextSnapshot snapshot) {
        ShardingSpherePreconditions.checkNotNull(snapshot.getWorkflowKind(),
                () -> new MCPWorkflowStateException(String.format("Workflow kind is required for plan_id `%s`.", snapshot.getPlanId()), snapshot.getPlanId()));
        return getRequired(snapshot.getWorkflowKind());
    }
}
