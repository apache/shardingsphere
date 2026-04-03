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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MCP URI template.
 */
@Getter
public final class MCPUriTemplate {
    
    private final String template;
    
    private final String routeSignature;
    
    private final ParsedUriTemplate parsedTemplate;
    
    /**
     * Create URI template.
     *
     * @param template template text
     */
    public MCPUriTemplate(final String template) {
        this.template = template;
        parsedTemplate = parseTemplate(template);
        routeSignature = createRouteSignature(parsedTemplate);
    }
    
    /**
     * Match URI with current template.
     *
     * @param uri uri text
     * @return matched result when present
     */
    public Optional<MCPUriTemplateMatch> match(final String uri) {
        ParsedUri parsedUri;
        try {
            parsedUri = parseUri(uri);
        } catch (final IllegalArgumentException ignored) {
            return Optional.empty();
        }
        if (!parsedTemplate.getScheme().equals(parsedUri.getScheme()) || parsedTemplate.getSegments().size() != parsedUri.getSegments().size()) {
            return Optional.empty();
        }
        Map<String, String> variables = new LinkedHashMap<>(parsedTemplate.getSegments().size(), 1F);
        for (int i = 0; i < parsedTemplate.getSegments().size(); i++) {
            Segment templateSegment = parsedTemplate.getSegments().get(i);
            String actualSegment = parsedUri.getSegments().get(i);
            if (templateSegment.isVariable()) {
                variables.put(templateSegment.getValue(), actualSegment);
                continue;
            }
            if (!templateSegment.getValue().equals(actualSegment)) {
                return Optional.empty();
            }
        }
        return Optional.of(new MCPUriTemplateMatch(template, variables));
    }
    
    /**
     * Determine whether current template overlaps with another template.
     *
     * @param other other template
     * @return whether overlap exists
     */
    public boolean overlaps(final MCPUriTemplate other) {
        if (!parsedTemplate.getScheme().equals(other.parsedTemplate.getScheme()) || parsedTemplate.getSegments().size() != other.parsedTemplate.getSegments().size()) {
            return false;
        }
        for (int i = 0; i < parsedTemplate.getSegments().size(); i++) {
            Segment left = parsedTemplate.getSegments().get(i);
            Segment right = other.parsedTemplate.getSegments().get(i);
            if (!left.isVariable() && !right.isVariable() && !left.getValue().equals(right.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    private ParsedUriTemplate parseTemplate(final String template) {
        ParsedUri actualUri = parseUri(template);
        Set<String> variableNames = new LinkedHashSet<>(actualUri.getSegments().size(), 1F);
        List<Segment> result = new ArrayList<>(actualUri.getSegments().size());
        for (String each : actualUri.getSegments()) {
            if (!isVariableSegment(each)) {
                result.add(new Segment(false, each));
                continue;
            }
            String variableName = extractVariableName(each, template);
            ShardingSpherePreconditions.checkState(variableNames.add(variableName), () -> new IllegalArgumentException(
                    String.format("Duplicate URI template variable `%s` in `%s`.", variableName, template)));
            result.add(new Segment(true, variableName));
        }
        return new ParsedUriTemplate(actualUri.getScheme(), result);
    }
    
    private String createRouteSignature(final ParsedUriTemplate parsedUriTemplate) {
        StringBuilder result = new StringBuilder(parsedUriTemplate.getScheme());
        result.append("://");
        for (int i = 0; i < parsedUriTemplate.getSegments().size(); i++) {
            if (0 < i) {
                result.append('/');
            }
            Segment each = parsedUriTemplate.getSegments().get(i);
            result.append(each.isVariable() ? "{}" : each.getValue());
        }
        return result.toString();
    }
    
    private ParsedUri parseUri(final String uri) {
        ShardingSpherePreconditions.checkNotEmpty(uri, () -> new IllegalArgumentException("URI is required."));
        int schemeSeparatorIndex = uri.indexOf("://");
        ShardingSpherePreconditions.checkState(0 < schemeSeparatorIndex, () -> new IllegalArgumentException(
                String.format("Invalid URI `%s`.", uri)));
        String scheme = uri.substring(0, schemeSeparatorIndex);
        ShardingSpherePreconditions.checkNotEmpty(scheme, () -> new IllegalArgumentException(
                String.format("URI scheme is required for `%s`.", uri)));
        String path = uri.substring(schemeSeparatorIndex + 3);
        ShardingSpherePreconditions.checkNotEmpty(path, () -> new IllegalArgumentException(
                String.format("URI path is required for `%s`.", uri)));
        String[] segments = path.split("/", -1);
        List<String> result = new ArrayList<>(segments.length);
        for (String each : segments) {
            ShardingSpherePreconditions.checkNotEmpty(each, () -> new IllegalArgumentException(
                    String.format("URI segment is required for `%s`.", uri)));
            result.add(each);
        }
        return new ParsedUri(scheme, result);
    }
    
    private boolean isVariableSegment(final String segment) {
        return 2 < segment.length() && segment.startsWith("{") && segment.endsWith("}");
    }
    
    private String extractVariableName(final String segment, final String template) {
        String result = segment.substring(1, segment.length() - 1);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(
                String.format("URI template variable is required in `%s`.", template)));
        return result;
    }
    
    @Getter
    @RequiredArgsConstructor
    private static final class ParsedUri {
        
        private final String scheme;
        
        private final List<String> segments;
    }
    
    @Getter
    @RequiredArgsConstructor
    private static final class ParsedUriTemplate {
        
        private final String scheme;
        
        private final List<Segment> segments;
    }
    
    @Getter
    @RequiredArgsConstructor
    private static final class Segment {
        
        private final boolean variable;
        
        private final String value;
    }
}
