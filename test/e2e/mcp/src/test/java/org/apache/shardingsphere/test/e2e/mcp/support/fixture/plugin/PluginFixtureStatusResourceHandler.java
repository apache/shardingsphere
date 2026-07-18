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

package org.apache.shardingsphere.test.e2e.mcp.support.fixture.plugin;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;

import java.util.List;
import java.util.Map;

/**
 * Test-only resource handler used to prove packaged distribution plugin discovery.
 */
public final class PluginFixtureStatusResourceHandler implements MCPResourceHandler<MCPRequestContext> {
    
    private static final String URI_PATTERN = "shardingsphere://features/test-fixture/status";
    
    @Override
    public Class<MCPRequestContext> getContextType() {
        return MCPRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return URI_PATTERN;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPRequestContext handlerContext, final MCPUriVariables uriVariables) {
        return new MCPMapPayload(Map.of("items", List.of(Map.of("feature", "test-fixture", "status", "ready"))));
    }
}
