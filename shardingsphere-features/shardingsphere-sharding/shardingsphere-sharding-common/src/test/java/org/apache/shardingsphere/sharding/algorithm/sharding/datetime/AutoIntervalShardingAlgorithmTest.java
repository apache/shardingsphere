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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime;

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.strategy.standard.StandardShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AutoIntervalShardingAlgorithmTest {
    
    private StandardShardingStrategy shardingStrategy;
    
    @Before
    public void setup() {
        AutoIntervalShardingAlgorithm shardingAlgorithm = new AutoIntervalShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("datetime-lower", "2020-01-01 00:00:00");
        shardingAlgorithm.getProps().setProperty("datetime-upper", "2020-01-01 00:00:16");
        shardingAlgorithm.getProps().setProperty("sharding-seconds", "4");
        shardingAlgorithm.init();
        shardingStrategy = new StandardShardingStrategy("create_time", shardingAlgorithm);
    }
    
    @Test
    public void assertPreciseDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Collections.singletonList(new ListRouteValue<>("create_time", "t_order", Arrays.asList("2020-01-01 00:00:01", "2020-01-01 00:00:02")));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(1));
        assertTrue(actual.contains("t_order_1"));
    }
    
    @Test
    public void assertPreciseDoShardingWithFirstPartition() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Collections.singletonList(new ListRouteValue<>("create_time", "t_order", Arrays.asList("2019-12-01 00:00:01", "2020-01-01 00:00:02")));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_0"));
        assertTrue(actual.contains("t_order_1"));
    }
    
    @Test
    public void assertPreciseDoShardingBeyondTheLastOne() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        List<RouteValue> shardingValues = Collections.singletonList(new ListRouteValue<>("create_time", "t_order", Collections.singletonList("2021-01-01 00:00:02")));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(1));
        assertTrue(actual.contains("t_order_5"));
    }
    
    @Test
    public void assertRangeDoShardingWithAllRange() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4");
        Range<String> rangeValue = Range.closed("2019-01-01 00:00:00", "2020-01-01 00:00:15");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(5));
    }
    
    @Test
    public void assertRangeDoShardingWithPartRange() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Range<String> rangeValue = Range.closed("2020-01-01 00:00:04", "2020-01-01 00:00:10");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    public void assertRangeDoShardingWithoutLowerBound() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Range<String> rangeValue = Range.lessThan("2020-01-01 00:00:11");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(4));
        assertTrue(actual.contains("t_order_0"));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    public void assertRangeDoShardingWithoutUpperBound() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        Range<String> rangeValue = Range.greaterThan("2020-01-01 00:00:09");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_3"));
        assertTrue(actual.contains("t_order_4"));
        assertTrue(actual.contains("t_order_5"));
    }
    
    @Test
    public void assertGetAutoTablesAmount() {
        AutoIntervalShardingAlgorithm shardingAlgorithm = new AutoIntervalShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("datetime-lower", "2020-01-01 00:00:00");
        shardingAlgorithm.getProps().setProperty("datetime-upper", "2021-01-01 00:00:00");
        shardingAlgorithm.getProps().setProperty("sharding-seconds", "86400");
        shardingAlgorithm.init();
        assertThat(shardingAlgorithm.getAutoTablesAmount(), is(368));
    }
}
