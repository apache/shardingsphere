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

package org.apache.shardingsphere.mcp.resource.dispatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resource URI pattern matcher.
 */
public final class ResourceUriMatcher {
    
    private static final String RESOURCE_PREFIX = "shardingsphere://";
    
    /**
     * Match one URI template with one resource URI.
     *
     * @param uriTemplate URI template
     * @param resourceUri resource URI
     * @return matched route
     */
    public Optional<ResourceUriMatch> match(final String uriTemplate, final String resourceUri) {
        List<String> templateSegments = splitSegments(uriTemplate);
        List<String> resourceSegments = splitSegments(resourceUri);
        if (templateSegments.isEmpty() || resourceSegments.isEmpty() || templateSegments.size() != resourceSegments.size()) {
            return Optional.empty();
        }
        Map<String, String> uriVariables = new LinkedHashMap<>(templateSegments.size(), 1F);
        for (int i = 0; i < templateSegments.size(); i++) {
            String templateSegment = templateSegments.get(i);
            String resourceSegment = resourceSegments.get(i);
            if (isVariableSegment(templateSegment)) {
                String variableName = extractVariableName(templateSegment);
                if (uriVariables.containsKey(variableName)) {
                    return Optional.empty();
                }
                uriVariables.put(variableName, resourceSegment);
                continue;
            }
            if (!templateSegment.equals(resourceSegment)) {
                return Optional.empty();
            }
        }
        return Optional.of(new ResourceUriMatch(resourceUri, uriTemplate, Collections.unmodifiableMap(uriVariables)));
    }
    
    /**
     * Create route signature for one URI template.
     *
     * @param uriTemplate URI template
     * @return route signature
     * @throws IllegalArgumentException when the URI template is invalid
     */
    public String createRouteSignature(final String uriTemplate) {
        List<String> segments = splitSegments(uriTemplate);
        if (segments.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid resource URI template `%s`.", uriTemplate));
        }
        StringBuilder result = new StringBuilder(RESOURCE_PREFIX);
        for (int i = 0; i < segments.size(); i++) {
            if (0 < i) {
                result.append('/');
            }
            result.append(isVariableSegment(segments.get(i)) ? "{}" : segments.get(i));
        }
        return result.toString();
    }
    
    private List<String> splitSegments(final String resourceUri) {
        if (null == resourceUri || !resourceUri.startsWith(RESOURCE_PREFIX)) {
            return Collections.emptyList();
        }
        String actualUri = resourceUri.substring(RESOURCE_PREFIX.length());
        if (actualUri.isEmpty()) {
            return Collections.emptyList();
        }
        String[] segments = actualUri.split("/", -1);
        List<String> result = new ArrayList<>(segments.length);
        for (String each : segments) {
            if (each.isEmpty()) {
                return Collections.emptyList();
            }
            result.add(each);
        }
        return result;
    }
    
    private boolean isVariableSegment(final String segment) {
        return 2 < segment.length() && segment.startsWith("{") && segment.endsWith("}");
    }
    
    private String extractVariableName(final String segment) {
        return segment.substring(1, segment.length() - 1);
    }
}
