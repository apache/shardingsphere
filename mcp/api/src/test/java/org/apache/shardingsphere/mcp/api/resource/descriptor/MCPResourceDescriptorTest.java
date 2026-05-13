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

import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPResourceDescriptorTest {
    
    @Test
    void assertGetUriForFixedResource() {
        MCPFixedResourceDescriptor actual = new MCPFixedResourceDescriptor("shardingsphere://databases", "databases", "Databases", "List databases.", Collections.emptyList(),
                "application/json", MCPAnnotations.EMPTY, null, Collections.emptyMap());
        assertThat(actual.getUri(), is("shardingsphere://databases"));
    }
    
    @Test
    void assertGetUriTemplateForResourceTemplate() {
        MCPResourceTemplateDescriptor actual = new MCPResourceTemplateDescriptor("shardingsphere://databases/{database}", "database", "Database", "Read one database.", Collections.emptyList(),
                "application/json", MCPAnnotations.EMPTY, Collections.emptyMap());
        assertThat(actual.getUriTemplate(), is("shardingsphere://databases/{database}"));
    }
    
    @Test
    void assertMetaIsKeptAsMetadataOnly() {
        MCPResourceDescriptor actual = new MCPFixedResourceDescriptor("shardingsphere://features/encrypt/algorithms", "encrypt-algorithms", "Encrypt Algorithms",
                "List encrypt algorithms.", Collections.emptyList(), "application/json", MCPAnnotations.EMPTY, null, Map.of("org.apache.shardingsphere/runtime-visibility", "ShardingSphere-Proxy"));
        assertThat(actual.getMeta(), is(Map.of("org.apache.shardingsphere/runtime-visibility", "ShardingSphere-Proxy")));
    }
}
