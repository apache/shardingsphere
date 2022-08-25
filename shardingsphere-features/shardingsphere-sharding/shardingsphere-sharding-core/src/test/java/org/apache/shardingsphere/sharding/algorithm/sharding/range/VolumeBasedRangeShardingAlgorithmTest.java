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

package org.apache.shardingsphere.sharding.algorithm.sharding.range;

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class VolumeBasedRangeShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private VolumeBasedRangeShardingAlgorithm shardingAlgorithm;
    
    @Before
    public void setUp() {
        shardingAlgorithm = (VolumeBasedRangeShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("VOLUME_RANGE", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("range-lower", 10);
        result.put("range-upper", 45);
        result.put("sharding-volume", 10);
        return result;
    }
    
    @Test
    public void assertPreciseDoSharding() {
        assertPreciseDoSharding(new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0L));
    }
    
    private void assertPreciseDoSharding(final PreciseShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, shardingValue), is("t_order_0"));
    }
    
    @Test
    public void assertPreciseDoShardingWithIntShardingValue() {
        assertPreciseDoSharding(new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0));
    }
    
    @Test
    public void assertRangeDoShardingWithoutLowerBound() {
        assertRangeDoShardingWithoutLowerBound(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.lessThan(12L)));
    }
    
    private void assertRangeDoShardingWithoutLowerBound(final RangeShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_0"));
        assertTrue(actual.contains("t_order_1"));
    }
    
    @Test
    public void assertRangeDoShardingWithoutLowerBoundWithIntShardingValue() {
        assertRangeDoShardingWithoutLowerBound(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.lessThan(12)));
    }
    
    @Test
    public void assertRangeDoShardingWithoutUpperBound() {
        assertRangeDoShardingWithoutUpperBound(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.greaterThan(40L)));
    }
    
    private void assertRangeDoShardingWithoutUpperBound(final RangeShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_4"));
        assertTrue(actual.contains("t_order_5"));
    }
    
    @Test
    public void assertRangeDoShardingWithoutUpperBoundWithIntShardingValue() {
        assertRangeDoShardingWithoutUpperBound(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.greaterThan(40)));
    }
    
    @Test
    public void assertRangeDoSharding() {
        assertRangeDoSharding(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(12L, 55L)));
    }
    
    private void assertRangeDoSharding(final RangeShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertThat(actual.size(), is(5));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
        assertTrue(actual.contains("t_order_4"));
        assertTrue(actual.contains("t_order_5"));
    }
    
    @Test
    public void assertRangeDoShardingWithIntegerShardingValue() {
        assertRangeDoSharding(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(12, 55)));
    }
    
    @Test
    public void assertGetAutoTablesAmount() {
        VolumeBasedRangeShardingAlgorithm shardingAlgorithm = new VolumeBasedRangeShardingAlgorithm();
        shardingAlgorithm.init(createPropertiesForStringValue());
        assertThat(shardingAlgorithm.getAutoTablesAmount(), is(6));
    }
    
    private Properties createPropertiesForStringValue() {
        Properties result = new Properties();
        result.setProperty("range-lower", "10");
        result.setProperty("range-upper", "45");
        result.setProperty("sharding-volume", "10");
        return result;
    }
}
