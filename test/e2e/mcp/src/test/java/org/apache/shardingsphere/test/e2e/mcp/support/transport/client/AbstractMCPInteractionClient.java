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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;

import java.io.IOException;
import java.util.List;
import java.util.Map;

abstract class AbstractMCPInteractionClient implements MCPInteractionClient {
    
    @Override
    public final Map<String, Object> call(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return MCPInteractionPayloads.getStructuredContent(sendInitializedRequest(actionName + "-1", "tools/call", Map.of("name", actionName, "arguments", arguments)));
    }
    
    @Override
    public final List<Map<String, Object>> listTools() throws IOException, InterruptedException {
        return MCPInteractionPayloads.castToList(MCPInteractionPayloads.getJsonRpcResult(sendInitializedRequest("tools-list-1", "tools/list", Map.of())).get("tools"));
    }
    
    @Override
    public final Map<String, Object> listResources() throws IOException, InterruptedException {
        return MCPInteractionPayloads.getListResourcesPayload(sendInitializedRequest("resources-list-1", "resources/list", Map.of()));
    }
    
    @Override
    public final Map<String, Object> listResourceTemplates() throws IOException, InterruptedException {
        return MCPInteractionPayloads.getJsonRpcResult(sendInitializedRequest("resources-templates-list-1", "resources/templates/list", Map.of()));
    }
    
    @Override
    public final Map<String, Object> readResource(final String resourceUri) throws IOException, InterruptedException {
        return MCPInteractionPayloads.getFirstResourcePayload(sendInitializedRequest("resources-read-1", "resources/read", Map.of("uri", resourceUri)));
    }
    
    protected abstract void ensureOpened();
    
    protected abstract Map<String, Object> sendRequest(String requestId, String method, Map<String, Object> params) throws IOException, InterruptedException;
    
    private Map<String, Object> sendInitializedRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        ensureOpened();
        return sendRequest(requestId, method, params);
    }
}
