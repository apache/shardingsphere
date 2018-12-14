/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlShardingRuleConfigurationTest {
    
    @Test
    public void assertGetShardingRuleConfigurationWithoutDefaultStrategy() {
        ShardingRuleConfiguration actual = createYamlShardingRuleConfigWithoutDefaultStrategy().getShardingRuleConfiguration();
        assertShardingRuleConfig(actual);
        assertWithoutDefaultStrategy(actual);
    }
    
    @Test
    public void assertGetShardingRuleConfigurationWithDefaultStrategy() {
        ShardingRuleConfiguration actual = createYamlShardingRuleConfigWithDefaultStrategy().getShardingRuleConfiguration();
        assertShardingRuleConfig(actual);
        assertWithDefaultStrategy(actual);
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfigWithoutDefaultStrategy() {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        result.setDefaultDataSourceName("default_ds");
        result.getTables().put("t_order", new YamlTableRuleConfiguration());
        result.getTables().put("t_order_item", new YamlTableRuleConfiguration());
        result.getBindingTables().add("t_order, t_order_item");
        result.getBroadcastTables().add("t_config");
        result.setDefaultKeyGeneratorClassName(DefaultKeyGenerator.class.getName());
        result.getMasterSlaveRules().put("master_slave_ds", createYamlMasterSlaveRuleConfig());
        return result;
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfigWithDefaultStrategy() {
        YamlShardingRuleConfiguration result = createYamlShardingRuleConfigWithoutDefaultStrategy();
        YamlShardingStrategyConfiguration yamlShardingStrategyConfig = new YamlShardingStrategyConfiguration();
        yamlShardingStrategyConfig.setNone(new YamlNoneShardingStrategyConfiguration());
        result.setDefaultDatabaseStrategy(yamlShardingStrategyConfig);
        result.setDefaultTableStrategy(yamlShardingStrategyConfig);
        return result;
    }
    
    private YamlMasterSlaveRuleConfiguration createYamlMasterSlaveRuleConfig() {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setName("master_slave_ds");
        result.setMasterDataSourceName("master_ds");
        result.setSlaveDataSourceNames(Arrays.asList("slave_ds_0", "slave_ds_1"));
        result.setLoadBalanceAlgorithmClassName(RoundRobinMasterSlaveLoadBalanceAlgorithm.class.getName());
        result.setLoadBalanceAlgorithmType(MasterSlaveLoadBalanceAlgorithmType.RANDOM);
        return result;
    }
    
    private void assertShardingRuleConfig(final ShardingRuleConfiguration actual) {
        assertThat(actual.getDefaultDataSourceName(), is("default_ds"));
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        Iterator<TableRuleConfiguration> tableRuleConfigIterator = actual.getTableRuleConfigs().iterator();
        assertThat(tableRuleConfigIterator.next().getLogicTable(), is("t_order"));
        assertThat(tableRuleConfigIterator.next().getLogicTable(), is("t_order_item"));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next(), is("t_order, t_order_item"));
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("t_config"));
        assertThat(actual.getDefaultKeyGenerator(), instanceOf(DefaultKeyGenerator.class));
        assertMasterSlaveRuleConfig(actual.getMasterSlaveRuleConfigs().iterator().next());
    }
    
    private void assertMasterSlaveRuleConfig(final MasterSlaveRuleConfiguration actual) {
        assertThat(actual.getName(), is("master_slave_ds"));
        assertThat(actual.getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertThat(actual.getLoadBalanceAlgorithm(), instanceOf(RoundRobinMasterSlaveLoadBalanceAlgorithm.class));
    }
    
    private void assertWithoutDefaultStrategy(final ShardingRuleConfiguration actual) {
        assertNull(actual.getDefaultDatabaseShardingStrategyConfig());
        assertNull(actual.getDefaultTableShardingStrategyConfig());
    }
    
    private void assertWithDefaultStrategy(final ShardingRuleConfiguration actual) {
        assertThat(actual.getDefaultDatabaseShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfiguration.class));
        assertThat(actual.getDefaultTableShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfiguration.class));
    }
}
