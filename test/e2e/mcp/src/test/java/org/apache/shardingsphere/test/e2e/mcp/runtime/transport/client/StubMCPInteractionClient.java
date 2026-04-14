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

package org.apache.shardingsphere.test.e2e.mcp.runtime.transport.client;

import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StubMCPInteractionClient implements MCPInteractionClient {
    
    private static final MCPInteractionResponse EMPTY_RESOURCE_LIST_RESPONSE = new MCPInteractionResponse(Map.of("resources", List.of()), "{}");
    
    private final Exception openFailure;
    
    private final Map<String, MCPInteractionResponse> responses;
    
    private final Map<String, Exception> toolFailures;
    
    private final Map<String, MCPInteractionResponse> resourceResponses;
    
    private final Map<String, Exception> resourceFailures;
    
    private final MCPInteractionResponse resourceListResponse;
    
    public StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses) {
        this(null, responses, Map.of(), Map.of(), Map.of(), EMPTY_RESOURCE_LIST_RESPONSE);
    }
    
    public StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses) {
        this(null, responses, resourceResponses, Map.of(), Map.of(), EMPTY_RESOURCE_LIST_RESPONSE);
    }
    
    public StubMCPInteractionClient(final Map<String, MCPInteractionResponse> responses, final Map<String, MCPInteractionResponse> resourceResponses,
                                    final MCPInteractionResponse resourceListResponse) {
        this(null, responses, resourceResponses, Map.of(), Map.of(), resourceListResponse);
    }
    
    public StubMCPInteractionClient(final Exception openFailure, final Map<String, MCPInteractionResponse> responses) {
        this(openFailure, responses, Map.of(), Map.of(), Map.of(), EMPTY_RESOURCE_LIST_RESPONSE);
    }
    
    private StubMCPInteractionClient(final Exception openFailure, final Map<String, MCPInteractionResponse> responses,
                                     final Map<String, MCPInteractionResponse> resourceResponses, final Map<String, Exception> toolFailures,
                                     final Map<String, Exception> resourceFailures, final MCPInteractionResponse resourceListResponse) {
        this.openFailure = openFailure;
        this.responses = new LinkedHashMap<>(responses);
        this.toolFailures = new LinkedHashMap<>(toolFailures);
        this.resourceResponses = new LinkedHashMap<>(resourceResponses);
        this.resourceFailures = new LinkedHashMap<>(resourceFailures);
        this.resourceListResponse = resourceListResponse;
    }
    
    @Override
    public void open() throws IOException, InterruptedException {
        throwFailure(openFailure);
    }
    
    @Override
    public MCPInteractionResponse call(final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        if (toolFailures.containsKey(toolName)) {
            throwFailure(toolFailures.get(toolName));
        }
        if (!responses.containsKey(toolName)) {
            throw new IOException("Unsupported tool in stub: " + toolName);
        }
        return responses.get(toolName);
    }
    
    @Override
    public MCPInteractionResponse readResource(final String resourceUri) throws IOException, InterruptedException {
        if (resourceFailures.containsKey(resourceUri)) {
            throwFailure(resourceFailures.get(resourceUri));
        }
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
    
    /**
     * Create stub with tool failure.
     *
     * @param toolName tool name
     * @param failure failure
     * @return stub MCP interaction client
     */
    public static StubMCPInteractionClient createWithToolFailure(final String toolName, final Exception failure) {
        return new StubMCPInteractionClient(null, Map.of(), Map.of(), Map.of(toolName, failure), Map.of(), EMPTY_RESOURCE_LIST_RESPONSE);
    }
    
    /**
     * Create stub with resource failure.
     *
     * @param resourceUri resource URI
     * @param failure failure
     * @return stub MCP interaction client
     */
    public static StubMCPInteractionClient createWithResourceFailure(final String resourceUri, final Exception failure) {
        return new StubMCPInteractionClient(null, Map.of(), Map.of(), Map.of(), Map.of(resourceUri, failure), EMPTY_RESOURCE_LIST_RESPONSE);
    }
    
    private void throwFailure(final Exception failure) throws IOException, InterruptedException {
        if (null == failure) {
            return;
        }
        if (failure instanceof IOException) {
            throw (IOException) failure;
        }
        if (failure instanceof InterruptedException) {
            throw (InterruptedException) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        throw new IllegalStateException("Unsupported stub failure type: " + failure.getClass().getName(), failure);
    }
}
