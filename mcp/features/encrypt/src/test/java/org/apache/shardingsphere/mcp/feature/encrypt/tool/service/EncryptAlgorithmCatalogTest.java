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

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptAlgorithmCatalogTest {
    
    @Test
    void assertFindCapability() {
        Map<String, Boolean> actual = EncryptAlgorithmCatalog.findCapability("aes");
        assertTrue(actual.get("supports_decrypt"));
        assertTrue(actual.get("supports_equivalent_filter"));
        assertFalse(actual.get("supports_like"));
    }
    
    @Test
    void assertFindCapabilityWithUnknownAlgorithm() {
        Map<String, Boolean> actual = EncryptAlgorithmCatalog.findCapability("MCP_CUSTOM");
        assertNull(actual.get("supports_decrypt"));
        assertNull(actual.get("supports_equivalent_filter"));
        assertNull(actual.get("supports_like"));
    }
    
    @Test
    void assertIsCapabilityConfirmedWithKnownAlgorithm() {
        boolean actual = EncryptAlgorithmCatalog.isCapabilityConfirmed("MD5");
        assertTrue(actual);
    }
    
    @Test
    void assertIsCapabilityConfirmedWithUnknownAlgorithm() {
        boolean actual = EncryptAlgorithmCatalog.isCapabilityConfirmed("MCP_CUSTOM");
        assertFalse(actual);
    }
    
    @Test
    void assertFindRequirements() {
        List<AlgorithmPropertyRequirement> actual = EncryptAlgorithmCatalog.findRequirements("assisted_query", "md5");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getAlgorithmRole(), is("assisted_query"));
        assertThat(actual.getFirst().getPropertyKey(), is("salt"));
        assertTrue(actual.getFirst().isSecret());
    }
    
    @Test
    void assertFindRequirementsWithUnknownAlgorithm() {
        List<AlgorithmPropertyRequirement> actual = EncryptAlgorithmCatalog.findRequirements("primary", "MCP_CUSTOM");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertGetSupportedAlgorithmTypes() {
        assertThat(EncryptAlgorithmCatalog.getSupportedAlgorithmTypes(), is(List.of("AES", "MD5")));
    }
}
