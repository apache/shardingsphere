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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

class LLMUsabilityScenarioCatalogTest {
    
    @Test
    void assertCreateMinimalBaselineIncludesSafetyAndWorkflowScenarios() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public", "orders",
                "SELECT COUNT(*) AS total_orders FROM orders", 2);
        Map<String, LLMUsabilityScenario> actualScenarios = actual.stream().collect(Collectors.toMap(LLMUsabilityScenario::getScenarioId, each -> each));
        assertThat(actualScenarios.keySet(), hasItems("tool-preview-update-h2", "tool-search-detail-uri-h2", "workflow-mask-preview-validate-h2"));
        assertThat(actualScenarios.get("tool-preview-update-h2").getLlmScenario().getRequiredToolNames(), is(List.of("execute_update", "execute_query")));
        assertThat(actualScenarios.get("tool-search-detail-uri-h2").getExpectedResourceUris(),
                is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders")));
        assertThat(actualScenarios.get("workflow-mask-preview-validate-h2").getLlmScenario().getRequiredToolNames(),
                is(List.of(MCPInteractionActionNames.READ_RESOURCE, "plan_mask_rule", "apply_workflow", "validate_workflow", "execute_query")));
    }
}
