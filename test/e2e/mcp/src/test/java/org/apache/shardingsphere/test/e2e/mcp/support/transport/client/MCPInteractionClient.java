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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * MCP interaction client.
 */
public interface MCPInteractionClient extends AutoCloseable {
    
    /**
     * Open.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    void open() throws IOException, InterruptedException;
    
    /**
     * Call.
     *
     * @param actionName action name
     * @param arguments arguments
     * @return MCP structured content payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> call(String actionName, Map<String, Object> arguments) throws IOException, InterruptedException;
    
    /**
     * Get initialize payload.
     *
     * @return raw initialize JSON-RPC payload
     */
    Map<String, Object> getInitializePayload();
    
    /**
     * List tools.
     *
     * @return MCP tool list payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    List<Map<String, Object>> listTools() throws IOException, InterruptedException;
    
    /**
     * List resources.
     *
     * @return MCP resources payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> listResources() throws IOException, InterruptedException;
    
    /**
     * List resource templates.
     *
     * @return MCP resource templates payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> listResourceTemplates() throws IOException, InterruptedException;
    
    /**
     * List prompts.
     *
     * @return MCP prompts payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> listPrompts() throws IOException, InterruptedException;
    
    /**
     * Get prompt.
     *
     * @param promptName prompt name
     * @param arguments prompt arguments
     * @return MCP prompt payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> getPrompt(String promptName, Map<String, Object> arguments) throws IOException, InterruptedException;
    
    /**
     * Complete one argument.
     *
     * @param reference prompt or resource reference
     * @param argumentName argument name
     * @param argumentValue argument prefix
     * @param contextArguments completion context arguments
     * @return MCP completion payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> complete(Map<String, Object> reference, String argumentName, String argumentValue,
                                 Map<String, String> contextArguments) throws IOException, InterruptedException;
    
    /**
     * Read resource.
     *
     * @param resourceUri resource URI
     * @return MCP resource payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> readResource(String resourceUri) throws IOException, InterruptedException;
    
    /**
     * Send raw JSON-RPC request.
     *
     * @param requestId request id
     * @param method method name
     * @param params request params
     * @return raw JSON-RPC payload
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    Map<String, Object> sendRawRequest(String requestId, String method, Map<String, Object> params) throws IOException, InterruptedException;
    
    /**
     * Send raw JSON-RPC notification.
     *
     * @param method method name
     * @param params notification params
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    void sendRawNotification(String method, Map<String, Object> params) throws IOException, InterruptedException;
    
    @Override
    void close() throws IOException, InterruptedException;
}
