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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

/**
 * Sharding algorithm configuration YAML swapper.
 */
public final class ShardingAlgorithmConfigurationYamlSwapper implements YamlSwapper<YamlShardingSphereAlgorithmConfiguration, ShardingSphereAlgorithmConfiguration> {
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
    }
    
    @Override
    public YamlShardingSphereAlgorithmConfiguration swap(final ShardingSphereAlgorithmConfiguration data) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        return result;
    }
    
    @Override
    public ShardingSphereAlgorithmConfiguration swap(final YamlShardingSphereAlgorithmConfiguration yamlConfiguration) {
        return new ShardingSphereAlgorithmConfiguration(yamlConfiguration.getType(), yamlConfiguration.getProps());
    }
}
