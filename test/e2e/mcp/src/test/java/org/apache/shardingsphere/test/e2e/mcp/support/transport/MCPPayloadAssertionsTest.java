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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPPayloadAssertionsTest {
    
    @Test
    void assertAssertSingleItemValue() {
        MCPPayloadAssertions.assertSingleItemValue(Map.of("items", List.of(Map.of("name", "orders"))), "name", "orders");
    }
    
    @Test
    void assertAssertItemValues() {
        MCPPayloadAssertions.assertItemValues(createPayload(), "name", List.of("orders", "users"));
    }
    
    @Test
    void assertGetSingleItem() {
        assertThat(MCPPayloadAssertions.getSingleItem(Map.of("items", List.of(Map.of("name", "orders")))), is(Map.of("name", "orders")));
    }
    
    @Test
    void assertFindItem() {
        assertThat(MCPPayloadAssertions.findItem(createPayload(), "name", "users"), is(Map.of("name", "users", "type", "table")));
    }
    
    @Test
    void assertGetItemValues() {
        assertThat(MCPPayloadAssertions.getItemValues(createPayload(), "name"), is(List.of("orders", "users")));
    }
    
    @Test
    void assertGetItems() {
        assertThat(MCPPayloadAssertions.getItems(createPayload()), is(List.of(Map.of("name", "orders", "type", "table"), Map.of("name", "users", "type", "table"))));
    }
    
    @Test
    void assertGetMap() {
        assertThat(MCPPayloadAssertions.getMap(Map.of("name", "orders")), is(Map.of("name", "orders")));
    }
    
    @Test
    void assertGetMapList() {
        assertThat(MCPPayloadAssertions.getMapList(List.of(Map.of("name", "orders"))), is(List.of(Map.of("name", "orders"))));
    }
    
    @Test
    void assertAssertToolDefinition() {
        MCPPayloadAssertions.assertToolDefinition(List.of(Map.of(
                "name", "database_gateway_execute_query",
                "title", "Execute Query",
                "inputSchema", Map.of(
                        "type", "object",
                        "required", List.of("sql"),
                        "properties", Map.of("sql", Map.of("type", "string"))))),
                "database_gateway_execute_query", "Execute Query", "sql", "sql", "string");
    }
    
    private Map<String, Object> createPayload() {
        return Map.of("items", List.of(Map.of("name", "orders", "type", "table"), Map.of("name", "users", "type", "table")));
    }
}
