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

package org.apache.shardingsphere.mcp.resource.handler.rule;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.apache.shardingsphere.mcp.tool.service.workflow.RuleInspectionService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuleResourceHandlerTest {
    
    @Test
    void assertHandleEncryptRules() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        List<Map<String, Object>> expectedItems = List.of(Map.of("table", "t_order"));
        try (
                MockedConstruction<RuleInspectionService> mockedConstruction = mockConstruction(RuleInspectionService.class,
                        (mock, context) -> when(mock.queryEncryptRules(same(runtimeContext), eq("logic_db"), eq(""))).thenReturn(expectedItems))) {
            EncryptRulesHandler handler = new EncryptRulesHandler();
            Map<String, Object> actual = handler.handle(runtimeContext, new MCPUriVariables(Map.of("database", "logic_db"))).toPayload();
            List<?> actualItems = (List<?>) actual.get("items");
            assertThat(actualItems.size(), is(1));
            assertThat(actualItems.get(0), is(expectedItems.get(0)));
            assertThat(mockedConstruction.constructed().size(), is(1));
            verify(mockedConstruction.constructed().get(0)).queryEncryptRules(runtimeContext, "logic_db", "");
        }
    }
    
    @Test
    void assertHandleMaskRule() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        List<Map<String, Object>> expectedItems = List.of(Map.of("table", "t_order", "column", "phone"));
        try (
                MockedConstruction<RuleInspectionService> mockedConstruction = mockConstruction(RuleInspectionService.class,
                        (mock, context) -> when(mock.queryMaskRules(same(runtimeContext), eq("logic_db"), eq("t_order"))).thenReturn(expectedItems))) {
            MaskRuleHandler handler = new MaskRuleHandler();
            Map<String, Object> actual = handler.handle(runtimeContext, new MCPUriVariables(Map.of("database", "logic_db", "table", "t_order"))).toPayload();
            List<?> actualItems = (List<?>) actual.get("items");
            assertThat(actualItems.size(), is(1));
            assertThat(actualItems.get(0), is(expectedItems.get(0)));
            assertThat(mockedConstruction.constructed().size(), is(1));
            verify(mockedConstruction.constructed().get(0)).queryMaskRules(runtimeContext, "logic_db", "t_order");
        }
    }
}
