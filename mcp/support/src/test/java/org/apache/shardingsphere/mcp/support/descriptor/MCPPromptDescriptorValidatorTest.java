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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPPromptDescriptorValidatorTest {
    
    @Test
    void assertValidate() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.", List.of(), Map.of());
        MCPPromptTemplateBinding binding = new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPPromptDescriptorValidator.validate(List.of(prompt), List.of(binding)));
        assertThat(actual.getMessage(), is("Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md` contains unsupported model-facing placeholder `{database}`."));
    }
}
