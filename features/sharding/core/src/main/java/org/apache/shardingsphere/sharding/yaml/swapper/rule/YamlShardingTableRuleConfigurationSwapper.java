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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

/**
 * YAML sharding table rule configuration swapper.
 */
public final class YamlShardingTableRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlTableRuleConfiguration, ShardingTableRuleConfiguration> {
    
    private final YamlShardingStrategyConfigurationSwapper shardingStrategySwapper = new YamlShardingStrategyConfigurationSwapper();
    
    private final YamlKeyGenerateStrategyConfigurationSwapper keyGenerateStrategySwapper = new YamlKeyGenerateStrategyConfigurationSwapper();
    
    private final YamlShardingAuditStrategyConfigurationSwapper auditStrategySwapper = new YamlShardingAuditStrategyConfigurationSwapper();
    
    @Override
    public YamlTableRuleConfiguration swapToYamlConfiguration(final ShardingTableRuleConfiguration data) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataNodes(data.getActualDataNodes());
        if (null != data.getDatabaseShardingStrategy()) {
            result.setDatabaseStrategy(shardingStrategySwapper.swapToYamlConfiguration(data.getDatabaseShardingStrategy()));
        }
        if (null != data.getTableShardingStrategy()) {
            result.setTableStrategy(shardingStrategySwapper.swapToYamlConfiguration(data.getTableShardingStrategy()));
        }
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategySwapper.swapToYamlConfiguration(data.getKeyGenerateStrategy()));
        }
        if (null != data.getAuditStrategy()) {
            result.setAuditStrategy(auditStrategySwapper.swapToYamlConfiguration(data.getAuditStrategy()));
        }
        return result;
    }
    
    @Override
    public ShardingTableRuleConfiguration swapToObject(final YamlTableRuleConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig.getLogicTable(), () -> new MissingRequiredShardingConfigurationException("Sharding Logic table"));
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(yamlConfig.getLogicTable(), yamlConfig.getActualDataNodes());
        if (null != yamlConfig.getDatabaseStrategy()) {
            result.setDatabaseShardingStrategy(shardingStrategySwapper.swapToObject(yamlConfig.getDatabaseStrategy()));
        }
        if (null != yamlConfig.getTableStrategy()) {
            result.setTableShardingStrategy(shardingStrategySwapper.swapToObject(yamlConfig.getTableStrategy()));
        }
        if (null != yamlConfig.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategySwapper.swapToObject(yamlConfig.getKeyGenerateStrategy()));
        }
        if (null != yamlConfig.getAuditStrategy()) {
            result.setAuditStrategy(auditStrategySwapper.swapToObject(yamlConfig.getAuditStrategy()));
        }
        return result;
    }
}
