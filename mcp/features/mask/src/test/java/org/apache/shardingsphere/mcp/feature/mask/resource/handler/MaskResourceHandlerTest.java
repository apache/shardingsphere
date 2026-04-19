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

package org.apache.shardingsphere.mcp.feature.mask.resource.handler;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskRuleInspectionService;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaskResourceHandlerTest {
    
    @Test
    void assertGetMaskAlgorithmsUriPattern() {
        assertThat(new MaskAlgorithmsHandler().getUriPattern(), is("shardingsphere://features/mask/algorithms"));
    }
    
    @Test
    void assertHandleMaskAlgorithms() throws ReflectiveOperationException {
        MaskAlgorithmsHandler handler = new MaskAlgorithmsHandler();
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of(Map.of("type", "MD5")));
        when(ruleInspectionService.enrichMaskAlgorithms(List.of(Map.of("type", "MD5")))).thenReturn(List.of(Map.of("type", "MD5", "source", "builtin")));
        setField(handler, "ruleInspectionService", ruleInspectionService);
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), new MCPUriVariables(Map.of()));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
    
    @Test
    void assertGetMaskRulesUriPattern() {
        assertThat(new MaskRulesHandler().getUriPattern(), is("shardingsphere://features/mask/databases/{database}/rules"));
    }
    
    @Test
    void assertHandleMaskRules() throws ReflectiveOperationException {
        MaskRulesHandler handler = new MaskRulesHandler();
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        setField(handler, "ruleInspectionService", ruleInspectionService);
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), new MCPUriVariables(Map.of("database", "logic_db")));
        verify(ruleInspectionService).queryMaskRules(any(), eq("logic_db"), eq(""));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
    
    @Test
    void assertGetMaskRuleUriPattern() {
        assertThat(new MaskRuleHandler().getUriPattern(), is("shardingsphere://features/mask/databases/{database}/tables/{table}/rules"));
    }
    
    @Test
    void assertHandleMaskRule() throws ReflectiveOperationException {
        MaskRuleHandler handler = new MaskRuleHandler();
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        setField(handler, "ruleInspectionService", ruleInspectionService);
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), new MCPUriVariables(Map.of("database", "logic_db", "table", "orders")));
        verify(ruleInspectionService).queryMaskRules(any(), eq("logic_db"), eq("orders"));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
}
