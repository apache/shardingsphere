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

import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
    private StandardShardingStrategyConfiguration dataSourceShardingStrategy;
    
    @Resource
    private StandardShardingStrategyConfiguration orderTableShardingStrategy;
    
    @Resource
    private NoneShardingStrategyConfiguration noneStrategy;
    
    @Test
    public void assertDataSourceShardingAlgorithm() {
        assertThat(dataSourceShardingAlgorithm.getType(), is("INLINE"));
        assertThat(dataSourceShardingAlgorithm.getProps().getProperty("algorithm.expression"), is("ds_$->{order_id % 2}"));
    }
    
    @Test
    public void assertOrderTableShardingAlgorithm() {
        assertThat(orderTableShardingAlgorithm.getType(), is("INLINE"));
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm.expression"), is("t_order_$->{order_id % 4}"));
    }
    
    @Test
    public void assertModShardingAlgorithm() {
        assertThat(modShardingAlgorithm.getType(), is("MOD"));
        assertThat(modShardingAlgorithm.getProps().getProperty("sharding.count"), is("2"));
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
        // TODO
    }
    
    @Test
    public void assertComplexStrategy() {
        // TODO
    }
    
    @Test
    public void assertHintStrategy() {
        // TODO
    }
    
    @Test
    public void assertNoneStrategy() {
        assertNotNull(noneStrategy);
    }
    
    @Test
    public void assertIncrementAlgorithm() {
        // TODO
    }
    
    @Test
    public void assertDefaultKeyGenerator() {
        // TODO
    }
    
    @Test
    public void assertOrderKeyGenerator() {
        // TODO
    }
    
    @Test
    public void assertSimpleRule() {
        // TODO
    }
    
    @Test
    public void assertComplexRule() {
        // TODO
    }
    
    @Test
    public void assertBindingRule() {
        // TODO
    }
    
    @Test
    public void assertBroadcastRule() {
        // TODO
    }
    
    @Test
    public void assertAutoRule() {
        // TODO
    }
}
