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
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoIntervalShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private AutoIntervalShardingAlgorithm shardingAlgorithm;
    
    @BeforeEach
    void setup() {
        shardingAlgorithm = (AutoIntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "AUTO_INTERVAL",
                PropertiesBuilder.build(new Property("datetime-lower", "2020-01-01 00:00:00"), new Property("datetime-upper", "2020-01-01 00:00:16"), new Property("sharding-seconds", "4")));
    }
    
    @Test
    void assertInitFailedWithInvalidDatetimeFormat() {
        assertThrows(InvalidDatetimeFormatException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "AUTO_INTERVAL", PropertiesBuilder.build(new Property("datetime-lower", "invalid"))));
    }
    
    @Test
    void assertPreciseDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-01-01 00:00:01")), is("t_order_1"));
    }
    
    @Test
    void assertPreciseDoShardingBeyondTheLastOne() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2021-01-01 00:00:02")), is("t_order_5"));
    }
    
    @Test
    void assertRangeDoShardingWithAllRange() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2019-01-01 00:00:00", "2020-01-01 00:00:15")));
        assertThat(actual.size(), is(5));
    }
    
    @Test
    void assertRangeDoShardingWithPartRange() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:04", "2020-01-01 00:00:10")));
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    void assertRangeDoShardingWithoutLowerBound() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.lessThan("2020-01-01 00:00:11")));
        assertThat(actual.size(), is(4));
        assertTrue(actual.contains("t_order_0"));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    void assertRangeDoShardingWithoutUpperBound() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.greaterThan("2020-01-01 00:00:09")));
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_3"));
        assertTrue(actual.contains("t_order_4"));
        assertTrue(actual.contains("t_order_5"));
    }
    
    @Test
    void assertGetAutoTablesAmount() {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-lower", "2020-01-01 00:00:00"), new Property("datetime-upper", "2021-01-01 00:00:00"), new Property("sharding-seconds", "86400"));
        assertThat(((AutoIntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "AUTO_INTERVAL", props)).getAutoTablesAmount(), is(368));
    }
    
    @Test
    void assertRangeDoShardingWithGreaterTenTables() {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-lower", "2020-01-01 00:00:00"), new Property("datetime-upper", "2020-01-01 00:00:30"), new Property("sharding-seconds", "1"));
        AutoIntervalShardingAlgorithm shardingAlgorithm = (AutoIntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "AUTO_INTERVAL", props);
        List<String> availableTargetNames = new LinkedList<>();
        for (int i = 0; i < 32; i++) {
            availableTargetNames.add("t_order_" + i);
        }
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:00", "2020-01-01 00:00:10")));
        assertThat(actual.size(), is(11));
    }
    
    @Test
    void assertRangeDoShardingInValueWithMilliseconds() {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-lower", "2020-01-01 00:00:00"), new Property("datetime-upper", "2020-01-01 00:00:30"), new Property("sharding-seconds", "1"));
        AutoIntervalShardingAlgorithm shardingAlgorithm = (AutoIntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "AUTO_INTERVAL", props);
        List<String> availableTargetNames = new LinkedList<>();
        for (int i = 0; i < 32; i++) {
            availableTargetNames.add("t_order_" + i);
        }
        Collection<String> actualWithoutMilliseconds = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:11", "2020-01-01 00:00:21")));
        assertThat(actualWithoutMilliseconds.size(), is(11));
        Collection<String> actualWithOneMillisecond = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:11.1", "2020-01-01 00:00:21.1")));
        assertThat(actualWithOneMillisecond.size(), is(11));
        Collection<String> actualWithTwoMilliseconds = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:11.12", "2020-01-01 00:00:21.12")));
        assertThat(actualWithTwoMilliseconds.size(), is(11));
        Collection<String> actualWithThreeMilliseconds = shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2020-01-01 00:00:11.123", "2020-01-01 00:00:21.123")));
        assertThat(actualWithThreeMilliseconds.size(), is(11));
    }
}
