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

package org.apache.shardingsphere.mcp.resource.handler.plugin;

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.apache.shardingsphere.mcp.tool.service.workflow.RuleInspectionService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginResourceHandlerTest {
    
    @Test
    void assertHandleEncryptAlgorithms() {
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        List<Map<String, Object>> rawRows = List.of(Map.of("type", "AES"));
        List<Map<String, Object>> enrichedRows = List.of(Map.of("type", "AES", "source", "builtin"));
        try (
                MockedConstruction<RuleInspectionService> mockedConstruction = mockConstruction(RuleInspectionService.class,
                        (mock, context) -> {
                            when(mock.queryEncryptAlgorithms(same(requestContext))).thenReturn(rawRows);
                            when(mock.enrichEncryptAlgorithms(rawRows)).thenReturn(enrichedRows);
                        })) {
            EncryptAlgorithmsHandler handler = new EncryptAlgorithmsHandler();
            Map<String, Object> actual = handler.handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
            List<?> actualItems = (List<?>) actual.get("items");
            assertThat(actualItems.size(), is(1));
            assertThat(actualItems.get(0), is(enrichedRows.get(0)));
            assertThat(mockedConstruction.constructed().size(), is(1));
            verify(mockedConstruction.constructed().get(0)).queryEncryptAlgorithms(requestContext);
            verify(mockedConstruction.constructed().get(0)).enrichEncryptAlgorithms(rawRows);
        }
    }
    
    @Test
    void assertHandleMaskAlgorithms() {
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        List<Map<String, Object>> rawRows = List.of(Map.of("type", "MD5"));
        List<Map<String, Object>> enrichedRows = List.of(Map.of("type", "MD5", "source", "builtin"));
        try (
                MockedConstruction<RuleInspectionService> mockedConstruction = mockConstruction(RuleInspectionService.class,
                        (mock, context) -> {
                            when(mock.queryMaskAlgorithms(same(requestContext))).thenReturn(rawRows);
                            when(mock.enrichMaskAlgorithms(rawRows)).thenReturn(enrichedRows);
                        })) {
            MaskAlgorithmsHandler handler = new MaskAlgorithmsHandler();
            Map<String, Object> actual = handler.handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
            List<?> actualItems = (List<?>) actual.get("items");
            assertThat(actualItems.size(), is(1));
            assertThat(actualItems.get(0), is(enrichedRows.get(0)));
            assertThat(mockedConstruction.constructed().size(), is(1));
            verify(mockedConstruction.constructed().get(0)).queryMaskAlgorithms(requestContext);
            verify(mockedConstruction.constructed().get(0)).enrichMaskAlgorithms(rawRows);
        }
    }
}
