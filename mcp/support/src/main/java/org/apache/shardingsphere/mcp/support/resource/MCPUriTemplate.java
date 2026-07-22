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

package org.apache.shardingsphere.mcp.support.resource;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP URI template.
 */
public final class MCPUriTemplate {
    
    private static final String SCHEME_SEPARATOR = "://";
    
    private static final String SCHEME = "shardingsphere";
    
    private final String uriTemplate;
    
    private final List<String> pathSegments;
    
    private final List<String> variableNames;
    
    private final Pattern compiledRegex;
    
    public MCPUriTemplate(final String uriTemplate) {
        this.uriTemplate = uriTemplate;
        int schemeSeparatorIndex = findSchemeSeparatorIndex(uriTemplate);
        validateScheme(uriTemplate, schemeSeparatorIndex);
        pathSegments = extractPathSegments(uriTemplate, schemeSeparatorIndex + SCHEME_SEPARATOR.length());
        variableNames = extractVariableNames(uriTemplate, pathSegments);
        compiledRegex = compileRegex(pathSegments);
    }
    
    private int findSchemeSeparatorIndex(final String uri) {
        int result = uri.indexOf(SCHEME_SEPARATOR);
        ShardingSpherePreconditions.checkState(0 < result, () -> new IllegalArgumentException(String.format("Invalid URI `%s`.", uri)));
        return result;
    }
    
    private void validateScheme(final String uri, final int schemeSeparatorIndex) {
        String scheme = uri.substring(0, schemeSeparatorIndex);
        ShardingSpherePreconditions.checkState(SCHEME.equals(scheme), () -> new IllegalArgumentException(String.format("Invalid URI `%s`.", uri)));
    }
    
    private List<String> extractPathSegments(final String uri, final int pathStartIndex) {
        String path = uri.substring(pathStartIndex);
        ShardingSpherePreconditions.checkNotEmpty(path, () -> new IllegalArgumentException(String.format("URI path is required for `%s`.", uri)));
        String[] segments = path.split("/", -1);
        List<String> result = new ArrayList<>(segments.length);
        for (String each : segments) {
            ShardingSpherePreconditions.checkNotEmpty(each, () -> new IllegalArgumentException(String.format("URI segment is required for `%s`.", uri)));
            ShardingSpherePreconditions.checkState(isVariableSegment(each) || !containsTemplateMarker(each),
                    () -> new IllegalArgumentException(String.format("URI template variables must occupy a complete path segment in `%s`.", uri)));
            result.add(each);
        }
        return result;
    }
    
    private List<String> extractVariableNames(final String template, final List<String> segments) {
        List<String> result = new ArrayList<>(segments.size());
        for (String each : segments) {
            if (!isVariableSegment(each)) {
                continue;
            }
            String variableName = each.substring(1, each.length() - 1);
            ShardingSpherePreconditions.checkNotEmpty(variableName, () -> new IllegalArgumentException(String.format("URI template variable is required in `%s`.", template)));
            ShardingSpherePreconditions.checkState(!containsTemplateMarker(variableName),
                    () -> new IllegalArgumentException(String.format("Invalid URI template variable `%s` in `%s`.", variableName, template)));
            ShardingSpherePreconditions.checkState(!result.contains(variableName),
                    () -> new IllegalArgumentException(String.format("Duplicate URI template variable `%s` in `%s`.", variableName, template)));
            result.add(variableName);
        }
        return result;
    }
    
    private Pattern compileRegex(final List<String> segments) {
        StringBuilder regex = new StringBuilder("^").append(Pattern.quote(SCHEME)).append(SCHEME_SEPARATOR);
        for (int i = 0; i < segments.size(); i++) {
            if (0 < i) {
                regex.append('/');
            }
            String each = segments.get(i);
            regex.append(isVariableSegment(each) ? "([^/]+)" : Pattern.quote(each));
        }
        return Pattern.compile(regex.append('$').toString());
    }
    
    private boolean isVariableSegment(final String pathSegment) {
        return 2 < pathSegment.length() && pathSegment.startsWith("{") && pathSegment.endsWith("}");
    }
    
    private boolean containsTemplateMarker(final String value) {
        return value.contains("{") || value.contains("}");
    }
    
    /**
     * Get URI template variable names.
     *
     * @return variable names
     */
    public List<String> getVariableNames() {
        return new ArrayList<>(variableNames);
    }
    
    /**
     * Expand URI template when all variables are present.
     *
     * @param variables URI variables
     * @return expanded URI, or empty when at least one variable is missing
     */
    public Optional<String> expandIfComplete(final MCPResourceURIVariables variables) {
        return containsAllVariables(variables) ? Optional.of(expandKnownVariables(variables)) : Optional.empty();
    }
    
    private boolean containsAllVariables(final MCPResourceURIVariables variables) {
        return variableNames.stream().allMatch(variables::containsVariable);
    }
    
    private String expandKnownVariables(final MCPResourceURIVariables variables) {
        String result = uriTemplate;
        for (String each : variableNames) {
            result = result.replace("{" + each + "}", MCPUriPathSegmentUtils.encodePathSegment(variables.getValue(each)));
        }
        return result;
    }
    
    /**
     * Parse URI variables using this template.
     *
     * @param uri URI
     * @return parsed variables when the URI matches
     */
    public Optional<MCPResourceURIVariables> parse(final String uri) {
        Matcher matcher = compiledRegex.matcher(uri);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        Map<String, String> variables = new LinkedHashMap<>(variableNames.size(), 1F);
        for (int i = 0; i < variableNames.size(); i++) {
            String encodedValue = matcher.group(i + 1);
            if (containsTemplateMarker(encodedValue)) {
                return Optional.empty();
            }
            Optional<String> decodedValue = MCPUriPathSegmentUtils.decodePathSegment(encodedValue);
            if (decodedValue.isEmpty()) {
                return Optional.empty();
            }
            variables.put(variableNames.get(i), decodedValue.get());
        }
        return Optional.of(new MCPResourceURIVariables(variables));
    }
    
    /**
     * Determine whether this template overlaps another template.
     *
     * @param other other template
     * @return whether the templates overlap
     */
    public boolean overlaps(final MCPUriTemplate other) {
        if (pathSegments.size() != other.pathSegments.size()) {
            return false;
        }
        for (int i = 0; i < pathSegments.size(); i++) {
            String left = pathSegments.get(i);
            String right = other.pathSegments.get(i);
            if (!isVariableSegment(left) && !isVariableSegment(right) && !left.equals(right)) {
                return false;
            }
        }
        return true;
    }
}
