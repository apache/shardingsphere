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
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MetadataSearchHit;
import org.apache.shardingsphere.mcp.tool.handler.type.ExecuteQueryToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.type.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.tool.response.MCPMetadataResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolHandlerTest {
    
    private final MCPRuntimeContext runtimeContext = createRuntimeContext();
    
    @Test
    void assertGetSearchMetadataToolDescriptor() {
        MCPToolDescriptor actual = new SearchMetadataToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("search_metadata"));
        assertThat(actual.getInputDefinition().getFields().size(), is(6));
    }
    
    @Test
    void assertHandleSearchMetadata() {
        MCPResponse actual = new SearchMetadataToolHandler().handle(runtimeContext, "session-1", Map.of("query", "order", "object_types", List.of(MetadataObjectType.INDEX.name())));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actual, isA(MCPMetadataResponse.class));
        assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).get(0)).getName(), is("order_idx"));
    }
    
    @Test
    void assertHandleSearchMetadataWithMissingQuery() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new SearchMetadataToolHandler().handle(runtimeContext, "session-1", Map.of("database", "logic_db")));
        assertThat(actual.getMessage(), is("Query is required."));
    }
    
    @Test
    void assertGetExecuteQueryToolDescriptor() {
        MCPToolDescriptor actual = new ExecuteQueryToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("execute_query"));
        assertThat(actual.getInputDefinition().getFields().size(), is(5));
    }
    
    @Test
    void assertHandleExecuteQuery() {
        Map<String, Object> actual = new ExecuteQueryToolHandler().handle(runtimeContext, "session-1", Map.of("database", "logic_db", "sql", "SELECT 1")).toPayload();
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
    }
    
    @Test
    void assertHandleWithInvalidExecuteQueryRequest() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new ExecuteQueryToolHandler().handle(runtimeContext, "session-1", Map.of("database", "logic_db")));
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
}
