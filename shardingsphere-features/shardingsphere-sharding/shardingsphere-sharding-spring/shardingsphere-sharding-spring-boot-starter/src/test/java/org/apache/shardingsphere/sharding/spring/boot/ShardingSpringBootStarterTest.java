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

package org.apache.shardingsphere.sharding.spring.boot;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShardingSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class ShardingSpringBootStarterTest {
    
    @Resource
    private InlineShardingAlgorithm databaseShardingAlgorithm;
    
    @Resource
    private InlineShardingAlgorithm orderTableShardingAlgorithm;
    
    @Resource
    private InlineShardingAlgorithm orderItemTableShardingAlgorithm;
    
    @Resource
    private SnowflakeKeyGenerateAlgorithm keyGenerator;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration shardingRuleConfig;
    
    @Test
    public void assertShardingAlgorithm() {
        assertThat(databaseShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
        assertThat(orderItemTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_item_$->{order_id % 2}"));
    }
    
    @Test
    public void assertKeyGenerateAlgorithm() {
        assertThat(keyGenerator.getProps().getProperty("worker-id"), is("123"));
    }
    
    @Test
    public void assertShardingConfiguration() {
        assertShardingConfigurationTables();
        assertShardingConfigurationBindingTableGroups();
        assertShardingConfigurationBroadcastTables();
        assertShardingConfigurationDefaultDatabaseShardingStrategy();
        assertShardingConfigurationShardingAlgorithms();
        assertShardingConfigurationKeyGenerators();
    }
    
    private void assertShardingConfigurationTables() {
        assertThat(shardingRuleConfig.getTables().size(), is(2));
        assertThat(shardingRuleConfig.getAutoTables().size(), is(1));
        List<ShardingTableRuleConfiguration> shardingTableRuleConfigs = Lists.newArrayList(shardingRuleConfig.getTables());
        assertThat(shardingTableRuleConfigs.get(0).getLogicTable(), is("t_order"));
        assertThat(shardingTableRuleConfigs.get(0).getActualDataNodes(), is("ds_$->{0..1}.t_order_$->{0..1}"));
        assertThat(shardingTableRuleConfigs.get(0).getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingTableRuleConfigs.get(0).getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigs.get(0).getTableShardingStrategy().getShardingAlgorithmName(), is("orderTableShardingAlgorithm"));
        assertThat(shardingTableRuleConfigs.get(0).getKeyGenerateStrategy().getColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigs.get(0).getKeyGenerateStrategy().getKeyGeneratorName(), is("keyGenerator"));
        assertThat(shardingTableRuleConfigs.get(1).getLogicTable(), is("t_order_item"));
        assertThat(shardingTableRuleConfigs.get(1).getActualDataNodes(), is("ds_$->{0..1}.t_order_item_$->{0..1}"));
        assertThat(shardingTableRuleConfigs.get(1).getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingTableRuleConfigs.get(1).getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigs.get(1).getTableShardingStrategy().getShardingAlgorithmName(), is("orderItemTableShardingAlgorithm"));
        assertThat(shardingTableRuleConfigs.get(1).getKeyGenerateStrategy().getColumn(), is("order_item_id"));
        assertThat(shardingTableRuleConfigs.get(1).getKeyGenerateStrategy().getKeyGeneratorName(), is("keyGenerator"));
        List<ShardingAutoTableRuleConfiguration> autoShardingTableRuleConfigs = Lists.newArrayList(shardingRuleConfig.getAutoTables());
        assertThat(autoShardingTableRuleConfigs.get(0).getLogicTable(), is("t_order_auto"));
        assertThat(autoShardingTableRuleConfigs.get(0).getActualDataSources(), is("ds0, ds1"));
        assertThat(autoShardingTableRuleConfigs.get(0).getShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) autoShardingTableRuleConfigs.get(0).getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoShardingTableRuleConfigs.get(0).getShardingStrategy().getShardingAlgorithmName(), is("mod"));
    }
    
    private void assertShardingConfigurationBindingTableGroups() {
        assertThat(shardingRuleConfig.getBindingTableGroups().size(), is(2));
        List<String> bindingTableGroupsList = new ArrayList<>(shardingRuleConfig.getBindingTableGroups());
        assertThat(bindingTableGroupsList.get(0), is("t_order"));
        assertThat(bindingTableGroupsList.get(1), is("t_order_item"));
    }
    
    private void assertShardingConfigurationBroadcastTables() {
        assertThat(shardingRuleConfig.getBroadcastTables().size(), is(1));
        assertThat(shardingRuleConfig.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    private void assertShardingConfigurationDefaultDatabaseShardingStrategy() {
        assertThat(shardingRuleConfig.getDefaultDatabaseShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingRuleConfig.getDefaultDatabaseShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(shardingRuleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName(), is("databaseShardingAlgorithm"));
    }
    
    private void assertShardingConfigurationShardingAlgorithms() {
        assertThat(shardingRuleConfig.getShardingAlgorithms().size(), is(3));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderItemTableShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderItemTableShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderItemTableShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("t_order_item_$->{order_id % 2}"));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("databaseShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("databaseShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("databaseShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderTableShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderTableShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfig.getShardingAlgorithms().get("orderTableShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
    }
    
    private void assertShardingConfigurationKeyGenerators() {
        assertThat(shardingRuleConfig.getKeyGenerators().size(), is(1));
        assertThat(shardingRuleConfig.getKeyGenerators().get("keyGenerator"), instanceOf(SnowflakeKeyGenerateAlgorithm.class));
        assertThat(shardingRuleConfig.getKeyGenerators().get("keyGenerator").getProps().getProperty("worker-id"), is("123"));
    }
}
