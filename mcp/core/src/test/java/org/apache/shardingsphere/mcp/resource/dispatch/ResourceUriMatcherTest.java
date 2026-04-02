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

package org.apache.shardingsphere.mcp.resource.dispatch;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUriMatcherTest {
    
    private final ResourceUriMatcher resourceUriPatternMatcher = new ResourceUriMatcher();
    
    @Test
    void assertMatch() {
        Optional<ResourceUriMatch> actual = resourceUriPatternMatcher.match("shardingsphere://capabilities", "shardingsphere://capabilities");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getUriTemplate(), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertMatchWithVariables() {
        Optional<ResourceUriMatch> actual = resourceUriPatternMatcher.match(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getUriVariables().get("database"), is("logic_db"));
        assertThat(actual.get().getUriVariables().get("schema"), is("public"));
        assertThat(actual.get().getUriVariables().get("table"), is("orders"));
        assertThat(actual.get().getUriVariables().get("column"), is("order_id"));
    }
    
    @Test
    void assertMatchWithInvalidPrefix() {
        Optional<ResourceUriMatch> actual = resourceUriPatternMatcher.match("shardingsphere://capabilities", "unsupported://capabilities");
        
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertMatchWithDifferentSegmentCount() {
        Optional<ResourceUriMatch> actual = resourceUriPatternMatcher.match(
                "shardingsphere://databases/{database}/schemas/{schema}",
                "shardingsphere://databases/logic_db/schemas/public/tables");
        
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertMatchWithDifferentLiteralSegment() {
        Optional<ResourceUriMatch> actual = resourceUriPatternMatcher.match(
                "shardingsphere://databases/{database}/schemas/{schema}/tables",
                "shardingsphere://databases/logic_db/schemas/public/views");
        
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateRouteSignature() {
        String actual = resourceUriPatternMatcher.createRouteSignature("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}");
        
        assertThat(actual, is("shardingsphere://databases/{}/schemas/{}/tables/{}/columns/{}"));
    }
    
    @Test
    void assertCreateRouteSignatureWithInvalidTemplate() {
        assertThrows(IllegalArgumentException.class, () -> resourceUriPatternMatcher.createRouteSignature("invalid-template"));
    }
}
