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

package org.apache.shardingsphere.core.rule.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.config.YamlConfiguration;

import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML Rule configuration builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlRuleConfigurationBuilder {
    
    /**
     * Build rules with sharding rule configuration.
     * 
     * @param yamlShardingRuleConfiguration YAML sharding rule configuration
     * @return rule configurations
     */
    public static Collection<YamlConfiguration> buildSharding(final YamlShardingRuleConfiguration yamlShardingRuleConfiguration) {
        Collection<YamlConfiguration> result = new LinkedList<>();
        result.add(yamlShardingRuleConfiguration);
        result.addAll(yamlShardingRuleConfiguration.getMasterSlaveRules().values());
        if (null != yamlShardingRuleConfiguration.getEncryptRule() && !yamlShardingRuleConfiguration.getEncryptRule().getTables().isEmpty()) {
            result.add(yamlShardingRuleConfiguration.getEncryptRule());
        }
        return result;
    }
}
