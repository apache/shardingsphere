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

package org.apache.shardingsphere.mcp.core.resource.uri;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP URI pattern.
 */
@Getter
public final class MCPUriPattern {
    
    private static final String SCHEME_SEPARATOR = "://";
    
    private static final String SCHEME = "shardingsphere";
    
    private final String pattern;
    
    private final List<String> variableNames;
    
    private final Pattern compiledRegex;
    
    public MCPUriPattern(final String pattern) {
        this.pattern = pattern;
        int schemeSeparatorIndex = findSchemeSeparatorIndex(pattern);
        validateScheme(pattern, schemeSeparatorIndex);
        List<String> pathSegments = extractPathSegments(pattern, schemeSeparatorIndex + SCHEME_SEPARATOR.length());
        variableNames = extractVariableNames(pattern, pathSegments);
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
            result.add(each);
        }
        return result;
    }
    
    private List<String> extractVariableNames(final String pattern, final List<String> pathSegments) {
        List<String> result = new ArrayList<>(pathSegments.size());
        for (String each : pathSegments) {
            if (!isVariableSegment(each)) {
                continue;
            }
            String variableName = extractVariableName(pattern, each);
            ShardingSpherePreconditions.checkState(!result.contains(variableName),
                    () -> new IllegalArgumentException(String.format("Duplicate URI pattern variable `%s` in `%s`.", variableName, pattern)));
            result.add(variableName);
        }
        return result;
    }
    
    private String extractVariableName(final String pattern, final String pathSegment) {
        String result = pathSegment.substring(1, pathSegment.length() - 1);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(String.format("URI pattern variable is required in `%s`.", pattern)));
        return result;
    }
    
    private Pattern compileRegex(final List<String> pathSegments) {
        StringBuilder regex = new StringBuilder("^");
        regex.append(Pattern.quote(SCHEME));
        regex.append(SCHEME_SEPARATOR);
        for (int i = 0; i < pathSegments.size(); i++) {
            if (0 < i) {
                regex.append('/');
            }
            String each = pathSegments.get(i);
            regex.append(isVariableSegment(each) ? "([^/]+)" : Pattern.quote(each));
        }
        regex.append('$');
        return Pattern.compile(regex.toString());
    }
    
    private boolean isVariableSegment(final String pathSegment) {
        return 2 < pathSegment.length() && pathSegment.startsWith("{") && pathSegment.endsWith("}");
    }
    
    /**
     * Parse URI.
     *
     * @param uri URI
     * @return parsed variables when present
     */
    public Optional<MCPResourceURIVariables> parse(final String uri) {
        Matcher matcher = compiledRegex.matcher(uri);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        Map<String, String> variables = new LinkedHashMap<>(variableNames.size(), 1F);
        for (int i = 0; i < variableNames.size(); i++) {
            String encodedValue = matcher.group(i + 1);
            if (containsRawTemplateMarker(encodedValue)) {
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
    
    private boolean containsRawTemplateMarker(final String encodedValue) {
        return encodedValue.contains("{") || encodedValue.contains("}");
    }
    
    /**
     * Determine whether current pattern overlaps with another pattern.
     *
     * @param other other pattern
     * @return overlap or not
     */
    public boolean overlaps(final MCPUriPattern other) {
        List<String> leftSegments = extractPathSegments(pattern, SCHEME.length() + SCHEME_SEPARATOR.length());
        List<String> rightSegments = extractPathSegments(other.getPattern(), SCHEME.length() + SCHEME_SEPARATOR.length());
        if (leftSegments.size() != rightSegments.size()) {
            return false;
        }
        for (int i = 0; i < leftSegments.size(); i++) {
            String left = leftSegments.get(i);
            String right = rightSegments.get(i);
            if (!isVariableSegment(left) && !isVariableSegment(right) && !left.equals(right)) {
                return false;
            }
        }
        return true;
    }
}
