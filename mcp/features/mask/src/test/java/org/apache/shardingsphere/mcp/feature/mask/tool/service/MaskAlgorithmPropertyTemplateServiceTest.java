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

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskAlgorithmPropertyTemplateServiceTest {
    
    private final MaskAlgorithmPropertyTemplateService service = new MaskAlgorithmPropertyTemplateService();
    
    @Test
    void assertFindRequirements() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("MASK_FROM_X_TO_Y");
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getPropertyKey(), is("from-x"));
        assertThat(actual.get(2).getDefaultValue(), is("*"));
    }
    
    @Test
    void assertFindRequirementsWithUnknownAlgorithm() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("UNKNOWN");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertMaskProperties() {
        List<AlgorithmPropertyRequirement> requirements = List.of(
                new AlgorithmPropertyRequirement("primary", "special-chars", true, false, "anchor", ""),
                new AlgorithmPropertyRequirement("primary", "secret-key", true, true, "secret", ""));
        Map<String, String> actual = service.maskProperties(requirements, Map.of("special-chars", "@", "secret-key", "abc"));
        assertThat(actual.get("special-chars"), is("@"));
        assertThat(actual.get("secret-key"), is("******"));
    }
}
