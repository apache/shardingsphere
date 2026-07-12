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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.tool.request.RuntimeDatabaseValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.RuntimeDatabaseValidationCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.response.RuntimeDatabaseValidationResult;
import org.apache.shardingsphere.mcp.support.database.tool.service.RuntimeDatabaseValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidateRuntimeDatabaseToolHandlerTest {
    
    @Test
    @SuppressWarnings("unchecked")
    void assertHandle() {
        try (
                MockedConstruction<RuntimeDatabaseValidationService> mocked =
                        mockConstruction(RuntimeDatabaseValidationService.class, (mock, context) -> when(mock.validate(any(), any(), any())).thenReturn(RuntimeDatabaseValidationResult.ready(
                                "logic_db", List.of(RuntimeDatabaseValidationCheckResult.passed("configuration", "Validated the request.")))))) {
            MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
            RuntimeDatabaseConfiguration runtimeDatabaseConfig = mock(RuntimeDatabaseConfiguration.class);
            when(databaseContext.findRuntimeDatabaseConfiguration("logic_db")).thenReturn(Optional.of(runtimeDatabaseConfig));
            MCPResponse actual = new ValidateRuntimeDatabaseToolHandler().handle(databaseContext, new MCPToolCall("session-1", Map.of("database", "logic_db")));
            assertThat(actual.toPayload().get("response_mode"), is("validation"));
            ArgumentCaptor<RuntimeDatabaseValidationRequest> requestCaptor = ArgumentCaptor.forClass(RuntimeDatabaseValidationRequest.class);
            ArgumentCaptor<Function<String, Optional<RuntimeDatabaseConfiguration>>> resolverCaptor = ArgumentCaptor.forClass(Function.class);
            verify(mocked.constructed().getFirst()).validate(requestCaptor.capture(), resolverCaptor.capture(), any());
            assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
            assertThat(resolverCaptor.getValue().apply("logic_db"), is(Optional.of(runtimeDatabaseConfig)));
        }
    }
}
