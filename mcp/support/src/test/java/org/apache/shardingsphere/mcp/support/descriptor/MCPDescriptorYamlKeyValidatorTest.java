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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPDescriptorYamlKeyValidatorTest {
    
    @Test
    void assertValidate() {
        String yamlContent = """
                resourceTemplates:
                  - uriTemplate: shardingsphere://workflows/{plan_id}
                    name: workflow-plan
                    title: Workflow Plan
                    description: Workflow plan.
                    icons:
                      - src: https://example.invalid/workflow.png
                        mimeType: image/png
                        sizes:
                          - 64x64
                        theme: dark
                    mimeType: application/json
                    annotations:
                      audience:
                        - assistant
                      priority: 0.5
                      lastModified: "2026-05-13T00:00:00Z"
                    extension:
                      uriVariables:
                        - name: plan_id
                          title: Plan ID
                          description: Plan ID.
                          required: true
                          scope: workflow-plan
                      resourceKind: detail
                tools:
                  - name: database_gateway_search_metadata
                    title: Search Metadata
                    description: Search metadata.
                    icons:
                      - src: data:image/png;base64,AA==
                        sizes:
                          - any
                    inputSchema:
                      type: object
                      properties:
                        query:
                          type: string
                          description: Query.
                      required:
                        - query
                      additionalProperties: false
                    annotations:
                      title: Search Metadata
                      readOnlyHint: true
                      destructiveHint: false
                      idempotentHint: true
                      openWorldHint: true
                    outputSchema:
                      type: object
                    runtime:
                      workflowRole: plan
                prompts:
                  - name: inspect_metadata
                    title: Inspect Metadata
                    description: Inspect metadata.
                    icons:
                      - src: https://example.invalid/prompt.png
                    binding:
                      templateResource: META-INF/shardingsphere-mcp/prompts/inspect-metadata.md
                    arguments:
                      - name: database
                        title: Database
                        description: Database.
                        required: false
                completionTargets:
                  - referenceType: prompt
                    reference: inspect_metadata
                    arguments:
                      - database
                    maxValues: 50
                resourceNavigation:
                  - from: database_gateway_apply_workflow
                    to: database_gateway_validate_workflow
                    requiredArguments:
                      - plan_id
                    carriedArguments:
                      - plan_id
                    description: Validate workflow.
                """;
        assertDoesNotThrow(() -> MCPDescriptorYamlKeyValidator.validate("test.yaml", toBytes(yamlContent)));
    }
    
    @Test
    void assertValidateUnknownRootKey() {
        assertUnknownKey("unknown: true", "$.unknown");
    }
    
    @Test
    void assertValidateUnknownToolKey() {
        assertUnknownKey("""
                tools:
                  - name: database_gateway_search_metadata
                    unknown: true
                """, "$.tools[0].unknown");
    }
    
    @Test
    void assertValidateUnknownToolAnnotationKey() {
        assertUnknownKey("""
                tools:
                  - name: database_gateway_search_metadata
                    annotations:
                      returnDirect: false
                """, "$.tools[0].annotations.returnDirect");
    }
    
    @Test
    void assertValidateUnknownIconKey() {
        assertUnknownKey("""
                resources:
                  - uri: shardingsphere://capabilities
                    icons:
                      - src: https://example.invalid/icon.png
                        unknown: true
                """, "$.resources[0].icons[0].unknown");
    }
    
    @Test
    void assertValidateUnknownToolExecutionKey() {
        assertUnknownKey("""
                tools:
                  - name: database_gateway_search_metadata
                    execution:
                      unknown: true
                """, "$.tools[0].execution");
    }
    
    @Test
    void assertValidateUnknownResourceExtensionKey() {
        assertUnknownKey("""
                resourceTemplates:
                  - uriTemplate: shardingsphere://workflows/{plan_id}
                    extension:
                      unknown: true
                """, "$.resourceTemplates[0].extension.unknown");
    }
    
    private void assertUnknownKey(final String yamlContent, final String expectedKeyPath) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPDescriptorYamlKeyValidator.validate("test.yaml", toBytes(yamlContent)));
        assertThat(actual.getMessage(), is(String.format("MCP descriptor resource `test.yaml` contains unknown key `%s`.", expectedKeyPath)));
    }
    
    private byte[] toBytes(final String yamlContent) {
        return yamlContent.getBytes(StandardCharsets.UTF_8);
    }
}
