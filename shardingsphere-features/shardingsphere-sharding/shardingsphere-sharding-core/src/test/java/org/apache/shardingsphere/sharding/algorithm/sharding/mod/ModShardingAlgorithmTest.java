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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ModShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    @Test
    public void assertPreciseDoShardingWithIntShardingValue() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        assertThat(algorithm.doSharding(createAvailableTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 17)), is("t_order_1"));
    }
    
    @Test
    public void assertPreciseDoShardingWithBigIntegerShardingValue() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        assertThat(algorithm.doSharding(createAvailableTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_12"));
    }
    
    @Test
    public void assertRangeDoShardingWithAllTargets() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        Collection<String> actual = algorithm.doSharding(createAvailableTargetNames(), new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 16L)));
        assertThat(actual.size(), is(16));
    }
    
    @Test
    public void assertRangeDoShardingWithPartTargets() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        Collection<String> actual = algorithm.doSharding(createAvailableTargetNames(),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 2L)));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("sharding-count", "16");
        return result;
    }
    
    private Collection<String> createAvailableTargetNames() {
        return Arrays.asList("t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7");
    }
    
    @Test
    public void assertPreciseDoShardingWithValueIsBigIntegerAndZeroPadding() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createZeroPaddingProperties()));
        assertThat(algorithm.doSharding(createAvailableIncludeZeroTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_07"));
    }
    
    @Test
    public void assertRangeDoShardingWithAllTargetsZeroPadding() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createZeroPaddingProperties()));
        Collection<String> actual = algorithm.doSharding(createAvailableIncludeZeroTargetNames(),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Range.closed(1L, 16L)));
        assertThat(actual.size(), is(16));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertRangeDoShardingWithWrongArgumentForStartOffset() {
        Properties properties = createZeroPaddingProperties();
        properties.setProperty("start-offset", "-1");
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", properties));
        assertThat(algorithm.doSharding(createAvailableIncludeZeroTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_07"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertRangeDoShardingWithWrongArgumentForStopOffset() {
        Properties properties = createZeroPaddingProperties();
        properties.setProperty("stop-offset", "-1");
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", properties));
        assertThat(algorithm.doSharding(createAvailableIncludeZeroTargetNames(), new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "12345678910111213141516")), is("t_order_07"));
    }
    
    private Properties createZeroPaddingProperties() {
        Properties result = new Properties();
        result.setProperty("sharding-count", "16");
        result.setProperty("zero-padding", Boolean.TRUE.toString());
        result.setProperty("start-offset", "1");
        result.setProperty("stop-offset", "1");
        return result;
    }
    
    private Collection<String> createAvailableIncludeZeroTargetNames() {
        return Arrays.asList("t_order_08", "t_order_09", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                "t_order_00", "t_order_01", "t_order_02", "t_order_03", "t_order_04", "t_order_05", "t_order_06", "t_order_07");
    }
}
