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
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashModShardingAlgorithmTest {
    
    private static final String NORMALIZE_NUMERIC_INT_RANGE_KEY = "normalize-numeric-int-range";
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private HashModShardingAlgorithm shardingAlgorithm;
    
    private HashModShardingAlgorithm normalizedShardingAlgorithm;
    
    @BeforeEach
    void setup() {
        shardingAlgorithm = (HashModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "HASH_MOD", PropertiesBuilder.build(new Property("sharding-count", "4")));
        normalizedShardingAlgorithm = (HashModShardingAlgorithm) TypedSPILoader.getService(
                ShardingAlgorithm.class, "HASH_MOD", PropertiesBuilder.build(new Property("sharding-count", "4"), new Property(NORMALIZE_NUMERIC_INT_RANGE_KEY, Boolean.TRUE.toString())));
    }
    
    @Test
    void assertPreciseDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_type", DATA_NODE_INFO, "a")), is("t_order_1"));
    }
    
    @Test
    void assertPreciseDoShardingWithSameNegativeNumberValueForLegacyMode() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -1)), is("t_order_1"));
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -1L)), is("t_order_0"));
        assertThat(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, BigInteger.valueOf(-1L))), is("t_order_1"));
    }
    
    @Test
    void assertPreciseDoShardingWithSameNegativeNumberValueForNormalizedMode() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(normalizedShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -1)), is("t_order_1"));
        assertThat(normalizedShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -1L)), is("t_order_1"));
        assertThat(normalizedShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, BigInteger.valueOf(-1L))), is("t_order_1"));
    }
    
    @Test
    void assertPreciseDoShardingWithLongValueOutOfIntegerRangeForNormalizedMode() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(normalizedShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, Long.MAX_VALUE)), is("t_order_0"));
    }
    
    @Test
    void assertPreciseDoShardingWithBigIntegerBoundaryValuesForNormalizedMode() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(normalizedShardingAlgorithm.doSharding(
                availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, BigInteger.valueOf(Integer.MIN_VALUE))), is("t_order_0"));
        assertThat(normalizedShardingAlgorithm.doSharding(
                availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, BigInteger.valueOf(Integer.MAX_VALUE))), is("t_order_3"));
    }
    
    @Test
    void assertPreciseDoShardingWithBigIntegerOutOfIntegerRangeForNormalizedMode() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        BigInteger greaterThanIntegerMax = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        BigInteger lessThanIntegerMin = BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE);
        assertThat(normalizedShardingAlgorithm.doSharding(
                availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, greaterThanIntegerMax)),
                is(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, greaterThanIntegerMax))));
        assertThat(normalizedShardingAlgorithm.doSharding(
                availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, lessThanIntegerMin)),
                is(shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, lessThanIntegerMin))));
    }
    
    @Test
    void assertRangeDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("a", "f")));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    void assertRangeDoShardingWithWrongArgumentForShardingCount() {
        Properties props = PropertiesBuilder.build(new Property("sharding-count", "0"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "HASH_MOD", props));
    }
}
