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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MaskWorkflowIntentResolverTest {
    
    @Test
    void assertResolveRecordsSharedInference() {
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("customer_phone");
        request.setNaturalLanguageIntent("请删除现有脱敏规则");
        ClarifiedIntent actual = new MaskWorkflowIntentResolver().resolve(request);
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getFieldSemantics(), is("phone"));
        assertThat(actual.getInferredValues().get("operation_type"), is("drop"));
        assertThat(actual.getInferredValues().get("field_semantics"), is("phone"));
        assertThat(actual.getReasoningNotes(), is("Resolved from explicit arguments, heuristic inference for operation_type, field_semantics."));
    }
}
