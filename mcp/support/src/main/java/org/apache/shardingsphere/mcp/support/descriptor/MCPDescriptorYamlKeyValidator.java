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
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPDescriptorYamlKeyValidator {
    
    private static final Collection<String> ROOT_KEYS = Set.of("resources", "resourceTemplates", "tools", "prompts", "completionTargets", "resourceNavigation");
    
    private static final Collection<String> RESOURCE_KEYS = Set.of("uri", "name", "title", "description", "icons", "mimeType", "annotations", "meta", "extension");
    
    private static final Collection<String> RESOURCE_TEMPLATE_KEYS = Set.of("uriTemplate", "name", "title", "description", "icons", "mimeType", "annotations", "meta", "extension");
    
    private static final Collection<String> ICON_KEYS = Set.of("src", "mimeType", "sizes", "theme");
    
    private static final Collection<String> RESOURCE_EXTENSION_KEYS = Set.of("uriVariables", "resourceKind", "objectScope", "feature", "relatedTools", "relatedResources", "useBefore");
    
    private static final Collection<String> RESOURCE_URI_VARIABLE_KEYS = Set.of("name", "title", "description", "required", "scope");
    
    private static final Collection<String> RESOURCE_ANNOTATION_KEYS = Set.of("audience", "priority", "lastModified");
    
    private static final Collection<String> TOOL_KEYS = Set.of("name", "title", "description", "icons", "inputSchema", "outputSchema", "annotations", "runtime", "meta");
    
    private static final Collection<String> TOOL_ANNOTATION_KEYS = Set.of("title", "readOnlyHint", "destructiveHint", "idempotentHint", "openWorldHint");
    
    private static final Collection<String> TOOL_RUNTIME_KEYS = Set.of("workflowRole", "requiresUserApproval", "sideEffectScope");
    
    private static final Collection<String> PROMPT_KEYS = Set.of("name", "title", "description", "icons", "arguments", "binding", "meta");
    
    private static final Collection<String> PROMPT_ARGUMENT_KEYS = Set.of("name", "title", "description", "required");
    
    private static final Collection<String> PROMPT_BINDING_KEYS = Set.of("templateResource");
    
    private static final Collection<String> COMPLETION_TARGET_KEYS = Set.of("referenceType", "reference", "arguments", "maxValues", "meta");
    
    private static final Collection<String> RESOURCE_NAVIGATION_KEYS = Set.of("from", "to", "requiredArguments", "carriedArguments", "description");
    
    static void validate(final String resourceName, final byte[] yamlBytes) {
        String yamlContent = new String(yamlBytes, StandardCharsets.UTF_8);
        if (yamlContent.isBlank()) {
            return;
        }
        Map<?, ?> root = asMap(resourceName, "$", new Yaml().load(yamlContent));
        validateKeys(resourceName, "$", root, ROOT_KEYS);
        validateResources(resourceName, root.get("resources"));
        validateResourceTemplates(resourceName, root.get("resourceTemplates"));
        validateTools(resourceName, root.get("tools"));
        validatePrompts(resourceName, root.get("prompts"));
        validateCompletionTargets(resourceName, root.get("completionTargets"));
        validateResourceNavigation(resourceName, root.get("resourceNavigation"));
    }
    
    private static void validateResources(final String resourceName, final Object resources) {
        int index = 0;
        for (Object each : asList(resourceName, "$.resources", resources)) {
            Map<?, ?> resource = asMap(resourceName, "$.resources[" + index + "]", each);
            validateKeys(resourceName, "$.resources[" + index + "]", resource, RESOURCE_KEYS);
            validateIcons(resourceName, "$.resources[" + index + "].icons", resource.get("icons"));
            validateOptionalMap(resourceName, "$.resources[" + index + "].annotations", resource.get("annotations"), RESOURCE_ANNOTATION_KEYS);
            validateResourceExtension(resourceName, "$.resources[" + index + "].extension", resource.get("extension"));
            index++;
        }
    }
    
    private static void validateResourceTemplates(final String resourceName, final Object resourceTemplates) {
        int index = 0;
        for (Object each : asList(resourceName, "$.resourceTemplates", resourceTemplates)) {
            Map<?, ?> resourceTemplate = asMap(resourceName, "$.resourceTemplates[" + index + "]", each);
            validateKeys(resourceName, "$.resourceTemplates[" + index + "]", resourceTemplate, RESOURCE_TEMPLATE_KEYS);
            validateIcons(resourceName, "$.resourceTemplates[" + index + "].icons", resourceTemplate.get("icons"));
            validateOptionalMap(resourceName, "$.resourceTemplates[" + index + "].annotations", resourceTemplate.get("annotations"), RESOURCE_ANNOTATION_KEYS);
            validateResourceExtension(resourceName, "$.resourceTemplates[" + index + "].extension", resourceTemplate.get("extension"));
            index++;
        }
    }
    
    private static void validateResourceExtension(final String resourceName, final String path, final Object extension) {
        if (null == extension) {
            return;
        }
        Map<?, ?> extensionMap = asMap(resourceName, path, extension);
        validateKeys(resourceName, path, extensionMap, RESOURCE_EXTENSION_KEYS);
        validateUriVariables(resourceName, path + ".uriVariables", extensionMap.get("uriVariables"));
    }
    
    private static void validateUriVariables(final String resourceName, final String path, final Object uriVariables) {
        int index = 0;
        for (Object each : asList(resourceName, path, uriVariables)) {
            validateKeys(resourceName, path + "[" + index + "]", asMap(resourceName, path + "[" + index + "]", each), RESOURCE_URI_VARIABLE_KEYS);
            index++;
        }
    }
    
    private static void validateIcons(final String resourceName, final String path, final Object icons) {
        int index = 0;
        for (Object each : asList(resourceName, path, icons)) {
            validateKeys(resourceName, path + "[" + index + "]", asMap(resourceName, path + "[" + index + "]", each), ICON_KEYS);
            index++;
        }
    }
    
    private static void validateTools(final String resourceName, final Object tools) {
        int index = 0;
        for (Object each : asList(resourceName, "$.tools", tools)) {
            Map<?, ?> tool = asMap(resourceName, "$.tools[" + index + "]", each);
            validateKeys(resourceName, "$.tools[" + index + "]", tool, TOOL_KEYS);
            validateIcons(resourceName, "$.tools[" + index + "].icons", tool.get("icons"));
            validateOptionalMap(resourceName, "$.tools[" + index + "].annotations", tool.get("annotations"), TOOL_ANNOTATION_KEYS);
            validateOptionalMap(resourceName, "$.tools[" + index + "].runtime", tool.get("runtime"), TOOL_RUNTIME_KEYS);
            index++;
        }
    }
    
    private static void validatePrompts(final String resourceName, final Object prompts) {
        int index = 0;
        for (Object each : asList(resourceName, "$.prompts", prompts)) {
            Map<?, ?> prompt = asMap(resourceName, "$.prompts[" + index + "]", each);
            validateKeys(resourceName, "$.prompts[" + index + "]", prompt, PROMPT_KEYS);
            validateIcons(resourceName, "$.prompts[" + index + "].icons", prompt.get("icons"));
            validatePromptArguments(resourceName, "$.prompts[" + index + "].arguments", prompt.get("arguments"));
            validateOptionalMap(resourceName, "$.prompts[" + index + "].binding", prompt.get("binding"), PROMPT_BINDING_KEYS);
            index++;
        }
    }
    
    private static void validatePromptArguments(final String resourceName, final String path, final Object arguments) {
        int index = 0;
        for (Object each : asList(resourceName, path, arguments)) {
            validateKeys(resourceName, path + "[" + index + "]", asMap(resourceName, path + "[" + index + "]", each), PROMPT_ARGUMENT_KEYS);
            index++;
        }
    }
    
    private static void validateCompletionTargets(final String resourceName, final Object completionTargets) {
        int index = 0;
        for (Object each : asList(resourceName, "$.completionTargets", completionTargets)) {
            validateKeys(resourceName, "$.completionTargets[" + index + "]", asMap(resourceName, "$.completionTargets[" + index + "]", each), COMPLETION_TARGET_KEYS);
            index++;
        }
    }
    
    private static void validateResourceNavigation(final String resourceName, final Object resourceNavigation) {
        int index = 0;
        for (Object each : asList(resourceName, "$.resourceNavigation", resourceNavigation)) {
            validateKeys(resourceName, "$.resourceNavigation[" + index + "]", asMap(resourceName, "$.resourceNavigation[" + index + "]", each), RESOURCE_NAVIGATION_KEYS);
            index++;
        }
    }
    
    private static void validateOptionalMap(final String resourceName, final String path, final Object value, final Collection<String> allowedKeys) {
        if (null != value) {
            validateKeys(resourceName, path, asMap(resourceName, path, value), allowedKeys);
        }
    }
    
    private static Iterable<?> asList(final String resourceName, final String path, final Object value) {
        if (null == value) {
            return Set.of();
        }
        if (value instanceof Iterable) {
            return (Iterable<?>) value;
        }
        throw new IllegalArgumentException(String.format("MCP descriptor resource `%s` expects list at `%s`.", resourceName, path));
    }
    
    private static Map<?, ?> asMap(final String resourceName, final String path, final Object value) {
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        throw new IllegalArgumentException(String.format("MCP descriptor resource `%s` expects map at `%s`.", resourceName, path));
    }
    
    private static void validateKeys(final String resourceName, final String path, final Map<?, ?> value, final Collection<String> allowedKeys) {
        for (Object each : value.keySet()) {
            if (each instanceof String && allowedKeys.contains(each)) {
                continue;
            }
            throw new IllegalArgumentException(String.format("MCP descriptor resource `%s` contains unknown key `%s.%s`.", resourceName, path, each));
        }
    }
}
