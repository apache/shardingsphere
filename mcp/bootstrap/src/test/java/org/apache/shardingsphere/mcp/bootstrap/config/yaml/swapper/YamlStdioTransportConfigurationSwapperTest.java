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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlStdioTransportConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlStdioTransportConfigurationSwapperTest {
    
    private final YamlStdioTransportConfigurationSwapper swapper = new YamlStdioTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        YamlStdioTransportConfiguration yamlConfig = new YamlStdioTransportConfiguration();
        yamlConfig.setEnabled(true);
        StdioTransportConfiguration actual = swapper.swapToObject(yamlConfig);
        assertTrue(actual.isEnabled());
    }
    
    @Test
    void assertSwapToObjectWithOmittedEnabled() {
        assertFalse(swapper.swapToObject(new YamlStdioTransportConfiguration()).isEnabled());
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        assertThat(assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null)).getMessage(), is("Property `transport.stdio` is required."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        assertFalse(swapper.swapToYamlConfiguration(new StdioTransportConfiguration(false)).isEnabled());
    }
}
