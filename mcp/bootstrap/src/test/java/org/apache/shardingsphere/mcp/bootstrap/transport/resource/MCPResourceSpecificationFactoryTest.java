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

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MCPResourceSpecificationFactoryTest {
    
    @Test
    void assertCreateResourceSpecifications() {
        MCPResourceSpecificationFactory factory = createFactory();
        List<SyncResourceSpecification> actual = factory.createResourceSpecifications();
        assertThat(actual.size(), is(2));
        assertTrue(actual.stream().noneMatch(each -> each.resource().uri().contains("{")));
        assertTrue(actual.stream().map(each -> each.resource().uri()).toList().contains("shardingsphere://capabilities"));
        assertTrue(actual.stream().map(each -> each.resource().uri()).toList().contains("shardingsphere://databases"));
        SyncResourceSpecification actualSpecification = findResourceSpecification(actual, "shardingsphere://capabilities");
        assertThat(actualSpecification.resource().name(), is("capabilities"));
        assertThat(actualSpecification.resource().description(), is("ShardingSphere MCP resource: shardingsphere://capabilities"));
        assertThat(actualSpecification.resource().mimeType(), is("application/json"));
        assertNotNull(actualSpecification.readHandler());
        McpSchema.ReadResourceResult actualResult = actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new McpSchema.ReadResourceRequest("shardingsphere://capabilities"));
        assertThat(actualResult.contents().size(), is(1));
        assertThat(actualResult.contents().get(0), isA(TextResourceContents.class));
        TextResourceContents actualContent = (TextResourceContents) actualResult.contents().get(0);
        Map<String, Object> actualPayload = JsonUtils.fromJsonString(actualContent.text(), new TypeReference<>() {
        });
        assertTrue(((List<?>) actualPayload.get("supportedResources")).contains("shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}"));
        assertTrue(((List<?>) actualPayload.get("supportedTools")).contains("search_metadata"));
        assertTrue(((List<?>) actualPayload.get("supportedStatementClasses")).contains("QUERY"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCreateResourceTemplateSpecificationsArguments")
    void assertCreateResourceTemplateSpecifications(final String name, final String uriTemplate, final String actualUri, final String expectedName,
                                                    final String expectedItemKey, final String expectedItemValue) {
        MCPResourceSpecificationFactory factory = createFactory();
        List<SyncResourceTemplateSpecification> actual = factory.createResourceTemplateSpecifications();
        assertTrue(actual.stream().allMatch(each -> each.resourceTemplate().uriTemplate().contains("{")));
        SyncResourceTemplateSpecification actualSpecification = findResourceTemplateSpecification(actual, uriTemplate);
        assertThat(actualSpecification.resourceTemplate().name(), is(expectedName));
        assertThat(actualSpecification.resourceTemplate().description(), is("ShardingSphere MCP resource template: " + uriTemplate));
        assertThat(actualSpecification.resourceTemplate().mimeType(), is("application/json"));
        assertNotNull(actualSpecification.readHandler());
        McpSchema.ReadResourceResult actualResult = actualSpecification.readHandler().apply(mock(McpSyncServerExchange.class), new McpSchema.ReadResourceRequest(actualUri));
        assertThat(actualResult.contents().size(), is(1));
        assertThat(actualResult.contents().get(0), isA(TextResourceContents.class));
        TextResourceContents actualContent = (TextResourceContents) actualResult.contents().get(0);
        Map<String, Object> actualPayload = JsonUtils.fromJsonString(actualContent.text(), new TypeReference<>() {
        });
        assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("items")).get(0)).get(expectedItemKey), is(expectedItemValue));
    }
    
    private static Stream<Arguments> assertCreateResourceTemplateSpecificationsArguments() {
        return Stream.of(
                Arguments.of("database resource", "shardingsphere://databases/{database}", "shardingsphere://databases/logic_db", "{database}", "database", "logic_db"),
                Arguments.of("schema resource", "shardingsphere://databases/{database}/schemas/{schema}", "shardingsphere://databases/logic_db/schemas/public", "{schema}", "schema", "public"),
                Arguments.of("sequence resource", "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq", "{sequence}", "sequence", "order_seq"));
    }
    
    private SyncResourceSpecification findResourceSpecification(final List<SyncResourceSpecification> specifications, final String uri) {
        return specifications.stream().filter(each -> uri.equals(each.resource().uri())).findFirst().orElseThrow(IllegalStateException::new);
    }
    
    private SyncResourceTemplateSpecification findResourceTemplateSpecification(final List<SyncResourceTemplateSpecification> specifications, final String uriTemplate) {
        return specifications.stream().filter(each -> uriTemplate.equals(each.resourceTemplate().uriTemplate())).findFirst().orElseThrow(IllegalStateException::new);
    }
    
    private MCPResourceSpecificationFactory createFactory() {
        return new MCPResourceSpecificationFactory(new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), createMetadataCatalog()));
    }
    
    private MCPDatabaseMetadataCatalog createMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(2, 1F);
        databaseMetadataMap.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(), List.of(), List.of()))));
        databaseMetadataMap.put("runtime_db", new MCPDatabaseMetadata("runtime_db", "H2", "",
                List.of(new MCPSchemaMetadata("runtime_db", "public", List.of(), List.of(), List.of(new MCPSequenceMetadata("runtime_db", "public", "order_seq"))))));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
}
