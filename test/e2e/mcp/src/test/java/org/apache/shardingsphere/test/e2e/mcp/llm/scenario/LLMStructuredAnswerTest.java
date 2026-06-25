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

package org.apache.shardingsphere.test.e2e.mcp.llm.scenario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMStructuredAnswerTest {
    
    @Test
    void assertFromJson() {
        LLMStructuredAnswer actual = LLMStructuredAnswer.fromJson("""
                {
                  "database": " logic_db ",
                  "schema": " public ",
                  "table": " orders ",
                  "query": " SELECT COUNT(*) FROM orders ",
                  "totalOrders": 2,
                  "interactionSequence": [
                    "mcp_read_resource",
                    "database_gateway_execute_query",
                    "mcp_complete",
                    "database_gateway_search_metadata"
                  ]
                }
                """);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getTable(), is("orders"));
        assertThat(actual.getQuery(), is("SELECT COUNT(*) FROM orders"));
        assertThat(actual.getTotalOrders(), is(2));
        assertThat(actual.getInteractionSequence(), is(List.of("mcp_read_resource", "database_gateway_execute_query", "mcp_complete", "database_gateway_search_metadata")));
    }
    
    @Test
    void assertFromJsonRejectsObjectInteractionSequenceEntries() {
        assertThrows(IllegalArgumentException.class, () -> LLMStructuredAnswer.fromJson("""
                {"database":"logic_db","schema":"public","table":"orders","query":"SELECT 1","totalOrders":"2","interactionSequence":[{"tool":"database_gateway_execute_query"}]}
                """));
    }
    
    @Test
    void assertFromJsonWithInvalidTotalOrders() {
        assertThrows(IllegalArgumentException.class, () -> LLMStructuredAnswer.fromJson("""
                {"database":"logic_db","schema":"public","table":"orders","query":"SELECT 1","totalOrders":"bad"}
                """));
    }
    
    @Test
    void assertFromJsonWithInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> LLMStructuredAnswer.fromJson("{invalid"));
    }
}
