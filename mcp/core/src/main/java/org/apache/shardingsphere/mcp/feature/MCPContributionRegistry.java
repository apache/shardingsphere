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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.feature.spi.MCPContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.feature.spi.MCPResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolContribution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MCP contribution registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPContributionRegistry {
    
    /**
     * Load contributions from MCP feature providers.
     *
     * @return contributions
     */
    public static Collection<MCPContribution> loadContributions() {
        Collection<MCPContribution> result = new LinkedList<>();
        for (MCPFeatureProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)) {
            result.addAll(createContributions(each));
        }
        return List.copyOf(result);
    }
    
    static Collection<MCPToolContribution> loadToolContributions() {
        return createToolContributions(loadContributions());
    }
    
    static Collection<MCPResourceContribution> loadResourceContributions() {
        return createResourceContributions(loadContributions());
    }
    
    static Collection<MCPContribution> createContributions(final MCPFeatureProvider featureProvider) {
        Collection<MCPContribution> contributions = Objects.requireNonNull(featureProvider.getContributions(),
                () -> String.format("Contributions are required for `%s`.", featureProvider.getClass().getName()));
        return List.copyOf(contributions);
    }
    
    static Collection<MCPToolContribution> createToolContributions(final MCPFeatureProvider featureProvider) {
        return createToolContributions(createContributions(featureProvider));
    }
    
    private static Collection<MCPToolContribution> createToolContributions(final Collection<MCPContribution> contributions) {
        Collection<MCPToolContribution> result = new LinkedList<>();
        for (MCPContribution each : contributions) {
            if (each instanceof MCPToolContribution) {
                result.add((MCPToolContribution) each);
            }
        }
        return List.copyOf(result);
    }
    
    static Collection<MCPResourceContribution> createResourceContributions(final MCPFeatureProvider featureProvider) {
        return createResourceContributions(createContributions(featureProvider));
    }
    
    private static Collection<MCPResourceContribution> createResourceContributions(final Collection<MCPContribution> contributions) {
        Collection<MCPResourceContribution> result = new LinkedList<>();
        for (MCPContribution each : contributions) {
            if (each instanceof MCPResourceContribution) {
                result.add((MCPResourceContribution) each);
            }
        }
        return List.copyOf(result);
    }
}
