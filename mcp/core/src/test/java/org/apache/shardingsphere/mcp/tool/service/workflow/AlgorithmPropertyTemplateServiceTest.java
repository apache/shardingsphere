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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AlgorithmPropertyTemplateServiceTest {
    
    @Test
    void assertFindRequirementsBuildsRoleSpecificTemplates() {
        AlgorithmPropertyTemplateService service = new AlgorithmPropertyTemplateService();
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("encrypt", "AES", "MD5", "");
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getAlgorithmRole(), is("primary"));
        assertThat(actual.get(0).getPropertyKey(), is("aes-key-value"));
        assertThat(actual.get(1).getDefaultValue(), is("SHA-1"));
        assertThat(actual.get(2).getAlgorithmRole(), is("assisted_query"));
    }
    
    @Test
    void assertMaskPropertiesAndFindMissingRequiredPropertiesRespectSecrets() {
        AlgorithmPropertyTemplateService service = new AlgorithmPropertyTemplateService();
        List<AlgorithmPropertyRequirement> requirements = List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""),
                new AlgorithmPropertyRequirement("primary", "digest-algorithm-name", false, false, "digest", ""));
        Map<String, String> actualProperties = new LinkedHashMap<>(2, 1F);
        actualProperties.put("aes-key-value", "123456");
        actualProperties.put("digest-algorithm-name", "SHA-256");
        Map<String, String> actualMasked = service.maskProperties(requirements, actualProperties);
        List<String> actualMissing = service.findMissingRequiredProperties(requirements, Map.of("digest-algorithm-name", "SHA-256"));
        assertThat(actualMasked.get("aes-key-value"), is("******"));
        assertThat(actualMasked.get("digest-algorithm-name"), is("SHA-256"));
        assertThat(actualMissing, is(List.of("aes-key-value")));
    }
}
