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
import org.apache.shardingsphere.mcp.support.markdown.MCPMarkdownResourceLoader;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCP prompt template loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPPromptTemplateLoader {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");
    
    /**
     * Load template from classpath.
     *
     * @param templateResource template resource
     * @return template text
     * @throws IllegalStateException when the template cannot be loaded
     */
    public static String load(final String templateResource) {
        return MCPMarkdownResourceLoader.load(templateResource, "prompt template");
    }
    
    /**
     * Extract placeholders from template.
     *
     * @param template template text
     * @return placeholders
     */
    public static Set<String> extractPlaceholders(final String template) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
    
    /**
     * Render template with arguments.
     *
     * @param template template text
     * @param arguments prompt arguments
     * @return rendered template
     */
    public static String render(final String template, final Map<String, Object> arguments) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(Objects.toString(arguments.get(matcher.group(1)), "")));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
