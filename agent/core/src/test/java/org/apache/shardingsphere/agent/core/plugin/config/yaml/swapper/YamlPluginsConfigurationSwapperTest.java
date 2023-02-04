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

package org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper;

import static org.junit.Assert.assertEquals;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class YamlPluginsConfigurationSwapperTest {
    
    @Test
    public void testSwap() {
        // create sample yaml config
        YamlPluginCategoryConfiguration plugins = new YamlPluginCategoryConfiguration();
        
        // create sample logging config
        Map<String, YamlPluginConfiguration> logging = new LinkedHashMap<>();
        logging.put("logging1", new YamlPluginConfiguration());
        logging.put("logging2", new YamlPluginConfiguration());
        plugins.setLogging(logging);
        YamlAgentConfiguration yamlConfig = new YamlAgentConfiguration();
        
        // create sample metrics config
        Map<String, YamlPluginConfiguration> metrics = new LinkedHashMap<>();
        metrics.put("metrics1", new YamlPluginConfiguration());
        plugins.setMetrics(metrics);
        
        yamlConfig.setPlugins(plugins);
        
        // call swap method
        Map<String, PluginConfiguration> result = YamlPluginsConfigurationSwapper.swap(yamlConfig);
        
        // check that the result is correct
        assertEquals(3, result.size());
        
        assertEquals(true, result.get("metrics1").getProps().size() < 1);
    }
    
    @Test
    public void testSwapNullPlugins() {
        // create sample yaml config with null plugins
        YamlAgentConfiguration yamlConfig = new YamlAgentConfiguration();
        yamlConfig.setPlugins(null);
        
        // call swap method
        Map<String, PluginConfiguration> result = YamlPluginsConfigurationSwapper.swap(yamlConfig);
        
        // check that the result is an empty map
        assertEquals(Collections.emptyMap(), result);
    }
    
}
