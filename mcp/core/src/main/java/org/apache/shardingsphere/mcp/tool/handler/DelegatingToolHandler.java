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

package org.apache.shardingsphere.mcp.tool.handler;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolInvoker;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;

import java.util.Map;

/**
 * Delegating tool handler backed by contribution metadata.
 */
@RequiredArgsConstructor
public final class DelegatingToolHandler implements ToolHandler {
    
    private final MCPToolDescriptor toolDescriptor;
    
    private final MCPToolInvoker toolInvoker;
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return toolDescriptor;
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        return toolInvoker.invoke(requestContext, sessionId, arguments);
    }
}
