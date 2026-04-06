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

package org.apache.shardingsphere.mcp.tool;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestFactory;
import org.apache.shardingsphere.mcp.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolControllerTest {
    
    @Test
    void assertHandleWithUnsupportedTool() {
        Map<String, Object> actual = createController().handle("session-1", "unsupported_tool", Map.of()).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Unsupported tool."));
    }
    
    @Test
    void assertHandleSearchMetadata() {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "order", "object_types", List.of("index"))).toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actual.get("items")).get(0)).getName(), is("order_idx"));
    }
    
    @Test
    void assertHandleExecuteQuery() {
        Map<String, Object> actual = createController().handle("session-1", "execute_query", Map.of("database", "logic_db", "sql", "SELECT 1")).toPayload();
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
    }
    
    @Test
    void assertHandleWithInvalidRequest() {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("schema", "public", "query", "orders")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertHandleWithMissingQuery() {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("database", "logic_db", "object_types", List.of(MetadataObjectType.TABLE.name()))).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Query is required."));
    }
    
    private MCPToolController createController() {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContextTestFactory().create(ResourceTestDataFactory.createDatabaseMetadataCatalog(), createStatementExecutor());
        runtimeContext.getSessionManager().createSession("session-1");
        return new MCPToolController(runtimeContext);
    }
    
    private MCPJdbcStatementExecutor createStatementExecutor() {
        MCPJdbcStatementExecutor result = mock(MCPJdbcStatementExecutor.class);
        when(result.execute(any(ExecutionRequest.class), any(ClassificationResult.class))).thenReturn(ExecuteQueryResponse.resultSet(
                List.of(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false)), List.of(List.of(1)), false));
        return result;
    }
}
