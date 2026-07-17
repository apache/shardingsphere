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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

import java.util.function.BiFunction;
import java.util.function.Function;

@Getter(AccessLevel.PACKAGE)
final class ShardingWorkflowLifecycleSpec {
    
    private final WorkflowKind workflowKind;
    
    private final String defaultOperationType;
    
    private final String summary;
    
    private final Function<ShardingWorkflowRequest, Boolean> existsSupplier;
    
    private final BiFunction<ShardingWorkflowRequest, WorkflowContextSnapshot, Boolean> requiredInputSupplier;
    
    private final BiFunction<ShardingWorkflowRequest, WorkflowContextSnapshot, Boolean> algorithmPlanSupplier;
    
    private final Function<ShardingWorkflowRequest, RuleArtifact> artifactSupplier;
    
    ShardingWorkflowLifecycleSpec(final WorkflowKind workflowKind, final String defaultOperationType, final String summary,
                                  final Function<ShardingWorkflowRequest, Boolean> existsSupplier,
                                  final BiFunction<ShardingWorkflowRequest, WorkflowContextSnapshot, Boolean> requiredInputSupplier,
                                  final BiFunction<ShardingWorkflowRequest, WorkflowContextSnapshot, Boolean> algorithmPlanSupplier,
                                  final Function<ShardingWorkflowRequest, RuleArtifact> artifactSupplier) {
        this.workflowKind = workflowKind;
        this.defaultOperationType = defaultOperationType;
        this.summary = summary;
        this.existsSupplier = existsSupplier;
        this.requiredInputSupplier = requiredInputSupplier;
        this.algorithmPlanSupplier = algorithmPlanSupplier;
        this.artifactSupplier = artifactSupplier;
    }
    
}
