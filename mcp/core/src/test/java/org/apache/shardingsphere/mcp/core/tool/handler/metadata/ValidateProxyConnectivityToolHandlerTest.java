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
import org.apache.shardingsphere.mcp.support.database.tool.request.ProxyPreflightValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.ProxyPreflightCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.response.ProxyPreflightValidationResult;
import org.apache.shardingsphere.mcp.support.database.tool.service.ProxyPreflightValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidateProxyConnectivityToolHandlerTest {
    
    @Test
    void assertGetToolName() {
        assertThat(new ValidateProxyConnectivityToolHandler().getToolName(), is("database_gateway_validate_proxy_connectivity"));
    }
    
    @Test
    void assertHandle() {
        ProxyPreflightValidationService validationService = mock(ProxyPreflightValidationService.class);
        when(validationService.validate(any(), any()))
                .thenReturn(ProxyPreflightValidationResult.ready("logic_db", List.of(ProxyPreflightCheckResult.passed("configuration", "Validated the request."))));
        MCPResponse actual = new ValidateProxyConnectivityToolHandler(validationService).handle(mock(MCPDatabaseHandlerContext.class), new MCPToolCall("session-1", Map.of(
                "databaseType", "MySQL",
                "jdbcUrl", "jdbc:mysql://127.0.0.1:3307/logic_db",
                "username", "demo",
                "password", "  secret  ",
                "driverClassName", "com.mysql.cj.jdbc.Driver",
                "database", "logic_db")));
        assertThat(actual.toPayload().get("response_mode"), is("validation"));
        ArgumentCaptor<ProxyPreflightValidationRequest> requestCaptor = ArgumentCaptor.forClass(ProxyPreflightValidationRequest.class);
        verify(validationService).validate(requestCaptor.capture(), any());
        assertThat(requestCaptor.getValue().getPassword(), is("  secret  "));
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
    }
}
