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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPDescriptorCatalogPayloadBuilder {
    
    private final MCPDescriptorCatalog catalog;
    
    static Map<String, Object> build(final MCPDescriptorCatalog catalog, final Collection<?> supportedStatements) {
        return new MCPDescriptorCatalogPayloadBuilder(catalog).build(supportedStatements);
    }
    
    private Map<String, Object> build(final Collection<?> supportedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        List<Map<String, Object>> completionTargets = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = catalog.getShardingSphereDescriptors().getResourceNavigationDescriptors().stream().map(this::toResourceNavigationPayload).toList();
        result.put("response_mode", MCPResponseMode.CATALOG);
        result.put("supportedStatementClasses", supportedStatements);
        result.put("completionTargets", completionTargets);
        result.put("resourceNavigation", resourceNavigation);
        return result;
    }
    
    private Map<String, Object> toCompletionTargetPayload(final MCPCompletionTargetDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("arguments", descriptor.getArguments());
        result.put("maxValues", descriptor.getMaxValues());
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toResourceNavigationPayload(final MCPResourceNavigationDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("from", descriptor.getFrom());
        result.put("from_type", resolveReferenceType(descriptor.getFrom()));
        result.put("to", descriptor.getTo());
        result.put("to_type", resolveReferenceType(descriptor.getTo()));
        result.put("requiredArguments", descriptor.getRequiredArguments());
        result.put("carriedArguments", descriptor.getCarriedArguments());
        result.put("description", descriptor.getDescription());
        return result;
    }
    
    private String resolveReferenceType(final String reference) {
        if (catalog.getProtocolDescriptors().getAllResourceDescriptors().stream().anyMatch(each -> each.getUriTemplate().equals(reference))) {
            return reference.contains("{") ? "resource_template" : "resource";
        }
        if (catalog.getProtocolDescriptors().getToolDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "tool";
        }
        if (catalog.getProtocolDescriptors().getPromptDescriptors().stream().anyMatch(each -> each.getName().equals(reference))) {
            return "prompt";
        }
        return "unknown";
    }
    
}
