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

package org.apache.shardingsphere.mcp.feature.encrypt.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptRuleInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptResourceHandlerTest {
    
    @Test
    void assertGetEncryptAlgorithmsUriPattern() {
        assertThat(new EncryptAlgorithmsHandler().getUriPattern(), is("shardingsphere://features/encrypt/algorithms"));
    }
    
    @Test
    void assertHandleEncryptAlgorithms() throws ReflectiveOperationException {
        EncryptAlgorithmsHandler handler = new EncryptAlgorithmsHandler();
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(ruleInspectionService.enrichEncryptAlgorithms(queryFacade)).thenReturn(List.of(Map.of("type", "AES", "source", "builtin")));
        Plugins.getMemberAccessor().set(((Object) handler).getClass().getDeclaredField("ruleInspectionService"), handler, ruleInspectionService);
        MCPResponse actual = handler.handle(databaseContext, new MCPUriVariables(Map.of()));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
    
    @Test
    void assertGetEncryptRulesUriPattern() {
        assertThat(new EncryptRulesHandler().getUriPattern(), is("shardingsphere://features/encrypt/databases/{database}/rules"));
    }
    
    @Test
    void assertHandleEncryptRules() throws ReflectiveOperationException {
        EncryptRulesHandler handler = new EncryptRulesHandler();
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        Plugins.getMemberAccessor().set(((Object) handler).getClass().getDeclaredField("ruleInspectionService"), handler, ruleInspectionService);
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), new MCPUriVariables(Map.of("database", "logic_db")));
        verify(ruleInspectionService).queryEncryptRules(any(), eq("logic_db"), eq(""));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
    
    @Test
    void assertGetEncryptRuleUriPattern() {
        assertThat(new EncryptRuleHandler().getUriPattern(), is("shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules"));
    }
    
    @Test
    void assertHandleEncryptRule() throws ReflectiveOperationException {
        EncryptRuleHandler handler = new EncryptRuleHandler();
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        Plugins.getMemberAccessor().set(((Object) handler).getClass().getDeclaredField("ruleInspectionService"), handler, ruleInspectionService);
        MCPResponse actual = handler.handle(mock(MCPDatabaseHandlerContext.class), new MCPUriVariables(Map.of("database", "logic_db", "table", "orders")));
        verify(ruleInspectionService).queryEncryptRules(any(), eq("logic_db"), eq("orders"));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
    }
}
