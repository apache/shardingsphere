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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MCP URI template utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPUriTemplateUtils {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    /**
     * Extract URI template variable names.
     *
     * @param uriTemplate URI template
     * @return variable names
     */
    public static List<String> extractVariableNames(final String uriTemplate) {
        List<String> result = new LinkedList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(uriTemplate);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
    
    /**
     * Expand URI template when all variables are present.
     *
     * @param uriTemplate URI template
     * @param variables URI variables
     * @return expanded URI, or empty when at least one variable is missing
     */
    public static Optional<String> expandIfComplete(final String uriTemplate, final MCPUriVariables variables) {
        List<String> missingVariableNames = getMissingVariableNames(uriTemplate, variables);
        if (!missingVariableNames.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(expandKnownVariables(uriTemplate, variables));
    }
    
    private static List<String> getMissingVariableNames(final String uriTemplate, final MCPUriVariables variables) {
        return extractVariableNames(uriTemplate).stream().filter(each -> !variables.containsVariable(each)).collect(Collectors.toList());
    }
    
    private static String expandKnownVariables(final String uriTemplate, final MCPUriVariables variables) {
        String result = uriTemplate;
        for (String each : extractVariableNames(uriTemplate)) {
            result = result.replace("{" + each + "}", encodePathSegment(variables.getValue(each)));
        }
        return result;
    }
    
    /**
     * Encode one MCP resource URI path segment.
     *
     * @param pathSegment raw path segment
     * @return encoded path segment
     */
    public static String encodePathSegment(final String pathSegment) {
        return URLEncoder.encode(pathSegment, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
