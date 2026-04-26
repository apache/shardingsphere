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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptAlgorithmPropertyTemplateServiceTest {
    
    private final EncryptAlgorithmPropertyTemplateService service = new EncryptAlgorithmPropertyTemplateService();
    
    @Test
    void assertFindRequirements() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("AES", "MD5", "AES");
        assertThat(actual.size(), is(5));
        assertThat(actual.get(0).getAlgorithmRole(), is("primary"));
        assertThat(actual.get(2).getAlgorithmRole(), is("assisted_query"));
        assertThat(actual.get(3).getAlgorithmRole(), is("like_query"));
        assertThat(actual.get(0).getPropertyKey(), is("aes-key-value"));
    }
    
    @Test
    void assertFindRequirementsWithUnknownAlgorithms() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("", "UNKNOWN", "");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertMaskProperties() {
        List<AlgorithmPropertyRequirement> requirements = List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""),
                new AlgorithmPropertyRequirement("primary", "digest-algorithm-name", false, false, "digest", "SHA-1"));
        Map<String, String> actual = service.maskProperties(requirements, Map.of("aes-key-value", "secret", "digest-algorithm-name", "SHA-256"));
        assertThat(actual.get("aes-key-value"), is("******"));
        assertThat(actual.get("digest-algorithm-name"), is("SHA-256"));
    }
}
