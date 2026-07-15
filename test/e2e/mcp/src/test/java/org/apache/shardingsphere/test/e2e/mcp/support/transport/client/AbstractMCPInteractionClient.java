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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractMCPInteractionClient implements MCPInteractionClient {
    
    @Override
    public final Map<String, Object> call(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return MCPInteractionPayloads.getToolCallPayload(sendInitializedRequest(actionName + "-1", "tools/call", Map.of("name", actionName, "arguments", arguments)));
    }
    
    @Override
    public final List<Map<String, Object>> listTools() throws IOException, InterruptedException {
        return MCPInteractionPayloads.getRequiredObjectList(
                MCPInteractionPayloads.getRequiredJsonRpcResult(sendInitializedRequest("tools-list-1", "tools/list", Map.of())), "tools");
    }
    
    @Override
    public final Map<String, Object> listResources() throws IOException, InterruptedException {
        return MCPInteractionPayloads.getListResourcesPayload(sendInitializedRequest("resources-list-1", "resources/list", Map.of()));
    }
    
    @Override
    public final Map<String, Object> listResourceTemplates() throws IOException, InterruptedException {
        return getObjectListResultOrError(sendInitializedRequest("resources-templates-list-1", "resources/templates/list", Map.of()), "resourceTemplates");
    }
    
    @Override
    public final Map<String, Object> readResource(final String resourceUri) throws IOException, InterruptedException {
        return MCPInteractionPayloads.getFirstResourcePayload(sendInitializedRequest("resources-read-1", "resources/read", Map.of("uri", resourceUri)));
    }
    
    @Override
    public final Map<String, Object> sendRawRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        return sendInitializedRequest(requestId, method, params);
    }
    
    @Override
    public final Map<String, Object> listPrompts() throws IOException, InterruptedException {
        return getObjectListResultOrError(sendInitializedRequest("prompts-list-1", "prompts/list", Map.of()), "prompts");
    }
    
    @Override
    public final Map<String, Object> getPrompt(final String promptName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return getObjectListResultOrError(sendInitializedRequest("prompts-get-1", "prompts/get", Map.of("name", promptName, "arguments", arguments)), "messages");
    }
    
    @Override
    public final Map<String, Object> complete(final Map<String, Object> reference, final String argumentName, final String argumentValue,
                                              final Map<String, String> contextArguments) throws IOException, InterruptedException {
        Map<String, Object> params = new LinkedHashMap<>(3, 1F);
        params.put("ref", reference);
        params.put("argument", Map.of("name", argumentName, "value", argumentValue));
        if (!contextArguments.isEmpty()) {
            params.put("context", Map.of("arguments", contextArguments));
        }
        Map<String, Object> payload = sendInitializedRequest("completion-complete-1", "completion/complete", params);
        if (MCPInteractionPayloads.hasJsonRpcError(payload)) {
            return MCPInteractionPayloads.getJsonRpcErrorPayload(payload);
        }
        Map<String, Object> result = MCPInteractionPayloads.getRequiredJsonRpcResult(payload);
        MCPInteractionPayloads.getRequiredObject(result, "completion");
        return result;
    }
    
    protected abstract void ensureOpened();
    
    protected abstract Map<String, Object> sendRequest(String requestId, String method, Map<String, Object> params) throws IOException, InterruptedException;
    
    private Map<String, Object> sendInitializedRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        ensureOpened();
        return sendRequest(requestId, method, params);
    }
    
    private Map<String, Object> getObjectListResultOrError(final Map<String, Object> payload, final String fieldName) {
        if (MCPInteractionPayloads.hasJsonRpcError(payload)) {
            return MCPInteractionPayloads.getJsonRpcErrorPayload(payload);
        }
        Map<String, Object> result = MCPInteractionPayloads.getRequiredJsonRpcResult(payload);
        MCPInteractionPayloads.getRequiredObjectList(result, fieldName);
        return result;
    }
}
