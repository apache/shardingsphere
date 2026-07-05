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
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Validator for MCP resource descriptors.
 */
public final class MCPResourceDescriptorValidator {
    
    private MCPResourceDescriptorValidator() {
    }
    
    /**
     * Validate fixed and templated resources in one descriptor catalog.
     *
     * @param catalog descriptor catalog
     */
    public static void validate(final MCPDescriptorCatalog catalog) {
        int expectedSize = catalog.getProtocolDescriptors().getResourceDescriptors().size() + catalog.getProtocolDescriptors().getResourceTemplateDescriptors().size();
        Map<String, MCPResourceDescriptor> registered = new LinkedHashMap<>(expectedSize, 1F);
        for (MCPResourceDescriptor each : catalog.getProtocolDescriptors().getResourceDescriptors()) {
            checkNotBlank(each.getUriTemplate(), "Resource URI");
            ShardingSpherePreconditions.checkState(!each.isTemplated(),
                    () -> new IllegalStateException(String.format("Fixed resource `%s` must not contain template variables.", each.getUriTemplate())));
            validateResourceDescriptor(each, registered);
        }
        for (MCPResourceDescriptor each : catalog.getProtocolDescriptors().getResourceTemplateDescriptors()) {
            checkNotBlank(each.getUriTemplate(), "Resource template URI");
            ShardingSpherePreconditions.checkState(each.isTemplated(),
                    () -> new IllegalStateException(String.format("Resource template `%s` must contain template variables.", each.getUriTemplate())));
            validateResourceDescriptor(each, registered);
            validateResourceVariables(each, findShardingSphereResourceMetadata(catalog, each.getUriTemplate()).map(ShardingSphereMCPResourceMetadata::getUriVariables).orElse(List.of()));
        }
    }
    
    private static void validateResourceDescriptor(final MCPResourceDescriptor descriptor, final Map<String, MCPResourceDescriptor> registered) {
        ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(descriptor.getUriTemplate(), descriptor),
                () -> new IllegalStateException(String.format("Duplicate MCP resource descriptor `%s`.", descriptor.getUriTemplate())));
    }
    
    private static Optional<ShardingSphereMCPResourceMetadata> findShardingSphereResourceMetadata(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getShardingSphereDescriptors().getResourceMetadata().stream().filter(each -> uriTemplate.equals(each.getUriTemplate())).findFirst();
    }
    
    private static void validateResourceVariables(final MCPResourceDescriptor descriptor, final Collection<MCPUriVariableDescriptor> uriVariables) {
        List<String> templateVariables = new MCPUriTemplate(descriptor.getUriTemplate()).getVariableNames();
        Set<String> registeredTemplateVariables = new HashSet<>();
        for (String each : templateVariables) {
            ShardingSpherePreconditions.checkState(registeredTemplateVariables.add(each),
                    () -> new IllegalStateException(String.format("Duplicate URI template variable `%s` in resource descriptor `%s`.", each, descriptor.getUriTemplate())));
        }
        Map<String, MCPUriVariableDescriptor> declaredParameters = new LinkedHashMap<>(uriVariables.size(), 1F);
        for (MCPUriVariableDescriptor each : uriVariables) {
            ShardingSpherePreconditions.checkState(each.isRequired(), () -> new IllegalStateException(
                    String.format("Resource parameter `%s.%s` must be required because URI template variables are required.", descriptor.getUriTemplate(), each.getName())));
            ShardingSpherePreconditions.checkState(registeredTemplateVariables.contains(each.getName()),
                    () -> new IllegalStateException(String.format("Resource descriptor `%s` declares non-template parameter `%s`.", descriptor.getUriTemplate(), each.getName())));
            ShardingSpherePreconditions.checkState(null == declaredParameters.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP resource parameter `%s.%s`.", descriptor.getUriTemplate(), each.getName())));
        }
        for (String variableName : templateVariables) {
            ShardingSpherePreconditions.checkState(declaredParameters.containsKey(variableName),
                    () -> new IllegalStateException(String.format("Resource descriptor `%s` must describe URI template variable `%s`.", descriptor.getUriTemplate(), variableName)));
        }
    }
    
    private static void checkNotBlank(final String value, final String label) {
        ShardingSpherePreconditions.checkState(null != value && !value.isBlank(), () -> new IllegalStateException(String.format("%s is required.", label)));
    }
}
