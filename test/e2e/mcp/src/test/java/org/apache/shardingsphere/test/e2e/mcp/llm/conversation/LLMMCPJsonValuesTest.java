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
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMMCPJsonValuesTest {
    
    @Test
    void assertParseToolArguments() {
        assertThat(LLMMCPJsonValues.parseToolArguments("{\"sql\":\"SELECT 1\",\"limit\":1}"), is(Map.of("sql", "SELECT 1", "limit", 1)));
    }
    
    @Test
    void assertParseToolArgumentsWithInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> LLMMCPJsonValues.parseToolArguments("{invalid"));
    }
    
    @Test
    void assertCastToRows() {
        assertThat(LLMMCPJsonValues.castToRows(List.of(List.of("orders", 2))), is(List.of(List.of("orders", 2))));
    }
    
    @Test
    void assertCastToMap() {
        assertThat(LLMMCPJsonValues.castToMap(Map.of("database", "logic_db")), is(Map.of("database", "logic_db")));
    }
    
    @Test
    void assertCastToList() {
        assertThat(LLMMCPJsonValues.<Map<String, Object>>castToList(List.of(Map.of("name", "orders"))), is(List.of(Map.of("name", "orders"))));
    }
    
    @Test
    void assertCastToListWithNull() {
        assertThat(LLMMCPJsonValues.castToList(null), is(List.of()));
    }
    
    @Test
    void assertCastToStringMap() {
        assertThat(LLMMCPJsonValues.castToStringMap(Map.of("schema", "public")), is(Map.of("schema", "public")));
    }
}
