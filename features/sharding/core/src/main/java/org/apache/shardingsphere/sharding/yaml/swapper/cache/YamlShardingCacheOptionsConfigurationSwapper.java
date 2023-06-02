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

package org.apache.shardingsphere.sharding.yaml.swapper.cache;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;

/**
 * YAML sharding cache options configuration swapper.
 */
public final class YamlShardingCacheOptionsConfigurationSwapper implements YamlConfigurationSwapper<YamlShardingCacheOptionsConfiguration, ShardingCacheOptionsConfiguration> {
    
    @Override
    public YamlShardingCacheOptionsConfiguration swapToYamlConfiguration(final ShardingCacheOptionsConfiguration data) {
        YamlShardingCacheOptionsConfiguration result = new YamlShardingCacheOptionsConfiguration();
        result.setSoftValues(data.isSoftValues());
        result.setInitialCapacity(data.getInitialCapacity());
        result.setMaximumSize(data.getMaximumSize());
        return result;
    }
    
    @Override
    public ShardingCacheOptionsConfiguration swapToObject(final YamlShardingCacheOptionsConfiguration yamlConfig) {
        return new ShardingCacheOptionsConfiguration(yamlConfig.isSoftValues(), yamlConfig.getInitialCapacity(), yamlConfig.getMaximumSize());
    }
}
