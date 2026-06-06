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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReadwriteSplittingWorkflowRequestTest {
    
    @Test
    void assertRuleRequestParsesReadStorageUnits() {
        ReadwriteSplittingRuleWorkflowRequest actual = new ReadwriteSplittingRuleWorkflowRequest();
        actual.setReadStorageUnits("read_ds_0, read_ds_1");
        assertThat(actual.getReadStorageUnits(), is(List.of("read_ds_0", "read_ds_1")));
    }
    
    @Test
    void assertRuleRequestCopy() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest();
        ReadwriteSplittingRuleWorkflowRequest actual = request.copy();
        assertThat(actual.getRuleName(), is("readwrite_ds"));
        assertThat(actual.getLoadBalancerProperties(), is(Map.of("read_ds_0", "2")));
    }
    
    @Test
    void assertRuleRequestMerge() {
        ReadwriteSplittingRuleWorkflowRequest previous = createRuleRequest();
        ReadwriteSplittingRuleWorkflowRequest current = new ReadwriteSplittingRuleWorkflowRequest();
        current.setRuleName("new_rule");
        ReadwriteSplittingRuleWorkflowRequest actual = ReadwriteSplittingRuleWorkflowRequest.merge(previous, current);
        assertThat(actual.getRuleName(), is("new_rule"));
        assertThat(actual.getWriteStorageUnit(), is("write_ds"));
    }
    
    @Test
    void assertRuleRequestMergeFromBaseRequest() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setDatabase("logic_db");
        ReadwriteSplittingRuleWorkflowRequest actual = ReadwriteSplittingRuleWorkflowRequest.merge(previous, new ReadwriteSplittingRuleWorkflowRequest());
        assertThat(actual.getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertStatusRequestMerge() {
        ReadwriteSplittingStatusWorkflowRequest previous = new ReadwriteSplittingStatusWorkflowRequest();
        previous.setRuleName("readwrite_ds");
        ReadwriteSplittingStatusWorkflowRequest current = new ReadwriteSplittingStatusWorkflowRequest();
        current.setStorageUnit("read_ds_0");
        ReadwriteSplittingStatusWorkflowRequest actual = ReadwriteSplittingStatusWorkflowRequest.merge(previous, current);
        assertThat(actual.getRuleName(), is("readwrite_ds"));
        assertThat(actual.getStorageUnit(), is("read_ds_0"));
    }
    
    private ReadwriteSplittingRuleWorkflowRequest createRuleRequest() {
        ReadwriteSplittingRuleWorkflowRequest result = new ReadwriteSplittingRuleWorkflowRequest();
        result.setRuleName("readwrite_ds");
        result.setWriteStorageUnit("write_ds");
        result.setReadStorageUnits("read_ds_0");
        result.setTransactionalReadQueryStrategy("DYNAMIC");
        result.setLoadBalancerType("WEIGHT");
        result.putLoadBalancerProperties(Map.of("read_ds_0", "2"));
        return result;
    }
}
