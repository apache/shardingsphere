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

package org.apache.shardingsphere.orchestration.core.common.rule.converter;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.core.yaml.representer.processor.ShardingTupleProcessorFactory;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

/**
 * Sharding rule configuration convert.
 */
public final class ShardingRuleConfigurationConverter implements RuleConfigurationConverter<ShardingRuleConfiguration> {
    
    @Override
    public boolean match(final String context) {
        if (context.contains("encryptRule:\n")) {
            return true;
        }
        if (context.contains("tables:\n")
                && !context.contains("encryptors:\n")) {
            return true;
        }
        return context.contains("defaultTableStrategy:\n");
    }
    
    @Override
    public ShardingRuleConfiguration unmarshal(final String context) {
        return new ShardingRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(context, YamlShardingRuleConfiguration.class, new YamlRootShardingConfigurationConstructor()));
    }
    
    @Override
    public String marshal(final ShardingRuleConfiguration shardingRuleConfiguration, final String shardingSchemaName) {
        Preconditions.checkState(null != shardingRuleConfiguration
                        && (!shardingRuleConfiguration.getTableRuleConfigs().isEmpty() || shardingRuleConfiguration.getDefaultTableShardingStrategyConfig() != null),
                "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
        return YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration), ShardingTupleProcessorFactory.newInstance());
    }
}

