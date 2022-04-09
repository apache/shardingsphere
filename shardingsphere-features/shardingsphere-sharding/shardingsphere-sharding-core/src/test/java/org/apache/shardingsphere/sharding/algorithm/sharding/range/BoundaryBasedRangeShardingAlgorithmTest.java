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
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BoundaryBasedRangeShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private BoundaryBasedRangeShardingAlgorithm shardingAlgorithm;
    
    @Before
    public void setUp() {
        shardingAlgorithm = new BoundaryBasedRangeShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("sharding-ranges", "1,5,10");
        shardingAlgorithm.init();
    }
    
    @Test
    public void assertPreciseDoSharding() {
        assertPreciseDoSharding(new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0L));
    }
    
    private void assertPreciseDoSharding(final PreciseShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, shardingValue), is("t_order_0"));
    }
    
    @Test
    public void assertPreciseDoShardingWithIntShardingValue() {
        assertPreciseDoSharding(new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0));
    }
    
    @Test
    public void assertRangeDoSharding() {
        assertRangeDoSharding(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(2L, 15L)));
    }
    
    private void assertRangeDoSharding(final RangeShardingValue<Comparable<?>> shardingValue) {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
        assertTrue(actual.contains("t_order_3"));
    }
    
    @Test
    public void assertRangeDoShardingWithIntShardingValue() {
        assertRangeDoSharding(new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(2, 15)));
    }
    
    @Test
    public void assertGetAutoTablesAmount() {
        BoundaryBasedRangeShardingAlgorithm shardingAlgorithm = new BoundaryBasedRangeShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("sharding-ranges", "1,5,10");
        shardingAlgorithm.init();
        assertThat(shardingAlgorithm.getAutoTablesAmount(), is(4));
    }
}
