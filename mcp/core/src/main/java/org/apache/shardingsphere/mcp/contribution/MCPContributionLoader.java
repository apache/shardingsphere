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

package org.apache.shardingsphere.mcp.contribution;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceContribution;
import org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider;
import org.apache.shardingsphere.mcp.api.tool.MCPToolContribution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MCP contribution loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPContributionLoader {
    
    /**
     * Load tool contributions from MCP contribution providers.
     *
     * @return tool contributions
     */
    public static Collection<MCPToolContribution> loadToolContributions() {
        Collection<MCPToolContribution> result = new LinkedList<>();
        for (MCPContributionProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPContributionProvider.class)) {
            result.addAll(createToolContributions(each));
        }
        return List.copyOf(result);
    }
    
    /**
     * Load resource contributions from MCP contribution providers.
     *
     * @return resource contributions
     */
    public static Collection<MCPResourceContribution> loadResourceContributions() {
        Collection<MCPResourceContribution> result = new LinkedList<>();
        for (MCPContributionProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPContributionProvider.class)) {
            result.addAll(createResourceContributions(each));
        }
        return List.copyOf(result);
    }
    
    static Collection<MCPToolContribution> createToolContributions(final MCPContributionProvider provider) {
        Collection<MCPToolContribution> contributions = Objects.requireNonNull(provider.getToolContributions(),
                () -> String.format("Tool contributions are required for `%s`.", provider.getClass().getName()));
        contributions.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Tool contribution is required for `%s`.", provider.getClass().getName())));
        return List.copyOf(contributions);
    }
    
    static Collection<MCPResourceContribution> createResourceContributions(final MCPContributionProvider provider) {
        Collection<MCPResourceContribution> contributions = Objects.requireNonNull(provider.getResourceContributions(),
                () -> String.format("Resource contributions are required for `%s`.", provider.getClass().getName()));
        contributions.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Resource contribution is required for `%s`.", provider.getClass().getName())));
        return List.copyOf(contributions);
    }
}
