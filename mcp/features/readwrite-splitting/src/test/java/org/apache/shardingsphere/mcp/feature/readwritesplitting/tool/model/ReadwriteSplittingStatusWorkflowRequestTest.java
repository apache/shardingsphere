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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReadwriteSplittingStatusWorkflowRequestTest {
    
    @Test
    void assertMerge() {
        ReadwriteSplittingStatusWorkflowRequest previous = new ReadwriteSplittingStatusWorkflowRequest();
        previous.setRuleName("readwrite_ds");
        ReadwriteSplittingStatusWorkflowRequest current = new ReadwriteSplittingStatusWorkflowRequest();
        current.setStorageUnit("read_ds_0");
        ReadwriteSplittingStatusWorkflowRequest actual = ReadwriteSplittingStatusWorkflowRequest.merge(previous, current);
        assertThat(actual.getRuleName(), is("readwrite_ds"));
        assertThat(actual.getStorageUnit(), is("read_ds_0"));
    }
}
