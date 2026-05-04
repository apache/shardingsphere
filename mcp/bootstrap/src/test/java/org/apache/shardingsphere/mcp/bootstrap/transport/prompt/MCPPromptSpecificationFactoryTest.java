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

package org.apache.shardingsphere.mcp.bootstrap.transport.prompt;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class MCPPromptSpecificationFactoryTest {
    
    @Test
    void assertCreatePromptSpecifications() {
        List<SyncPromptSpecification> actual = new MCPPromptSpecificationFactory().createPromptSpecifications();
        SyncPromptSpecification actualPromptSpecification = findPrompt(actual, "safe_sql_execution");
        assertThat(actualPromptSpecification.prompt().title(), is("Safe SQL Execution"));
        assertThat(actualPromptSpecification.prompt().arguments().size(), is(3));
        assertThat(actualPromptSpecification.prompt().meta().get("relatedTools"), is(List.of("execute_query", "execute_update")));
    }
    
    @Test
    void assertRenderPromptTemplate() {
        SyncPromptSpecification promptSpecification = findPrompt(new MCPPromptSpecificationFactory().createPromptSpecifications(), "inspect_metadata");
        McpSchema.GetPromptResult actual = promptSpecification.promptHandler().apply(mock(McpSyncServerExchange.class),
                new McpSchema.GetPromptRequest("inspect_metadata", Map.of("database", "logic_db", "schema", "public", "query", "orders")));
        assertThat(actual.description(),
                is("Guide the model to inspect ShardingSphere logical metadata by reading capability and metadata resources before choosing search_metadata or detail resources."));
        assertThat(((McpSchema.TextContent) actual.messages().get(0).content()).text(), containsString("database: logic_db"));
        assertThat(((McpSchema.TextContent) actual.messages().get(0).content()).text(), containsString("query: orders"));
        assertThat(((McpSchema.TextContent) actual.messages().get(0).content()).text(), containsString("Stop conditions:"));
        assertThat(actual.meta().get("stopConditions"), is(List.of(
                "Stop after returning resolved metadata paths or after identifying the exact resource/tool to call next.",
                "Stop without SQL execution when the user only asked to inspect metadata.")));
        assertThat(actual.meta().get("templateResource"), is("META-INF/shardingsphere-mcp/prompts/inspect-metadata.md"));
    }
    
    private SyncPromptSpecification findPrompt(final List<SyncPromptSpecification> specifications, final String name) {
        return specifications.stream().filter(each -> name.equals(each.prompt().name())).findFirst().orElseThrow();
    }
}
