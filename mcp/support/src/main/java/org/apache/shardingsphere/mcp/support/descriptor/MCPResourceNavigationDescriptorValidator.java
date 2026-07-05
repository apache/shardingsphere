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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for MCP resource navigation descriptors.
 */
public final class MCPResourceNavigationDescriptorValidator {
    
    private MCPResourceNavigationDescriptorValidator() {
    }
    
    /**
     * Validate resource navigation endpoints against public descriptor identifiers.
     *
     * @param descriptors resource navigation descriptors
     * @param catalog descriptor catalog
     */
    public static void validate(final Collection<MCPResourceNavigationDescriptor> descriptors, final MCPDescriptorCatalog catalog) {
        Set<String> publicIdentifiers = createPublicIdentifiers(catalog);
        Set<String> registered = new HashSet<>();
        for (MCPResourceNavigationDescriptor each : descriptors) {
            ShardingSpherePreconditions.checkState(publicIdentifiers.contains(each.getFrom()),
                    () -> new IllegalStateException(String.format("Resource navigation references unknown source `%s`.", each.getFrom())));
            ShardingSpherePreconditions.checkState(publicIdentifiers.contains(each.getTo()),
                    () -> new IllegalStateException(String.format("Resource navigation references unknown target `%s`.", each.getTo())));
            ShardingSpherePreconditions.checkState(registered.add(each.getFrom() + "->" + each.getTo()),
                    () -> new IllegalStateException(String.format("Duplicate MCP resource navigation `%s` to `%s`.", each.getFrom(), each.getTo())));
        }
    }
    
    private static Set<String> createPublicIdentifiers(final MCPDescriptorCatalog catalog) {
        Set<String> result = new HashSet<>();
        catalog.getProtocolDescriptors().getAllResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).forEach(result::add);
        catalog.getProtocolDescriptors().getToolDescriptors().stream().map(MCPToolDescriptor::getName).forEach(result::add);
        catalog.getProtocolDescriptors().getPromptDescriptors().stream().map(MCPPromptDescriptor::getName).forEach(result::add);
        return result;
    }
}
