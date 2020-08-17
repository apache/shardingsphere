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

package org.apache.shardingsphere.sharding.algorithm.sharding.inline;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.strategy.standard.StandardShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class InlineShardingAlgorithmTest {

    private StandardShardingStrategy shardingStrategy;

    private StandardShardingStrategy shardingStrategyWithSimplified;
    
    @Before
    public void setUp() {
        shardingStrategy = createShardingStrategy();
        shardingStrategyWithSimplified = createShardingStrategyWithSimplified();
    }
    
    private StandardShardingStrategy createShardingStrategy() {
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("algorithm.expression", "t_order_$->{order_id % 4}");
        shardingAlgorithm.getProps().setProperty("allow.range.query.with.inline.sharding", "true");
        shardingAlgorithm.init();
        return new StandardShardingStrategy("order_id", shardingAlgorithm);
    }
    
    private StandardShardingStrategy createShardingStrategyWithSimplified() {
        InlineShardingAlgorithm shardingAlgorithmWithSimplified = new InlineShardingAlgorithm();
        shardingAlgorithmWithSimplified.getProps().setProperty("algorithm.expression", "t_order_${order_id % 4}");
        shardingAlgorithmWithSimplified.init();
        return new StandardShardingStrategy("order_id", shardingAlgorithmWithSimplified);
    }
    
    @Test
    public void assertDoSharding() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("order_id", "t_order", Lists.newArrayList(0, 1, 2, 3)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(4));
        Collection<String> actualWithSimplified = shardingStrategyWithSimplified.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actualWithSimplified.size(), is(4));
    }
    
    @Test
    public void assertDoShardingWithRangeRouteValue() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Lists.newArrayList(new RangeRouteValue<>("order_id", "t_order", mock(Range.class)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    @Test
    public void assertDoShardingWithNonExistNodes() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1");
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("order_id", "t_order", Lists.newArrayList(0, 1, 2, 3)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        Collection<String> actualWithSimplified = shardingStrategyWithSimplified.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actualWithSimplified.size(), is(2));
    }
    
    @Test
    public void assertGetShardingColumns() {
        assertThat(shardingStrategy.getShardingColumns().size(), is(1));
        assertThat(shardingStrategy.getShardingColumns().iterator().next(), is("order_id"));
        assertThat(shardingStrategyWithSimplified.getShardingColumns().size(), is(1));
        assertThat(shardingStrategyWithSimplified.getShardingColumns().iterator().next(), is("order_id"));
    }
}
