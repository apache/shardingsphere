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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMMCPActionExecutorTest {
    
    @Test
    void assertExecuteSafelyWithListResources() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        assertThat(new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.LIST_RESOURCES, Map.of()), is(Map.of("resources", List.of())));
    }
    
    @Test
    void assertExecuteSafelyWithReadResource() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        assertThat(new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.READ_RESOURCE, Map.of("uri", " shardingsphere://databases ")), is(Map.of("items", List.of())));
        assertThat(client.resourceUri, is("shardingsphere://databases"));
    }
    
    @Test
    void assertExecuteSafelyWithEmptyResourceUri() {
        assertThrows(IllegalArgumentException.class, () -> new LLMMCPActionExecutor(new FakeMCPInteractionClient()).executeSafely(MCPInteractionActionNames.READ_RESOURCE, Map.of("uri", " ")));
    }
    
    @Test
    void assertExecuteSafelyWithListPrompts() throws InterruptedException {
        assertThat(new LLMMCPActionExecutor(new FakeMCPInteractionClient()).executeSafely(MCPInteractionActionNames.LIST_PROMPTS, Map.of()), is(Map.of("prompts", List.of())));
    }
    
    @Test
    void assertExecuteSafelyWithGetPrompt() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        assertThat(new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.GET_PROMPT, Map.of("name", " inspect_metadata ", "arguments", Map.of("database", "logic_db"))),
                is(Map.of("messages", List.of())));
        assertThat(client.promptName, is("inspect_metadata"));
        assertThat(client.promptArguments, is(Map.of("database", "logic_db")));
    }
    
    @Test
    void assertExecuteSafelyWithCompletionReference() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        Map<String, Object> actual = new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.COMPLETE, Map.of(
                "reference", Map.of("type", "prompt", "name", "inspect_metadata"),
                "argument_name", "schema",
                "argument_value", "pub",
                "context_arguments", Map.of("database", "logic_db")));
        assertThat(actual, is(Map.of("completion", "public")));
        assertThat(client.completionReference, is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(client.completionArgumentName, is("schema"));
        assertThat(client.completionArgumentValue, is("pub"));
        assertThat(client.contextArguments, is(Map.of("database", "logic_db")));
    }
    
    @Test
    void assertExecuteSafelyWithInlineCompletionReference() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.COMPLETE, Map.of("reference_type", "resource", "resource_uri", "shardingsphere://databases", "argument_name", "uri"));
        assertThat(client.completionReference, is(Map.of("type", "ref/resource", "uri", "shardingsphere://databases")));
    }
    
    @Test
    void assertExecuteSafelyWithCompletionRecovery() throws InterruptedException {
        Map<String, Object> actual = new LLMMCPActionExecutor(new FakeMCPInteractionClient()).executeSafely(MCPInteractionActionNames.COMPLETE, Map.of("argument_value", "pub"));
        assertThat(actual.get("response_mode"), is("recovery"));
        assertThat(actual.get("error_code"), is("invalid_tool_arguments"));
        assertThat(actual.get("message"), is("mcp_complete requires a reference object and argument_name."));
    }
    
    @Test
    void assertExecuteSafelyWithToolCall() throws InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        assertThat(new LLMMCPActionExecutor(client).executeSafely("database_gateway_execute_query", Map.of("sql", "SELECT 1")), is(Map.of("result_kind", "result_set")));
        assertThat(client.actionName, is("database_gateway_execute_query"));
        assertThat(client.arguments, is(Map.of("sql", "SELECT 1")));
    }
    
    @Test
    void assertExecuteSafelyWithIOException() {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient();
        client.failWithIOException = true;
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new LLMMCPActionExecutor(client).executeSafely(MCPInteractionActionNames.LIST_RESOURCES, Map.of()));
        assertThat(actual.getMessage(), is("MCP action `mcp_list_resources` failed: unavailable"));
        assertThat(actual.getCause(), isA(IOException.class));
    }
    
    private static final class FakeMCPInteractionClient implements MCPInteractionClient {
        
        private boolean failWithIOException;
        
        private String actionName;
        
        private Map<String, Object> arguments = Map.of();
        
        private String resourceUri;
        
        private String promptName;
        
        private Map<String, Object> promptArguments = Map.of();
        
        private Map<String, Object> completionReference = Map.of();
        
        private String completionArgumentName;
        
        private String completionArgumentValue;
        
        private Map<String, String> contextArguments = Map.of();
        
        @Override
        public void open() {
        }
        
        @Override
        public Map<String, Object> call(final String actionName, final Map<String, Object> arguments) {
            this.actionName = actionName;
            this.arguments = arguments;
            return Map.of("result_kind", "result_set");
        }
        
        @Override
        public Map<String, Object> listResources() throws IOException {
            if (failWithIOException) {
                throw new IOException("unavailable");
            }
            return Map.of("resources", List.of());
        }
        
        @Override
        public Map<String, Object> readResource(final String resourceUri) {
            this.resourceUri = resourceUri;
            return Map.of("items", List.of());
        }
        
        @Override
        public Map<String, Object> listPrompts() {
            return Map.of("prompts", List.of());
        }
        
        @Override
        public Map<String, Object> getPrompt(final String promptName, final Map<String, Object> arguments) {
            this.promptName = promptName;
            promptArguments = arguments;
            return Map.of("messages", List.of());
        }
        
        @Override
        public Map<String, Object> complete(final Map<String, Object> reference, final String argumentName, final String argumentValue, final Map<String, String> contextArguments) {
            completionReference = reference;
            completionArgumentName = argumentName;
            completionArgumentValue = argumentValue;
            this.contextArguments = contextArguments;
            return Map.of("completion", "public");
        }
        
        @Override
        public void close() {
        }
    }
}
