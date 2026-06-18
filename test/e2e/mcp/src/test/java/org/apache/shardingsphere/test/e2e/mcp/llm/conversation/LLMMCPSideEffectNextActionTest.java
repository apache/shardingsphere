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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPSideEffectNextActionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("executionActionProvider")
    void assertIsExecutionAction(final String name, final Map<String, Object> action, final boolean expected) {
        assertThat(LLMMCPSideEffectNextAction.isExecutionAction(action), is(expected));
    }
    
    private static Stream<Object[]> executionActionProvider() {
        return Stream.of(
                new Object[]{"execute update", Map.of("type", "tool_call", "tool_name", "database_gateway_execute_update", "arguments", Map.of("execution_mode", "execute")), true},
                new Object[]{"review workflow", Map.of("type", "tool_call", "tool_name", "database_gateway_apply_workflow", "arguments", Map.of("execution_mode", "review-then-execute")), true},
                new Object[]{"read only tool call", Map.of("type", "tool_call", "tool_name", "database_gateway_execute_query", "arguments", Map.of()), false},
                new Object[]{"resource action", Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases"), false});
    }
}
