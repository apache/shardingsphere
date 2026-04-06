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

package org.apache.shardingsphere.mcp.tool.handler;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestFactory;
import org.apache.shardingsphere.mcp.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolDispatchKind;
import org.apache.shardingsphere.mcp.tool.MetadataSearchHit;
import org.apache.shardingsphere.mcp.tool.handler.capability.GetCapabilitiesToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.execution.ExecuteQueryToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.DescribeTableToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.DescribeViewToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListColumnsToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListDatabasesToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListIndexesToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListSchemasToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListTablesToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.ListViewsToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.tool.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.tool.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.tool.response.MCPServiceCapabilityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolHandlerTest {
    
    private final MCPRuntimeContext runtimeContext = createRuntimeContext();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("descriptorCases")
    void assertGetToolDescriptor(final DescriptorCase descriptorCase) {
        MCPToolDescriptor actual = descriptorCase.getHandler().getToolDescriptor();
        assertThat(actual.getName(), is(descriptorCase.getExpectedToolName()));
        assertThat(actual.getDispatchKind(), is(descriptorCase.getExpectedDispatchKind()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertHandle(final HandlerCase handlerCase) {
        MCPResponse actual = handlerCase.getHandler().handle("session-1", runtimeContext, handlerCase.getArguments());
        Map<String, Object> actualPayload = actual.toPayload();
        if (HandlerResultType.DATABASE_CAPABILITY == handlerCase.getExpectedType()) {
            assertThat(actual, isA(MCPDatabaseCapabilityResponse.class));
            assertThat(actualPayload.get("database"), is(handlerCase.getExpectedDatabase()));
            return;
        }
        if (HandlerResultType.SERVICE_CAPABILITY == handlerCase.getExpectedType()) {
            assertThat(actual, isA(MCPServiceCapabilityResponse.class));
            assertTrue(((List<?>) actualPayload.get("supportedTools")).contains("execute_query"));
            return;
        }
        if (HandlerResultType.EXECUTION == handlerCase.getExpectedType()) {
            assertThat(actualPayload.get("result_kind"), is("result_set"));
            assertThat(((List<?>) actualPayload.get("rows")).size(), is(1));
            return;
        }
        assertThat(actual, isA(MCPMetadataResponse.class));
        assertThat(extractMetadataNames(actualPayload), is(handlerCase.getExpectedObjectNames()));
        assertThat(actualPayload.getOrDefault("next_page_token", ""), is(handlerCase.getExpectedNextPageToken()));
    }
    
    @Test
    void assertHandleWithUnsupportedIndexTool() {
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> new ListIndexesToolHandler().handle("session-1", runtimeContext, Map.of("database", "warehouse", "schema", "warehouse", "table", "facts")));
        assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertHandleWithInvalidExecuteQueryRequest() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new ExecuteQueryToolHandler().handle("session-1", runtimeContext, Map.of("database", "logic_db")));
        assertThat(actual.getMessage(), is("Database and sql are required."));
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        MCPRuntimeContext result = new MCPRuntimeContextTestFactory().create(ResourceTestDataFactory.createDatabaseMetadataCatalog(), createStatementExecutor());
        result.getSessionManager().createSession("session-1");
        return result;
    }
    
    private MCPJdbcStatementExecutor createStatementExecutor() {
        MCPJdbcStatementExecutor result = mock(MCPJdbcStatementExecutor.class);
        when(result.execute(any(ExecutionRequest.class), any(ClassificationResult.class))).thenReturn(ExecuteQueryResponse.resultSet(
                List.of(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false)), List.of(List.of(1)), false));
        return result;
    }
    
    private List<String> extractMetadataNames(final Map<String, Object> payload) {
        List<String> result = new LinkedList<>();
        for (Object each : getMetadataItems(payload)) {
            if (each instanceof MCPDatabaseMetadata) {
                result.add(((MCPDatabaseMetadata) each).getDatabase());
                continue;
            }
            if (each instanceof MCPSchemaMetadata) {
                result.add(((MCPSchemaMetadata) each).getSchema());
                continue;
            }
            if (each instanceof MCPTableMetadata) {
                result.add(((MCPTableMetadata) each).getTable());
                continue;
            }
            if (each instanceof MCPViewMetadata) {
                result.add(((MCPViewMetadata) each).getView());
                continue;
            }
            if (each instanceof MCPColumnMetadata) {
                result.add(((MCPColumnMetadata) each).getColumn());
                continue;
            }
            if (each instanceof MCPIndexMetadata) {
                result.add(((MCPIndexMetadata) each).getIndex());
                continue;
            }
            if (each instanceof MetadataSearchHit) {
                result.add(((MetadataSearchHit) each).getName());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getMetadataItems(final Map<String, Object> payload) {
        return (List<Object>) payload.get("items");
    }
    
    private static Stream<DescriptorCase> descriptorCases() {
        return Stream.of(
                new DescriptorCase("get capabilities descriptor", new GetCapabilitiesToolHandler(), "get_capabilities", MCPToolDispatchKind.CAPABILITY),
                new DescriptorCase("execute query descriptor", new ExecuteQueryToolHandler(), "execute_query", MCPToolDispatchKind.EXECUTION),
                new DescriptorCase("list databases descriptor", new ListDatabasesToolHandler(), "list_databases", MCPToolDispatchKind.METADATA),
                new DescriptorCase("list schemas descriptor", new ListSchemasToolHandler(), "list_schemas", MCPToolDispatchKind.METADATA),
                new DescriptorCase("list tables descriptor", new ListTablesToolHandler(), "list_tables", MCPToolDispatchKind.METADATA),
                new DescriptorCase("list views descriptor", new ListViewsToolHandler(), "list_views", MCPToolDispatchKind.METADATA),
                new DescriptorCase("list columns descriptor", new ListColumnsToolHandler(), "list_columns", MCPToolDispatchKind.METADATA),
                new DescriptorCase("list indexes descriptor", new ListIndexesToolHandler(), "list_indexes", MCPToolDispatchKind.METADATA),
                new DescriptorCase("search metadata descriptor", new SearchMetadataToolHandler(), "search_metadata", MCPToolDispatchKind.METADATA),
                new DescriptorCase("describe table descriptor", new DescribeTableToolHandler(), "describe_table", MCPToolDispatchKind.METADATA),
                new DescriptorCase("describe view descriptor", new DescribeViewToolHandler(), "describe_view", MCPToolDispatchKind.METADATA));
    }
    
    private static Stream<HandlerCase> handlerCases() {
        return Stream.of(
                new HandlerCase("service capabilities", new GetCapabilitiesToolHandler(), Map.of(), HandlerResultType.SERVICE_CAPABILITY, "", List.of(), ""),
                new HandlerCase("database capabilities", new GetCapabilitiesToolHandler(), Map.of("database", "logic_db"), HandlerResultType.DATABASE_CAPABILITY, "logic_db", List.of(), ""),
                new HandlerCase("execute query", new ExecuteQueryToolHandler(), Map.of("database", "logic_db", "sql", "SELECT 1"), HandlerResultType.EXECUTION, "", List.of(), ""),
                new HandlerCase("list databases", new ListDatabasesToolHandler(), Map.of(), HandlerResultType.METADATA, "", List.of("logic_db", "warehouse"), ""),
                new HandlerCase("list schemas", new ListSchemasToolHandler(), Map.of("database", "logic_db"), HandlerResultType.METADATA, "", List.of("public"), ""),
                new HandlerCase("list tables", new ListTablesToolHandler(), Map.of("database", "logic_db", "schema", "public", "search", "order", "page_size", 1),
                        HandlerResultType.METADATA, "", List.of("order_items"), "1"),
                new HandlerCase("list views", new ListViewsToolHandler(), Map.of("database", "logic_db", "schema", "public"), HandlerResultType.METADATA, "", List.of("orders_view"), ""),
                new HandlerCase("list columns", new ListColumnsToolHandler(),
                        Map.of("database", "logic_db", "schema", "public", "object_type", "table", "object_name", "orders"),
                        HandlerResultType.METADATA, "", List.of("order_id"), ""),
                new HandlerCase("list indexes", new ListIndexesToolHandler(), Map.of("database", "logic_db", "schema", "public", "table", "orders"),
                        HandlerResultType.METADATA, "", List.of("order_idx"), ""),
                new HandlerCase("search metadata", new SearchMetadataToolHandler(), Map.of("query", "order", "object_types", List.of("index")),
                        HandlerResultType.METADATA, "", List.of("order_idx"), ""),
                new HandlerCase("describe table", new DescribeTableToolHandler(), Map.of("database", "logic_db", "schema", "public", "table", "orders"),
                        HandlerResultType.METADATA, "", List.of("orders"), ""),
                new HandlerCase("describe view", new DescribeViewToolHandler(), Map.of("database", "logic_db", "schema", "public", "view", "orders_view"),
                        HandlerResultType.METADATA, "", List.of("orders_view"), ""));
    }
    
    private static final class DescriptorCase {
        
        private final String description;
        
        private final ToolHandler handler;
        
        private final String expectedToolName;
        
        private final MCPToolDispatchKind expectedDispatchKind;
        
        private DescriptorCase(final String description, final ToolHandler handler, final String expectedToolName, final MCPToolDispatchKind expectedDispatchKind) {
            this.description = description;
            this.handler = handler;
            this.expectedToolName = expectedToolName;
            this.expectedDispatchKind = expectedDispatchKind;
        }
        
        private ToolHandler getHandler() {
            return handler;
        }
        
        private String getExpectedToolName() {
            return expectedToolName;
        }
        
        private MCPToolDispatchKind getExpectedDispatchKind() {
            return expectedDispatchKind;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    private static final class HandlerCase {
        
        private final String description;
        
        private final ToolHandler handler;
        
        private final Map<String, Object> arguments;
        
        private final HandlerResultType expectedType;
        
        private final String expectedDatabase;
        
        private final List<String> expectedObjectNames;
        
        private final String expectedNextPageToken;
        
        private HandlerCase(final String description, final ToolHandler handler, final Map<String, Object> arguments, final HandlerResultType expectedType,
                            final String expectedDatabase, final List<String> expectedObjectNames, final String expectedNextPageToken) {
            this.description = description;
            this.handler = handler;
            this.arguments = arguments;
            this.expectedType = expectedType;
            this.expectedDatabase = expectedDatabase;
            this.expectedObjectNames = expectedObjectNames;
            this.expectedNextPageToken = expectedNextPageToken;
        }
        
        private ToolHandler getHandler() {
            return handler;
        }
        
        private Map<String, Object> getArguments() {
            return arguments;
        }
        
        private HandlerResultType getExpectedType() {
            return expectedType;
        }
        
        private String getExpectedDatabase() {
            return expectedDatabase;
        }
        
        private List<String> getExpectedObjectNames() {
            return expectedObjectNames;
        }
        
        private String getExpectedNextPageToken() {
            return expectedNextPageToken;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    private enum HandlerResultType {
        
        SERVICE_CAPABILITY,
        
        DATABASE_CAPABILITY,
        
        EXECUTION,
        
        METADATA
    }
}
