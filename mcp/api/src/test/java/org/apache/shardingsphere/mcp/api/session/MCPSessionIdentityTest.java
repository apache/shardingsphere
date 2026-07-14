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

package org.apache.shardingsphere.mcp.api.session;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class MCPSessionIdentityTest {
    
    @Test
    void assertEquals() {
        MCPSessionIdentity actual = new MCPSessionIdentity("subject", "gateway", Map.of("region", "ap-south"));
        MCPSessionIdentity expected = new MCPSessionIdentity("subject", "gateway", Map.of("region", "ap-south"));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertHashCode() {
        MCPSessionIdentity actual = new MCPSessionIdentity("subject", "gateway", Map.of("region", "ap-south"));
        MCPSessionIdentity expected = new MCPSessionIdentity("subject", "gateway", Map.of("region", "ap-south"));
        assertThat(actual.hashCode(), is(expected.hashCode()));
    }
    
    @Test
    void assertNotEquals() {
        MCPSessionIdentity actual = new MCPSessionIdentity("subject", "gateway", Map.of("region", "ap-south"));
        MCPSessionIdentity expected = new MCPSessionIdentity("other", "gateway", Map.of("region", "ap-south"));
        assertThat(actual, is(not(expected)));
    }
    
    @Test
    void assertAttributesAreSnapshot() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("region", "ap-south");
        MCPSessionIdentity actual = new MCPSessionIdentity("subject", "gateway", attributes);
        attributes.put("region", "eu-west");
        assertThat(actual.getAttributes(), is(Map.of("region", "ap-south")));
    }
}
