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

package org.apache.shardingsphere.mcp.resource.handler.metadata;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataResourceHandlerTest {
    
    @Test
    void assertGetUriPattern() {
        MetadataResourceHandler actual = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> List.of());
        assertThat(actual.getUriPattern(), is("shardingsphere://databases"));
    }
    
    @Test
    void assertHandle() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}",
                (requestContext, uriVariables) -> List.of(Map.of("database", uriVariables.getVariable("database"))));
        MCPUriVariables uriVariables = mock(MCPUriVariables.class);
        when(uriVariables.getVariable("database")).thenReturn("logic_db");
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), uriVariables);
        assertThat(actual.toPayload(), is(Map.of("items", List.of(Map.of("database", "logic_db")))));
    }
}
