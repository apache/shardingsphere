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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.tool.request.ProxyPreflightValidationRequest;
import org.apache.shardingsphere.mcp.support.database.tool.service.ProxyPreflightValidationService;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.Map;

/**
 * Handler for runtime database validation tool.
 */
@RequiredArgsConstructor
public final class ValidateProxyConnectivityToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    public static final String TOOL_NAME = "database_gateway_validate_runtime_database";
    
    private final ProxyPreflightValidationService validationService;
    
    public ValidateProxyConnectivityToolHandler() {
        this(new ProxyPreflightValidationService());
    }
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPToolCall toolCall) {
        return validationService.validate(ProxyPreflightValidationRequest.from(toolCall.getArguments()), databaseContext::findRuntimeDatabaseConfiguration, this::createRecoveryPayload);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> createRecoveryPayload(final RuntimeDatabaseConnectionException cause) {
        Object result = MCPErrorConverter.convert(cause).toPayload().get(MCPPayloadFieldNames.RECOVERY);
        return result instanceof Map ? (Map<String, Object>) result : Map.of();
    }
}
