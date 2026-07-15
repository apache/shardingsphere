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

import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
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

class ShardingGovernanceResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final Supplier<ShardingGovernanceResourceHandler> handlerSupplier, final MCPUriVariables uriVariables,
                      final List<Map<String, Object>> rows, final String expectedSelfUri) {
        try (
                MockedConstruction<ShardingInspectionService> ignored = mockConstruction(
                        ShardingInspectionService.class, (mock, context) -> stubInspectionService(mock, rows))) {
            MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
            when(requestContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
            MCPSuccessPayload actual = handlerSupplier.get().handle(requestContext, uriVariables);
            assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
            assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
        }
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rows = List.of(Map.of("name", "dml_auditor"));
        return Stream.of(
                createArguments("auditors", ShardingGovernanceResourceHandler::auditors, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/auditors"),
                createArguments("unused auditors", ShardingGovernanceResourceHandler::unusedAuditors, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/unused-auditors"),
                createArguments("auditor used table rules", ShardingGovernanceResourceHandler::auditorUsedTableRules,
                        Map.of("database", "logic_db", "auditor", "dml_auditor"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/auditors/dml_auditor/table-rules"),
                createArguments("rule count", ShardingGovernanceResourceHandler::ruleCount, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/rule-count"));
    }
    
    private static Arguments createArguments(final String name, final Supplier<ShardingGovernanceResourceHandler> handlerSupplier,
                                             final Map<String, String> uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri) {
        return Arguments.of(name, handlerSupplier, new MCPUriVariables(uriVariables), rows, expectedSelfUri);
    }
    
    private static void stubInspectionService(final ShardingInspectionService inspectionService, final List<Map<String, Object>> rows) {
        when(inspectionService.queryAuditors(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryUnusedAuditors(any(), eq("logic_db"))).thenReturn(rows);
        when(inspectionService.queryTableRulesUsedAuditor(any(), eq("logic_db"), eq("dml_auditor"))).thenReturn(rows);
        when(inspectionService.queryRuleCount(any(), eq("logic_db"))).thenReturn(rows);
    }
}
