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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingInspectionServiceTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertDatabaseScopedQueryArguments")
    void assertDatabaseScopedQuery(final String name, final QueryInvocation invocation, final String expectedSQL) {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        invocation.invoke(new ShardingInspectionService(), queryFacade);
        verify(queryFacade).query(eq("logic_db"), eq(expectedSQL));
    }
    
    @Test
    void assertQueryAlgorithmPlugins() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHARDING ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "INLINE")));
        List<Map<String, Object>> actual = new ShardingInspectionService().queryAlgorithmPlugins(queryFacade);
        assertThat(actual.getFirst().get("property_guidance").toString(), containsString("algorithm-expression"));
    }
    
    @Test
    void assertQueryAlgorithmPluginsWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHARDING ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'SHARDING ALGORITHM PLUGINS'", new SQLSyntaxErrorException("syntax error")));
        List<Map<String, Object>> actual = new ShardingInspectionService().queryAlgorithmPlugins(queryFacade);
        assertTrue(actual.stream().anyMatch(each -> "INLINE".equals(each.get("type"))));
        assertTrue(actual.stream().anyMatch(each -> "INLINE".equals(each.get("type")) && String.valueOf(each.get("property_guidance")).contains("algorithm-expression")));
    }
    
    @Test
    void assertQueryAlgorithmPluginsPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHARDING ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> new ShardingInspectionService().queryAlgorithmPlugins(queryFacade));
    }
    
    @Test
    void assertQueryKeyGenerateAlgorithmPlugins() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW KEY GENERATE ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "UUID")));
        List<Map<String, Object>> actual = new ShardingInspectionService().queryKeyGenerateAlgorithmPlugins(queryFacade);
        assertThat(actual.getFirst().get("property_guidance").toString(), is("No required properties."));
    }
    
    @Test
    void assertQueryKeyGenerateAlgorithmPluginsWithUnavailableDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW KEY GENERATE ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("syntax error near 'KEY GENERATE ALGORITHM PLUGINS'", new SQLSyntaxErrorException("syntax error")));
        List<Map<String, Object>> actual = new ShardingInspectionService().queryKeyGenerateAlgorithmPlugins(queryFacade);
        assertTrue(actual.stream().anyMatch(each -> "SNOWFLAKE".equals(each.get("type"))));
        assertTrue(actual.stream().anyMatch(each -> "SNOWFLAKE".equals(each.get("type")) && String.valueOf(each.get("property_guidance")).contains("worker-id")));
    }
    
    @Test
    void assertQueryKeyGenerateAlgorithmPluginsPropagatesQueryFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW KEY GENERATE ALGORITHM PLUGINS"))
                .thenThrow(new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused.")));
        assertThrows(MCPQueryFailedException.class, () -> new ShardingInspectionService().queryKeyGenerateAlgorithmPlugins(queryFacade));
    }
    
    private static Stream<Arguments> assertDatabaseScopedQueryArguments() {
        return Stream.of(
                Arguments.of("algorithms", (QueryInvocation) (service, facade) -> service.queryAlgorithms(facade, "logic_db"),
                        "SHOW SHARDING ALGORITHMS FROM logic_db"),
                Arguments.of("table rules", (QueryInvocation) (service, facade) -> service.queryTableRules(facade, "logic_db"),
                        "SHOW SHARDING TABLE RULES FROM logic_db"),
                Arguments.of("table rule", (QueryInvocation) (service, facade) -> service.queryTableRule(facade, "logic_db", "t_order"),
                        "SHOW SHARDING TABLE RULE t_order FROM logic_db"),
                Arguments.of("table nodes", (QueryInvocation) (service, facade) -> service.queryTableNodes(facade, "logic_db"),
                        "SHOW SHARDING TABLE NODES FROM logic_db"),
                Arguments.of("table node", (QueryInvocation) (service, facade) -> service.queryTableNode(facade, "logic_db", "t_order"),
                        "SHOW SHARDING TABLE NODES t_order FROM logic_db"),
                Arguments.of("table reference rules", (QueryInvocation) (service, facade) -> service.queryTableReferenceRules(facade, "logic_db"),
                        "SHOW SHARDING TABLE REFERENCE RULES FROM logic_db"),
                Arguments.of("table reference rule", (QueryInvocation) (service, facade) -> service.queryTableReferenceRule(facade, "logic_db", "ref_rule"),
                        "SHOW SHARDING TABLE REFERENCE RULE ref_rule FROM logic_db"),
                Arguments.of("default strategy", (QueryInvocation) (service, facade) -> service.queryDefaultStrategy(facade, "logic_db"),
                        "SHOW DEFAULT SHARDING STRATEGY FROM logic_db"),
                Arguments.of("key generators", (QueryInvocation) (service, facade) -> service.queryKeyGenerators(facade, "logic_db"),
                        "SHOW SHARDING KEY GENERATORS FROM logic_db"),
                Arguments.of("key generator", (QueryInvocation) (service, facade) -> service.queryKeyGenerator(facade, "logic_db", "snowflake_generator"),
                        "SHOW SHARDING KEY GENERATOR snowflake_generator FROM logic_db"),
                Arguments.of("key generate strategies", (QueryInvocation) (service, facade) -> service.queryKeyGenerateStrategies(facade, "logic_db"),
                        "SHOW SHARDING KEY GENERATE STRATEGIES FROM logic_db"),
                Arguments.of("key generate strategy", (QueryInvocation) (service, facade) -> service.queryKeyGenerateStrategy(facade, "logic_db", "order_key_strategy"),
                        "SHOW SHARDING KEY GENERATE STRATEGY order_key_strategy FROM logic_db"),
                Arguments.of("auditors", (QueryInvocation) (service, facade) -> service.queryAuditors(facade, "logic_db"),
                        "SHOW SHARDING AUDITORS FROM logic_db"),
                Arguments.of("unused algorithms", (QueryInvocation) (service, facade) -> service.queryUnusedAlgorithms(facade, "logic_db"),
                        "SHOW UNUSED SHARDING ALGORITHMS FROM logic_db"),
                Arguments.of("unused key generators", (QueryInvocation) (service, facade) -> service.queryUnusedKeyGenerators(facade, "logic_db"),
                        "SHOW UNUSED SHARDING KEY GENERATORS FROM logic_db"),
                Arguments.of("unused auditors", (QueryInvocation) (service, facade) -> service.queryUnusedAuditors(facade, "logic_db"),
                        "SHOW UNUSED SHARDING AUDITORS FROM logic_db"),
                Arguments.of("used algorithm", (QueryInvocation) (service, facade) -> service.queryTableRulesUsedAlgorithm(facade, "logic_db", "inline_algorithm"),
                        "SHOW SHARDING TABLE RULES USED ALGORITHM inline_algorithm FROM logic_db"),
                Arguments.of("used key generator", (QueryInvocation) (service, facade) -> service.queryTableRulesUsedKeyGenerator(facade, "logic_db", "snowflake_generator"),
                        "SHOW SHARDING TABLE RULES USED KEY GENERATOR snowflake_generator FROM logic_db"),
                Arguments.of("used auditor", (QueryInvocation) (service, facade) -> service.queryTableRulesUsedAuditor(facade, "logic_db", "dml_auditor"),
                        "SHOW SHARDING TABLE RULES USED AUDITOR dml_auditor FROM logic_db"),
                Arguments.of("rule count", (QueryInvocation) (service, facade) -> service.queryRuleCount(facade, "logic_db"),
                        "COUNT SHARDING RULE FROM logic_db"));
    }
    
    private interface QueryInvocation {
        
        void invoke(ShardingInspectionService service, MCPFeatureQueryFacade queryFacade);
    }
}
