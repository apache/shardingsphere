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

package org.apache.shardingsphere.traffic.yaml.swapper;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;

/**
 * YAML traffic rule configuration swapper.
 */
public final class YamlTrafficStrategyConfigurationSwapper implements YamlConfigurationSwapper<YamlTrafficStrategyConfiguration, TrafficStrategyConfiguration> {
    
    @Override
    public YamlTrafficStrategyConfiguration swapToYamlConfiguration(final TrafficStrategyConfiguration data) {
        YamlTrafficStrategyConfiguration result = new YamlTrafficStrategyConfiguration();
        result.setName(data.getName());
        result.setLabels(data.getLabels());
        result.setAlgorithmName(data.getAlgorithmName());
        result.setLoadBalancerName(data.getLoadBalancerName());
        return result;
    }
    
    @Override
    public TrafficStrategyConfiguration swapToObject(final YamlTrafficStrategyConfiguration yamlConfig) {
        return new TrafficStrategyConfiguration(yamlConfig.getName(), yamlConfig.getLabels(), yamlConfig.getAlgorithmName(), yamlConfig.getLoadBalancerName());
    }
}
