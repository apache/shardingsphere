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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AbstractMCPInteractionClientTest {
    
    @Test
    void assertCall() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of(
                "content", List.of(Map.of("type", "text", "text", "ok")), "structuredContent", Map.of("status", "ok"))));
        assertThat(client.call("fixture_ping", Map.of("message", "hello")), is(Map.of("status", "ok")));
        assertThat(client.requestId, is("fixture_ping-1"));
        assertThat(client.method, is("tools/call"));
        assertThat(client.params, is(Map.of("name", "fixture_ping", "arguments", Map.of("message", "hello"))));
        assertThat(client.openCount, is(1));
    }
    
    @Test
    void assertListTools() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("tools", List.of(Map.of("name", "fixture_ping")))));
        assertThat(client.listTools(), is(List.of(Map.of("name", "fixture_ping"))));
        assertThat(client.requestId, is("tools-list-1"));
        assertThat(client.method, is("tools/list"));
        assertThat(client.params, is(Map.of()));
    }
    
    @Test
    void assertListResources() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("resources", List.of(Map.of("uri", "shardingsphere://databases")))));
        assertThat(client.listResources(), is(Map.of("resources", List.of(Map.of("uri", "shardingsphere://databases")))));
        assertThat(client.requestId, is("resources-list-1"));
        assertThat(client.method, is("resources/list"));
    }
    
    @Test
    void assertListResourceTemplates() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("resourceTemplates", List.of(Map.of("uriTemplate", "shardingsphere://databases/{database}")))));
        assertThat(client.listResourceTemplates(), is(Map.of("resourceTemplates", List.of(Map.of("uriTemplate", "shardingsphere://databases/{database}")))));
        assertThat(client.requestId, is("resources-templates-list-1"));
        assertThat(client.method, is("resources/templates/list"));
    }
    
    @Test
    void assertReadResource() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("contents", List.of(Map.of("text", "{\"items\":[{\"name\":\"orders\"}]}")))));
        assertThat(client.readResource("shardingsphere://databases"), is(Map.of("items", List.of(Map.of("name", "orders")))));
        assertThat(client.requestId, is("resources-read-1"));
        assertThat(client.method, is("resources/read"));
        assertThat(client.params, is(Map.of("uri", "shardingsphere://databases")));
    }
    
    @Test
    void assertSendRawRequest() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("ok", true)));
        assertThat(client.sendRawRequest("id", "custom/method", Map.of("name", "value")), is(Map.of("result", Map.of("ok", true))));
        assertThat(client.requestId, is("id"));
        assertThat(client.method, is("custom/method"));
        assertThat(client.params, is(Map.of("name", "value")));
    }
    
    @Test
    void assertSendRawNotification() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of());
        client.sendRawNotification("notifications/cancelled", Map.of("requestId", "id", "reason", "not needed"));
        assertThat(client.method, is("notifications/cancelled"));
        assertThat(client.params, is(Map.of("requestId", "id", "reason", "not needed")));
        assertThat(client.openCount, is(1));
    }
    
    @Test
    void assertListPrompts() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("prompts", List.of(Map.of("name", "inspect_metadata")))));
        assertThat(client.listPrompts(), is(Map.of("prompts", List.of(Map.of("name", "inspect_metadata")))));
        assertThat(client.requestId, is("prompts-list-1"));
        assertThat(client.method, is("prompts/list"));
    }
    
    @Test
    void assertNormalizeListPromptsError() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("error", Map.of("message", "denied")));
        assertThat(client.listPrompts(), is(Map.of("error_code", "json_rpc_error", "message", "denied")));
    }
    
    @Test
    void assertGetPrompt() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("messages", List.of(Map.of("role", "user")))));
        assertThat(client.getPrompt("inspect_metadata", Map.of("database", "logic_db")), is(Map.of("messages", List.of(Map.of("role", "user")))));
        assertThat(client.requestId, is("prompts-get-1"));
        assertThat(client.method, is("prompts/get"));
        assertThat(client.params, is(Map.of("name", "inspect_metadata", "arguments", Map.of("database", "logic_db"))));
    }
    
    @Test
    void assertComplete() throws IOException, InterruptedException {
        Map<String, Object> completion = Map.of("values", List.of("public"));
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("completion", completion)));
        assertThat(client.complete(Map.of("type", "ref/prompt", "name", "inspect_metadata"), "schema", "pub", Map.of("database", "logic_db")), is(Map.of("completion", completion)));
        assertThat(client.requestId, is("completion-complete-1"));
        assertThat(client.method, is("completion/complete"));
        assertThat(client.params, is(Map.of(
                "ref", Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "argument", Map.of("name", "schema", "value", "pub"),
                "context", Map.of("arguments", Map.of("database", "logic_db")))));
    }
    
    @Test
    void assertCompleteWithoutContextArguments() throws IOException, InterruptedException {
        FakeMCPInteractionClient client = new FakeMCPInteractionClient(Map.of("result", Map.of("completion", Map.of("values", List.of("public")))));
        client.complete(Map.of("type", "ref/prompt", "name", "inspect_metadata"), "schema", "pub", Map.of());
        assertThat(client.params, is(Map.of(
                "ref", Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "argument", Map.of("name", "schema", "value", "pub"))));
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FakeMCPInteractionClient extends AbstractMCPInteractionClient {
        
        private final Map<String, Object> response;
        
        private int openCount;
        
        private String requestId;
        
        private String method;
        
        private Map<String, Object> params = Map.of();
        
        @Override
        public void open() {
        }
        
        @Override
        public Map<String, Object> getInitializePayload() {
            throw new UnsupportedOperationException("initialize payload is not available.");
        }
        
        @Override
        public void close() {
        }
        
        @Override
        protected void ensureOpened() {
            openCount++;
        }
        
        @Override
        protected Map<String, Object> sendRequest(final String requestId, final String method, final Map<String, Object> params) {
            this.requestId = requestId;
            this.method = method;
            this.params = params;
            return response;
        }
        
        @Override
        protected void sendNotification(final String method, final Map<String, Object> params) {
            this.method = method;
            this.params = params;
        }
    }
}
