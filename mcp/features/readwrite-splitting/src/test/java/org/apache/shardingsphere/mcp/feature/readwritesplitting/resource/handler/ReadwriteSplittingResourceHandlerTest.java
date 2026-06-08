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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingInspectionService;
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

class ReadwriteSplittingResourceHandlerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertHandleArguments")
    void assertHandle(final String name, final MCPResourceHandler<MCPDatabaseHandlerContext> handler, final MCPUriVariables uriVariables,
                      final List<Map<String, Object>> rows, final String expectedSelfUri, final String expectedParentUri) {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
        MCPResponse actual = handler.handle(databaseContext, uriVariables);
        assertThat(((List<?>) actual.toPayload().get("items")).size(), is(rows.size()));
        assertThat(actual.toPayload().get("self_uri"), is(expectedSelfUri));
        if (!expectedParentUri.isEmpty()) {
            assertThat(((Map<?, ?>) actual.toPayload().get("parent_resource")).get("uri"), is(expectedParentUri));
        }
    }
    
    private static Stream<Arguments> assertHandleArguments() {
        List<Map<String, Object>> rules = List.of(Map.of("name", "readwrite_ds"));
        List<Map<String, Object>> statuses = List.of(Map.of("name", "readwrite_ds", "storage_unit", "read_ds_0", "status", "ENABLED"));
        List<Map<String, Object>> count = List.of(Map.of("rule_name", "readwrite_splitting", "count", 1));
        List<Map<String, Object>> plugins = List.of(Map.of("type", "RANDOM"));
        ReadwriteSplittingInspectionService service = mock(ReadwriteSplittingInspectionService.class);
        when(service.queryRules(any(), eq("logic_db"))).thenReturn(rules);
        when(service.queryRule(any(), eq("logic_db"), eq("readwrite_ds"))).thenReturn(rules);
        when(service.queryStatuses(any(), eq("logic_db"))).thenReturn(statuses);
        when(service.queryRuleStatus(any(), eq("logic_db"), eq("readwrite_ds"))).thenReturn(statuses);
        when(service.queryRuleCount(any(), eq("logic_db"))).thenReturn(count);
        when(service.queryLoadBalanceAlgorithmPlugins(any())).thenReturn(plugins);
        return Stream.of(
                Arguments.of("rules", new ReadwriteSplittingRulesHandler(service), new MCPUriVariables(Map.of("database", "logic_db")), rules,
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules", ""),
                Arguments.of("single rule", new ReadwriteSplittingRuleHandler(service), new MCPUriVariables(Map.of("database", "logic_db", "rule", "readwrite_ds")), rules,
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules/readwrite_ds",
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules"),
                Arguments.of("status", new ReadwriteSplittingStatusHandler(service), new MCPUriVariables(Map.of("database", "logic_db")), statuses,
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/status",
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules"),
                Arguments.of("rule status", new ReadwriteSplittingRuleStatusHandler(service), new MCPUriVariables(Map.of("database", "logic_db", "rule", "readwrite_ds")), statuses,
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules/readwrite_ds/status",
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/status"),
                Arguments.of("rule count", new ReadwriteSplittingRuleCountHandler(service), new MCPUriVariables(Map.of("database", "logic_db")), count,
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rule-count",
                        "shardingsphere://features/readwrite-splitting/databases/logic_db/rules"),
                Arguments.of("plugins", new LoadBalanceAlgorithmPluginsHandler(service), new MCPUriVariables(Map.of()), plugins,
                        "shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins", ""));
    }
}
