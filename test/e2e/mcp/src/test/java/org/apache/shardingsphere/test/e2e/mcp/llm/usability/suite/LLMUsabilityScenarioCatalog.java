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

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenario;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

final class LLMUsabilityScenarioCatalog {
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating an MCP server.
            Use the provided MCP actions exactly as needed.
            Do not guess database structure or query results.
            Return JSON only when asked for the final answer.
            """.trim();
    
    private static final String RESOURCE_LIST_BRIDGE_NAME = "mcp_list_resources";
    
    private static final String RESOURCE_READ_BRIDGE_NAME = "mcp_read_resource";
    
    List<LLMUsabilityScenario> createMinimalBaseline(final String runtimeKind, final String databaseName, final String schemaName,
                                                     final String tableName, final String query, final int totalOrders) {
        List<LLMUsabilityScenario> result = new LinkedList<>();
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        result.add(createScenario("resource-capabilities-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-capabilities-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `shardingsphere://capabilities`. "
                                + "Then use search_metadata to locate `" + tableName + "` in " + databaseName + "." + schemaName
                                + " and verify the count using the SQL `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "search_metadata", "execute_query"),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "search_metadata", "execute_query")),
                List.of(RESOURCE_READ_BRIDGE_NAME), List.of("shardingsphere://capabilities"), true, false));
        result.add(createScenario("resource-table-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-table-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `" + tableResourceUri + "`. "
                                + "Then verify the count in " + databaseName + "." + schemaName + "." + tableName + " using `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query"),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                List.of(RESOURCE_READ_BRIDGE_NAME), List.of(tableResourceUri), true, false));
        result.add(createScenario("tool-list-tables-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("tool-list-tables-" + runtimeKind, SYSTEM_PROMPT,
                        "Use search_metadata first for " + databaseName + "." + schemaName + " to locate `" + tableName + "`, then verify `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, false));
        result.add(createScenario("resource-list-discovery-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-list-discovery-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_LIST_BRIDGE_NAME + " to discover the available metadata resources. "
                                + "Then read `" + tableResourceUri + "` and verify `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_LIST_BRIDGE_NAME, RESOURCE_READ_BRIDGE_NAME, "execute_query"),
                        List.of(RESOURCE_LIST_BRIDGE_NAME, RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                List.of(RESOURCE_LIST_BRIDGE_NAME), List.of(tableResourceUri), true, false));
        result.add(createScenario("recovery-missing-database-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                new LLME2EScenario("recovery-missing-database-" + runtimeKind, SYSTEM_PROMPT,
                        "First call search_metadata with only schema `" + schemaName + "` and query `" + tableName + "`. "
                                + "After the server rejects it, recover with the correct database and finish by verifying `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, true));
        result.add(createScenario("recovery-bad-resource-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                new LLME2EScenario("recovery-bad-resource-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `shardingsphere://databases/unknown/schemas/unknown/tables/" + tableName + "`. "
                                + "After the server rejects it, recover by reading `" + tableResourceUri + "` and finish by verifying `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query"),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                List.of(RESOURCE_READ_BRIDGE_NAME), List.of(tableResourceUri), true, true));
        if ("h2".equals(runtimeKind)) {
            result.add(createScenario("resource-database-disambiguation-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                    new LLME2EScenario("resource-database-disambiguation-" + runtimeKind, SYSTEM_PROMPT,
                            "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `shardingsphere://databases` to inspect the available databases. "
                                    + "Choose the live transactional database instead of the analytics snapshot, locate `" + tableName + "` in "
                                    + databaseName + "." + schemaName + ", and then verify `" + query + "`.",
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                            List.of(RESOURCE_READ_BRIDGE_NAME, "search_metadata", "execute_query"),
                            List.of(RESOURCE_READ_BRIDGE_NAME, "search_metadata", "execute_query")),
                    List.of(RESOURCE_READ_BRIDGE_NAME), List.of("shardingsphere://databases"), true, false));
        }
        if ("mysql".equals(runtimeKind)) {
            result.add(createScenario("recovery-unsupported-sequence-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                    new LLME2EScenario("recovery-unsupported-sequence-" + runtimeKind, SYSTEM_PROMPT,
                            "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `shardingsphere://databases/" + databaseName + "/schemas/" + schemaName + "/sequences`. "
                                    + "After the server rejects it because sequences are unsupported, recover by reading `" + tableResourceUri + "` and finish by verifying `"
                                    + query + "`.",
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                            List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query"),
                            List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                    List.of(RESOURCE_READ_BRIDGE_NAME), List.of(tableResourceUri), true, true));
        }
        return result;
    }
    
    private LLMUsabilityScenario createScenario(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind,
                                                final LLME2EScenario llmScenario, final List<String> expectedFirstActionNames,
                                                final List<String> expectedResourceUris, final boolean resourceHitRequired,
                                                final boolean recoveryExpected) {
        return new LLMUsabilityScenario(scenarioId, dimension, runtimeKind, llmScenario, expectedFirstActionNames, expectedResourceUris, resourceHitRequired, recoveryExpected);
    }
}
