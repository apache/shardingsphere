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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPCompletionTargetDescriptorValidatorTest {
    
    @Test
    void assertValidate() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.",
                List.of(new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false)), Map.of());
        MCPCompletionTargetDescriptor completion = new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50,
                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("database", List.of("tenant"))));
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPCompletionTargetDescriptorValidator.validate(List.of(completion), List.of(prompt), List.of()));
        assertThat(actual.getMessage(), is("Completion target `prompt:test_prompt` context argument `tenant` for `database` is not declared by the target."));
    }
}
