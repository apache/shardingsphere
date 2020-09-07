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
    private AlgorithmProvidedShardingRuleConfiguration shardingRuleConfiguration;
    
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
        assertThat(shardingRuleConfiguration.getTables().size(), is(2));
        List<ShardingTableRuleConfiguration> shardingTableRuleConfigurationList = Lists.newArrayList(shardingRuleConfiguration.getTables());
        assertThat(shardingTableRuleConfigurationList.get(0).getLogicTable(), is("t_order"));
        assertThat(shardingTableRuleConfigurationList.get(0).getActualDataNodes(), is("ds_$->{0..1}.t_order_$->{0..1}"));
        assertThat(shardingTableRuleConfigurationList.get(0).getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingTableRuleConfigurationList.get(0).getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigurationList.get(0).getTableShardingStrategy().getShardingAlgorithmName(), is("orderTableShardingAlgorithm"));
        assertThat(shardingTableRuleConfigurationList.get(0).getKeyGenerateStrategy().getColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigurationList.get(0).getKeyGenerateStrategy().getKeyGeneratorName(), is("keyGenerator"));
        assertThat(shardingTableRuleConfigurationList.get(1).getLogicTable(), is("t_order_item"));
        assertThat(shardingTableRuleConfigurationList.get(1).getActualDataNodes(), is("ds_$->{0..1}.t_order_item_$->{0..1}"));
        assertThat(shardingTableRuleConfigurationList.get(1).getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingTableRuleConfigurationList.get(1).getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(shardingTableRuleConfigurationList.get(1).getTableShardingStrategy().getShardingAlgorithmName(), is("orderItemTableShardingAlgorithm"));
        assertThat(shardingTableRuleConfigurationList.get(1).getKeyGenerateStrategy().getColumn(), is("order_item_id"));
        assertThat(shardingTableRuleConfigurationList.get(1).getKeyGenerateStrategy().getKeyGeneratorName(), is("keyGenerator"));
    }
    
    private void assertShardingConfigurationBindingTableGroups() {
        assertThat(shardingRuleConfiguration.getBindingTableGroups().size(), is(2));
        List<String> bindingTableGroupsList = new ArrayList<>(shardingRuleConfiguration.getBindingTableGroups());
        assertThat(bindingTableGroupsList.get(0), is("t_order"));
        assertThat(bindingTableGroupsList.get(1), is("t_order_item"));
    }
    
    private void assertShardingConfigurationBroadcastTables() {
        assertThat(shardingRuleConfiguration.getBroadcastTables().size(), is(1));
        assertThat(shardingRuleConfiguration.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    private void assertShardingConfigurationDefaultDatabaseShardingStrategy() {
        assertThat(shardingRuleConfiguration.getDefaultDatabaseShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) shardingRuleConfiguration.getDefaultDatabaseShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(shardingRuleConfiguration.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName(), is("databaseShardingAlgorithm"));
    }
    
    private void assertShardingConfigurationShardingAlgorithms() {
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().size(), is(3));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderItemTableShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderItemTableShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderItemTableShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("t_order_item_$->{order_id % 2}"));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("databaseShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("databaseShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("databaseShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderTableShardingAlgorithm"), instanceOf(InlineShardingAlgorithm.class));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderTableShardingAlgorithm").getType(), is("INLINE"));
        assertThat(shardingRuleConfiguration.getShardingAlgorithms().get("orderTableShardingAlgorithm").getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
    }
    
    private void assertShardingConfigurationKeyGenerators() {
        assertThat(shardingRuleConfiguration.getKeyGenerators().size(), is(1));
        assertThat(shardingRuleConfiguration.getKeyGenerators().get("keyGenerator"), instanceOf(SnowflakeKeyGenerateAlgorithm.class));
        assertThat(shardingRuleConfiguration.getKeyGenerators().get("keyGenerator").getProps().getProperty("worker-id"), is("123"));
    }
}
