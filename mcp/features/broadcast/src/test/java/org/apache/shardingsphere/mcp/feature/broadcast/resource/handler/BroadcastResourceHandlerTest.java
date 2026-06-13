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

package org.apache.shardingsphere.mcp.feature.broadcast.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastRuleInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final MCPResourceHandler<MCPDatabaseHandlerContext> handler, final List<Map<String, Object>> rows,
                      final Map<String, String> uriVariables, final String expectedSelfUri, final String expectedParentUri) {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("MySQL");
        MCPResponse actual = handler.handle(databaseContext, new MCPUriVariables(uriVariables));
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
        if (!expectedParentUri.isEmpty()) {
            assertThat(((Map<?, ?>) actual.toPayload().get("parent_resource")).get("uri"), is(expectedParentUri));
        }
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rules = List.of(Map.of("broadcast_table", "t_order"), Map.of("broadcast_table", "t_order_item"));
        List<Map<String, Object>> count = List.of(Map.of("rule_name", "broadcast_table", "count", 1));
        BroadcastRuleInspectionService rulesService = mock(BroadcastRuleInspectionService.class);
        BroadcastRuleInspectionService tableRuleService = mock(BroadcastRuleInspectionService.class);
        BroadcastRuleInspectionService countService = mock(BroadcastRuleInspectionService.class);
        when(rulesService.queryBroadcastRules(any(), eq("logic_db"))).thenReturn(rules);
        when(tableRuleService.queryBroadcastRules(any(), eq("logic_db"))).thenReturn(rules);
        when(countService.queryBroadcastRuleCount(any(), eq("logic_db"))).thenReturn(count);
        return Stream.of(
                Arguments.of("broadcast rules", new BroadcastRulesHandler(rulesService), rules,
                        Map.of("database", "logic_db"), "shardingsphere://features/broadcast/databases/logic_db/rules", ""),
                Arguments.of("broadcast table rule", new BroadcastTableRuleHandler(tableRuleService), List.of(Map.of("broadcast_table", "t_order")),
                        Map.of("database", "logic_db", "table", "t_order"), "shardingsphere://features/broadcast/databases/logic_db/tables/t_order/rule",
                        "shardingsphere://features/broadcast/databases/logic_db/rules"),
                Arguments.of("broadcast rule count", new BroadcastRuleCountHandler(countService), count,
                        Map.of("database", "logic_db"),
                        "shardingsphere://features/broadcast/databases/logic_db/rule-count",
                        "shardingsphere://features/broadcast/databases/logic_db/rules"));
    }
}
