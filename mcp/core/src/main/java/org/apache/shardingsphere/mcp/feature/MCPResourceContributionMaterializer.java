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
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPResourceContribution;
import org.apache.shardingsphere.mcp.resource.handler.DelegatingResourceHandler;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Materializer for resource contributions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResourceContributionMaterializer {
    
    static Collection<ResourceHandler> materialize(final Collection<MCPResourceContribution> contributions) {
        Collection<ResourceHandler> result = new LinkedList<>();
        for (MCPResourceContribution each : contributions) {
            if (each instanceof MCPDirectResourceContribution) {
                MCPDirectResourceContribution resourceContribution = (MCPDirectResourceContribution) each;
                result.add(new DelegatingResourceHandler(resourceContribution.getUriPattern(), resourceContribution.getResourceReader()));
                continue;
            }
            throw new IllegalArgumentException(String.format("Unsupported resource contribution `%s`.", each.getClass().getName()));
        }
        return List.copyOf(result);
    }
}
