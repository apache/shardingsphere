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

package org.apache.shardingsphere.sharding.cache.yaml.swapper;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.cache.config.ShardingCacheOptions;
import org.apache.shardingsphere.sharding.cache.yaml.YamlShardingCacheOptionsConfiguration;

/**
 * YAML sharding cache options configuration swapper.
 */
public final class YamlShardingCacheOptionsConfigurationSwapper implements YamlConfigurationSwapper<YamlShardingCacheOptionsConfiguration, ShardingCacheOptions> {
    
    @Override
    public YamlShardingCacheOptionsConfiguration swapToYamlConfiguration(final ShardingCacheOptions data) {
        YamlShardingCacheOptionsConfiguration result = new YamlShardingCacheOptionsConfiguration();
        result.setSoftValues(data.isSoftValues());
        result.setInitialCapacity(data.getInitialCapacity());
        result.setMaximumSize(data.getMaximumSize());
        return result;
    }
    
    @Override
    public ShardingCacheOptions swapToObject(final YamlShardingCacheOptionsConfiguration yamlConfig) {
        return new ShardingCacheOptions(yamlConfig.isSoftValues(), yamlConfig.getInitialCapacity(), yamlConfig.getMaximumSize());
    }
}
