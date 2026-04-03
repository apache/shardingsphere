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

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriTemplateTest {
    
    @Test
    void assertMatch() {
        Optional<UriTemplateMatch> actual = new UriTemplate("shardingsphere://capabilities").match("shardingsphere://capabilities");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getTemplate(), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertMatchWithVariables() {
        Optional<UriTemplateMatch> actual = new UriTemplate(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}")
                .match("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getVariable("database"), is("logic_db"));
        assertThat(actual.orElseThrow().getVariable("schema"), is("public"));
        assertThat(actual.orElseThrow().getVariable("table"), is("orders"));
        assertThat(actual.orElseThrow().getVariable("column"), is("order_id"));
    }
    
    @Test
    void assertMatchWithInvalidUri() {
        Optional<UriTemplateMatch> actual = new UriTemplate("shardingsphere://capabilities").match("unsupported://capabilities");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetRouteSignature() {
        String actual = new UriTemplate("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}").getRouteSignature();
        assertThat(actual, is("shardingsphere://databases/{}/schemas/{}/tables/{}/columns/{}"));
    }
    
    @Test
    void assertOverlaps() {
        boolean actual = new UriTemplate("shardingsphere://databases/{database}")
                .overlaps(new UriTemplate("shardingsphere://databases/default_db"));
        assertTrue(actual);
    }
    
    @Test
    void assertCreateWithInvalidTemplate() {
        assertThrows(IllegalArgumentException.class, () -> new UriTemplate("invalid-template"));
    }
    
    @Test
    void assertGetVariableWithMissingVariable() {
        Optional<UriTemplateMatch> actual = new UriTemplate("shardingsphere://capabilities").match("shardingsphere://capabilities");
        assertThrows(IllegalArgumentException.class, () -> actual.orElseThrow().getVariable("database"));
    }
}
