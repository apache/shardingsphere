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

import org.apache.shardingsphere.mcp.api.resource.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPDescriptorCatalogPayloadBuilderTest {
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildShardingSphereCapabilityPayload() {
        MCPResourceDescriptor resourceTemplate = new MCPResourceDescriptor("shardingsphere://databases/{database}", "database", "Database", "Read a database.",
                "application/json", MCPResourceAnnotations.EMPTY, Map.of());
        MCPToolDescriptor tool = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", Map.of(), Map.of(), null, Map.of());
        MCPCompletionTargetDescriptor completionTarget = new MCPCompletionTargetDescriptor("resource_template", resourceTemplate.getUriTemplate(), List.of("database"), 50,
                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("database", List.of())));
        MCPResourceNavigationDescriptor navigation = new MCPResourceNavigationDescriptor(resourceTemplate.getUriTemplate(), tool.getName(), List.of("database"), List.of("database"),
                "Call the test tool after reading the database.");
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(resourceTemplate), List.of(tool), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(completionTarget), List.of(navigation), List.of()));
        Map<String, Object> actual = MCPDescriptorCatalogPayloadBuilder.build(catalog, List.of("SelectStatement"));
        assertThat(actual.keySet(), is(Set.of("response_mode", "supportedStatementClasses", "completionTargets", "resourceNavigation")));
        assertThat(actual.get("response_mode"), is("catalog"));
        assertThat(actual.get("supportedStatementClasses"), is(List.of("SelectStatement")));
        Map<String, Object> actualCompletionTarget = ((List<Map<String, Object>>) actual.get("completionTargets")).getFirst();
        assertThat(actualCompletionTarget.get("reference"), is(resourceTemplate.getUriTemplate()));
        assertThat(actualCompletionTarget.get("meta"), is(completionTarget.getMeta()));
        Map<String, Object> actualNavigation = ((List<Map<String, Object>>) actual.get("resourceNavigation")).getFirst();
        assertThat(actualNavigation.get("from_type"), is("resource_template"));
        assertThat(actualNavigation.get("to_type"), is("tool"));
        assertFalse(actual.containsKey("resources"));
        assertFalse(actual.containsKey("tools"));
        assertFalse(actual.containsKey("protocolAvailability"));
    }
}
