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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReadwriteSplittingRuleWorkflowRequestTest {
    
    @Test
    void assertSetReadStorageUnits() {
        ReadwriteSplittingRuleWorkflowRequest actual = new ReadwriteSplittingRuleWorkflowRequest();
        actual.setReadStorageUnits("read_ds_0, read_ds_1");
        assertThat(actual.getReadStorageUnits(), is(List.of("read_ds_0", "read_ds_1")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("setReadStorageUnitsArguments")
    void assertSetReadStorageUnitsFromCollection(final String scenario, final Collection<String> values, final Collection<String> expectedValues) {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        Collection<String> actualValues = request.getReadStorageUnits();
        actualValues.add("foo_previous");
        request.setReadStorageUnits(values);
        assertThat(request.getReadStorageUnits(), sameInstance(actualValues));
        assertThat(actualValues, is(expectedValues));
    }
    
    private static Stream<Arguments> setReadStorageUnitsArguments() {
        return Stream.of(
                Arguments.of("null collection", null, List.of("foo_previous")),
                Arguments.of("empty collection", List.of(), List.of("foo_previous")),
                Arguments.of("normalized values and duplicates", Arrays.asList(null, "", " ", " foo_read ", "bar_value", "foo_read"), List.of("foo_read", "bar_value", "foo_read")));
    }
    
    @Test
    void assertSetReadStorageUnitsWithNullString() {
        ReadwriteSplittingRuleWorkflowRequest actual = new ReadwriteSplittingRuleWorkflowRequest();
        actual.setReadStorageUnits(List.of("foo_read"));
        actual.setReadStorageUnits((String) null);
        assertThat(actual.getReadStorageUnits(), is(List.of("foo_read")));
    }
    
    @Test
    void assertCopy() {
        ReadwriteSplittingRuleWorkflowRequest actual = createRequest().copy();
        assertThat(actual.getRuleName(), is("readwrite_ds"));
        assertThat(actual.getLoadBalancerProperties(), is(Map.of("read_ds_0", "2")));
    }
    
    @Test
    void assertMerge() {
        ReadwriteSplittingRuleWorkflowRequest current = new ReadwriteSplittingRuleWorkflowRequest();
        current.setRuleName("new_rule");
        ReadwriteSplittingRuleWorkflowRequest actual = ReadwriteSplittingRuleWorkflowRequest.merge(createRequest(), current);
        assertThat(actual.getRuleName(), is("new_rule"));
        assertThat(actual.getWriteStorageUnit(), is("write_ds"));
    }
    
    @Test
    void assertMergeFromBaseRequest() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setDatabase("logic_db");
        ReadwriteSplittingRuleWorkflowRequest actual = ReadwriteSplittingRuleWorkflowRequest.merge(previous, new ReadwriteSplittingRuleWorkflowRequest());
        assertThat(actual.getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertMergeWithNullRequests() {
        assertNull(ReadwriteSplittingRuleWorkflowRequest.merge(null, null));
    }
    
    @Test
    void assertSetReadStorageUnitsWithMissingListValues() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setReadStorageUnits(List.of(" ", " foo_read "));
        assertThat(request.getReadStorageUnits(), is(List.of("foo_read")));
    }
    
    private ReadwriteSplittingRuleWorkflowRequest createRequest() {
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
