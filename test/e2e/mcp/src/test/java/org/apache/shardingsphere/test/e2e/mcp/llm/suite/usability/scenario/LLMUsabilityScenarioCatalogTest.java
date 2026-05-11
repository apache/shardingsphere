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
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createCoreGate("h2", "logic_db", "public", "orders",
                "SELECT COUNT(*) AS total_orders FROM orders", 2);
        Map<String, LLMUsabilityScenario> actualScenarios = actual.stream().collect(Collectors.toMap(LLMUsabilityScenario::getScenarioId, each -> each));
        assertThat(actualScenarios.keySet(), hasItems("natural-metadata-lookup-h2", "natural-read-only-sql-h2", "natural-side-effect-preview-h2",
                "natural-workflow-manual-export-h2", "natural-table-resource-h2"));
        assertThat(actualScenarios.get("natural-side-effect-preview-h2").getLlmScenario().getRequiredToolNames(), is(List.of("execute_update", "execute_query")));
        assertThat(actualScenarios.get("natural-table-resource-h2").getExpectedResourceUris(),
                is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders")));
        assertThat(actualScenarios.get("natural-workflow-manual-export-h2").getLlmScenario().getRequiredToolNames(),
                is(List.of("plan_mask_rule", "apply_workflow", "execute_query")));
        assertThat(actualScenarios.get("natural-workflow-manual-export-h2").getExpectedResourceUris(), is(List.of()));
        assertThat(actualScenarios.get("natural-workflow-manual-export-h2").getLlmScenario().getUserPrompt(),
                containsString("table `orders`, and column `status`"));
        assertTrue(actual.stream().allMatch(each -> each.getTags().contains("natural")));
        assertTrue(actual.stream().allMatch(LLMUsabilityScenario::isNaturalTask));
        assertTrue(actual.stream().noneMatch(LLMUsabilityScenario::isProtocolContract));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("First call")));
    }
    
    @Test
    void assertCreateExtendedScoreIncludesRecoveryAndDiagnosticsScenarios() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createExtendedScore("h2", "logic_db", "public", "orders",
                "SELECT COUNT(*) AS total_orders FROM orders", 2);
        Map<String, LLMUsabilityScenario> actualScenarios = actual.stream().collect(Collectors.toMap(LLMUsabilityScenario::getScenarioId, each -> each));
        assertThat(actualScenarios.keySet(), hasItems("extended-prompt-completion-inspect-h2", "extended-runtime-status-h2", "extended-recovery-missing-database-h2",
                "extended-workflow-context-recovery-h2", "extended-database-disambiguation-h2"));
        assertThat(actualScenarios.get("extended-runtime-status-h2").getExpectedResourceUris(), is(List.of("shardingsphere://runtime")));
        assertTrue(actualScenarios.get("extended-recovery-missing-database-h2").isRecoveryExpected());
        assertThat(actualScenarios.get("extended-recovery-missing-database-h2").getExpectedRecoveryCategory(), is("ambiguous"));
        assertTrue(actualScenarios.get("extended-recovery-bad-resource-h2").isRecoveryExpected());
        assertThat(actualScenarios.get("extended-recovery-bad-resource-h2").getExpectedRecoveryCategory(), is("not_found"));
        assertThat(actualScenarios.get("extended-prompt-completion-inspect-h2").getLlmScenario().getUserPrompt(), containsString("Use the MCP prompt list"));
        assertThat(actualScenarios.get("extended-workflow-context-recovery-h2").getLlmScenario().getRequiredToolNames(),
                is(List.of("mcp_read_resource", "plan_mask_rule", "apply_workflow", "execute_query")));
        assertThat(actualScenarios.get("extended-workflow-context-recovery-h2").getLlmScenario().getUserPrompt(), containsString("Do not validate the workflow"));
        assertTrue(actualScenarios.get("extended-recovery-missing-database-h2").isNaturalTask());
        assertTrue(actualScenarios.get("extended-prompt-completion-inspect-h2").isProtocolContract());
        assertTrue(actualScenarios.get("extended-runtime-status-h2").isProtocolContract());
        assertTrue(actual.stream().allMatch(each -> each.getTags().contains("extended")));
        assertTrue(actual.stream().allMatch(each -> each.isNaturalTask() || each.isProtocolContract()));
        assertTrue(actual.stream().noneMatch(each -> each.isNaturalTask() && each.isProtocolContract()));
        assertTrue(actual.stream().noneMatch(each -> each.getLlmScenario().getUserPrompt().contains("First call")));
        assertFalse(actual.stream().filter(LLMUsabilityScenario::isNaturalTask).anyMatch(each -> each.getLlmScenario().getUserPrompt().contains("Use the MCP prompt list")));
    }
}
