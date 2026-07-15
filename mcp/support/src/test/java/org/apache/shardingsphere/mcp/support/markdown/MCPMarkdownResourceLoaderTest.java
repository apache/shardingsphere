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
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPMarkdownResourceLoaderTest {
    
    private static final String TEMPLATE_RESOURCE = "META-INF/shardingsphere-mcp/prompts/fixture-markdown-resource.md";
    
    private static final String BLANK_RESOURCE = "META-INF/shardingsphere-mcp/prompts/fixture-blank.md";
    
    private static final String MISSING_RESOURCE = "META-INF/shardingsphere-mcp/prompts/fixture-missing.md";
    
    @Test
    void assertLoad() {
        assertThat(MCPMarkdownResourceLoader.load(TEMPLATE_RESOURCE, "fixture markdown"), is("Fixture template for {{database}}."));
    }
    
    @Test
    void assertLoadUsesFallbackClassLoader() {
        assertThat(loadWithContextClassLoader(null, TEMPLATE_RESOURCE), is("Fixture template for {{database}}."));
    }
    
    @Test
    void assertLoadPreservesAuthoredLeadingComment() {
        assertThat(loadWithContextClassLoader(new FixtureResourceClassLoader("fixture-authored-comment.md", "<!-- authored comment -->\nVisible content."), "fixture-authored-comment.md"),
                is("<!-- authored comment -->\nVisible content."));
    }
    
    @Test
    void assertLoadRejectsMissingResource() {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPMarkdownResourceLoader.load(MISSING_RESOURCE, "fixture markdown"));
        assertThat(actual.getMessage(), is("MCP fixture markdown resource `META-INF/shardingsphere-mcp/prompts/fixture-missing.md` is not found."));
    }
    
    @Test
    void assertLoadRejectsUnreadableResource() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> loadWithContextClassLoader(new FixtureResourceClassLoader("fixture-unreadable.md"), "fixture-unreadable.md"));
        assertThat(actual.getMessage(), is("Failed to load MCP fixture markdown resource `fixture-unreadable.md`."));
        assertThat(actual.getCause(), isA(IOException.class));
    }
    
    @Test
    void assertLoadRejectsBlankResource() {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPMarkdownResourceLoader.load(BLANK_RESOURCE, "fixture markdown"));
        assertThat(actual.getMessage(), is("MCP fixture markdown resource `META-INF/shardingsphere-mcp/prompts/fixture-blank.md` is blank."));
    }
    
    private String loadWithContextClassLoader(final ClassLoader classLoader, final String resourceName) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);
            return MCPMarkdownResourceLoader.load(resourceName, "fixture markdown");
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }
    
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FixtureResourceClassLoader extends ClassLoader {
        
        private final String resourceName;
        
        private final String content;
        
        private FixtureResourceClassLoader(final String resourceName) {
            this(resourceName, null);
        }
        
        @Override
        public InputStream getResourceAsStream(final String name) {
            if (!resourceName.equals(name)) {
                return super.getResourceAsStream(name);
            }
            return null == content ? new UnreadableInputStream() : new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private static final class UnreadableInputStream extends InputStream {
        
        @Override
        public int read() throws IOException {
            throw new IOException("unreadable resource");
        }
    }
}
