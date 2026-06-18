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
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundaryBasedRangeShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private static final List<String> AVAILABLE_TARGET_NAMES = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
    
    private final BoundaryBasedRangeShardingAlgorithm shardingAlgorithm = (BoundaryBasedRangeShardingAlgorithm) TypedSPILoader.getService(
            ShardingAlgorithm.class, "BOUNDARY_RANGE", PropertiesBuilder.build(new Property("sharding-ranges", "1,5,10")));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("preciseShardingValueArguments")
    void assertPreciseDoSharding(final String name, final Comparable<?> shardingValue, final String expectedTargetName) {
        assertThat(shardingAlgorithm.doSharding(AVAILABLE_TARGET_NAMES, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, shardingValue)), is(expectedTargetName));
    }
    
    @Test
    void assertGetAutoTablesAmount() {
        assertThat(shardingAlgorithm.getAutoTablesAmount(), is(4));
    }
    
    @Test
    void assertCalculatePartitionRangeWithMissingShardingRanges() {
        assertThrows(AlgorithmInitializationException.class, () -> shardingAlgorithm.calculatePartitionRange(new Properties()));
    }
    
    @Test
    void assertCalculatePartitionRangeWithInvalidShardingRanges() {
        assertThrows(AlgorithmInitializationException.class, () -> shardingAlgorithm.calculatePartitionRange(PropertiesBuilder.build(new Property("sharding-ranges", "invalid"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("rangeShardingValueArguments")
    void assertRangeDoSharding(final String name, final RangeShardingValue<Comparable<?>> shardingValue, final List<String> expectedTargetNames) {
        Collection<String> actual = shardingAlgorithm.doSharding(AVAILABLE_TARGET_NAMES, shardingValue);
        assertThat(actual.size(), is(expectedTargetNames.size()));
        expectedTargetNames.forEach(each -> assertTrue(actual.contains(each)));
    }
    
    private static Stream<Arguments> preciseShardingValueArguments() {
        return Stream.of(
                Arguments.of("precise value below first boundary", 0L, "t_order_0"),
                Arguments.of("precise value on first boundary", 1L, "t_order_1"),
                Arguments.of("precise value on last boundary", 10L, "t_order_3"));
    }
    
    private static Stream<Arguments> rangeShardingValueArguments() {
        return Stream.of(
                Arguments.of("range with both bounds", createRangeShardingValue(Range.closed(2L, 15L)), Arrays.asList("t_order_1", "t_order_2", "t_order_3")),
                Arguments.of("range without lower bound", createRangeShardingValue(Range.lessThan(0L)), Collections.singletonList("t_order_0")),
                Arguments.of("range without upper bound", createRangeShardingValue(Range.atLeast(10L)), Collections.singletonList("t_order_3")));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static RangeShardingValue<Comparable<?>> createRangeShardingValue(final Range<? extends Comparable<?>> valueRange) {
        return new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, (Range) valueRange);
    }
}
