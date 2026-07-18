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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.prompt.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.MCPPromptDescriptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Validator for MCP prompt descriptors and their internal templates.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPPromptDescriptorValidator {
    
    private static final String CLIENT_FORM_ONLY_ARGUMENTS = "org.apache.shardingsphere/client-form-only-arguments";
    
    private static final Pattern SINGLE_BRACE_PLACEHOLDER_PATTERN = Pattern.compile("(?<!\\{)\\{\\s*([a-zA-Z0-9_.-]+)\\s*}(?!})");
    
    /**
     * Validate prompt descriptors against their internal templates.
     *
     * @param descriptors prompt descriptors
     * @param templateBindings prompt template bindings
     */
    public static void validate(final Collection<MCPPromptDescriptor> descriptors, final Collection<MCPPromptTemplateBinding> templateBindings) {
        Map<String, MCPPromptDescriptor> registered = new LinkedHashMap<>(descriptors.size(), 1F);
        Map<String, MCPPromptTemplateBinding> bindings = templateBindings.stream()
                .collect(Collectors.toMap(MCPPromptTemplateBinding::getPromptName, each -> each));
        for (MCPPromptDescriptor each : descriptors) {
            MCPPromptTemplateBinding binding = bindings.get(each.getName());
            ShardingSpherePreconditions.checkState(null != binding, () -> new IllegalStateException(String.format("Prompt `%s` must declare an internal template binding.", each.getName())));
            ShardingSpherePreconditions.checkState(null == registered.putIfAbsent(each.getName(), each),
                    () -> new IllegalStateException(String.format("Duplicate MCP prompt descriptor `%s`.", each.getName())));
            validatePromptTemplate(each, binding);
        }
    }
    
    private static void validatePromptTemplate(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding) {
        String template = MCPPromptTemplateLoader.load(binding.getTemplateResource());
        validateNoUnsupportedModelFacingPlaceholders(binding, template);
        Set<String> declaredArguments = descriptor.getArguments().stream().map(MCPPromptArgumentDescriptor::getName).collect(Collectors.toSet());
        Set<String> renderedArguments = MCPPromptTemplateLoader.extractPlaceholders(template);
        for (String each : renderedArguments) {
            ShardingSpherePreconditions.checkState(declaredArguments.contains(each),
                    () -> new IllegalStateException(String.format("Prompt template `%s` has undeclared placeholder `%s`.", binding.getTemplateResource(), each)));
        }
        validateDeclaredPromptArgumentsRendered(descriptor, binding, declaredArguments, renderedArguments);
    }
    
    private static void validateNoUnsupportedModelFacingPlaceholders(final MCPPromptTemplateBinding binding, final String template) {
        for (String each : template.lines().toList()) {
            validateNoUnsupportedModelFacingPlaceholderInLine(binding, each);
        }
    }
    
    private static void validateNoUnsupportedModelFacingPlaceholderInLine(final MCPPromptTemplateBinding binding, final String line) {
        Matcher matcher = SINGLE_BRACE_PLACEHOLDER_PATTERN.matcher(line);
        while (matcher.find()) {
            if (!isResourceUriTemplateVariable(line, matcher.start())) {
                throw new IllegalStateException(String.format("Prompt template `%s` contains unsupported model-facing placeholder `{%s}`.",
                        binding.getTemplateResource(), matcher.group(1)));
            }
        }
    }
    
    private static boolean isResourceUriTemplateVariable(final String line, final int placeholderStartIndex) {
        int tokenStartIndex = findTokenStartIndex(line, placeholderStartIndex);
        int resourceUriStartIndex = line.lastIndexOf("shardingsphere://", placeholderStartIndex);
        return resourceUriStartIndex >= tokenStartIndex;
    }
    
    private static int findTokenStartIndex(final String line, final int index) {
        for (int i = index - 1; i >= 0; i--) {
            if (Character.isWhitespace(line.charAt(i))) {
                return i + 1;
            }
        }
        return 0;
    }
    
    private static void validateDeclaredPromptArgumentsRendered(final MCPPromptDescriptor descriptor, final MCPPromptTemplateBinding binding,
                                                                final Set<String> declaredArguments, final Set<String> renderedArguments) {
        Set<String> clientFormOnlyArguments = getClientFormOnlyArguments(descriptor);
        for (String each : declaredArguments) {
            ShardingSpherePreconditions.checkState(renderedArguments.contains(each) || clientFormOnlyArguments.contains(each),
                    () -> new IllegalStateException(String.format("Prompt `%s` declares argument `%s` but template `%s` does not render it.",
                            descriptor.getName(), each, binding.getTemplateResource())));
        }
    }
    
    private static Set<String> getClientFormOnlyArguments(final MCPPromptDescriptor descriptor) {
        Object value = descriptor.getMeta().get(CLIENT_FORM_ONLY_ARGUMENTS);
        if (null == value) {
            return Set.of();
        }
        ShardingSpherePreconditions.checkState(value instanceof Collection,
                () -> new IllegalStateException(String.format("Prompt `%s` metadata `%s` must be a list.", descriptor.getName(), CLIENT_FORM_ONLY_ARGUMENTS)));
        return ((Collection<?>) value).stream().map(String::valueOf).collect(Collectors.toSet());
    }
}
