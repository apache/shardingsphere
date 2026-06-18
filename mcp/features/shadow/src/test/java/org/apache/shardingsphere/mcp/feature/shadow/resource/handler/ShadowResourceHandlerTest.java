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

package org.apache.shardingsphere.mcp.feature.shadow.resource.handler;

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowInspectionService;
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

class ShadowResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final ShadowResourceHandler handler, final MCPUriVariables uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri) {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
        MCPResponse actual = handler.handle(databaseContext, uriVariables);
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rows = List.of(Map.of("rule_name", "shadow_rule"));
        ShadowInspectionService service = mock(ShadowInspectionService.class);
        when(service.queryRules(any(), eq("logic_db"))).thenReturn(rows);
        when(service.queryRule(any(), eq("logic_db"), eq("shadow_rule"))).thenReturn(rows);
        when(service.queryTableRules(any(), eq("logic_db"))).thenReturn(rows);
        when(service.queryTableRule(any(), eq("logic_db"), eq("t_order"))).thenReturn(rows);
        when(service.queryAlgorithms(any(), eq("logic_db"))).thenReturn(rows);
        when(service.queryDefaultAlgorithm(any(), eq("logic_db"))).thenReturn(rows);
        when(service.queryRuleCount(any(), eq("logic_db"))).thenReturn(rows);
        when(service.queryAlgorithmPlugins(any())).thenReturn(rows);
        return Stream.of(
                Arguments.of("rules", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/rules",
                        ShadowResourceHandler.ResourceKind.RULES, service), new MCPUriVariables(Map.of("database", "logic_db")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/rules"),
                Arguments.of("rule", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/rules/{rule}",
                        ShadowResourceHandler.ResourceKind.RULE, service), new MCPUriVariables(Map.of("database", "logic_db", "rule", "shadow_rule")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/rules/shadow_rule"),
                Arguments.of("table rules", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/table-rules",
                        ShadowResourceHandler.ResourceKind.TABLE_RULES, service), new MCPUriVariables(Map.of("database", "logic_db")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/table-rules"),
                Arguments.of("table rule", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/tables/{table}/rules",
                        ShadowResourceHandler.ResourceKind.TABLE_RULE, service), new MCPUriVariables(Map.of("database", "logic_db", "table", "t_order")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/tables/t_order/rules"),
                Arguments.of("algorithms", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/algorithms",
                        ShadowResourceHandler.ResourceKind.ALGORITHMS, service), new MCPUriVariables(Map.of("database", "logic_db")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/algorithms"),
                Arguments.of("default algorithm", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/default-algorithm",
                        ShadowResourceHandler.ResourceKind.DEFAULT_ALGORITHM, service), new MCPUriVariables(Map.of("database", "logic_db")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/default-algorithm"),
                Arguments.of("rule count", new ShadowResourceHandler("shardingsphere://features/shadow/databases/{database}/rule-count",
                        ShadowResourceHandler.ResourceKind.RULE_COUNT, service), new MCPUriVariables(Map.of("database", "logic_db")), rows,
                        "shardingsphere://features/shadow/databases/logic_db/rule-count"),
                Arguments.of("plugins", new ShadowResourceHandler("shardingsphere://features/shadow/algorithm-plugins",
                        ShadowResourceHandler.ResourceKind.ALGORITHM_PLUGINS, service), new MCPUriVariables(Map.of()), rows,
                        "shardingsphere://features/shadow/algorithm-plugins"));
    }
}
