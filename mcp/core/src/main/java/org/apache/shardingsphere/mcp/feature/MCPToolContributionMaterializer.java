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

package org.apache.shardingsphere.mcp.feature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectToolContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPWorkflowToolContribution;
import org.apache.shardingsphere.mcp.tool.handler.DelegatingToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowExecutionToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowValidationToolHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Materializer for tool contributions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPToolContributionMaterializer {
    
    static Collection<ToolHandler> materialize(final Collection<MCPToolContribution> contributions) {
        Collection<ToolHandler> result = new LinkedList<>();
        for (MCPToolContribution each : contributions) {
            if (each instanceof MCPDirectToolContribution) {
                result.add(materialize((MCPDirectToolContribution) each));
                continue;
            }
            if (each instanceof MCPWorkflowToolContribution) {
                result.addAll(materialize((MCPWorkflowToolContribution) each));
                continue;
            }
            throw new IllegalArgumentException(String.format("Unsupported tool contribution `%s`.", each.getClass().getName()));
        }
        return List.copyOf(result);
    }
    
    private static ToolHandler materialize(final MCPDirectToolContribution contribution) {
        return new DelegatingToolHandler(contribution.getToolDescriptor(), contribution.getToolInvoker());
    }
    
    private static Collection<ToolHandler> materialize(final MCPWorkflowToolContribution contribution) {
        Collection<ToolHandler> result = new LinkedList<>();
        result.add(new DelegatingToolHandler(contribution.getPlanningToolDescriptor(), contribution.getPlanningToolInvoker()));
        result.add(new WorkflowExecutionToolHandler(contribution.getApplyToolName()));
        result.add(new WorkflowValidationToolHandler(contribution.getValidateToolName(), contribution.getWorkflowValidationHandler()));
        return List.copyOf(result);
    }
}
