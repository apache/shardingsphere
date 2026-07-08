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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilityScenarioCatalogTest {
    
    @Test
    void assertCreateCoreGateIncludesNaturalSafetyAndWorkflowScenarios() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createCoreGate("mysql", "logic_db", "logic_db", "orders",
                "SELECT COUNT(*) AS total_orders FROM orders", 2);
        Map<String, LLMUsabilityScenario> actualScenarios = actual.stream().collect(Collectors.toMap(LLMUsabilityScenario::getScenarioId, each -> each));
        assertThat(actualScenarios.keySet(), hasItems("natural-metadata-lookup-mysql", "natural-read-only-sql-mysql", "natural-side-effect-preview-mysql",
                "natural-workflow-manual-export-mysql", "natural-mask-rule-md5-mysql", "natural-encrypt-rule-md5-mysql", "natural-table-resource-mysql"));
        assertThat(actualScenarios.get("natural-side-effect-preview-mysql").getLlmScenario().getRequiredToolNames(), is(List.of("database_gateway_execute_update", "database_gateway_execute_query")));
        assertThat(actualScenarios.get("natural-table-resource-mysql").getExpectedResourceUris(),
                is(List.of("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders")));
        assertThat(actualScenarios.get("natural-workflow-manual-export-mysql").getLlmScenario().getRequiredToolNames(),
                is(List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow", "database_gateway_execute_query")));
        assertThat(actualScenarios.get("natural-workflow-manual-export-mysql").getExpectedResourceUris(), is(List.of()));
        assertThat(actualScenarios.get("natural-workflow-manual-export-mysql").getLlmScenario().getUserPrompt(),
                containsString("table `orders`, and column `status`"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getRequiredToolNames(),
                is(List.of("database_gateway_plan_mask_rule", "database_gateway_apply_workflow", "database_gateway_validate_workflow", "database_gateway_execute_query")));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("create a mask rule"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("table `orders`"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("MD5 mask algorithm"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("Do not send plan_id to the planning tool"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("manual-only"));
        assertThat(actualScenarios.get("natural-mask-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("validate the workflow"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getRequiredToolNames(),
                is(List.of("database_gateway_plan_encrypt_rule", "database_gateway_apply_workflow", "database_gateway_validate_workflow", "database_gateway_execute_query")));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("identify and create an encrypt rule now"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("table `orders`"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("MD5 encrypt algorithm"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("cipher column `status_cipher`"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("irreversible hashing, no equality, and no like"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("Do not send plan_id to the planning tool"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("manual-only"));
        assertThat(actualScenarios.get("natural-encrypt-rule-md5-mysql").getLlmScenario().getUserPrompt(), containsString("validate the workflow"));
        assertTrue(actual.stream().allMatch(each -> each.getTags().contains("natural")));
        assertTrue(actual.stream().allMatch(LLMUsabilityScenario::isNaturalTask));
        assertTrue(actual.stream().noneMatch(LLMUsabilityScenario::isProtocolContract));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("First call")));
    }
    
    @Test
    void assertCreateExtendedScoreIncludesRecoveryAndDiagnosticsScenarios() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createExtendedScore("mysql", "logic_db", "logic_db", "orders",
                "SELECT COUNT(*) AS total_orders FROM orders", 2);
        Map<String, LLMUsabilityScenario> actualScenarios = actual.stream().collect(Collectors.toMap(LLMUsabilityScenario::getScenarioId, each -> each));
        assertThat(actualScenarios.keySet(), hasItems("extended-prompt-completion-inspect-mysql", "extended-resource-list-discovery-mysql", "extended-runtime-status-mysql",
                "extended-recovery-missing-database-mysql", "extended-recovery-bad-resource-mysql"));
        assertThat(actualScenarios.get("extended-resource-list-discovery-mysql").getExpectedFirstActionNames(), is(List.of(MCPInteractionActionNames.LIST_TOOLS)));
        List<String> expectedDiscoveryToolNames = List.of(
                MCPInteractionActionNames.LIST_TOOLS, MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.LIST_RESOURCE_TEMPLATES,
                MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query");
        assertThat(actualScenarios.get("extended-resource-list-discovery-mysql").getLlmScenario().getAllowedToolNames(), is(expectedDiscoveryToolNames));
        assertThat(actualScenarios.get("extended-resource-list-discovery-mysql").getLlmScenario().getRequiredToolNames(), is(expectedDiscoveryToolNames));
        assertThat(actualScenarios.get("extended-runtime-status-mysql").getExpectedResourceUris(), is(List.of("shardingsphere://runtime")));
        assertFalse(actualScenarios.get("extended-recovery-missing-database-mysql").isRecoveryExpected());
        assertTrue(actualScenarios.get("extended-recovery-bad-resource-mysql").isRecoveryExpected());
        assertThat(actualScenarios.get("extended-recovery-bad-resource-mysql").getExpectedRecoveryCategory(), is("unknown_database"));
        assertThat(actualScenarios.get("extended-prompt-completion-inspect-mysql").getLlmScenario().getUserPrompt(), containsString("Use the MCP prompt list"));
        assertTrue(actualScenarios.get("extended-recovery-missing-database-mysql").isNaturalTask());
        assertTrue(actualScenarios.get("extended-prompt-completion-inspect-mysql").isProtocolContract());
        assertTrue(actualScenarios.get("extended-runtime-status-mysql").isProtocolContract());
        assertTrue(actual.stream().allMatch(each -> each.getTags().contains("extended")));
        assertTrue(actual.stream().allMatch(each -> each.isNaturalTask() || each.isProtocolContract()));
        assertTrue(actual.stream().noneMatch(each -> each.isNaturalTask() && each.isProtocolContract()));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("First call")));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("database_scope")));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("page_offset")));
        assertFalse(actual.stream().filter(LLMUsabilityScenario::isNaturalTask).anyMatch(each -> each.getLlmScenario().getUserPrompt().contains("Use the MCP prompt list")));
    }
}
