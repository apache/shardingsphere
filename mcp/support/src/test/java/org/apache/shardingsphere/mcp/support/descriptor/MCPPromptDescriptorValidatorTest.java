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

import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPPromptDescriptorValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPrompts")
    void assertValidateWithInvalidPrompt(final String name, final Collection<MCPPromptDescriptor> descriptors, final Collection<MCPPromptTemplateBinding> bindings,
                                         final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPPromptDescriptorValidator.validate(descriptors, bindings));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Collection<Arguments> invalidPrompts() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.", List.of(), Map.of());
        MCPPromptTemplateBinding binding = new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md");
        return List.of(
                Arguments.of("unsupported placeholder", List.of(prompt),
                        List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md")),
                        "Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md` contains unsupported model-facing placeholder `{database}`."),
                Arguments.of("missing binding", List.of(prompt), List.of(), "Prompt `test_prompt` must declare an internal template binding."),
                Arguments.of("duplicate prompt", List.of(prompt, prompt), List.of(binding), "Duplicate MCP prompt descriptor `test_prompt`."),
                Arguments.of("undeclared placeholder", List.of(prompt), List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-markdown-resource.md")),
                        "Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-markdown-resource.md` has undeclared placeholder `database`."),
                Arguments.of("client form metadata is not a list", List.of(new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.", List.of(),
                        Map.of("org.apache.shardingsphere/client-form-only-arguments", "database"))), List.of(binding),
                        "Prompt `test_prompt` metadata `org.apache.shardingsphere/client-form-only-arguments` must be a list."));
    }
}
