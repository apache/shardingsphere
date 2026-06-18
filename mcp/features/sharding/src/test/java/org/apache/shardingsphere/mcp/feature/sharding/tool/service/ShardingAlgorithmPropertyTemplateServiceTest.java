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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingAlgorithmPropertyTemplateServiceTest {
    
    private final ShardingAlgorithmPropertyTemplateService service = new ShardingAlgorithmPropertyTemplateService();
    
    @Test
    void assertFindAlgorithmRequirementsForInline() {
        List<AlgorithmPropertyRequirement> actual = service.findAlgorithmRequirements("INLINE");
        assertThat(actual.getFirst().getAlgorithmRole(), is("primary"));
        assertThat(actual.getFirst().getPropertyKey(), is("algorithm-expression"));
        assertTrue(actual.getFirst().isRequired());
    }
    
    @Test
    void assertFindAlgorithmRequirementsForMod() {
        assertThat(service.findAlgorithmRequirements("MOD").getFirst().getPropertyKey(), is("sharding-count"));
    }
    
    @Test
    void assertFindAlgorithmRequirementsForHashMod() {
        List<AlgorithmPropertyRequirement> actual = service.findAlgorithmRequirements("HASH_MOD");
        assertThat(actual.getFirst().getPropertyKey(), is("sharding-count"));
        assertTrue(actual.getFirst().isRequired());
    }
    
    @Test
    void assertFindAlgorithmRequirementsForInlineVariants() {
        assertThat(service.findAlgorithmRequirements("COMPLEX_INLINE").getFirst().getPropertyKey(), is("algorithm-expression"));
        assertThat(service.findAlgorithmRequirements("HINT_INLINE").getFirst().getPropertyKey(), is("algorithm-expression"));
    }
    
    @Test
    void assertFindKeyGeneratorRequirementsForSnowflake() {
        List<AlgorithmPropertyRequirement> actual = service.findKeyGeneratorRequirements("SNOWFLAKE");
        assertThat(actual.getFirst().getAlgorithmRole(), is("key_generator"));
        assertThat(actual.getFirst().getPropertyKey(), is("worker-id"));
    }
    
    @Test
    void assertFindKeyGeneratorRequirementsForUuid() {
        assertTrue(service.findKeyGeneratorRequirements("UUID").isEmpty());
    }
}
