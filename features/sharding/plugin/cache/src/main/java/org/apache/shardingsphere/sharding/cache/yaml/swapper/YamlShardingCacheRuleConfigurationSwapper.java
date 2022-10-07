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

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.yaml.YamlShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

/**
 * YAML sharding cache rule configuration swapper.
 */
public final class YamlShardingCacheRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlShardingCacheRuleConfiguration, ShardingCacheRuleConfiguration> {
    
    private final YamlShardingCacheOptionsConfigurationSwapper cacheOptionsConfigurationSwapper = new YamlShardingCacheOptionsConfigurationSwapper();
    
    @Override
    public YamlShardingCacheRuleConfiguration swapToYamlConfiguration(final ShardingCacheRuleConfiguration data) {
        YamlShardingCacheRuleConfiguration result = new YamlShardingCacheRuleConfiguration();
        result.setAllowedMaxSqlLength(data.getAllowedMaxSqlLength());
        result.setRouteCache(cacheOptionsConfigurationSwapper.swapToYamlConfiguration(data.getRouteCache()));
        return result;
    }
    
    @Override
    public ShardingCacheRuleConfiguration swapToObject(final YamlShardingCacheRuleConfiguration yamlConfig) {
        return new ShardingCacheRuleConfiguration(yamlConfig.getAllowedMaxSqlLength(), cacheOptionsConfigurationSwapper.swapToObject(yamlConfig.getRouteCache()));
    }
    
    @Override
    public Class<ShardingCacheRuleConfiguration> getTypeClass() {
        return ShardingCacheRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHARDING_CACHE";
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ALGORITHM_PROVIDER_ORDER + 1;
    }
}
