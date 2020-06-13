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

package org.apache.shardingsphere.sharding.yaml.swapper.algorithm;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.api.config.algorithm.ShardingAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.algorithm.YamlShardingAlgorithmConfiguration;

/**
 * Sharding algorithm configuration YAML swapper.
 */
public final class ShardingAlgorithmConfigurationYamlSwapper implements YamlSwapper<YamlShardingAlgorithmConfiguration, ShardingAlgorithmConfiguration> {
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
    }
    
    @Override
    public YamlShardingAlgorithmConfiguration swap(final ShardingAlgorithmConfiguration data) {
        YamlShardingAlgorithmConfiguration result = new YamlShardingAlgorithmConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        return result;
    }
    
    @Override
    public ShardingAlgorithmConfiguration swap(final YamlShardingAlgorithmConfiguration yamlConfiguration) {
        return new ShardingAlgorithmConfiguration(yamlConfiguration.getType(), yamlConfiguration.getProps());
    }
}
