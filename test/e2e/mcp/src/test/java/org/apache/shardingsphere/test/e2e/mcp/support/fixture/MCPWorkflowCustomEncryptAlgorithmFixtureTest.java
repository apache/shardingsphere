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

package org.apache.shardingsphere.test.e2e.mcp.support.fixture;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPWorkflowCustomEncryptAlgorithmFixtureTest {
    
    @Test
    void assertEncrypt() {
        assertThat(new MCPWorkflowCustomEncryptAlgorithmFixture().encrypt("plain", null), is("mcp_custom:plain"));
    }
    
    @Test
    void assertDecrypt() {
        assertThat(new MCPWorkflowCustomEncryptAlgorithmFixture().decrypt("mcp_custom:plain", null), is("plain"));
    }
    
    @Test
    void assertDecryptWithRawValue() {
        assertThat(new MCPWorkflowCustomEncryptAlgorithmFixture().decrypt("plain", null), is("plain"));
    }
    
    @Test
    void assertGetMetaData() {
        EncryptAlgorithmMetaData actual = new MCPWorkflowCustomEncryptAlgorithmFixture().getMetaData();
        assertTrue(actual.isSupportDecrypt());
        assertFalse(actual.isSupportEquivalentFilter());
        assertFalse(actual.isSupportLike());
    }
    
    @Test
    void assertToConfiguration() {
        AlgorithmConfiguration actual = new MCPWorkflowCustomEncryptAlgorithmFixture().toConfiguration();
        assertThat(actual.getType(), is("MCP_CUSTOM_REVERSIBLE"));
        assertTrue(actual.getProps().isEmpty());
    }
    
    @Test
    void assertGetType() {
        assertThat(new MCPWorkflowCustomEncryptAlgorithmFixture().getType(), is("MCP_CUSTOM_REVERSIBLE"));
    }
}
