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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPResourceNavigationPayloadBuilderTest {
    
    @Test
    void assertCreate() {
        MCPResourceDescriptor descriptor = createDescriptor("shardingsphere://databases/{database}");
        Map<String, Object> actual = MCPResourceNavigationPayloadBuilder.create(descriptor, new MCPUriVariables(Map.of("database", "foo_db")));
        assertThat(((Map<?, ?>) actual.get(MCPPayloadFieldNames.SELF_RESOURCE)).get("uri"), is("shardingsphere://databases/foo_db"));
        assertThat(actual.size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resourceKinds")
    void assertCreateWithParent(final String name, final String parentUriTemplate, final String expectedResourceKind) {
        MCPResourceDescriptor descriptor = createDescriptor("shardingsphere://databases/{database}");
        Map<String, Object> actual = MCPResourceNavigationPayloadBuilder.create(
                descriptor, new MCPUriVariables(Map.of("database", "foo_db")), parentUriTemplate);
        Map<?, ?> actualParent = (Map<?, ?>) actual.get(MCPPayloadFieldNames.PARENT_RESOURCE);
        assertThat(actualParent.get("resource_kind"), is(expectedResourceKind));
        assertThat(actualParent.get("source_field"), is(MCPPayloadFieldNames.PARENT_RESOURCE));
    }
    
    @Test
    void assertCreateWithIncompleteUriVariables() {
        MCPResourceDescriptor descriptor = createDescriptor("shardingsphere://databases/{database}");
        Map<String, Object> actual = MCPResourceNavigationPayloadBuilder.create(
                descriptor, new MCPUriVariables(Map.of()), "shardingsphere://databases/{database}/rules");
        assertThat(actual, is(Map.of()));
    }
    
    private static Stream<Arguments> resourceKinds() {
        return Stream.of(
                Arguments.of("rule", "shardingsphere://databases/{database}/rules", "rule"),
                Arguments.of("algorithm", "shardingsphere://databases/{database}/algorithms", "algorithm"),
                Arguments.of("column", "shardingsphere://databases/{database}/columns", "column"),
                Arguments.of("index", "shardingsphere://databases/{database}/indexes", "index"),
                Arguments.of("resource", "shardingsphere://databases/{database}", "resource"));
    }
    
    private MCPResourceDescriptor createDescriptor(final String uriTemplate) {
        MCPResourceDescriptor result = mock(MCPResourceDescriptor.class);
        when(result.getUriTemplate()).thenReturn(uriTemplate);
        return result;
    }
}
