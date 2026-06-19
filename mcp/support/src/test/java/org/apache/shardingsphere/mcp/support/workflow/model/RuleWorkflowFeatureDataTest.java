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

package org.apache.shardingsphere.mcp.support.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleWorkflowFeatureDataTest {
    
    @Test
    void assertCopyPreservesRuleStates() {
        RuleWorkflowFeatureData actual = new RuleWorkflowFeatureData(List.of(createRule("old")), List.of(createRule("new"))).copy();
        assertThat(actual.getBeforeRules().get(0).get("algorithm_props"), is(Map.of("aes-key-value", "old")));
        assertThat(actual.getExpectedRules().get(0).get("algorithm_props"), is(Map.of("aes-key-value", "new")));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertCopyIsIndependent() {
        Map<String, Object> expectedRule = createRule("new");
        RuleWorkflowFeatureData original = new RuleWorkflowFeatureData(List.of(), List.of(expectedRule));
        RuleWorkflowFeatureData actual = original.copy();
        ((Map<String, Object>) original.getExpectedRules().get(0).get("algorithm_props")).put("aes-key-value", "changed");
        assertThat(((Map<?, ?>) actual.getExpectedRules().get(0).get("algorithm_props")).get("aes-key-value"), is("new"));
    }
    
    @Test
    void assertAlgorithmPropertiesAreEmpty() {
        assertTrue(new RuleWorkflowFeatureData().getAlgorithmProperties("primary").isEmpty());
    }
    
    private Map<String, Object> createRule(final String keyValue) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("column", "phone");
        result.put("algorithm_props", new LinkedHashMap<>(Map.of("aes-key-value", keyValue)));
        return result;
    }
}
