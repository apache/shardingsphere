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

package org.apache.shardingsphere.mcp.support.markdown;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * MCP Markdown resource loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPMarkdownResourceLoader {
    
    private static final Pattern APACHE_LICENSE_HEADER_PATTERN = Pattern.compile(
            "\\A<!--(?:(?!-->)[\\s\\S])*Licensed to the Apache Software Foundation(?:(?!-->)[\\s\\S])*Apache License, Version 2\\.0(?:(?!-->)[\\s\\S])*-->\\s*");
    
    /**
     * Load resource from classpath.
     *
     * @param resourceName resource name
     * @param resourceDescription resource description
     * @return Markdown content without source-control header
     */
    public static String load(final String resourceName, final String resourceDescription) {
        ClassLoader resourceClassLoader = Optional.ofNullable(Thread.currentThread().getContextClassLoader()).orElse(MCPMarkdownResourceLoader.class.getClassLoader());
        String result = removeApacheLicenseHeader(load(resourceName, resourceDescription, resourceClassLoader)).strip();
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalStateException(String.format("MCP %s resource `%s` is blank.", resourceDescription, resourceName)));
        return result;
    }
    
    private static String load(final String resourceName, final String resourceDescription, final ClassLoader classLoader) {
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            ShardingSpherePreconditions.checkNotNull(inputStream, () -> new IllegalStateException(String.format("MCP %s resource `%s` is not found.", resourceDescription, resourceName)));
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalStateException(String.format("Failed to load MCP %s resource `%s`.", resourceDescription, resourceName), ex);
        }
    }
    
    private static String removeApacheLicenseHeader(final String content) {
        return APACHE_LICENSE_HEADER_PATTERN.matcher(content).replaceFirst("");
    }
}
