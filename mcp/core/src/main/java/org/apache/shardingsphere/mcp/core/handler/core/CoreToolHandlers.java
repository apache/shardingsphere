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

package org.apache.shardingsphere.mcp.core.handler.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ExecuteQueryToolHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ExecuteUpdateToolHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.ValidateProxyConnectivityToolHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.workflow.WorkflowExecutionToolHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.workflow.WorkflowValidationToolHandler;

import java.util.Collection;
import java.util.List;

/**
 * Core tool handlers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CoreToolHandlers {
    
    static Collection<MCPToolHandler<?>> createHandlers() {
        WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry = WorkflowRuntimeDefinitionRegistry.load();
        return List.of(
                new SearchMetadataToolHandler(),
                new ValidateProxyConnectivityToolHandler(),
                new ExecuteQueryToolHandler(),
                new ExecuteUpdateToolHandler(),
                new WorkflowExecutionToolHandler(workflowRuntimeDefinitionRegistry),
                new WorkflowValidationToolHandler(workflowRuntimeDefinitionRegistry));
    }
}
