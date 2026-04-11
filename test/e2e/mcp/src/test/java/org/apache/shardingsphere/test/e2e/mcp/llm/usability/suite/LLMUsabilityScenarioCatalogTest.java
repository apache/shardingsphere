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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.suite;

import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class LLMUsabilityScenarioCatalogTest {
    
    @Test
    void assertCreateMinimalBaseline() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public",
                "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2);
        
        assertThat(actual, hasSize(6));
        assertThat(actual.get(0).getDimension(), is(LLMUsabilityDimension.RESOURCE));
        assertThat(actual.get(4).getDimension(), is(LLMUsabilityDimension.RECOVERY));
        assertThat(actual.get(1).getExpectedResourceUris().get(0), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCreateMinimalBaselineScenarioCases")
    void assertCreateMinimalBaselineScenario(final String name, final int scenarioIndex, final String expectedScenarioId,
                                             final LLMUsabilityDimension expectedDimension, final List<String> expectedFirstActionNames,
                                             final List<String> expectedResourceUris, final boolean expectedResourceHitRequired,
                                             final boolean expectedRecoveryExpected, final List<String> expectedAllowedToolNames,
                                             final List<String> expectedRequiredToolNames) {
        LLMUsabilityScenario actual = createMinimalBaseline().get(scenarioIndex);
        
        assertThat(actual.getScenarioId(), is(expectedScenarioId));
        assertThat(actual.getRuntimeKind(), is("h2"));
        assertThat(actual.getDimension(), is(expectedDimension));
        assertThat(actual.getExpectedFirstActionNames(), is(expectedFirstActionNames));
        assertThat(actual.getExpectedResourceUris(), is(expectedResourceUris));
        assertThat(actual.isResourceHitRequired(), is(expectedResourceHitRequired));
        assertThat(actual.isRecoveryExpected(), is(expectedRecoveryExpected));
        assertThat(actual.getLlmScenario().getAllowedToolNames(), is(expectedAllowedToolNames));
        assertThat(actual.getLlmScenario().getRequiredToolNames(), is(expectedRequiredToolNames));
        assertThat(actual.getLlmScenario().getExpectedAnswer().getDatabase(), is("logic_db"));
        assertThat(actual.getLlmScenario().getExpectedAnswer().getSchema(), is("public"));
        assertThat(actual.getLlmScenario().getExpectedAnswer().getTable(), is("orders"));
        assertThat(actual.getLlmScenario().getExpectedAnswer().getQuery(), is("SELECT COUNT(*) AS total_orders FROM orders"));
        assertThat(actual.getLlmScenario().getExpectedAnswer().getTotalOrders(), is(2));
    }
    
    static Stream<Arguments> assertCreateMinimalBaselineScenarioCases() {
        return Stream.of(
                Arguments.of("resource capabilities scenario", 0, "resource-capabilities-h2", LLMUsabilityDimension.RESOURCE,
                        List.of("mcp_read_resource"), List.of("shardingsphere://capabilities"), true, false,
                        List.of("mcp_read_resource", "search_metadata", "execute_query"),
                        List.of("mcp_read_resource", "search_metadata", "execute_query")),
                Arguments.of("resource table scenario", 1, "resource-table-h2", LLMUsabilityDimension.RESOURCE,
                        List.of("mcp_read_resource"), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, false,
                        List.of("mcp_read_resource", "execute_query"), List.of("mcp_read_resource", "execute_query")),
                Arguments.of("tool list tables scenario", 2, "tool-list-tables-h2", LLMUsabilityDimension.TOOL,
                        List.of("search_metadata"), List.of(), false, false,
                        List.of("search_metadata", "execute_query"), List.of("search_metadata", "execute_query")),
                Arguments.of("tool search metadata scenario", 3, "tool-search-metadata-h2", LLMUsabilityDimension.TOOL,
                        List.of("search_metadata"), List.of(), false, false,
                        List.of("search_metadata", "mcp_read_resource", "execute_query"),
                        List.of("search_metadata", "mcp_read_resource", "execute_query")),
                Arguments.of("recovery missing database scenario", 4, "recovery-missing-database-h2", LLMUsabilityDimension.RECOVERY,
                        List.of("search_metadata"), List.of(), false, true,
                        List.of("search_metadata", "execute_query"), List.of("search_metadata", "execute_query")),
                Arguments.of("recovery bad resource scenario", 5, "recovery-bad-resource-h2", LLMUsabilityDimension.RECOVERY,
                        List.of("mcp_read_resource"), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, true,
                        List.of("mcp_read_resource", "execute_query"), List.of("mcp_read_resource", "execute_query")));
    }
    
    private List<LLMUsabilityScenario> createMinimalBaseline() {
        return new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public",
                "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2);
    }
}
