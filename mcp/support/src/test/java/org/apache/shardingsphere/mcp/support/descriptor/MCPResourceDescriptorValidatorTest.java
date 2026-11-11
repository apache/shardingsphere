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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPResourceDescriptorValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidResources")
    void assertValidateWithInvalidResource(final String name, final MCPDescriptorCatalog catalog, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPResourceDescriptorValidator.validate(catalog));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Collection<Arguments> invalidResources() {
        MCPResourceDescriptor fixedResource = createResource("shardingsphere://capabilities");
        MCPResourceDescriptor template = createResource("shardingsphere://databases/{database}");
        MCPUriVariableDescriptor variable = new MCPUriVariableDescriptor("database", "Database", "Logical database.", true, "database");
        return List.of(
                Arguments.of("template without variables", createCatalog(List.of(fixedResource), List.of(fixedResource), List.of()),
                        "Resource template `shardingsphere://capabilities` must contain template variables."),
                Arguments.of("fixed resource with variables", createCatalog(List.of(template), List.of(), List.of()),
                        "Fixed resource `shardingsphere://databases/{database}` must not contain template variables."),
                Arguments.of("blank URI", createCatalog(List.of(createResource(" ")), List.of(), List.of()), "Resource URI is required."),
                Arguments.of("duplicate URI", createCatalog(List.of(fixedResource, fixedResource), List.of(), List.of()), "Duplicate MCP resource descriptor `shardingsphere://capabilities`."),
                Arguments.of("missing variable metadata", createCatalog(List.of(), List.of(template), List.of()),
                        "Resource descriptor `shardingsphere://databases/{database}` must describe URI template variable `database`."),
                Arguments.of("optional variable", createCatalog(List.of(), List.of(template), List.of(new MCPUriVariableDescriptor("database", "Database", "Logical database.", false, "database"))),
                        "Resource parameter `shardingsphere://databases/{database}.database` must be required because URI template variables are required."),
                Arguments.of("non-template variable", createCatalog(List.of(), List.of(template), List.of(new MCPUriVariableDescriptor("schema", "Schema", "Logical schema.", true, "schema"))),
                        "Resource descriptor `shardingsphere://databases/{database}` declares non-template parameter `schema`."),
                Arguments.of("duplicate variable metadata", createCatalog(List.of(), List.of(template), List.of(variable, variable)),
                        "Duplicate MCP resource parameter `shardingsphere://databases/{database}.database`."));
    }
    
    private static MCPResourceDescriptor createResource(final String uri) {
        return new MCPResourceDescriptor(uri, "foo_resource", "Resource", "Read resource.", "application/json", MCPResourceAnnotations.EMPTY, Map.of());
    }
    
    private static MCPDescriptorCatalog createCatalog(final Collection<MCPResourceDescriptor> resources, final Collection<MCPResourceDescriptor> templates,
                                                      final Collection<MCPUriVariableDescriptor> variables) {
        Collection<ShardingSphereMCPResourceMetadata> metadata = templates.stream().map(each -> new ShardingSphereMCPResourceMetadata(each.getUriTemplate(), variables,
                "detail", "database", "", List.of(), List.of(), List.of())).toList();
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(resources, templates, List.of(), List.of()),
                new MCPShardingSphereDescriptorCatalog(metadata, List.of(), List.of(), List.of(), List.of()));
    }
}
