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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
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

class ShardingStrategyResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final ShardingStrategyResourceHandler handler, final MCPUriVariables uriVariables, final List<Map<String, Object>> rows,
                      final String expectedSelfUri) {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
        MCPResponse actual = handler.handle(databaseContext, uriVariables);
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rows = List.of(Map.of("name", "snowflake_generator"));
        ShardingInspectionService service = createInspectionService(rows);
        return Stream.of(
                createArguments("default strategy", "shardingsphere://features/sharding/databases/{database}/default-strategy",
                        ShardingStrategyResourceHandler.ResourceKind.DEFAULT_STRATEGY, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/default-strategy", service),
                createArguments("key generators", "shardingsphere://features/sharding/databases/{database}/key-generators",
                        ShardingStrategyResourceHandler.ResourceKind.KEY_GENERATORS, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators", service),
                createArguments("key generator", "shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}",
                        ShardingStrategyResourceHandler.ResourceKind.KEY_GENERATOR, Map.of("database", "logic_db", "keyGenerator", "snowflake_generator"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators/snowflake_generator", service),
                createArguments("key generate strategies", "shardingsphere://features/sharding/databases/{database}/key-generate-strategies",
                        ShardingStrategyResourceHandler.ResourceKind.KEY_GENERATE_STRATEGIES, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generate-strategies", service),
                createArguments("key generate strategy", "shardingsphere://features/sharding/databases/{database}/key-generate-strategies/{strategy}",
                        ShardingStrategyResourceHandler.ResourceKind.KEY_GENERATE_STRATEGY, Map.of("database", "logic_db", "strategy", "order_key_strategy"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generate-strategies/order_key_strategy", service),
                createArguments("unused key generators", "shardingsphere://features/sharding/databases/{database}/unused-key-generators",
                        ShardingStrategyResourceHandler.ResourceKind.UNUSED_KEY_GENERATORS, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/unused-key-generators", service),
                createArguments("key generator used table rules", "shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}/table-rules",
                        ShardingStrategyResourceHandler.ResourceKind.KEY_GENERATOR_USED_TABLE_RULES,
                        Map.of("database", "logic_db", "keyGenerator", "snowflake_generator"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/key-generators/snowflake_generator/table-rules", service));
    }
    
    private static Arguments createArguments(final String name, final String resourceUriTemplate, final ShardingStrategyResourceHandler.ResourceKind resourceKind,
                                             final Map<String, String> uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri,
                                             final ShardingInspectionService service) {
        return Arguments.of(name, new ShardingStrategyResourceHandler(resourceUriTemplate, resourceKind, service), new MCPUriVariables(uriVariables), rows, expectedSelfUri);
    }
    
    private static ShardingInspectionService createInspectionService(final List<Map<String, Object>> rows) {
        ShardingInspectionService result = mock(ShardingInspectionService.class);
        when(result.queryDefaultStrategy(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryKeyGenerators(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator"))).thenReturn(rows);
        when(result.queryKeyGenerateStrategies(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryKeyGenerateStrategy(any(), eq("logic_db"), eq("order_key_strategy"))).thenReturn(rows);
        when(result.queryUnusedKeyGenerators(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryTableRulesUsedKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator"))).thenReturn(rows);
        return result;
    }
}
