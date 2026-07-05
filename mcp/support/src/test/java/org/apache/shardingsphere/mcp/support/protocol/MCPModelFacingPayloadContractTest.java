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

package org.apache.shardingsphere.mcp.support.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPModelFacingPayloadContractTest {
    
    @Test
    void assertIsRemovedFieldNameWithRemovedAlias() {
        assertTrue(MCPModelFacingPayloadContract.isRemovedFieldName("target_tool"));
    }
    
    @Test
    void assertIsRemovedFieldNameWithCanonicalField() {
        assertFalse(MCPModelFacingPayloadContract.isRemovedFieldName(MCPPayloadFieldNames.NEXT_ACTIONS));
    }
    
    @Test
    void assertGetNextActionRequiredFields() {
        assertThat(MCPModelFacingPayloadContract.getNextActionRequiredFields("tool_call"), is(Set.of("order", "type", "title", "tool_name", "arguments")));
    }
    
    @Test
    void assertGetNextActionAllowedFields() {
        assertThat(MCPModelFacingPayloadContract.getNextActionAllowedFields("completion"),
                is(Set.of("order", "type", "title", "ref", "argument", "context", "missing_context_arguments", "resume_ref", "resume_arguments", "reason", "depends_on")));
    }
    
    @Test
    void assertGetNextActionSchemaAllowedFields() {
        Collection<String> actual = MCPModelFacingPayloadContract.getNextActionSchemaAllowedFields();
        assertTrue(actual.contains("resource_uri"));
        assertFalse(actual.contains("target_tool"));
    }
    
    @Test
    void assertGetModelCriticalFieldNames() {
        Collection<String> actual = MCPModelFacingPayloadContract.getModelCriticalFieldNames();
        assertTrue(actual.contains(MCPPayloadFieldNames.NEXT_ACTIONS));
        assertTrue(actual.contains("manual_follow_up"));
    }
}
