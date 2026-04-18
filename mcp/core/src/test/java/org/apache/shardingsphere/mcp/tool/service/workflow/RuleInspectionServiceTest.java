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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuleInspectionServiceTest {
    
    @Test
    void assertQueryEncryptRulesUsesShowAllSqlWhenTableNameIsBlank() {
        WorkflowProxyQueryService queryService = mock(WorkflowProxyQueryService.class);
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(queryService.query(runtimeContext, "logic_db", "", "SHOW ENCRYPT RULES FROM logic_db")).thenReturn(List.of(Map.of("table", "orders")));
        RuleInspectionService service = new RuleInspectionService(queryService);
        List<Map<String, Object>> actual = service.queryEncryptRules(runtimeContext, "logic_db", "");
        assertThat(actual.size(), is(1));
        verify(queryService).query(runtimeContext, "logic_db", "", "SHOW ENCRYPT RULES FROM logic_db");
    }
    
    @Test
    void assertQueryMaskRulesUsesSpecificTableSql() {
        WorkflowProxyQueryService queryService = mock(WorkflowProxyQueryService.class);
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(queryService.query(runtimeContext, "logic_db", "", "SHOW MASK RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "order_id", "mask_algorithm", "MD5", "props", "replace-char=*")));
        RuleInspectionService service = new RuleInspectionService(queryService);
        List<Map<String, Object>> actual = service.queryMaskRules(runtimeContext, "logic_db", "orders");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get("column"), is("order_id"));
        assertThat(actual.get(0).get("algorithm_type"), is("MD5"));
        assertThat(actual.get(0).get("algorithm_props"), is("replace-char=*"));
        verify(queryService).query(runtimeContext, "logic_db", "", "SHOW MASK RULE orders FROM logic_db");
    }
    
    @Test
    void assertQueryEncryptRulesUsesSpecificTableSql() {
        WorkflowProxyQueryService queryService = mock(WorkflowProxyQueryService.class);
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(queryService.query(runtimeContext, "logic_db", "", "SHOW ENCRYPT TABLE RULE orders FROM logic_db"))
                .thenReturn(List.of(Map.of("logic_column", "status", "cipher_column", "status_cipher")));
        RuleInspectionService service = new RuleInspectionService(queryService);
        List<Map<String, Object>> actual = service.queryEncryptRules(runtimeContext, "logic_db", "orders");
        assertThat(actual.size(), is(1));
        verify(queryService).query(runtimeContext, "logic_db", "", "SHOW ENCRYPT TABLE RULE orders FROM logic_db");
    }
    
    @Test
    void assertQueryEncryptAlgorithmsDelegatesToProxyQueryService() {
        WorkflowProxyQueryService queryService = mock(WorkflowProxyQueryService.class);
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(queryService.queryWithAnyDatabase(runtimeContext, "SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "AES")));
        RuleInspectionService service = new RuleInspectionService(queryService);
        List<Map<String, Object>> actual = service.queryEncryptAlgorithms(runtimeContext);
        assertThat(actual.size(), is(1));
        verify(queryService).queryWithAnyDatabase(runtimeContext, "SHOW ENCRYPT ALGORITHM PLUGINS");
    }
    
    @Test
    void assertQueryMaskAlgorithmsDelegatesToProxyQueryService() {
        WorkflowProxyQueryService queryService = mock(WorkflowProxyQueryService.class);
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        when(queryService.queryWithAnyDatabase(runtimeContext, "SHOW MASK ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "MD5")));
        RuleInspectionService service = new RuleInspectionService(queryService);
        List<Map<String, Object>> actual = service.queryMaskAlgorithms(runtimeContext);
        assertThat(actual.size(), is(1));
        verify(queryService).queryWithAnyDatabase(runtimeContext, "SHOW MASK ALGORITHM PLUGINS");
    }
    
    @Test
    void assertEnrichMaskAlgorithmsAddsBuiltinAndCustomSpiMetadata() {
        RuleInspectionService service = new RuleInspectionService(mock(WorkflowProxyQueryService.class));
        List<Map<String, Object>> actual = service.enrichMaskAlgorithms(List.of(Map.of("type", "MD5"), Map.of("type", "CUSTOM_MASK")));
        assertThat(actual.get(0).get("source"), is("builtin"));
        assertThat(actual.get(1).get("source"), is("custom-spi"));
    }
    
    @Test
    void assertEnrichEncryptAlgorithmsKeepsCustomSpiCapabilityAsUnknownWithoutFailure() {
        RuleInspectionService service = new RuleInspectionService(mock(WorkflowProxyQueryService.class));
        List<Map<String, Object>> actual = service.enrichEncryptAlgorithms(List.of(Map.of("type", "CUSTOM_AES")));
        assertThat(actual.get(0).get("source"), is("custom-spi"));
        assertThat(actual.get(0).get("supports_decrypt"), is((Object) null));
        assertThat(actual.get(0).get("supports_like"), is((Object) null));
    }
    
    @Test
    void assertQueryMaskRulesRejectsUnsafeIdentifier() {
        RuleInspectionService service = new RuleInspectionService(mock(WorkflowProxyQueryService.class));
        Exception actual = assertThrows(RuntimeException.class, () -> service.queryMaskRules(mock(MCPRuntimeContext.class), "logic_db", "bad table"));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
    
    @Test
    void assertQueryEncryptRulesRejectsUnsafeIdentifier() {
        RuleInspectionService service = new RuleInspectionService(mock(WorkflowProxyQueryService.class));
        Exception actual = assertThrows(RuntimeException.class, () -> service.queryEncryptRules(mock(MCPRuntimeContext.class), "logic_db", "bad table"));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
}
