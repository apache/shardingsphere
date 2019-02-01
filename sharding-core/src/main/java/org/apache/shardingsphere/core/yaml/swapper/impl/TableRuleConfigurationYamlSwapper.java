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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;

/**
 * Table rule configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class TableRuleConfigurationYamlSwapper implements YamlSwapper<YamlTableRuleConfiguration, TableRuleConfiguration> {
    
    @Override
    public YamlTableRuleConfiguration swap(final TableRuleConfiguration data) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataNodes(data.getActualDataNodes());
        if (null != data.getDatabaseShardingStrategyConfig()) {
            result.setDatabaseStrategy(new ShardingStrategyConfigurationYamlSwapper().swap(data.getDatabaseShardingStrategyConfig()));
        }
        if (null != data.getTableShardingStrategyConfig()) {
            result.setTableStrategy(new ShardingStrategyConfigurationYamlSwapper().swap(data.getTableShardingStrategyConfig()));
        }
        if (null != data.getKeyGeneratorConfig()) {
            result.setKeyGenerator(new KeyGeneratorConfigurationYamlSwapper().swap(data.getKeyGeneratorConfig()));
        }
        if (null != data.getEncryptorConfig()) {
            result.setEncryptor(new EncryptorConfigurationYamlSwapper().swap(data.getEncryptorConfig()));
        }
        result.setLogicIndex(data.getLogicIndex());
        return result;
    }
    
    @Override
    public TableRuleConfiguration swap(final YamlTableRuleConfiguration yamlConfiguration) {
        Preconditions.checkNotNull(yamlConfiguration.getLogicTable(), "Logic table cannot be null.");
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(yamlConfiguration.getLogicTable());
        result.setActualDataNodes(yamlConfiguration.getActualDataNodes());
        if (null != yamlConfiguration.getDatabaseStrategy()) {
            result.setDatabaseShardingStrategyConfig(new ShardingStrategyConfigurationYamlSwapper().swap(yamlConfiguration.getDatabaseStrategy()));
        }
        if (null != yamlConfiguration.getTableStrategy()) {
            result.setTableShardingStrategyConfig(new ShardingStrategyConfigurationYamlSwapper().swap(yamlConfiguration.getTableStrategy()));
        }
        if (null != yamlConfiguration.getKeyGenerator()) {
            result.setKeyGeneratorConfig(new KeyGeneratorConfigurationYamlSwapper().swap(yamlConfiguration.getKeyGenerator()));
        }
        if (null != yamlConfiguration.getEncryptor()) {
            result.setEncryptorConfig(new EncryptorConfigurationYamlSwapper().swap(yamlConfiguration.getEncryptor()));
        }
        result.setLogicIndex(yamlConfiguration.getLogicIndex());
        return result;
    }
}
