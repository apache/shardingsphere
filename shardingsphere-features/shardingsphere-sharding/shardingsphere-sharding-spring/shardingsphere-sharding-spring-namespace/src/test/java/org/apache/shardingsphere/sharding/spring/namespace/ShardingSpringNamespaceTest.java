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
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        assertThat(dataSourceShardingAlgorithm.getType(), is("INLINE"));
        assertThat(dataSourceShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("ds_$->{order_id % 2}"));
    }
    
    @Test
    public void assertOrderTableShardingAlgorithm() {
        assertThat(orderTableShardingAlgorithm.getType(), is("INLINE"));
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 4}"));
    }
    
    @Test
    public void assertModShardingAlgorithm() {
        assertThat(modShardingAlgorithm.getType(), is("MOD"));
        assertThat(modShardingAlgorithm.getProps().getProperty("sharding-count"), is("2"));
    }
    
    @Test
    public void assertComplexShardingAlgorithm() {
        assertThat(complexShardingAlgorithm.getType(), is("COMPLEX_TEST"));
    }
    
    @Test
    public void assertHintShardingAlgorithm() {
        assertThat(hintShardingAlgorithm.getType(), is("HINT_TEST"));
    }
    
    @Test
    public void assertDataSourceShardingStrategy() {
        assertThat(dataSourceShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(dataSourceShardingStrategy.getShardingAlgorithmName(), is("dataSourceShardingAlgorithm"));
    }
    
    @Test
    public void assertOrderTableShardingStrategy() {
        assertThat(orderTableShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(orderTableShardingStrategy.getShardingAlgorithmName(), is("orderTableShardingAlgorithm"));
    }
    
    @Test
    public void assertModStrategy() {
        assertThat(modStrategy.getShardingColumn(), is("order_id"));
        assertThat(modStrategy.getShardingAlgorithmName(), is("modShardingAlgorithm"));
    }
    
    @Test
    public void assertComplexStrategy() {
        assertThat(complexStrategy.getShardingColumns(), is("order_id,user_id"));
        assertThat(complexStrategy.getShardingAlgorithmName(), is("complexShardingAlgorithm"));
    }
    
    @Test
    public void assertHintStrategy() {
        assertThat(hintShardingStrategy.getShardingAlgorithmName(), is("hintShardingAlgorithm"));
    }
    
    @Test
    public void assertNoneStrategy() {
        assertNotNull(noneStrategy);
    }
    
    @Test
    public void assertIncrementAlgorithm() {
        assertThat(incrementAlgorithm.getType(), is("INCREMENT"));
    }
    
    @Test
    public void assertDefaultKeyGenerator() {
        assertThat(defaultKeyGenerator.getColumn(), is("id"));
        assertThat(defaultKeyGenerator.getKeyGeneratorName(), is("incrementAlgorithm"));
    }
    
    @Test
    public void assertOrderKeyGenerator() {
        assertThat(orderKeyGenerator.getColumn(), is("order_id"));
        assertThat(orderKeyGenerator.getKeyGeneratorName(), is("incrementAlgorithm"));
    }
    
    @Test
    public void assertSimpleRule() {
        Collection<ShardingTableRuleConfiguration> actualSimpleRuleConfigs = simpleRule.getTables();
        assertThat(actualSimpleRuleConfigs.size(), is(1));
        ShardingTableRuleConfiguration actualSimpleRuleConfig = actualSimpleRuleConfigs.iterator().next();
        assertThat(actualSimpleRuleConfig.getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertComplexRule() {
        Collection<ShardingTableRuleConfiguration> actualComplexRuleConfigs = complexRule.getTables();
        assertThat(actualComplexRuleConfigs.size(), is(1));
        ShardingTableRuleConfiguration actualComplexRuleConfig = actualComplexRuleConfigs.iterator().next();
        assertThat(actualComplexRuleConfig.getLogicTable(), is("t_order"));
        assertThat(actualComplexRuleConfig.getActualDataNodes(), is("ds_$->{0..1}.t_order_$->{0..3}"));
        assertThat(actualComplexRuleConfig.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("dataSourceShardingAlgorithm"));
        assertThat(actualComplexRuleConfig.getTableShardingStrategy().getShardingAlgorithmName(), is("orderTableShardingAlgorithm"));
        assertThat(actualComplexRuleConfig.getKeyGenerateStrategy().getKeyGeneratorName(), is("incrementAlgorithm"));
        assertThat(complexRule.getDefaultKeyGenerateStrategy().getKeyGeneratorName(), is("incrementAlgorithm"));
        
    }
    
    @Test
    public void assertBindingRule() {
        Collection<ShardingTableRuleConfiguration> actualBindingTableRuleConfigs = bindingRule.getTables();
        assertThat(actualBindingTableRuleConfigs.size(), is(4));
        Iterator<ShardingTableRuleConfiguration> actualIterator = actualBindingTableRuleConfigs.iterator();
        assertThat(actualIterator.next().getLogicTable(), is("t_order"));
        assertThat(actualIterator.next().getLogicTable(), is("t_order_item"));
        assertThat(actualIterator.next().getLogicTable(), is("t_user"));
        assertThat(actualIterator.next().getLogicTable(), is("t_user_detail"));
        Collection<String> actualBindingTableGroups = bindingRule.getBindingTableGroups();
        assertThat(actualBindingTableGroups.size(), is(2));
        assertTrue(actualBindingTableGroups.containsAll(Arrays.asList("t_order, t_order_item", "t_order, t_order_item")));
    }
    
    @Test
    public void assertBroadcastRule() {
        Collection<ShardingTableRuleConfiguration> actualBroadcastTableConfigs = broadcastRule.getTables();
        assertThat(actualBroadcastTableConfigs.size(), is(2));
        Iterator<ShardingTableRuleConfiguration> actualIterator = actualBroadcastTableConfigs.iterator();
        assertThat(actualIterator.next().getLogicTable(), is("t_order"));
        assertThat(actualIterator.next().getLogicTable(), is("t_order_item"));
        Collection<String> broadcastTables = broadcastRule.getBroadcastTables();
        assertThat(broadcastTables.size(), is(2));
        assertTrue(broadcastTables.containsAll(Arrays.asList("t_dict", "t_address")));
        Collection<String> actualBindingTableGroups = broadcastRule.getBindingTableGroups();
        assertThat(actualBindingTableGroups.size(), is(1));
        assertTrue(actualBindingTableGroups.contains("t_order, t_order_item"));
    }
    
    @Test
    public void assertAutoRule() {
        Collection<ShardingAutoTableRuleConfiguration> actualAutoTableConfigurations = autoRule.getAutoTables();
        assertThat(actualAutoTableConfigurations.size(), is(1));
        ShardingAutoTableRuleConfiguration actualAutoTableRuleConfig = actualAutoTableConfigurations.iterator().next();
        assertThat(actualAutoTableRuleConfig.getLogicTable(), is("t_order"));
        assertThat(actualAutoTableRuleConfig.getActualDataSources(), is("ds_0, ds_1"));
        assertThat(actualAutoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName(), is("modShardingAlgorithm"));
    }
}
