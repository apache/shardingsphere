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

package org.apache.shardingsphere.mcp.core.handler.core;

import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreHandlerProviderTest {
    
    @Test
    void assertGetToolHandlers() {
        Collection<MCPToolHandler<?>> actual = new CoreHandlerProvider().getToolHandlers();
        assertThat(actual.stream().map(MCPToolHandler::getToolName).toList(),
                is(List.of("database_gateway_search_metadata", "database_gateway_validate_proxy_connectivity", "database_gateway_execute_query", "database_gateway_execute_update",
                        "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
    }
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new CoreHandlerProvider().getResourceHandlers();
        assertThat(actual.size(), is(20));
        List<String> actualUris = actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList();
        assertTrue(actualUris.contains("shardingsphere://capabilities"));
        assertTrue(actualUris.contains("shardingsphere://runtime"));
        assertTrue(actualUris.contains("shardingsphere://workflows/{plan_id}"));
        assertTrue(actualUris.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
}
