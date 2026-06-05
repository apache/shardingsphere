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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPNextActionsTest {
    
    @Test
    void assertGetNextActions() {
        Map<String, Object> topLevelAction = Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases");
        Map<String, Object> recoveryAction = Map.of("type", "tool_call", "tool_name", "database_gateway_search_metadata");
        assertThat(LLMMCPNextActions.getNextActions(Map.of(
                "next_actions", List.of(topLevelAction, "ignored"),
                "recovery", Map.of("next_actions", List.of(recoveryAction)))), is(List.of(topLevelAction, recoveryAction)));
    }
    
    @Test
    void assertGetNextActionsWithNoActions() {
        assertThat(LLMMCPNextActions.getNextActions(Map.of("next_actions", "ignored", "recovery", Map.of("next_actions", "ignored"))), is(List.of()));
    }
}
