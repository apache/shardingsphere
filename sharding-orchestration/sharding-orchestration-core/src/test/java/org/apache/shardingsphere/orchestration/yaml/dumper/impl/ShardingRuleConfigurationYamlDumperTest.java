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

package org.apache.shardingsphere.orchestration.yaml.dumper.impl;

import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import org.apache.shardingsphere.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingRuleConfigurationYamlDumperTest {
    
    private static final String WITHOUT_DEFAULT_KEY_GENERATOR_YAML = "defaultDatabaseStrategy:\n" + "  inline:\n" + "    algorithmExpression: ds_${user_id % 2}\n"
            + "    shardingColumn: user_id\n" + "defaultTableStrategy:\n" + "  inline:\n" + "    algorithmExpression: t_order_${order_id % 2}\n"
            + "    shardingColumn: order_id\n" + "masterSlaveRules:\n" + "  ds_0:\n"
            + "    loadBalanceAlgorithmClassName: org.apache.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm\n"
            + "    masterDataSourceName: ds_0_master\n" + "    name: ds_0\n" + "    slaveDataSourceNames:\n" + "    - ds_0_slave\n" + "  ds_1:\n"
            + "    loadBalanceAlgorithmClassName: org.apache.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm\n"
            + "    masterDataSourceName: ds_1_master\n" + "    name: ds_1\n" + "    slaveDataSourceNames:\n" + "    - ds_1_slave\n" + "tables:\n" + "  t_order:\n"
            + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "    logicTable: t_order\n";
    
    private static final String WITH_DEFAULT_KEY_GENERATOR_YAML = "defaultDatabaseStrategy:\n" + "  inline:\n" + "    algorithmExpression: ds_${user_id % 2}\n"
            + "    shardingColumn: user_id\n" + "defaultKeyGenerator:\n" + "  column: id\n" + "  type: UUID\n" + "defaultTableStrategy:\n" + "  inline:\n"
            + "    algorithmExpression: t_order_${order_id % 2}\n" + "    shardingColumn: order_id\n" + "masterSlaveRules:\n" + "  ds_0:\n"
            + "    loadBalanceAlgorithmClassName: org.apache.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm\n"
            + "    masterDataSourceName: ds_0_master\n" + "    name: ds_0\n" + "    slaveDataSourceNames:\n" + "    - ds_0_slave\n" + "  ds_1:\n"
            + "    loadBalanceAlgorithmClassName: org.apache.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm\n"
            + "    masterDataSourceName: ds_1_master\n" + "    name: ds_1\n" + "    slaveDataSourceNames:\n"
            + "    - ds_1_slave\n" + "tables:\n" + "  t_order:\n" + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "    logicTable: t_order\n";
    
    @Test
    public void assertDumpWithoutDefaultKeyGenerator() {
        assertThat(new ShardingRuleConfigurationYamlDumper().dump(createShardingRuleConfigurationWithoutDefaultKeyGenerator()), is(WITHOUT_DEFAULT_KEY_GENERATOR_YAML));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfigurationWithoutDefaultKeyGenerator() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration();
        tableRuleConfiguration.setLogicTable("t_order");
        tableRuleConfiguration.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        result.getTableRuleConfigs().add(tableRuleConfiguration);
        result.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        result.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        result.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ds_0", "ds_0_master", Collections.singletonList("ds_0_slave"), MasterSlaveLoadBalanceAlgorithmType.getDefaultAlgorithmType().getAlgorithm()));
        result.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ds_1", "ds_1_master", Collections.singletonList("ds_1_slave"), MasterSlaveLoadBalanceAlgorithmType.getDefaultAlgorithmType().getAlgorithm()));
        return result;
    }
    
    @Test
    public void assertDumpWithDefaultKeyGenerator() {
        assertThat(new ShardingRuleConfigurationYamlDumper().dump(createShardingRuleConfigurationWithDefaultKeyGenerator()), is(WITH_DEFAULT_KEY_GENERATOR_YAML));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfigurationWithDefaultKeyGenerator() {
        ShardingRuleConfiguration result = createShardingRuleConfigurationWithoutDefaultKeyGenerator();
        result.setDefaultKeyGeneratorConfig(new KeyGeneratorConfiguration("id", "UUID", new Properties()));
        return result;
    }
}
