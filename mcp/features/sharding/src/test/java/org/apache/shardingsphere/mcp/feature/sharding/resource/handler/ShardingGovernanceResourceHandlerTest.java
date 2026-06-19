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

class ShardingGovernanceResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final ShardingGovernanceResourceHandler handler, final MCPUriVariables uriVariables, final List<Map<String, Object>> rows,
                      final String expectedSelfUri) {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
        MCPResponse actual = handler.handle(databaseContext, uriVariables);
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rows = List.of(Map.of("name", "dml_auditor"));
        ShardingInspectionService service = createInspectionService(rows);
        return Stream.of(
                createArguments("auditors", "shardingsphere://features/sharding/databases/{database}/auditors",
                        ShardingGovernanceResourceHandler.ResourceKind.AUDITORS, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/auditors", service),
                createArguments("unused auditors", "shardingsphere://features/sharding/databases/{database}/unused-auditors",
                        ShardingGovernanceResourceHandler.ResourceKind.UNUSED_AUDITORS, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/unused-auditors", service),
                createArguments("auditor used table rules", "shardingsphere://features/sharding/databases/{database}/auditors/{auditor}/table-rules",
                        ShardingGovernanceResourceHandler.ResourceKind.AUDITOR_USED_TABLE_RULES, Map.of("database", "logic_db", "auditor", "dml_auditor"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/auditors/dml_auditor/table-rules", service),
                createArguments("rule count", "shardingsphere://features/sharding/databases/{database}/rule-count",
                        ShardingGovernanceResourceHandler.ResourceKind.RULE_COUNT, Map.of("database", "logic_db"), rows,
                        "shardingsphere://features/sharding/databases/logic_db/rule-count", service));
    }
    
    private static Arguments createArguments(final String name, final String resourceUriTemplate, final ShardingGovernanceResourceHandler.ResourceKind resourceKind,
                                             final Map<String, String> uriVariables, final List<Map<String, Object>> rows, final String expectedSelfUri,
                                             final ShardingInspectionService service) {
        return Arguments.of(name, new ShardingGovernanceResourceHandler(resourceUriTemplate, resourceKind, service), new MCPUriVariables(uriVariables), rows, expectedSelfUri);
    }
    
    private static ShardingInspectionService createInspectionService(final List<Map<String, Object>> rows) {
        ShardingInspectionService result = mock(ShardingInspectionService.class);
        when(result.queryAuditors(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryUnusedAuditors(any(), eq("logic_db"))).thenReturn(rows);
        when(result.queryTableRulesUsedAuditor(any(), eq("logic_db"), eq("dml_auditor"))).thenReturn(rows);
        when(result.queryRuleCount(any(), eq("logic_db"))).thenReturn(rows);
        return result;
    }
}
