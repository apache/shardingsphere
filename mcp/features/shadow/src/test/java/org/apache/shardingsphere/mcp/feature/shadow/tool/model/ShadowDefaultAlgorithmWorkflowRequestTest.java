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

package org.apache.shardingsphere.mcp.feature.shadow.tool.model;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowDefaultAlgorithmWorkflowRequestTest {
    
    @Test
    void assertMerge() {
        ShadowDefaultAlgorithmWorkflowRequest previous = new ShadowDefaultAlgorithmWorkflowRequest();
        previous.setAlgorithmType("SQL_HINT");
        ShadowDefaultAlgorithmWorkflowRequest current = new ShadowDefaultAlgorithmWorkflowRequest();
        current.putAlgorithmProperties(Map.of("k", "v"));
        ShadowDefaultAlgorithmWorkflowRequest actual = ShadowDefaultAlgorithmWorkflowRequest.merge(previous, current);
        assertThat(actual.getAlgorithmType(), is("SQL_HINT"));
        assertThat(actual.getAlgorithmProperties().get("k"), is("v"));
    }
    
    @Test
    void assertMergeFromBaseRequest() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setDatabase("foo_db");
        ShadowDefaultAlgorithmWorkflowRequest current = new ShadowDefaultAlgorithmWorkflowRequest();
        current.setAlgorithmType("SQL_HINT");
        ShadowDefaultAlgorithmWorkflowRequest actual = ShadowDefaultAlgorithmWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getAlgorithmType(), is("SQL_HINT"));
    }
    
    @Test
    void assertGetAlgorithmProperties() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.putAlgorithmProperties(Map.of("foo_key", "foo_value"));
        assertThat(request.getAlgorithmProperties("primary"), is(Map.of("foo_key", "foo_value")));
    }
}
