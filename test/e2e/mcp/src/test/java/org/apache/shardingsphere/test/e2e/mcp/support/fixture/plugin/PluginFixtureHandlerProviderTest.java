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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginFixtureHandlerProviderTest {
    
    @Test
    void assertGetToolHandlers() {
        MCPToolHandler<?> actual = new PluginFixtureHandlerProvider().getToolHandlers().iterator().next();
        assertThat(actual.getClass(), is(PluginFixturePingToolHandler.class));
        assertThat(actual.getToolName(), is("fixture_ping"));
        assertThat(actual.getContextType(), is(MCPRequestContext.class));
    }
    
    @Test
    void assertGetResourceHandlers() {
        MCPResourceHandler<?> actual = new PluginFixtureHandlerProvider().getResourceHandlers().iterator().next();
        assertThat(actual.getClass(), is(PluginFixtureStatusResourceHandler.class));
        assertThat(actual.getResourceUriTemplate(), is("shardingsphere://features/test-fixture/status"));
        assertThat(actual.getContextType(), is(MCPRequestContext.class));
    }
    
    @Test
    void assertGetCompletionHandlers() {
        assertTrue(new PluginFixtureHandlerProvider().getCompletionHandlers().isEmpty());
    }
}
