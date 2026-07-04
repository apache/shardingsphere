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

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP URI template.
 */
public final class MCPUriTemplate {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    private final String uriTemplate;
    
    private final List<String> variableNames;
    
    public MCPUriTemplate(final String uriTemplate) {
        this.uriTemplate = uriTemplate;
        variableNames = extractVariableNames(uriTemplate);
    }
    
    private static List<String> extractVariableNames(final String uriTemplate) {
        List<String> result = new LinkedList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(uriTemplate);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
    
    /**
     * Get URI template variable names.
     *
     * @return variable names
     */
    public List<String> getVariableNames() {
        return new LinkedList<>(variableNames);
    }
    
    /**
     * Expand URI template when all variables are present.
     *
     * @param variables URI variables
     * @return expanded URI, or empty when at least one variable is missing
     */
    public Optional<String> expandIfComplete(final MCPUriVariables variables) {
        return containsAllVariables(variables) ? Optional.of(expandKnownVariables(variables)) : Optional.empty();
    }
    
    private boolean containsAllVariables(final MCPUriVariables variables) {
        return variableNames.stream().allMatch(variables::containsVariable);
    }
    
    private String expandKnownVariables(final MCPUriVariables variables) {
        String result = uriTemplate;
        for (String each : variableNames) {
            result = result.replace("{" + each + "}", MCPUriPathSegmentUtils.encodePathSegment(variables.getValue(each)));
        }
        return result;
    }
}
