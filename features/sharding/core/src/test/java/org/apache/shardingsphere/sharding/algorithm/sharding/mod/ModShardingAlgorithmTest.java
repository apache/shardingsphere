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

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.data.ShardingValueOffsetException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    @Test
    void assertPreciseDoShardingWithIntShardingValue() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "16")));
        assertThat(algorithm.doSharding(createAvailableTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 17)), is("t_order_1"));
    }
    
    @Test
    void assertPreciseDoShardingWithBigIntegerShardingValue() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "16")));
        assertThat(algorithm.doSharding(createAvailableTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_12"));
    }
    
    @Test
    void assertPreciseDoShardingWhenOffsetOverload() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(
                new Property("sharding-count", "16"), new Property("start-offset", "10"), new Property("stop-offset", "1")));
        assertThrows(ShardingValueOffsetException.class, () -> algorithm.doSharding(createAvailableTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "1")));
    }
    
    @Test
    void assertRangeDoShardingWithAllTargets() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "16")));
        Collection<String> actual = algorithm.doSharding(createAvailableTargetNames(), new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 16L)));
        assertThat(actual.size(), is(16));
    }
    
    @Test
    void assertRangeDoShardingWithPartTargets() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "16")));
        Collection<String> actual = algorithm.doSharding(createAvailableTargetNames(),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 2L)));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
    }
    
    private Collection<String> createAvailableTargetNames() {
        return Arrays.asList("t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7");
    }
    
    @Test
    void assertPreciseDoShardingWithValueIsBigIntegerAndZeroPadding() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", createZeroPaddingProperties());
        assertThat(algorithm.doSharding(createAvailableIncludeZeroTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_07"));
    }
    
    @Test
    void assertRangeDoShardingWithAllTargetsZeroPadding() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", createZeroPaddingProperties());
        Collection<String> actual = algorithm.doSharding(createAvailableIncludeZeroTargetNames(),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 16L)));
        assertThat(actual.size(), is(16));
    }
    
    @Test
    void assertRangeDoShardingWithWrongArgumentForShardingCount() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "0"))));
    }
    
    @Test
    void assertRangeDoShardingWithWrongArgumentForStartOffset() {
        Properties props = createZeroPaddingProperties();
        props.setProperty("start-offset", "-1");
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", props));
    }
    
    @Test
    void assertRangeDoShardingWithWrongArgumentForStopOffset() {
        Properties props = createZeroPaddingProperties();
        props.setProperty("stop-offset", "-1");
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", props));
    }
    
    @Test
    void assertRangeDoShardingWithLargeRange() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", createZeroPaddingProperties());
        Collection<String> actual = algorithm.doSharding(createAvailableTargetNames(),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1164582715995979777L, 1164583049303058023L)));
        assertThat(actual.size(), is(16));
    }
    
    private Properties createZeroPaddingProperties() {
        return PropertiesBuilder.build(
                new Property("sharding-count", "16"), new Property("zero-padding", Boolean.TRUE.toString()), new Property("start-offset", "1"), new Property("stop-offset", "1"));
    }
    
    private Collection<String> createAvailableIncludeZeroTargetNames() {
        return Arrays.asList("t_order_08", "t_order_09", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                "t_order_00", "t_order_01", "t_order_02", "t_order_03", "t_order_04", "t_order_05", "t_order_06", "t_order_07");
    }
}
