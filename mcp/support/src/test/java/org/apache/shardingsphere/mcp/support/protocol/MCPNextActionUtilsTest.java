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

package org.apache.shardingsphere.mcp.support.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPNextActionUtilsTest {
    
    @Test
    void assertCallToolUsesCanonicalFields() {
        Map<String, Object> actual = MCPNextActionUtils.callTool("search_metadata", "Search metadata.", Map.of("page_size", 100), false);
        assertThat(actual.get("order"), is(1));
        assertThat(actual.get("type"), is("tool_call"));
        assertThat(actual.get("title"), is("Call search_metadata"));
        assertThat(actual.get("tool_name"), is("search_metadata"));
        assertThat(actual.get("arguments"), is(Map.of("page_size", 100)));
        assertFalse((Boolean) actual.get("requires_user_approval"));
    }
    
    @Test
    void assertOrderedCopiesActions() {
        Map<String, Object> action = Map.of("type", "terminal");
        List<Map<String, Object>> actual = MCPNextActionUtils.ordered(action);
        assertThat(actual.get(0).get("order"), is(1));
        assertFalse(action.containsKey("order"));
    }
    
    @Test
    void assertDependsOnCopiesAction() {
        Map<String, Object> action = Map.of("type", "tool_call");
        Map<String, Object> actual = MCPNextActionUtils.dependsOn(action, 1);
        assertThat(actual.get("depends_on"), is(List.of(1)));
        assertFalse(action.containsKey("depends_on"));
    }
}
