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

package org.apache.shardingsphere.mcp.bootstrap.transport.resource;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.mcp.resource.MCPResourceController;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPResourceSpecificationFactoryTest {
    
    @Test
    void assertCreateResourceSpecifications() {
        MCPResourceSpecificationFactory actualFactory = new MCPResourceSpecificationFactory(
                List.of("shardingsphere://capabilities", "shardingsphere://databases/{database}"), mock(MCPResourceController.class));
        List<SyncResourceSpecification> actual = actualFactory.createResourceSpecifications();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).resource().uri(), is("shardingsphere://capabilities"));
        assertThat(actual.get(0).resource().name(), is("capabilities"));
        assertThat(actual.get(0).resource().description(), is("ShardingSphere MCP resource: shardingsphere://capabilities"));
        assertThat(actual.get(0).resource().mimeType(), is("application/json"));
        assertNotNull(actual.get(0).readHandler());
    }
    
    @Test
    void assertCreateResourceSpecificationsHandleReadResource() {
        MCPResourceController resourceController = mock(MCPResourceController.class);
        Map<String, Object> expectedPayload = Map.of("status", "ok");
        when(resourceController.handle("shardingsphere://capabilities")).thenReturn(() -> expectedPayload);
        SyncResourceSpecification actualSpecification = new MCPResourceSpecificationFactory(List.of("shardingsphere://capabilities"), resourceController)
                .createResourceSpecifications().get(0);
        ReadResourceResult actual = actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new ReadResourceRequest("shardingsphere://capabilities"));
        verify(resourceController).handle("shardingsphere://capabilities");
        assertThat(((TextResourceContents) actual.contents().get(0)).text(), is("{\"status\":\"ok\"}"));
    }
    
    @Test
    void assertCreateResourceTemplateSpecifications() {
        MCPResourceSpecificationFactory actualFactory = new MCPResourceSpecificationFactory(
                List.of("shardingsphere://capabilities", "shardingsphere://databases/{database}"), mock(MCPResourceController.class));
        List<SyncResourceTemplateSpecification> actual = actualFactory.createResourceTemplateSpecifications();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).resourceTemplate().uriTemplate(), is("shardingsphere://databases/{database}"));
        assertThat(actual.get(0).resourceTemplate().name(), is("{database}"));
        assertThat(actual.get(0).resourceTemplate().description(), is("ShardingSphere MCP resource template: shardingsphere://databases/{database}"));
        assertThat(actual.get(0).resourceTemplate().mimeType(), is("application/json"));
        assertNotNull(actual.get(0).readHandler());
    }
}
