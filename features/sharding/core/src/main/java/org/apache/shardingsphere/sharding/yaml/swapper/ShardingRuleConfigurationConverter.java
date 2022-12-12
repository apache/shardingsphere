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

package org.apache.shardingsphere.sharding.yaml.swapper;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.util.Collection;
import java.util.Optional;

/**
 * Sharding rule configuration converter.
 */
public final class ShardingRuleConfigurationConverter {
    
    /**
     * Find and convert sharding rule configuration from YAML.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return sharding rule configuration
     * @throws IllegalStateException if there is no available sharding rule
     */
    public static ShardingRuleConfiguration findAndConvertShardingRuleConfiguration(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        return new YamlShardingRuleConfigurationSwapper().swapToObject(findYamlShardingRuleConfiguration(yamlRuleConfigs));
    }
    
    /**
     * Find YAML sharding rule configuration.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return YAML sharding rule configuration
     * @throws IllegalStateException if there is no available sharding rule
     */
    public static YamlShardingRuleConfiguration findYamlShardingRuleConfiguration(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Optional<YamlRuleConfiguration> ruleConfig = yamlRuleConfigs.stream().filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst();
        Preconditions.checkState(ruleConfig.isPresent(), "No available sharding rule.");
        return (YamlShardingRuleConfiguration) ruleConfig.get();
    }
}
