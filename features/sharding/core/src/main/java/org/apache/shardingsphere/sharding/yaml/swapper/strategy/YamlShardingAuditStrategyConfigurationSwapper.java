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

package org.apache.shardingsphere.sharding.yaml.swapper.strategy;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;

/**
 * YAML sharding audit strategy configuration swapper.
 */
public final class YamlShardingAuditStrategyConfigurationSwapper implements YamlConfigurationSwapper<YamlShardingAuditStrategyConfiguration, ShardingAuditStrategyConfiguration> {
    
    @Override
    public YamlShardingAuditStrategyConfiguration swapToYamlConfiguration(final ShardingAuditStrategyConfiguration data) {
        YamlShardingAuditStrategyConfiguration result = new YamlShardingAuditStrategyConfiguration();
        result.setAuditorNames(data.getAuditorNames());
        result.setAllowHintDisable(data.isAllowHintDisable());
        return result;
    }
    
    @Override
    public ShardingAuditStrategyConfiguration swapToObject(final YamlShardingAuditStrategyConfiguration yamlConfig) {
        return new ShardingAuditStrategyConfiguration(yamlConfig.getAuditorNames(), yamlConfig.isAllowHintDisable());
    }
}
