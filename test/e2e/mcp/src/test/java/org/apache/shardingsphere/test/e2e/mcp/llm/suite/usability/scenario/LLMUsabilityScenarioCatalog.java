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
            Use MCP resources, metadata search, SQL tools, prompts, completions, and workflow tools only when they help the task.
            Inspect MCP context before touching unknown runtime, metadata, or side-effecting operations.
            Preview side effects and ask the user before execution.
            Do not guess database structure, workflow state, or query results.
            Return JSON only when asked for the final answer.
            """.trim();
    
    /**
     * Create core gate scenarios.
     *
     * @param runtimeKind runtime kind
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param query query
     * @param totalOrders total orders
     * @return core gate scenarios
     */
    public List<LLMUsabilityScenario> createCoreGate(final String runtimeKind, final String databaseName, final String schemaName, final String tableName,
                                                     final String query, final int totalOrders) {
        List<LLMUsabilityScenario> result = new LinkedList<>();
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        String toolContext = String.format(Locale.ENGLISH, " Use logical database `%s` and schema `%s` when the MCP action needs explicit runtime scope.", databaseName, schemaName);
        result.add(createScenario("natural-metadata-lookup-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural", "metadata"),
                new LLME2EScenario("natural-metadata-lookup-" + runtimeKind, SYSTEM_PROMPT,
                        "A user wants to inspect the live `" + tableName + "` table shape and confirm how many rows are visible. "
                                + "Use MCP metadata instead of guessing, then verify the count with `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "search_metadata"), List.of(), false, false));
        result.add(createScenario("natural-read-only-sql-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural", "read-only-sql"),
                new LLME2EScenario("natural-read-only-sql-" + runtimeKind, SYSTEM_PROMPT,
                        "A user asks how many rows are in `" + tableName + "` right now. Answer only after checking the database with `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"),
                        List.of("execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"), List.of(), false, false));
        String previewUpdateSql = "UPDATE orders SET status = status WHERE order_id = -1";
        result.add(createScenario("natural-side-effect-preview-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural", "side-effect-preview"),
                new LLME2EScenario("natural-side-effect-preview-" + runtimeKind, SYSTEM_PROMPT,
                        "A user is considering SQL `" + previewUpdateSql + "`. Review its side-effect scope without changing data before doing any row-count check. "
                                + "After the preview result is available, verify the row count with `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_update", "execute_query"),
                        List.of("execute_update", "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_update"), List.of(), false, false));
        List<String> workflowActions = List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "execute_query");
        List<String> workflowRequiredActions = List.of("plan_mask_rule", "apply_workflow", "execute_query");
        result.add(createScenario("natural-workflow-manual-export-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural", "workflow"),
                new LLME2EScenario("natural-workflow-manual-export-" + runtimeKind, SYSTEM_PROMPT,
                        "Prepare a mask-rule workflow for logical database `" + databaseName + "`, schema `" + schemaName + "`, table `" + tableName
                                + "`, and column `status` using MD5. Keep runtime side effects out of MCP, export reviewable artifacts for manual execution, "
                                + "and finish by verifying `" + query + "`.",
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        workflowActions, workflowRequiredActions),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule"), List.of(), false, false));
        result.add(createScenario("natural-table-resource-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural", "resource"),
                new LLME2EScenario("natural-table-resource-" + runtimeKind, SYSTEM_PROMPT,
                        "The user points to `" + tableResourceUri + "` as the table context. Read the live table detail when useful, then verify `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, false));
        return result;
    }
    
    /**
     * Create extended score scenarios.
     *
     * @param runtimeKind runtime kind
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param query query
     * @param totalOrders total orders
     * @return extended score scenarios
     */
    public List<LLMUsabilityScenario> createExtendedScore(final String runtimeKind, final String databaseName, final String schemaName, final String tableName,
                                                          final String query, final int totalOrders) {
        List<LLMUsabilityScenario> result = new LinkedList<>();
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        String toolContext = String.format(Locale.ENGLISH, " Use logical database `%s` and schema `%s` when the MCP action needs explicit runtime scope.", databaseName, schemaName);
        List<String> promptCompletionActions = List.of(
                MCPInteractionActionNames.LIST_PROMPTS, MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE, "execute_query");
        result.add(createScenario("extended-prompt-completion-inspect-" + runtimeKind, LLMUsabilityDimension.TOOL, runtimeKind,
                List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "prompt", "completion"),
                new LLME2EScenario("extended-prompt-completion-inspect-" + runtimeKind, SYSTEM_PROMPT,
                        "Use the MCP prompt list, the `inspect_metadata` prompt with database `" + databaseName + "`, schema `" + schemaName
                                + "`, and query `" + tableName + "`, and MCP completion support with reference `{\"type\":\"ref/prompt\",\"name\":\"inspect_metadata\"}`, "
                                + "argument_name `schema`, argument_value `pub`, and context_arguments `{\"database\":\"" + databaseName + "\"}`. Then verify `" + query + "`."
                                + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders), promptCompletionActions, promptCompletionActions),
                List.of(MCPInteractionActionNames.LIST_PROMPTS, MCPInteractionActionNames.GET_PROMPT, MCPInteractionActionNames.COMPLETE), List.of(), false, false));
        result.add(createScenario("extended-resource-list-discovery-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "resource-discovery"),
                new LLME2EScenario("extended-resource-list-discovery-" + runtimeKind, SYSTEM_PROMPT,
                        "Discover the available metadata resources, read exact table resource `" + tableResourceUri
                                + "`, and verify `" + query + "`. Do not read placeholder URI text from tool schema descriptions." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.LIST_RESOURCES), List.of(tableResourceUri), true, false));
        result.add(createScenario("extended-runtime-status-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "runtime-diagnostics"),
                new LLME2EScenario("extended-runtime-status-" + runtimeKind, SYSTEM_PROMPT,
                        "Read exact runtime resource `shardingsphere://runtime` and configured databases before answering. "
                                + "Follow any read-only resource next_actions from the runtime response before verifying `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://runtime"), true, false));
        result.add(createScenario("extended-recovery-missing-database-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "extended", "recovery"),
                new LLME2EScenario("extended-recovery-missing-database-" + runtimeKind, SYSTEM_PROMPT,
                        "The user only remembers schema `" + schemaName + "` and table `" + tableName + "`. Search metadata broadly with query `" + tableName
                                + "` and object type `table` without setting database or schema. If more than one database contains `" + tableName + "`, choose logical database `"
                                + databaseName + "` and do not use `analytics_db`. Then verify `" + query + "`.",
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of("search_metadata", "execute_query"),
                        List.of("search_metadata", "execute_query")),
                List.of("search_metadata"), List.of(), false, true, "ambiguous"));
        result.add(createScenario("extended-recovery-bad-resource-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "recovery", "resource"),
                new LLME2EScenario("extended-recovery-bad-resource-" + runtimeKind, SYSTEM_PROMPT,
                        "The user pasted stale resource `shardingsphere://databases/unknown/schemas/unknown/tables/" + tableName
                                + "`. Read the stale resource, recover by reading exact live table resource `" + tableResourceUri + "`, and verify `" + query + "`." + toolContext,
                        createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                        List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, true, "not_found"));
        if ("h2".equals(runtimeKind)) {
            result.add(createScenario("extended-workflow-context-recovery-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                    List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "workflow", "recovery"),
                    new LLME2EScenario("extended-workflow-context-recovery-" + runtimeKind, SYSTEM_PROMPT,
                            "Read exact runtime resource `shardingsphere://runtime`, plan a MD5 mask workflow for `" + tableName
                                    + "` column `status` without qualifying the column name, then use the exact plan_id from the planning response to preview and export manual artifacts. "
                                    + "Do not validate the workflow before manual artifacts are executed. After manual artifacts are exported, still call execute_query for the independent "
                                    + "read-only verification `" + query + "` before the final answer." + toolContext,
                            new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of(
                                    MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "execute_query")),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://runtime"), true, false));
            result.add(createScenario("extended-database-disambiguation-" + runtimeKind, LLMUsabilityDimension.RESOURCE, runtimeKind,
                    List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "multi-database"),
                    new LLME2EScenario("extended-database-disambiguation-" + runtimeKind, SYSTEM_PROMPT,
                            "Before any metadata search or SQL, read exact database list resource `shardingsphere://databases`, choose the live transactional database instead of the "
                                    + "analytics snapshot, locate `" + tableName + "`, and verify `" + query + "`." + toolContext,
                            createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "search_metadata", "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://databases"), true, false));
        }
        if ("mysql".equals(runtimeKind)) {
            result.add(createScenario("extended-recovery-unsupported-sequence-" + runtimeKind, LLMUsabilityDimension.RECOVERY, runtimeKind,
                    List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "extended", "recovery", "unsupported-resource"),
                    new LLME2EScenario("extended-recovery-unsupported-sequence-" + runtimeKind, SYSTEM_PROMPT,
                            "The user asks about sequence metadata near `" + tableName + "` on this MySQL runtime. Recover safely if sequence resources are unsupported, then verify `"
                                    + query + "`." + toolContext,
                            createAnswer(databaseName, schemaName, tableName, query, totalOrders),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query"),
                            List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_query")),
                    List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), true, true, "not_found"));
        }
        return result;
    }
    
    private LLMStructuredAnswer createAnswer(final String databaseName, final String schemaName, final String tableName, final String query, final int totalOrders) {
        return new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, List.of());
    }
    
    private LLMUsabilityScenario createScenario(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind,
                                                final List<String> tags, final LLME2EScenario llmScenario,
                                                final List<String> expectedFirstActionNames, final List<String> expectedResourceUris,
                                                final boolean resourceHitRequired, final boolean recoveryExpected) {
        return createScenario(scenarioId, dimension, runtimeKind, tags, llmScenario, expectedFirstActionNames, expectedResourceUris, resourceHitRequired, recoveryExpected, "");
    }
    
    private LLMUsabilityScenario createScenario(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind,
                                                final List<String> tags, final LLME2EScenario llmScenario,
                                                final List<String> expectedFirstActionNames, final List<String> expectedResourceUris,
                                                final boolean resourceHitRequired, final boolean recoveryExpected, final String expectedRecoveryCategory) {
        return new LLMUsabilityScenario(scenarioId, dimension, runtimeKind, tags, llmScenario, expectedFirstActionNames, expectedResourceUris, resourceHitRequired, recoveryExpected,
                expectedRecoveryCategory);
    }
}
