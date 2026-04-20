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

package org.apache.shardingsphere.mcp.bootstrap.fixture.plugin;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;

import java.util.List;
import java.util.Map;

/**
 * Test-only resource handler used to prove bootstrap discovers arbitrary SPI plugins from classpath.
 */
public final class FixtureStatusResourceHandler implements ResourceHandler {
    
    private static final String URI_PATTERN = "shardingsphere://features/test-fixture/status";
    
    @Override
    public String getUriPattern() {
        return URI_PATTERN;
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final MCPUriVariables uriVariables) {
        return new MCPMapResponse(Map.of("items", List.of(Map.of("feature", "test-fixture", "status", "ready"))));
    }
}
