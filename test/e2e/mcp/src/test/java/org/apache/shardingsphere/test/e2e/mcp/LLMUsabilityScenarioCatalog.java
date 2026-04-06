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

package org.apache.shardingsphere.test.e2e.mcp;

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
    
    private static final String RESOURCE_READ_BRIDGE_NAME = "mcp_read_resource";
    
    List<LLMUsabilityScenario> createMinimalBaseline(final String runtimeKind, final String databaseName, final String schemaName,
                                                     final String tableName, final String query, final int totalOrders) {
        List<LLMUsabilityScenario> result = new LinkedList<>();
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        result.add(createScenario("resource-capabilities-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-capabilities-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `shardingsphere://capabilities`. "
                                + "Then discover and verify the count in " + databaseName + "." + schemaName + "." + tableName
                                + " using the SQL `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "list_tables", "describe_table", "execute_query"),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                List.of(RESOURCE_READ_BRIDGE_NAME), List.of("shardingsphere://capabilities"), true, false));
        result.add(createScenario("resource-table-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-table-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + RESOURCE_READ_BRIDGE_NAME + " with uri `" + tableResourceUri + "`. "
                                + "Then verify the count in " + databaseName + "." + schemaName + "." + tableName + " using `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "describe_table", "execute_query"),
                        List.of(RESOURCE_READ_BRIDGE_NAME, "execute_query")),
                List.of(RESOURCE_READ_BRIDGE_NAME), List.of(tableResourceUri), true, false));
        result.add(createScenario("tool-list-tables-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("tool-list-tables-" + runtimeKind, SYSTEM_PROMPT,
                        "Use list_tables first for " + databaseName + "." + schemaName + ", then describe the orders table and verify `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("list_tables", "describe_table", "execute_query"),
                        List.of("list_tables", "describe_table", "execute_query")),
                List.of("list_tables"), List.of(), false, false));
        result.add(createScenario("tool-search-metadata-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("tool-search-metadata-" + runtimeKind, SYSTEM_PROMPT,
                        "Use search_metadata first to locate the orders table in " + databaseName + "." + schemaName
                                + ", then describe it and verify `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "describe_table", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, false));
        result.add(createScenario("recovery-missing-database-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                new LLME2EScenario("recovery-missing-database-" + runtimeKind, SYSTEM_PROMPT,
                        "First call search_metadata with only schema `" + schemaName + "` and query `" + tableName + "`. "
                                + "After the server rejects it, recover with the correct database and finish by verifying `" + query + "`.",
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "describe_table", "execute_query"),
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
        return result;
    }
    
    private LLMUsabilityScenario createScenario(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind,
                                                final LLME2EScenario llmScenario, final List<String> expectedFirstActionNames,
                                                final List<String> expectedResourceUris, final boolean resourceHitRequired,
                                                final boolean recoveryExpected) {
        return new LLMUsabilityScenario(scenarioId, dimension, runtimeKind, llmScenario, expectedFirstActionNames, expectedResourceUris, resourceHitRequired, recoveryExpected);
    }
}
