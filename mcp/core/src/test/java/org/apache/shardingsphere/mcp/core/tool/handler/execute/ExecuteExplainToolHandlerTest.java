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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecuteExplainToolHandlerTest {
    
    @Test
    void assertHandleExplainQuery() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.executeExplain(any(), any())).thenReturn(createExplainResult());
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getSessionId()).thenReturn("session-1");
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPSuccessPayload actual = new ExecuteExplainToolHandler().handle(requestContext,
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN SELECT * FROM orders"));
        assertThat(actual.toPayload().get("statement_class"), is("explain"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).executeExplain(requestCaptor.capture(), eq("SELECT * FROM orders"));
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
        assertThat(requestCaptor.getValue().getSql(), is("EXPLAIN SELECT * FROM orders"));
        assertTrue(requestCaptor.getValue().isReadOnlyExecution());
    }
    
    @Test
    void assertHandleExplainQueryWithSingleSchemaDefault() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.executeExplain(any(), any())).thenReturn(createExplainResult());
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(new ShardingSphereSchema("public", mock(DatabaseType.class))));
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getSessionId()).thenReturn("session-1");
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        when(requestContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        new ExecuteExplainToolHandler().handle(requestContext, Map.of("database", "logic_db", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN SELECT * FROM orders"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).executeExplain(requestCaptor.capture(), eq("SELECT * FROM orders"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
    }
    
    private SQLExecutionResult createExplainResult() {
        return SQLExecutionResult.resultSet(SupportedMCPStatement.EXPLAIN, "EXPLAIN", List.of(), List.of(), false, 100, 0, "EXPLAIN SELECT * FROM orders");
    }
}
