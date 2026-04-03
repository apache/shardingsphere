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

package org.apache.shardingsphere.mcp.uri;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MCP URI pattern.
 */
@Getter
public final class MCPUriPattern {
    
    private final String pattern;
    
    private final ParsedUriPattern parsedPattern;
    
    private final String routeSignature;
    
    public MCPUriPattern(final String pattern) {
        this.pattern = pattern;
        parsedPattern = parsePattern(pattern);
        routeSignature = createRouteSignature(parsedPattern);
    }
    
    private ParsedUriPattern parsePattern(final String pattern) {
        ParsedUri parsedUri = parseUri(pattern);
        Set<String> variableNames = new LinkedHashSet<>(parsedUri.getSegments().size(), 1F);
        List<Segment> segments = new ArrayList<>(parsedUri.getSegments().size());
        for (String each : parsedUri.getSegments()) {
            if (!isVariableSegment(each)) {
                segments.add(new Segment(false, each));
                continue;
            }
            String variableName = extractVariableName(each, pattern);
            ShardingSpherePreconditions.checkState(variableNames.add(variableName),
                    () -> new IllegalArgumentException(String.format("Duplicate URI pattern variable `%s` in `%s`.", variableName, pattern)));
            segments.add(new Segment(true, variableName));
        }
        return new ParsedUriPattern(parsedUri.getScheme(), segments);
    }
    
    private ParsedUri parseUri(final String uri) {
        int schemeSeparatorIndex = uri.indexOf("://");
        ShardingSpherePreconditions.checkState(0 < schemeSeparatorIndex, () -> new IllegalArgumentException(String.format("Invalid URI `%s`.", uri)));
        String scheme = uri.substring(0, schemeSeparatorIndex);
        ShardingSpherePreconditions.checkNotEmpty(scheme, () -> new IllegalArgumentException(String.format("URI scheme is required for `%s`.", uri)));
        String path = uri.substring(schemeSeparatorIndex + 3);
        ShardingSpherePreconditions.checkNotEmpty(path, () -> new IllegalArgumentException(String.format("URI path is required for `%s`.", uri)));
        String[] segments = path.split("/", -1);
        Arrays.stream(segments).forEach(each -> ShardingSpherePreconditions.checkNotEmpty(each, () -> new IllegalArgumentException(String.format("URI segment is required for `%s`.", uri))));
        return new ParsedUri(scheme, Arrays.asList(segments));
    }
    
    private boolean isVariableSegment(final String segment) {
        return 2 < segment.length() && segment.startsWith("{") && segment.endsWith("}");
    }
    
    private String extractVariableName(final String segment, final String pattern) {
        String result = segment.substring(1, segment.length() - 1);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(String.format("URI pattern variable is required in `%s`.", pattern)));
        return result;
    }
    
    private String createRouteSignature(final ParsedUriPattern parsedUriPattern) {
        StringBuilder result = new StringBuilder(parsedUriPattern.getScheme());
        result.append("://");
        for (int i = 0; i < parsedUriPattern.getSegments().size(); i++) {
            if (0 < i) {
                result.append('/');
            }
            Segment each = parsedUriPattern.getSegments().get(i);
            result.append(each.isVariable() ? "{}" : each.getValue());
        }
        return result.toString();
    }
    
    /**
     * Match URI with current pattern.
     *
     * @param uri uri text
     * @return matched result when present
     */
    public Optional<MCPUriVariables> match(final String uri) {
        ParsedUri parsedUri;
        try {
            parsedUri = parseUri(uri);
        } catch (final IllegalArgumentException ignored) {
            return Optional.empty();
        }
        if (!parsedPattern.getScheme().equals(parsedUri.getScheme()) || parsedPattern.getSegments().size() != parsedUri.getSegments().size()) {
            return Optional.empty();
        }
        Map<String, String> variables = new LinkedHashMap<>(parsedPattern.getSegments().size(), 1F);
        for (int i = 0; i < parsedPattern.getSegments().size(); i++) {
            Segment patternSegment = parsedPattern.getSegments().get(i);
            String actualSegment = parsedUri.getSegments().get(i);
            if (patternSegment.isVariable()) {
                variables.put(patternSegment.getValue(), actualSegment);
                continue;
            }
            if (!patternSegment.getValue().equals(actualSegment)) {
                return Optional.empty();
            }
        }
        return Optional.of(new MCPUriVariables(variables));
    }
    
    /**
     * Determine whether current pattern overlaps with another pattern.
     *
     * @param other other pattern
     * @return whether overlap exists
     */
    public boolean overlaps(final MCPUriPattern other) {
        if (!parsedPattern.getScheme().equals(other.parsedPattern.getScheme()) || parsedPattern.getSegments().size() != other.parsedPattern.getSegments().size()) {
            return false;
        }
        for (int i = 0; i < parsedPattern.getSegments().size(); i++) {
            Segment left = parsedPattern.getSegments().get(i);
            Segment right = other.parsedPattern.getSegments().get(i);
            if (!left.isVariable() && !right.isVariable() && !left.getValue().equals(right.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class ParsedUri {
        
        private final String scheme;
        
        private final List<String> segments;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class ParsedUriPattern {
        
        private final String scheme;
        
        private final List<Segment> segments;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class Segment {
        
        private final boolean variable;
        
        private final String value;
    }
}
