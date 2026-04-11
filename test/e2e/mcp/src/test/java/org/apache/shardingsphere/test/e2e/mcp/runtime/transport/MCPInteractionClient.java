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

package org.apache.shardingsphere.test.e2e.mcp.runtime.transport;

import java.io.IOException;
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
     * @return MCP interaction response
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    MCPInteractionResponse call(String actionName, Map<String, Object> arguments) throws IOException, InterruptedException;
    
    /**
     * List resources.
     *
     * @return MCP interaction response
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     * @throws UnsupportedOperationException unsupported operation exception
     */
    default MCPInteractionResponse listResources() throws IOException, InterruptedException {
        throw new UnsupportedOperationException("resources/list is not supported.");
    }
    
    /**
     * Read resource.
     *
     * @param resourceUri resource URI
     * @return MCP interaction response
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     * @throws UnsupportedOperationException unsupported operation exception
     */
    default MCPInteractionResponse readResource(final String resourceUri) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("resources/read is not supported.");
    }
    
    @Override
    void close() throws IOException, InterruptedException;
}
