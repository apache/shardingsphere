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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ShadowResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final Supplier<ShadowResourceHandler> handlerSupplier, final MCPUriVariables uriVariables,
                      final String expectedKind, final String expectedSelfUri) {
        try (MockedConstruction<ShadowInspectionService> ignored = mockConstruction(ShadowInspectionService.class, (mock, context) -> {
            when(mock.queryRules(any(), eq("logic_db"))).thenReturn(createRows("rules"));
            when(mock.queryRule(any(), eq("logic_db"), eq("shadow_rule"))).thenReturn(createRows("rule"));
            when(mock.queryTableRules(any(), eq("logic_db"))).thenReturn(createRows("table_rules"));
            when(mock.queryTableRule(any(), eq("logic_db"), eq("t_order"))).thenReturn(createRows("table_rule"));
            when(mock.queryAlgorithms(any(), eq("logic_db"))).thenReturn(createRows("algorithms"));
            when(mock.queryDefaultAlgorithm(any(), eq("logic_db"))).thenReturn(createRows("default_algorithm"));
            when(mock.queryRuleCount(any(), eq("logic_db"))).thenReturn(createRows("rule_count"));
            when(mock.queryAlgorithmPlugins(any())).thenReturn(createRows("algorithm_plugins"));
        })) {
            MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
            when(requestContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
            MCPSuccessPayload actual = handlerSupplier.get().handle(requestContext, uriVariables);
            assertThat(actual.toPayload().get("items"), is(createRows(expectedKind)));
            assertThat(((Map<?, ?>) actual.toPayload().get("self_resource")).get("uri"), is(expectedSelfUri));
        }
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        return Stream.of(
                Arguments.of("rules", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::rules, new MCPUriVariables(Map.of("database", "logic_db")), "rules",
                        "shardingsphere://features/shadow/databases/logic_db/rules"),
                Arguments.of("rule", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::rule,
                        new MCPUriVariables(Map.of("database", "logic_db", "rule", "shadow_rule")), "rule",
                        "shardingsphere://features/shadow/databases/logic_db/rules/shadow_rule"),
                Arguments.of("table rules", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::tableRules,
                        new MCPUriVariables(Map.of("database", "logic_db")), "table_rules",
                        "shardingsphere://features/shadow/databases/logic_db/table-rules"),
                Arguments.of("table rule", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::tableRule,
                        new MCPUriVariables(Map.of("database", "logic_db", "table", "t_order")), "table_rule",
                        "shardingsphere://features/shadow/databases/logic_db/tables/t_order/rules"),
                Arguments.of("algorithms", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::algorithms,
                        new MCPUriVariables(Map.of("database", "logic_db")), "algorithms",
                        "shardingsphere://features/shadow/databases/logic_db/algorithms"),
                Arguments.of("default algorithm", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::defaultAlgorithm,
                        new MCPUriVariables(Map.of("database", "logic_db")), "default_algorithm",
                        "shardingsphere://features/shadow/databases/logic_db/default-algorithm"),
                Arguments.of("rule count", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::ruleCount,
                        new MCPUriVariables(Map.of("database", "logic_db")), "rule_count",
                        "shardingsphere://features/shadow/databases/logic_db/rule-count"),
                Arguments.of("plugins", (Supplier<ShadowResourceHandler>) ShadowResourceHandler::algorithmPlugins,
                        new MCPUriVariables(Map.of()), "algorithm_plugins",
                        "shardingsphere://features/shadow/algorithm-plugins"));
    }
    
    private static List<Map<String, Object>> createRows(final String kind) {
        return List.of(Map.of("kind", kind));
    }
}
