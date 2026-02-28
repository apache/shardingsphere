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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private ModShardingAlgorithm defaultAlgorithm;
    
    private ModShardingAlgorithm zeroPaddingAlgorithm;
    
    @BeforeAll
    void setUp() {
        defaultAlgorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "16")));
        zeroPaddingAlgorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(
                new Property("sharding-count", "16"), new Property("zero-padding", Boolean.TRUE.toString()), new Property("start-offset", "1"), new Property("stop-offset", "1")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPreciseDoShardingArguments")
    void assertPreciseDoSharding(final String name,
                                 final ModShardingAlgorithm algorithm, final Collection<String> availableTargetNames, final Comparable<?> shardingValue, final String expectedTargetName) {
        assertThat(algorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, shardingValue)), is(expectedTargetName));
    }
    
    @Test
    void assertPreciseDoShardingWhenOffsetOverload() {
        ModShardingAlgorithm algorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD",
                PropertiesBuilder.build(new Property("sharding-count", "16"), new Property("start-offset", "10"), new Property("stop-offset", "1")));
        assertThrows(ShardingValueOffsetException.class, () -> algorithm.doSharding(
                Arrays.asList("t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"),
                new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, "1")));
    }
    
    @SuppressWarnings("unchecked")
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRangeDoShardingWithAllTargetsArguments")
    void assertRangeDoShardingWithAllTargets(final String name,
                                             final ModShardingAlgorithm algorithm, final Collection<String> availableTargetNames, final Range<?> range, final int expectedSize) {
        assertThat(algorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, (Range<Comparable<?>>) range)).size(), is(expectedSize));
    }
    
    private Stream<Arguments> assertRangeDoShardingWithAllTargetsArguments() {
        return Stream.of(
                Arguments.of("closed range covers all targets", defaultAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"), Range.closed(1L, 16L), 16),
                Arguments.of("closed range covers all targets with zero padding", zeroPaddingAlgorithm, Arrays.asList(
                        "t_order_08", "t_order_09", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_00", "t_order_01", "t_order_02", "t_order_03", "t_order_04", "t_order_05", "t_order_06", "t_order_07"), Range.closed(1L, 16L), 16),
                Arguments.of("very large closed range covers all targets", zeroPaddingAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"),
                        Range.closed(1164582715995979777L, 1164583049303058023L), 16),
                Arguments.of("range without upper bound covers all targets", defaultAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"), Range.atLeast(1L), 16),
                Arguments.of("range without lower bound covers all targets", defaultAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"), Range.atMost(16L), 16));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRangeDoShardingWithPartTargets() {
        Collection<String> actual = defaultAlgorithm.doSharding(
                Arrays.asList("t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"),
                new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, (Range<Comparable<?>>) (Range<?>) Range.closed(1L, 2L)));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_1"));
        assertTrue(actual.contains("t_order_2"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertInitWithInvalidPropertiesArguments")
    void assertInitWithInvalidProperties(final String name, final Properties props) {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", props));
    }
    
    @Test
    void assertGetAutoTablesAmount() {
        assertThat(defaultAlgorithm.getAutoTablesAmount(), is(16));
    }
    
    private Stream<Arguments> assertInitWithInvalidPropertiesArguments() {
        return Stream.of(
                Arguments.of("missing sharding count", PropertiesBuilder.build()),
                Arguments.of("non-positive sharding count", PropertiesBuilder.build(new Property("sharding-count", "0"))),
                Arguments.of("negative start offset", PropertiesBuilder.build(
                        new Property("sharding-count", "16"), new Property("zero-padding", Boolean.TRUE.toString()), new Property("start-offset", "-1"), new Property("stop-offset", "1"))),
                Arguments.of("negative stop offset", PropertiesBuilder.build(
                        new Property("sharding-count", "16"), new Property("zero-padding", Boolean.TRUE.toString()), new Property("start-offset", "1"), new Property("stop-offset", "-1"))));
    }
    
    private Stream<Arguments> assertPreciseDoShardingArguments() {
        return Stream.of(
                Arguments.of("int sharding value", defaultAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"), 17, "t_order_1"),
                Arguments.of("string bigint sharding value", defaultAlgorithm, Arrays.asList(
                        "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"), "12345678910111213141516", "t_order_12"),
                Arguments.of("string bigint sharding value with zero padding", zeroPaddingAlgorithm, Arrays.asList(
                        "t_order_08", "t_order_09", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                        "t_order_00", "t_order_01", "t_order_02", "t_order_03", "t_order_04", "t_order_05", "t_order_06", "t_order_07"), "12345678910111213141516", "t_order_07"),
                Arguments.of("start offset zero and stop offset one", TypedSPILoader.getService(ShardingAlgorithm.class, "MOD",
                        PropertiesBuilder.build(new Property("sharding-count", "16"), new Property("start-offset", "0"), new Property("stop-offset", "1"))),
                        Arrays.asList(
                                "t_order_8", "t_order_9", "t_order_10", "t_order_11", "t_order_12", "t_order_13", "t_order_14", "t_order_15",
                                "t_order_0", "t_order_1", "t_order_2", "t_order_3", "t_order_4", "t_order_5", "t_order_6", "t_order_7"),
                        "19", "t_order_1"));
    }
}
