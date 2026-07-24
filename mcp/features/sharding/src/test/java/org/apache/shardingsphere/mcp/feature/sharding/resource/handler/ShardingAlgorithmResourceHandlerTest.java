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
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
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

class ShardingAlgorithmResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final Supplier<ShardingAlgorithmResourceHandler> handlerSupplier, final MCPResourceURIVariables uriVariables,
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
        List<Map<String, Object>> rows = List.of(Map.of("name", "inline_algorithm"));
        return Stream.of(
                createArguments("algorithm plugins", ShardingAlgorithmResourceHandler::algorithmPlugins, Map.of(), rows,
                        "shardingsphere://features/sharding/algorithm-plugins"),
                createArguments("key generate algorithm plugins", ShardingAlgorithmResourceHandler::keyGenerateAlgorithmPlugins, Map.of(), rows,
                        "shardingsphere://features/sharding/key-generate-algorithm-plugins"),
                createArguments("algorithms", ShardingAlgorithmResourceHandler::algorithms, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/algorithms"),
                createArguments("unused algorithms", ShardingAlgorithmResourceHandler::unusedAlgorithms, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/unused-algorithms"),
                createArguments("algorithm used table rules", ShardingAlgorithmResourceHandler::algorithmUsedTableRules,
                        Map.of("database", "logic_db", "algorithm", "inline_algorithm"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/algorithms/inline_algorithm/table-rules"));
    }
    
    private static Arguments createArguments(final String name, final Supplier<ShardingAlgorithmResourceHandler> handlerSupplier,
                                             final Map<String, String> uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri) {
        return Arguments.of(name, handlerSupplier, new MCPResourceURIVariables(uriVariables), rows, expectedSelfUri);
    }
    
    private static void stubInspectionService(final ShardingInspectionService inspectionService, final List<Map<String, Object>> rows) {
        when(inspectionService.queryAlgorithmPlugins(any())).thenReturn(WorkflowQueryResult.confirmed(rows));
        when(inspectionService.queryKeyGenerateAlgorithmPlugins(any())).thenReturn(WorkflowQueryResult.confirmed(rows));
        when(inspectionService.queryAlgorithms(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryUnusedAlgorithms(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryTableRulesUsedAlgorithm(any(), eq("logic_db"), eq("inline_algorithm"))).thenReturn(rows);
    }
}
