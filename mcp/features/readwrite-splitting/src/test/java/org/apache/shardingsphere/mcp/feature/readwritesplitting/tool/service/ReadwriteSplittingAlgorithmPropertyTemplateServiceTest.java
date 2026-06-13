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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingAlgorithmPropertyTemplateServiceTest {
    
    private final ReadwriteSplittingAlgorithmPropertyTemplateService service = new ReadwriteSplittingAlgorithmPropertyTemplateService();
    
    @Test
    void assertFindRequirementsForWeight() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("WEIGHT", List.of("read_ds_0", "read_ds_1"));
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getAlgorithmRole(), is("primary"));
        assertThat(actual.getFirst().getPropertyKey(), is("read_ds_0"));
        assertTrue(actual.getFirst().isRequired());
        assertThat(actual.get(1).getPropertyKey(), is("read_ds_1"));
    }
    
    @Test
    void assertFindRequirementsForRandom() {
        assertTrue(service.findRequirements("RANDOM", List.of("read_ds_0")).isEmpty());
    }
    
    @Test
    void assertFindRequirementsForRoundRobin() {
        assertTrue(service.findRequirements("ROUND_ROBIN", List.of("read_ds_0")).isEmpty());
    }
}
