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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP feature provider registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPFeatureProviderRegistry {
    
    private static final Map<String, MCPFeatureProvider> REGISTERED_PROVIDERS = createRegisteredProviders(ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class));
    
    static Map<String, MCPFeatureProvider> createRegisteredProviders(final Collection<MCPFeatureProvider> providers) {
        Map<String, MCPFeatureProvider> result = new LinkedHashMap<>(providers.size(), 1F);
        for (MCPFeatureProvider each : providers) {
            String featureType = String.valueOf(each.getType());
            ShardingSpherePreconditions.checkState(!featureType.isBlank(), () -> new IllegalArgumentException(
                    String.format("Feature type is required for `%s`.", each.getClass().getName())));
            ShardingSpherePreconditions.checkState(!each.getToolHandlers().isEmpty() || !each.getResourceHandlers().isEmpty(), () -> new IllegalArgumentException(
                    String.format("Feature provider `%s` must register at least one tool or resource.", each.getClass().getName())));
            MCPFeatureProvider previousProvider = result.putIfAbsent(featureType, each);
            ShardingSpherePreconditions.checkState(null == previousProvider, () -> new IllegalArgumentException(
                    String.format("Duplicate feature type `%s` with `%s` and `%s`.", featureType, previousProvider.getClass().getName(), each.getClass().getName())));
        }
        return result;
    }
    
    /**
     * Get registered feature providers.
     *
     * @return feature providers
     */
    public static Collection<MCPFeatureProvider> getRegisteredProviders() {
        return REGISTERED_PROVIDERS.values();
    }
}
