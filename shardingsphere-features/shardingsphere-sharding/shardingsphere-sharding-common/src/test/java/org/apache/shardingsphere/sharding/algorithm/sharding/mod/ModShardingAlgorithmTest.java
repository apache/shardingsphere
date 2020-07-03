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

package org.apache.shardingsphere.sharding.algorithm.sharding.mod;

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

public final class ModShardingAlgorithmTest {
    
    private StandardShardingStrategy shardingStrategy;
    
    @Before
    public void setup() {
        ModShardingAlgorithm shardingAlgorithm = new ModShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("sharding.count", "4");
        shardingAlgorithm.init();
        shardingStrategy = new StandardShardingStrategy("order_id", shardingAlgorithm);
    }
    
    @Test
    public void assertPreciseDoSharding() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("order_id", "t_order", Lists.newArrayList(10L, 11L, 13L)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    public void assertRangeDoShardingWithAllTargets() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Range<Long> rangeValue = Range.closed(11L, 14L);
        List<RouteValue> shardingValues = Lists.newArrayList(new RangeRouteValue<>("order_id", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertRangeDoShardingWithPartTargets() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Range<Long> rangeValue = Range.closed(11L, 12L);
        List<RouteValue> shardingValues = Lists.newArrayList(new RangeRouteValue<>("order_id", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_3"));
        assertTrue(actual.contains("t_order_0"));
    }
}
