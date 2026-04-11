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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import org.apache.shardingsphere.test.e2e.mcp.runtime.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.MCPInteractionResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class StubMCPInteractionClient implements MCPInteractionClient {
    
    private final RuntimeException openFailure;
    
    private final Map<String, MCPInteractionResponse> responses;
    
    private final Map<String, MCPInteractionResponse> resourceResponses;
    
    private final MCPInteractionResponse resourceListResponse;
    
    StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses) {
        this(null, responses, Map.of(), new MCPInteractionResponse(Map.of("resources", List.of()), "{}"));
    }
    
    StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses) {
        this(null, responses, resourceResponses, new MCPInteractionResponse(Map.of("resources", List.of()), "{}"));
    }
    
    StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses,
                             final MCPInteractionResponse resourceListResponse) {
        this(null, responses, resourceResponses, resourceListResponse);
    }
    
    StubMCPInteractionClient(final RuntimeException openFailure, final Map<String, MCPInteractionResponse> responses) {
        this(openFailure, responses, Map.of(), new MCPInteractionResponse(Map.of("resources", List.of()), "{}"));
    }
    
    StubMCPInteractionClient(final RuntimeException openFailure, final Map<String, MCPInteractionResponse> responses,
                             final Map<String, MCPInteractionResponse> resourceResponses, final MCPInteractionResponse resourceListResponse) {
        this.openFailure = openFailure;
        this.responses = new LinkedHashMap<>(responses);
        this.resourceResponses = new LinkedHashMap<>(resourceResponses);
        this.resourceListResponse = resourceListResponse;
    }
    
    @Override
    public void open() {
        if (null != openFailure) {
            throw openFailure;
        }
    }
    
    @Override
    public MCPInteractionResponse call(final String toolName, final Map<String, Object> arguments) throws IOException {
        if (!responses.containsKey(toolName)) {
            throw new IOException("Unsupported tool in stub: " + toolName);
        }
        return responses.get(toolName);
    }
    
    @Override
    public MCPInteractionResponse readResource(final String resourceUri) throws IOException {
        if (!resourceResponses.containsKey(resourceUri)) {
            throw new IOException("Unsupported resource in stub: " + resourceUri);
        }
        return resourceResponses.get(resourceUri);
    }
    
    @Override
    public MCPInteractionResponse listResources() {
        return resourceListResponse;
    }
    
    @Override
    public void close() {
    }
}
