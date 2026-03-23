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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlHttpTransportConfigurationSwapperTest {
    
    private final YamlHttpTransportConfigurationSwapper swapper = new YamlHttpTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithDefaults() {
        HttpTransportConfiguration actual = swapper.swapToObject(new YamlHttpTransportConfiguration());
        
        assertTrue(actual.isEnabled());
        assertThat(actual.getPort(), is(18088));
    }
    
    @Test
    void assertSwapToObjectWithDisabledHttpIgnoresHttpConfiguration() {
        YamlHttpTransportConfiguration yamlConfig = new YamlHttpTransportConfiguration();
        yamlConfig.setEnabled(false);
        yamlConfig.setPort(-1);
        
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig, Map.of("enabled", false, "port", -1));
        
        assertFalse(actual.isEnabled());
        assertThat(actual.getPort(), is(18088));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlHttpTransportConfiguration actual = swapper.swapToYamlConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"));
        
        assertTrue(actual.isEnabled());
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
}
