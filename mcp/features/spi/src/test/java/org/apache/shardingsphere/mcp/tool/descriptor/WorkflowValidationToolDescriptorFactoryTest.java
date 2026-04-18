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

package org.apache.shardingsphere.mcp.tool.descriptor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowValidationToolDescriptorFactoryTest {
    
    @Test
    void assertCreateBuildsExpectedFields() {
        MCPToolDescriptor actual = WorkflowValidationToolDescriptorFactory.create("validate_encrypt_rule");
        assertThat(actual.getName(), is("validate_encrypt_rule"));
        assertThat(actual.getFields().stream().map(MCPToolFieldDefinition::getName).toList(), is(List.of("plan_id")));
        assertTrue(actual.getFields().get(0).isRequired());
    }
}
