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

package org.apache.shardingsphere.sharding.spring.namespace;

import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@ContextConfiguration(locations = "classpath:META-INF/spring/sharding-application-context.xml")
public final class ShardingSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private InlineShardingAlgorithm dataSourceShardingAlgorithm;
    
    @Resource
    private InlineShardingAlgorithm orderTableShardingAlgorithm;
    
    @Resource
    private ModShardingAlgorithm modShardingAlgorithm;
    
    @Resource
    private ComplexKeysShardingAlgorithm<?> complexShardingAlgorithm;
    
    @Resource
    private HintShardingAlgorithm<?> hintShardingAlgorithm;
    
    @Resource
    private KeyGenerateAlgorithm incrementAlgorithm;
    
    @Resource
    private StandardShardingStrategyConfiguration dataSourceShardingStrategy;
    
    @Resource
    private StandardShardingStrategyConfiguration orderTableShardingStrategy;
    
    @Resource
    private StandardShardingStrategyConfiguration modStrategy;
    
    @Resource
    private ComplexShardingStrategyConfiguration complexStrategy;
    
    @Resource
    private HintShardingStrategyConfiguration hintShardingStrategy;
    
    @Resource
    private KeyGenerateStrategyConfiguration defaultKeyGenerator;
    
    @Resource
    private KeyGenerateStrategyConfiguration orderKeyGenerator;
    
    @Resource
    private NoneShardingStrategyConfiguration noneStrategy;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration complexRule;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration simpleRule;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration bindingRule;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration broadcastRule;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration autoRule;
    
    @Test
    public void assertDataSourceShardingAlgorithm() {
        Assert.assertThat(dataSourceShardingAlgorithm.getType(), CoreMatchers.is("INLINE"));
        Assert.assertThat(dataSourceShardingAlgorithm.getProps().getProperty("algorithm-expression"), CoreMatchers.is("ds_$->{order_id % 2}"));
    }
    
    @Test
    public void assertOrderTableShardingAlgorithm() {
        Assert.assertThat(orderTableShardingAlgorithm.getType(), CoreMatchers.is("INLINE"));
        Assert.assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), CoreMatchers.is("t_order_$->{order_id % 4}"));
    }
    
    @Test
    public void assertModShardingAlgorithm() {
        Assert.assertThat(modShardingAlgorithm.getType(), CoreMatchers.is("MOD"));
        Assert.assertThat(modShardingAlgorithm.getProps().getProperty("sharding-count"), CoreMatchers.is("2"));
    }
    
    @Test
    public void assertComplexShardingAlgorithm() {
        Assert.assertThat(complexShardingAlgorithm.getType(), CoreMatchers.is("COMPLEX_TEST"));
    }
    
    @Test
    public void assertHintShardingAlgorithm() {
        Assert.assertThat(hintShardingAlgorithm.getType(), CoreMatchers.is("HINT_TEST"));
    }
    
    @Test
    public void assertDataSourceShardingStrategy() {
        Assert.assertThat(dataSourceShardingStrategy.getShardingColumn(), CoreMatchers.is("order_id"));
        Assert.assertThat(dataSourceShardingStrategy.getShardingAlgorithmName(), CoreMatchers.is("dataSourceShardingAlgorithm"));
    }
    
    @Test
    public void assertOrderTableShardingStrategy() {
        Assert.assertThat(orderTableShardingStrategy.getShardingColumn(), CoreMatchers.is("order_id"));
        Assert.assertThat(orderTableShardingStrategy.getShardingAlgorithmName(), CoreMatchers.is("orderTableShardingAlgorithm"));
    }
    
    @Test
    public void assertModStrategy() {
        Assert.assertThat(modStrategy.getShardingColumn(), CoreMatchers.is("order_id"));
        Assert.assertThat(modStrategy.getShardingAlgorithmName(), CoreMatchers.is("modShardingAlgorithm"));
    }
    
    @Test
    public void assertComplexStrategy() {
        Assert.assertThat(complexStrategy.getShardingColumns(), CoreMatchers.is("order_id,user_id"));
        Assert.assertThat(complexStrategy.getShardingAlgorithmName(), CoreMatchers.is("complexShardingAlgorithm"));
    }
    
    @Test
    public void assertHintStrategy() {
        Assert.assertThat(hintShardingStrategy.getShardingAlgorithmName(), CoreMatchers.is("hintShardingAlgorithm"));
    }
    
    @Test
    public void assertNoneStrategy() {
        Assert.assertNotNull(noneStrategy);
    }
    
    @Test
    public void assertIncrementAlgorithm() {
        Assert.assertThat(incrementAlgorithm.getType(), CoreMatchers.is("INCREMENT"));
    }
    
    @Test
    public void assertDefaultKeyGenerator() {
        Assert.assertThat(defaultKeyGenerator.getColumn(), CoreMatchers.is("id"));
        Assert.assertThat(defaultKeyGenerator.getKeyGeneratorName(), CoreMatchers.is("incrementAlgorithm"));
    }
    
    @Test
    public void assertOrderKeyGenerator() {
        Assert.assertThat(orderKeyGenerator.getColumn(), CoreMatchers.is("order_id"));
        Assert.assertThat(orderKeyGenerator.getKeyGeneratorName(), CoreMatchers.is("incrementAlgorithm"));
    }
    
    @Test
    public void assertSimpleRule() {
        Collection<ShardingTableRuleConfiguration> actualSimpleRuleConfigurations = simpleRule.getTables();
        Assert.assertThat(actualSimpleRuleConfigurations.size(), CoreMatchers.is(1));
        ShardingTableRuleConfiguration actualSimpleRuleConfiguration = actualSimpleRuleConfigurations.iterator().next();
        Assert.assertThat(actualSimpleRuleConfiguration.getLogicTable(), CoreMatchers.is("t_order"));
    }
    
    @Test
    public void assertComplexRule() {
        Collection<ShardingTableRuleConfiguration> actualComplexRuleConfigurations = complexRule.getTables();
        Assert.assertThat(actualComplexRuleConfigurations.size(), CoreMatchers.is(1));
        ShardingTableRuleConfiguration actualComplexRuleConfiguration = actualComplexRuleConfigurations.iterator().next();
        Assert.assertThat(actualComplexRuleConfiguration.getLogicTable(), CoreMatchers.is("t_order"));
        Assert.assertThat(actualComplexRuleConfiguration.getActualDataNodes(), CoreMatchers.is("ds_$->{0..1}.t_order_$->{0..3}"));
        Assert.assertThat(actualComplexRuleConfiguration.getDatabaseShardingStrategy().getShardingAlgorithmName(), CoreMatchers.is("dataSourceShardingAlgorithm"));
        Assert.assertThat(actualComplexRuleConfiguration.getTableShardingStrategy().getShardingAlgorithmName(), CoreMatchers.is("orderTableShardingAlgorithm"));
        Assert.assertThat(actualComplexRuleConfiguration.getKeyGenerateStrategy().getKeyGeneratorName(), CoreMatchers.is("incrementAlgorithm"));
        Assert.assertThat(complexRule.getDefaultKeyGenerateStrategy().getKeyGeneratorName(), CoreMatchers.is("incrementAlgorithm"));
        
    }
    
    @Test
    public void assertBindingRule() {
        Collection<ShardingTableRuleConfiguration> actualBindingTableRuleConfigurations = bindingRule.getTables();
        Assert.assertThat(actualBindingTableRuleConfigurations.size(), CoreMatchers.is(4));
        Iterator<ShardingTableRuleConfiguration> actualIterator = actualBindingTableRuleConfigurations.iterator();
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_order"));
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_order_item"));
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_user"));
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_user_detail"));
        Collection<String> actualBindingTableGroups = bindingRule.getBindingTableGroups();
        Assert.assertThat(actualBindingTableGroups.size(), CoreMatchers.is(2));
        Assert.assertTrue(actualBindingTableGroups.containsAll(Arrays.asList("t_order, t_order_item", "t_order, t_order_item")));
    }
    
    @Test
    public void assertBroadcastRule() {
        Collection<ShardingTableRuleConfiguration> actualBroadcastTableConfigurations = broadcastRule.getTables();
        Assert.assertThat(actualBroadcastTableConfigurations.size(), CoreMatchers.is(2));
        Iterator<ShardingTableRuleConfiguration> actualIterator = actualBroadcastTableConfigurations.iterator();
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_order"));
        Assert.assertThat(actualIterator.next().getLogicTable(), CoreMatchers.is("t_order_item"));
        Collection<String> broadcastTables = broadcastRule.getBroadcastTables();
        Assert.assertThat(broadcastTables.size(), CoreMatchers.is(2));
        Assert.assertTrue(broadcastTables.containsAll(Arrays.asList("t_dict", "t_address")));
        Collection<String> actualBindingTableGroups = broadcastRule.getBindingTableGroups();
        Assert.assertThat(actualBindingTableGroups.size(), CoreMatchers.is(1));
        Assert.assertTrue(actualBindingTableGroups.containsAll(Arrays.asList("t_order, t_order_item")));
    }
    
    @Test
    public void assertAutoRule() {
        Collection<ShardingAutoTableRuleConfiguration> actualAutoTableConfigurations = autoRule.getAutoTables();
        Assert.assertThat(actualAutoTableConfigurations.size(), CoreMatchers.is(1));
        ShardingAutoTableRuleConfiguration actualShardingAutoTableRuleConfiguration = actualAutoTableConfigurations.iterator().next();
        Assert.assertThat(actualShardingAutoTableRuleConfiguration.getLogicTable(), CoreMatchers.is("t_order"));
        Assert.assertThat(actualShardingAutoTableRuleConfiguration.getActualDataSources(), CoreMatchers.is("ds_0, ds_1"));
        Assert.assertThat(actualShardingAutoTableRuleConfiguration.getShardingStrategy().getShardingAlgorithmName(), CoreMatchers.is("modShardingAlgorithm"));
    }
}
