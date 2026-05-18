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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPHandlerDescriptorUtilsTest {
    
    @Test
    void assertGetRequiredToolDescriptor() {
        MCPToolHandler<?> handler = mock(MCPToolHandler.class);
        when(handler.getToolName()).thenReturn("database_gateway_apply_workflow");
        MCPToolDescriptor actual = MCPHandlerDescriptorUtils.getRequiredToolDescriptor(handler);
        assertThat(actual.getName(), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertGetRequiredResourceDescriptor() {
        MCPResourceHandler<?> handler = mock(MCPResourceHandler.class);
        when(handler.getResourceUriTemplate()).thenReturn("shardingsphere://workflows/{plan_id}");
        MCPResourceDescriptor actual = MCPHandlerDescriptorUtils.getRequiredResourceDescriptor(handler);
        assertThat(actual.getUriTemplate(), is("shardingsphere://workflows/{plan_id}"));
    }
}
