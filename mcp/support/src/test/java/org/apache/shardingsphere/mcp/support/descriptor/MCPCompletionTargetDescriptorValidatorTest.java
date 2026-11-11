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

import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptDescriptor;
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

class MCPCompletionTargetDescriptorValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCompletionTargets")
    void assertValidateWithInvalidCompletionTarget(final String name, final Collection<MCPCompletionTargetDescriptor> descriptors, final Collection<MCPResourceDescriptor> resources,
                                                   final String expectedMessage) {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.",
                List.of(new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false)), Map.of());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPCompletionTargetDescriptorValidator.validate(descriptors, List.of(prompt), resources));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Collection<Arguments> invalidCompletionTargets() {
        MCPCompletionTargetDescriptor completion = new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50, Map.of());
        MCPResourceDescriptor fixedResource = new MCPResourceDescriptor("shardingsphere://capabilities", "foo_fixed", "Capabilities", "Read capabilities.", "application/json",
                MCPResourceAnnotations.EMPTY, Map.of());
        MCPResourceDescriptor template = new MCPResourceDescriptor("shardingsphere://databases/{database}", "foo_template", "Database", "Read database.", "application/json",
                MCPResourceAnnotations.EMPTY, Map.of());
        return List.of(
                Arguments.of("unknown context argument",
                        List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50,
                                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("database", List.of("tenant"))))),
                        List.of(), "Completion target `prompt:test_prompt` context argument `tenant` for `database` is not declared by the target."),
                Arguments.of("duplicate arguments", List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database", "database"), 50, Map.of())), List.of(),
                        "Completion target `prompt:test_prompt` must not contain duplicate arguments."),
                Arguments.of("duplicate target", List.of(completion, completion), List.of(), "Duplicate MCP completion target `prompt:test_prompt`."),
                Arguments.of("unknown prompt", List.of(new MCPCompletionTargetDescriptor("prompt", "foo_unknown", List.of("database"), 50, Map.of())), List.of(),
                        "Completion target references unknown prompt `foo_unknown`."),
                Arguments.of("unknown resource", List.of(new MCPCompletionTargetDescriptor("resource", "shardingsphere://foo_unknown", List.of("database"), 50, Map.of())), List.of(),
                        "Completion target references unknown resource `shardingsphere://foo_unknown`."),
                Arguments.of("unsupported reference", List.of(new MCPCompletionTargetDescriptor("tool", "test_prompt", List.of("database"), 50, Map.of())), List.of(),
                        "Unsupported completion reference type `tool`."),
                Arguments.of("fixed resource target", List.of(new MCPCompletionTargetDescriptor("resource", "shardingsphere://capabilities", List.of(), 50, Map.of())), List.of(fixedResource),
                        "Completion target `resource:shardingsphere://capabilities` must reference a resource template."),
                Arguments.of("unknown resource argument", List.of(new MCPCompletionTargetDescriptor("resource", "shardingsphere://databases/{database}", List.of("schema"), 50, Map.of())),
                        List.of(template), "Completion target `resource:shardingsphere://databases/{database}` argument `schema` is not a URI template variable."),
                Arguments.of("context metadata is not object",
                        List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50, Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, "database"))),
                        List.of(), "Completion target `prompt:test_prompt` metadata `org.apache.shardingsphere/required-context-arguments` must be an object."),
                Arguments.of("unknown context metadata key",
                        List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50,
                                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("schema", List.of())))),
                        List.of(), "Completion target `prompt:test_prompt` required context argument `schema` is not declared by the target."),
                Arguments.of("context value is not list",
                        List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50,
                                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("database", "schema")))),
                        List.of(), "Completion target `prompt:test_prompt` required context for `database` must be a list."));
    }
}
