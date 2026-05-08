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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario;

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class LLMUsabilityScenarioCatalog {
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating an MCP server.
            Use the provided MCP actions exactly as needed.
            Do not guess database structure or query results.
            Return JSON only when asked for the final answer.
            """.trim();
    
    /**
     * Create the minimal usability baseline scenarios for one runtime kind.
     *
     * @param runtimeKind runtime kind
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param query query
     * @param totalOrders total orders
     * @return minimal usability baseline scenarios
     */
    public List<LLMUsabilityScenario> createMinimalBaseline(final String runtimeKind, final String databaseName, final String schemaName,
                                                            final String tableName, final String query, final int totalOrders) {
        List<LLMUsabilityScenario> result = new LinkedList<>();
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        String toolContext = " Use database `" + databaseName + "` and schema `" + schemaName
                + "` in SQL, metadata, and workflow tool arguments unless this scenario explicitly asks for a different invalid first call.";
        result.add(createScenario("resource-capabilities-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-capabilities-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `shardingsphere://capabilities`. "
                                + "Then use search_metadata to locate `" + tableName + "` in " + databaseName + "." + schemaName
                                + " and verify the count using the SQL `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://capabilities"), true, false));
        result.add(createScenario("resource-table-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-table-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `" + tableResourceUri + "`. "
                                + "Then verify the count in " + databaseName + "." + schemaName + "." + tableName + " using `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, false));
        result.add(createScenario("tool-list-tables-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("tool-list-tables-" + runtimeKind, SYSTEM_PROMPT,
                        "Use search_metadata first for " + databaseName + "." + schemaName + " to locate `" + tableName + "`, then verify `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, false));
        String previewUpdateSql = "UPDATE orders SET status = status WHERE order_id = -1";
        List<String> previewSqlActions = List.of("execute_update", "execute_query");
        result.add(createScenario("tool-preview-update-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("tool-preview-update-" + runtimeKind, SYSTEM_PROMPT,
                        "First preview the SQL `" + previewUpdateSql + "` with execute_update and execution_mode `preview`. "
                                + "Do not execute the update. Then verify `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()), previewSqlActions, previewSqlActions),
                List.of("execute_update"), List.of(), false, false));
        List<String> searchDetailActions = List.of("search_metadata", MCPInteractionActionNames.READ_RESOURCE, "execute_query");
        result.add(createScenario("tool-search-detail-uri-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("tool-search-detail-uri-" + runtimeKind, SYSTEM_PROMPT,
                        "Use search_metadata for `" + tableName + "` in " + databaseName + "." + schemaName
                                + ", read the returned table resource.uri, then verify `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()), searchDetailActions, searchDetailActions),
                List.of("search_metadata"), List.of(tableResourceUri), true, false));
        List<String> workflowActions = List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "validate_workflow", "execute_query");
        result.add(createScenario("workflow-mask-preview-validate-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("workflow-mask-preview-validate-" + runtimeKind, SYSTEM_PROMPT,
                        "Read `shardingsphere://features/mask/algorithms`, plan a mask rule for " + databaseName + "." + schemaName + "." + tableName
                                + ".status with algorithm_type `MD5`, preview apply_workflow, export it with execution_mode `manual-only`, validate the workflow, then verify `" + query + "`."
                                + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()), workflowActions, workflowActions),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://features/mask/algorithms"), true, false));
        List<String> promptCompletionActions = List.of(
                MCPInteractionActionNames.LIST_PROMPTS, MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "execute_query");
        result.add(createScenario("prompt-completion-inspect-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                new LLME2EScenario("prompt-completion-inspect-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.LIST_PROMPTS + ", then get the `inspect_metadata` prompt for database `" + databaseName + "` and query `"
                                + tableName + "`. Use " + MCPInteractionActionNames.COMPLETE + " for table prefix `ord`, then verify `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()), promptCompletionActions, promptCompletionActions),
                List.of(MCPInteractionActionNames.LIST_PROMPTS, MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE), List.of(), false, false));
        result.add(createScenario("resource-list-discovery-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-list-discovery-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.LIST_RESOURCES + " to discover the available metadata resources. "
                                + "Then read `" + tableResourceUri + "` and verify `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.LIST_RESOURCES), List.of(tableResourceUri), true, false));
        result.add(createScenario("resource-runtime-status-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                new LLME2EScenario("resource-runtime-status-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `shardingsphere://runtime` to inspect active transport and configured databases. "
                                + "Then verify `" + query + "` without guessing hidden runtime details." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://runtime"), true, false));
        result.add(createScenario("recovery-missing-database-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                new LLME2EScenario("recovery-missing-database-" + runtimeKind, SYSTEM_PROMPT,
                        "First call search_metadata with only schema `" + schemaName + "` and query `" + tableName + "`. "
                                + "After the server rejects it, recover with the correct database and finish by verifying `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of("search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, true));
        result.add(createScenario("recovery-bad-resource-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                new LLME2EScenario("recovery-bad-resource-" + runtimeKind, SYSTEM_PROMPT,
                        "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `shardingsphere://databases/unknown/schemas/unknown/tables/" + tableName + "`. "
                                + "After the server rejects it, recover by reading `" + tableResourceUri + "` and finish by verifying `" + query + "`." + toolContext,
                        new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, true));
        if ("h2".equals(runtimeKind)) {
            result.add(createScenario("workflow-context-recovery-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                    new LLME2EScenario("workflow-context-recovery-" + runtimeKind, SYSTEM_PROMPT,
                            "Read `shardingsphere://features/mask/algorithms`, plan a mask rule for " + databaseName + "." + schemaName + "." + tableName
                                    + ".status with algorithm_type `MD5`. Then simulate partial context loss: keep only the returned plan_id, read `shardingsphere://runtime`, "
                                    + "read `shardingsphere://workflows/{plan_id}` by replacing `{plan_id}` with the returned plan_id, export it with execution_mode `manual-only`, "
                                    + "validate the workflow, then verify `" + query + "`." + toolContext,
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of(
                                    MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "validate_workflow", "execute_query")),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "validate_workflow", "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "validate_workflow", "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://runtime"), true, false));
            result.add(createScenario("resource-database-disambiguation-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                    new LLME2EScenario("resource-database-disambiguation-" + runtimeKind, SYSTEM_PROMPT,
                            "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `shardingsphere://databases` to inspect the available databases. "
                                    + "Choose the live transactional database instead of the analytics snapshot, locate `" + tableName + "` in "
                                    + databaseName + "." + schemaName + ", and then verify `" + query + "`." + toolContext,
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://databases"), true, false));
        }
        if ("mysql".equals(runtimeKind)) {
            result.add(createScenario("recovery-unsupported-sequence-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                    new LLME2EScenario("recovery-unsupported-sequence-" + runtimeKind, SYSTEM_PROMPT,
                            "First call " + MCPInteractionActionNames.READ_RESOURCE + " with uri `shardingsphere://databases/" + databaseName + "/schemas/" + schemaName + "/sequences`. "
                                    + "After the server rejects it because sequences are unsupported, recover by reading `" + tableResourceUri + "` and finish by verifying `"
                                    + query + "`." + toolContext,
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of()),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, true));
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
