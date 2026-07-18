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

package org.apache.shardingsphere.mcp.feature.sharding.resource.handler;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
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

class ShardingStrategyResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final Supplier<ShardingStrategyResourceHandler> handlerSupplier, final MCPUriVariables uriVariables,
                      final List<Map<String, Object>> rows, final String expectedSelfUri) {
        try (
                MockedConstruction<ShardingInspectionService> ignored = mockConstruction(
                        ShardingInspectionService.class, (mock, context) -> stubInspectionService(mock, rows))) {
            MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
            when(requestContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
            MCPSuccessPayload actual = handlerSupplier.get().handle(requestContext, uriVariables);
            assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
            assertThat(((Map<?, ?>) actual.toPayload().get("self_resource")).get("uri"), is(expectedSelfUri));
        }
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rows = List.of(Map.of("name", "snowflake_generator"));
        return Stream.of(
                createArguments("default strategy", ShardingStrategyResourceHandler::defaultStrategy, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/default-strategy"),
                createArguments("key generators", ShardingStrategyResourceHandler::keyGenerators, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators"),
                createArguments("key generator", ShardingStrategyResourceHandler::keyGenerator,
                        Map.of("database", "logic_db", "keyGenerator", "snowflake_generator"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators/snowflake_generator"),
                createArguments("key generate strategies", ShardingStrategyResourceHandler::keyGenerateStrategies, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generate-strategies"),
                createArguments("key generate strategy", ShardingStrategyResourceHandler::keyGenerateStrategy,
                        Map.of("database", "logic_db", "strategy", "order_key_strategy"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generate-strategies/order_key_strategy"),
                createArguments("unused key generators", ShardingStrategyResourceHandler::unusedKeyGenerators, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/unused-key-generators"),
                createArguments("key generator used table rules", ShardingStrategyResourceHandler::keyGeneratorUsedTableRules,
                        Map.of("database", "logic_db", "keyGenerator", "snowflake_generator"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators/snowflake_generator/table-rules"));
    }
    
    private static Arguments createArguments(final String name, final Supplier<ShardingStrategyResourceHandler> handlerSupplier,
                                             final Map<String, String> uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri) {
        return Arguments.of(name, handlerSupplier, new MCPUriVariables(uriVariables), rows, expectedSelfUri);
    }
    
    private static void stubInspectionService(final ShardingInspectionService inspectionService, final List<Map<String, Object>> rows) {
        when(inspectionService.queryDefaultStrategy(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryKeyGenerators(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator"))).thenReturn(rows);
        when(inspectionService.queryKeyGenerateStrategies(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryKeyGenerateStrategy(any(), eq("logic_db"), eq("order_key_strategy"))).thenReturn(rows);
        when(inspectionService.queryUnusedKeyGenerators(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryTableRulesUsedKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator"))).thenReturn(rows);
    }
}
