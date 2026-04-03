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

package org.apache.shardingsphere.mcp.uri;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPUriPatternTest {
    
    @Test
    void assertGetPattern() {
        MCPUriPattern actual = new MCPUriPattern("shardingsphere://capabilities");
        assertThat(actual.getPattern(), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertCreateWithUnsupportedScheme() {
        assertThrows(IllegalArgumentException.class, () -> new MCPUriPattern("unsupported://capabilities"));
    }
    
    @Test
    void assertParse() {
        Optional<MCPUriVariables> actual = new MCPUriPattern("shardingsphere://capabilities").parse("shardingsphere://capabilities");
        assertTrue(actual.isPresent());
        assertTrue(actual.orElseThrow().getVariables().isEmpty());
    }
    
    @Test
    void assertGetVariableNames() {
        MCPUriPattern actual = new MCPUriPattern("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}");
        assertThat(actual.getVariableNames(), is(List.of("database", "schema", "table", "column")));
    }
    
    @Test
    void assertParseWithVariables() {
        Optional<MCPUriVariables> actual = new MCPUriPattern(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}")
                .parse("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getRequired("database"), is("logic_db"));
        assertThat(actual.orElseThrow().getRequired("schema"), is("public"));
        assertThat(actual.orElseThrow().getRequired("table"), is("orders"));
        assertThat(actual.orElseThrow().getRequired("column"), is("order_id"));
    }
    
    @Test
    void assertParseWithInvalidUri() {
        Optional<MCPUriVariables> actual = new MCPUriPattern("shardingsphere://capabilities").parse("unsupported://capabilities");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertIsOverlaps() {
        boolean actual = new MCPUriPattern("shardingsphere://databases/{database}").isOverlaps(new MCPUriPattern("shardingsphere://databases/default_db"));
        assertTrue(actual);
    }
    
    @Test
    void assertCreateWithInvalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> new MCPUriPattern("invalid-template"));
    }
    
    @Test
    void assertGetVariableWithMissingVariable() {
        Optional<MCPUriVariables> actual = new MCPUriPattern("shardingsphere://capabilities").parse("shardingsphere://capabilities");
        assertThrows(IllegalArgumentException.class, () -> actual.orElseThrow().getRequired("database"));
    }
}
