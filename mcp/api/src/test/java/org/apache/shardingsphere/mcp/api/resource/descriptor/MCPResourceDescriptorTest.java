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

package org.apache.shardingsphere.mcp.api.resource.descriptor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPResourceDescriptorTest {

    @Test
    void assertIsTemplated() {
        assertTrue(new MCPResourceDescriptor("shardingsphere://databases/{database}", "database", "Database", "Read one database.", "application/json").isTemplated());
        assertFalse(new MCPResourceDescriptor("shardingsphere://databases", "databases", "Databases", "List databases.", "application/json").isTemplated());
    }

    @Test
    void assertGetUriPattern() {
        MCPResourceDescriptor actual = new MCPResourceDescriptor("shardingsphere://databases/{database}", "database", "Database", "Read one database.", "application/json");
        assertThat(actual.getUriPattern(), is("shardingsphere://databases/{database}"));
        assertThat(actual.getUriTemplate(), is("shardingsphere://databases/{database}"));
    }

    @Test
    void assertLegacyMetaIsTyped() {
        MCPResourceDescriptor actual = new MCPResourceDescriptor("shardingsphere://features/encrypt/algorithms", "encrypt-algorithms", "Encrypt Algorithms",
                "List encrypt algorithms.", "application/json", List.of(), MCPResourceAnnotations.EMPTY,
                Map.of("resourceKind", "list", "feature", "encrypt", "relatedTools", List.of("plan_encrypt_rule"), "runtimeVisibility", "ShardingSphere-Proxy"));
        assertThat(actual.getResourceKind(), is("list"));
        assertThat(actual.getFeature(), is("encrypt"));
        assertThat(actual.getRelatedTools(), is(List.of("plan_encrypt_rule")));
        assertThat(actual.getMeta(), is(Map.of("runtimeVisibility", "ShardingSphere-Proxy")));
    }
}
