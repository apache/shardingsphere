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

package org.apache.shardingsphere.mcp.core.resource;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP resource controller.
 */
@RequiredArgsConstructor
public final class MCPResourceController {

    private final MCPRuntimeContext runtimeContext;

    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return MCP response
     */
    public MCPResponse handle(final String resourceUri) {
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            return ResourceHandlerRegistry.dispatch(requestScope, resourceUri).orElseThrow(() -> new UnsupportedResourceUriException(resourceUri));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return createResourceErrorResponse(resourceUri, MCPErrorConverter.convert(ex));
        }
    }

    private MCPResponse createResourceErrorResponse(final String resourceUri, final MCPErrorResponse errorResponse) {
        final Map<String, Object> errorPayload = errorResponse.toPayload();
        final Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_kind", "error");
        result.put("error_code", errorPayload.get("error_code"));
        result.put("message", errorPayload.get("message"));
        result.put("original_uri", resourceUri);
        final Map<String, Object> recovery = getRecovery(errorPayload);
        result.put("recoverable", !recovery.isEmpty());
        result.put("next_actions", recovery.getOrDefault("next_actions", List.of()));
        if (!recovery.isEmpty()) {
            result.put("recovery", recovery);
        }
        return new MCPMapResponse(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getRecovery(final Map<String, Object> errorPayload) {
        final Object recovery = errorPayload.get("recovery");
        return recovery instanceof Map ? (Map<String, Object>) recovery : Map.of();
    }
}
